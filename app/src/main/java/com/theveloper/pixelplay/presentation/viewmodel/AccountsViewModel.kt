package com.theveloper.pixelplay.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.gdrive.GDriveRepository
import com.theveloper.pixelplay.data.netease.NeteaseRepository
import com.theveloper.pixelplay.data.repository.MusicRepository
import com.theveloper.pixelplay.data.telegram.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

enum class ExternalServiceAccount {
    TELEGRAM,
    GOOGLE_DRIVE,
    NETEASE
}

data class ExternalAccountUiModel(
    val service: ExternalServiceAccount,
    val title: String,
    val accountLabel: String,
    val syncedContentLabel: String,
    val isLoggingOut: Boolean
)

data class AccountsUiState(
    val connectedAccounts: List<ExternalAccountUiModel> = emptyList(),
    val disconnectedServices: List<ExternalServiceAccount> = emptyList()
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val application: Application,
    private val telegramRepository: TelegramRepository,
    private val musicRepository: MusicRepository,
    private val gDriveRepository: GDriveRepository,
    private val neteaseRepository: NeteaseRepository
) : ViewModel() {

    private val loggingOutServices = MutableStateFlow<Set<ExternalServiceAccount>>(emptySet())

    private val telegramStateFlow = combine(
        telegramRepository.authorizationState
            .map { it is TdApi.AuthorizationStateReady }
            .distinctUntilChanged(),
        musicRepository.getAllTelegramChannels().map { it.size }
    ) { connected, channelCount ->
        connected to channelCount
    }

    private val gDriveStateFlow = combine(
        gDriveRepository.isLoggedInFlow,
        gDriveRepository.getFolders().map { it.size }
    ) { connected, folderCount ->
        connected to folderCount
    }

    private val neteaseStateFlow = combine(
        neteaseRepository.isLoggedInFlow,
        neteaseRepository.getPlaylists().map { it.size }
    ) { connected, playlistCount ->
        connected to playlistCount
    }

    val uiState: StateFlow<AccountsUiState> = combine(
        telegramStateFlow,
        gDriveStateFlow,
        neteaseStateFlow,
        loggingOutServices
    ) { telegramState, gDriveState, neteaseState, activeLogouts ->
        val (telegramConnected, telegramChannelCount) = telegramState
        val (gDriveConnected, gDriveFolderCount) = gDriveState
        val (neteaseConnected, neteasePlaylistCount) = neteaseState

        val connectedAccounts = buildList {
            if (telegramConnected) {
                add(
                    ExternalAccountUiModel(
                        service = ExternalServiceAccount.TELEGRAM,
                        title = application.getString(R.string.accounts_service_telegram),
                        accountLabel = application.getString(R.string.accounts_telegram_session_active),
                        syncedContentLabel = formatCount(
                            count = telegramChannelCount,
                            singularResId = R.string.accounts_synced_channel,
                            pluralResId = R.string.accounts_synced_channels
                        ),
                        isLoggingOut = ExternalServiceAccount.TELEGRAM in activeLogouts
                    )
                )
            }
            if (gDriveConnected) {
                add(
                    ExternalAccountUiModel(
                        service = ExternalServiceAccount.GOOGLE_DRIVE,
                        title = application.getString(R.string.accounts_service_google_drive),
                        accountLabel = gDriveRepository.userDisplayName
                            ?.takeIf { it.isNotBlank() }
                            ?: gDriveRepository.userEmail
                                ?.takeIf { it.isNotBlank() }
                            ?: application.getString(R.string.accounts_google_account_connected),
                        syncedContentLabel = formatCount(
                            count = gDriveFolderCount,
                            singularResId = R.string.accounts_synced_folder,
                            pluralResId = R.string.accounts_synced_folders
                        ),
                        isLoggingOut = ExternalServiceAccount.GOOGLE_DRIVE in activeLogouts
                    )
                )
            }
            if (neteaseConnected) {
                add(
                    ExternalAccountUiModel(
                        service = ExternalServiceAccount.NETEASE,
                        title = application.getString(R.string.accounts_service_netease),
                        accountLabel = neteaseRepository.userNickname
                            ?.takeIf { it.isNotBlank() }
                            ?: application.getString(R.string.accounts_netease_account_connected),
                        syncedContentLabel = formatCount(
                            count = neteasePlaylistCount,
                            singularResId = R.string.accounts_synced_playlist,
                            pluralResId = R.string.accounts_synced_playlists
                        ),
                        isLoggingOut = ExternalServiceAccount.NETEASE in activeLogouts
                    )
                )
            }
        }

        val disconnectedServices = buildList {
            if (!telegramConnected) add(ExternalServiceAccount.TELEGRAM)
            if (!gDriveConnected) add(ExternalServiceAccount.GOOGLE_DRIVE)
            if (!neteaseConnected) add(ExternalServiceAccount.NETEASE)
        }

        AccountsUiState(
            connectedAccounts = connectedAccounts,
            disconnectedServices = disconnectedServices
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    fun logout(service: ExternalServiceAccount) {
        if (service in loggingOutServices.value) return

        viewModelScope.launch {
            loggingOutServices.update { it + service }
            try {
                runCatching {
                    when (service) {
                        ExternalServiceAccount.TELEGRAM -> {
                            telegramRepository.logout()
                            telegramRepository.clearMemoryCache()
                            musicRepository.clearTelegramData()
                        }
                        ExternalServiceAccount.GOOGLE_DRIVE -> gDriveRepository.logout()
                        ExternalServiceAccount.NETEASE -> neteaseRepository.logout()
                    }
                }
            } finally {
                loggingOutServices.update { it - service }
            }
        }
    }

    private fun formatCount(count: Int, singularResId: Int, pluralResId: Int): String {
        return if (count == 1) {
            "1 ${application.getString(singularResId)}"
        } else {
            "$count ${application.getString(pluralResId)}"
        }
    }
}
