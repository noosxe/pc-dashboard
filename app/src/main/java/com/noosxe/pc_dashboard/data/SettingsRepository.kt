package com.noosxe.pc_dashboard.data

import com.noosxe.pc_dashboard.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Simplified repository for now, later can be persisted with DataStore
class SettingsRepository {
    private val _theme = MutableStateFlow(AppTheme.TokyoNight)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _theme.value = theme
    }
}
