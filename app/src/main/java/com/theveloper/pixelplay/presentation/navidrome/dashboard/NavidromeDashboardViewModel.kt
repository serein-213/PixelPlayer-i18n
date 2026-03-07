package com.theveloper.pixelplay.presentation.navidrome.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.database.NavidromePlaylistEntity
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.navidrome.NavidromeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavidromeDashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NavidromeRepository
) : ViewModel() {

    val playlists: StateFlow<List<NavidromePlaylistEntity>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _selectedPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistSongs.asStateFlow()

    val username: String? get() = repository.username
    val serverUrl: String? get() = repository.serverUrl
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedInFlow

    init {
        // Auto-sync playlists when the dashboard opens
        syncPlaylists()
    }

    fun syncAllPlaylistsAndSongs() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = context.getString(R.string.navidrome_sync_all_message)
            val result = repository.syncAllPlaylistsAndSongs()
            result.fold(
                onSuccess = { summary ->
                    _syncMessage.value = if (summary.failedPlaylistCount == 0) {
                        context.getString(R.string.synced_playlists_and_songs_format, summary.playlistCount, summary.syncedSongCount)
                    } else {
                        context.getString(R.string.synced_playlists_and_songs_failed_format, summary.playlistCount, summary.syncedSongCount, summary.failedPlaylistCount)
                    }
                },
                onFailure = { _syncMessage.value = context.getString(R.string.sync_failed, it.message ?: "") }
            )
            _isSyncing.value = false
        }
    }

    fun syncPlaylists() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = context.getString(R.string.navidrome_sync_playlists_message)
            val result = repository.syncPlaylists()
            result.fold(
                onSuccess = { _syncMessage.value = context.getString(R.string.synced_playlists_count_format, it.size) },
                onFailure = { _syncMessage.value = context.getString(R.string.sync_failed, it.message ?: "") }
            )
            _isSyncing.value = false
        }
    }

    fun syncPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = context.getString(R.string.navidrome_sync_songs_message)
            val result = repository.syncPlaylistSongs(playlistId)
            result.fold(
                onSuccess = { count ->
                    // Sync to unified library after successfully syncing individual playlist
                    repository.syncUnifiedLibrarySongsFromNavidrome()
                    _syncMessage.value = context.getString(R.string.songs_synced_count_format, count)
                },
                onFailure = { _syncMessage.value = context.getString(R.string.sync_failed, it.message ?: "") }
            )
            _isSyncing.value = false
        }
    }

    fun loadPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            repository.getPlaylistSongs(playlistId).collect { songs ->
                _selectedPlaylistSongs.value = songs
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
