package com.noosxe.pc_dashboard.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noosxe.pc_dashboard.data.SettingsRepository
import com.noosxe.pc_dashboard.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<AppTheme> = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.TokyoNight)
    val respectExpireTimeout: StateFlow<Boolean> = settingsRepository.respectExpireTimeout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationTimeout: StateFlow<Int> = settingsRepository.notificationTimeout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setRespectExpireTimeout(respect: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRespectExpireTimeout(respect)
        }
    }

    fun setNotificationTimeout(timeout: Int) {
        viewModelScope.launch {
            settingsRepository.setNotificationTimeout(timeout)
        }
    }
}
