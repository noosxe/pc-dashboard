package com.noosxe.pc_dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noosxe.pc_dashboard.data.MediaState
import com.noosxe.pc_dashboard.data.MockPcRepository
import com.noosxe.pc_dashboard.data.PcNotification
import com.noosxe.pc_dashboard.data.PcRepository
import com.noosxe.pc_dashboard.data.PcStats
import com.noosxe.pc_dashboard.data.PlayerState
import com.noosxe.pc_dashboard.data.SettingsRepository
import com.noosxe.pc_dashboard.data.WebSocketPcRepository
import com.noosxe.pc_dashboard.ui.theme.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val pcRepository: PcRepository,
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {

    val uiState: StateFlow<PcStats> = pcRepository.getPcStatsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PcStats()
        )

    val theme: StateFlow<AppTheme> = settingsRepository.theme

    val isLocked: StateFlow<Boolean> = pcRepository.getSessionLockFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val shouldKeepScreenOn: StateFlow<Boolean> = isLocked
        .flatMapLatest { locked ->
            flow {
                if (locked) {
                    emit(true)
                    delay(5 * 60 * 1000) // 5 minutes
                    emit(false)
                } else {
                    emit(true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val notifications: Flow<PcNotification> = pcRepository.getNotificationsFlow()

    val mediaState: StateFlow<MediaState> = pcRepository.getMediaStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (pcRepository is MockPcRepository) {
                MediaState(
                    players = listOf(
                        PlayerState(
                            player = "spotify",
                            identity = "Spotify",
                            status = "Playing",
                            title = "Blinding Lights",
                            artist = "The Weeknd",
                            positionMs = 45000,
                            lengthMs = 200000,
                            artUrl = "https://i.scdn.co/image/ab67616d0000b273c51bd0179a6d859e51c223c3"
                        )
                    )
                )
            } else {
                MediaState()
            }
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun onMediaCommand(player: String, command: String) {
        pcRepository.sendMediaCommand(player, command)
    }

    fun onNotificationAction(notificationId: Int, actionKey: String) {
        pcRepository.sendNotificationAction(notificationId, actionKey)
    }

    fun onNotificationDismiss(notificationId: Int) {
        pcRepository.sendNotificationAction(notificationId, "dismiss") // Protocol says 'dismiss' is a common action key
        pcRepository.dismissNotification(notificationId)
    }
}
