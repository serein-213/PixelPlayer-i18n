package com.theveloper.pixelplay.presentation.netease.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.database.NeteasePlaylistEntity
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.netease.NeteaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeteaseDashboardViewModel @Inject constructor(
    private val repository: NeteaseRepository
) : ViewModel() {

    sealed class SyncUiMessage {
        data class Syncing(val resId: Int) : SyncUiMessage()
        data class Success(val resId: Int, val args: List<Any> = emptyList()) : SyncUiMessage()
        data class Error(val resId: Int, val errorMessage: String? = null) : SyncUiMessage()
    }

    val playlists: StateFlow<List<NeteasePlaylistEntity>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncUiMessage = MutableStateFlow<SyncUiMessage?>(null)
    val syncUiMessage: StateFlow<SyncUiMessage?> = _syncUiMessage.asStateFlow()

    private val _selectedPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistSongs.asStateFlow()

    val userNickname: String? get() = repository.userNickname
    val userAvatar: String? get() = repository.userAvatar
    
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedInFlow

    init {
        // Auto-sync playlists when the dashboard opens
        syncPlaylists()
    }

    fun syncAllPlaylistsAndSongs() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncUiMessage.value = SyncUiMessage.Syncing(R.string.sync_notification_playlists)
            val result = repository.syncAllPlaylistsAndSongs()
            result.fold(
                onSuccess = { summary ->
                    _syncUiMessage.value = if (summary.failedPlaylistCount == 0) {
                        SyncUiMessage.Success(R.string.synced_playlists_and_songs_format, listOf(summary.playlistCount, summary.syncedSongCount))
                    } else {
                        SyncUiMessage.Success(R.string.synced_playlists_and_songs_failed_format, listOf(summary.playlistCount, summary.syncedSongCount, summary.failedPlaylistCount))
                    }
                },
                onFailure = { _syncUiMessage.value = SyncUiMessage.Error(R.string.sync_failed, it.message) }
            )
            _isSyncing.value = false
        }
    }

    fun syncPlaylists() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncUiMessage.value = SyncUiMessage.Syncing(R.string.sync_notification_playlists)
            val result = repository.syncUserPlaylists()
            result.fold(
                onSuccess = { _syncUiMessage.value = SyncUiMessage.Success(R.string.synced_playlists_count_format, listOf(it.size)) },
                onFailure = { _syncUiMessage.value = SyncUiMessage.Error(R.string.sync_failed, it.message) }
            )
            _isSyncing.value = false
        }
    }

    fun syncPlaylistSongs(playlistId: Long) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncUiMessage.value = SyncUiMessage.Syncing(R.string.sync_notification_songs)
            val result = repository.syncPlaylistSongs(playlistId)
            result.fold(
                onSuccess = { _syncUiMessage.value = SyncUiMessage.Success(R.string.songs_synced_count_format, listOf(it)) },
                onFailure = { _syncUiMessage.value = SyncUiMessage.Error(R.string.sync_failed, it.message) }
            )
            _isSyncing.value = false
        }
    }

    fun loadPlaylistSongs(playlistId: Long) {
        viewModelScope.launch {
            repository.getPlaylistSongs(playlistId).collect { songs ->
                _selectedPlaylistSongs.value = songs
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun clearSyncMessage() {
        _syncUiMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
