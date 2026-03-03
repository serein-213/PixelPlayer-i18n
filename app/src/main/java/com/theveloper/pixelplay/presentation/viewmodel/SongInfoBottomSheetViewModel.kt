package com.theveloper.pixelplay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.wear.WearPhoneTransferSender
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SongInfoBottomSheetViewModel @Inject constructor(
    private val wearPhoneTransferSender: WearPhoneTransferSender,
) : ViewModel() {

    private val _isPixelPlayWatchAvailable = MutableStateFlow(false)
    val isPixelPlayWatchAvailable: StateFlow<Boolean> = _isPixelPlayWatchAvailable.asStateFlow()

    private val _isSendingToWatch = MutableStateFlow(false)
    val isSendingToWatch: StateFlow<Boolean> = _isSendingToWatch.asStateFlow()

    fun refreshWatchAvailability() {
        viewModelScope.launch {
            _isPixelPlayWatchAvailable.value = wearPhoneTransferSender.isPixelPlayWatchAvailable()
        }
    }

    fun isLocalSongForWatchTransfer(song: Song): Boolean {
        val uri = song.contentUriString
        val isCloudSong = uri.startsWith("telegram://") ||
            uri.startsWith("netease://") ||
               uri.startsWith("qqmusic://") ||
            uri.startsWith("gdrive://")
        if (isCloudSong) return false

        if (song.path.isNotBlank()) {
            return File(song.path).exists()
        }

        return uri.startsWith("content://") || uri.startsWith("file://")
    }

    fun sendSongToWatch(song: Song, onComplete: (String) -> Unit) {
        if (_isSendingToWatch.value) return

        viewModelScope.launch {
            if (!isLocalSongForWatchTransfer(song)) {
                onComplete("Only local songs can be sent to watch")
                return@launch
            }

            _isSendingToWatch.update { true }
            val result = wearPhoneTransferSender.requestSongTransfer(song.id)
            _isSendingToWatch.update { false }

            if (result.isSuccess) {
                val nodeCount = result.getOrNull() ?: 1
                onComplete(
                    if (nodeCount > 1) {
                        "Transfer requested on $nodeCount watches"
                    } else {
                        "Transfer requested on watch"
                    }
                )
            } else {
                onComplete(result.exceptionOrNull()?.message ?: "Failed to request transfer")
                refreshWatchAvailability()
            }
        }
    }
}
