package com.noosxe.pc_dashboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noosxe.pc_dashboard.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val RESPECT_EXPIRE_TIMEOUT = booleanPreferencesKey("respect_expire_timeout")
        val NOTIFICATION_TIMEOUT = intPreferencesKey("notification_timeout")
        val SERVER_HOST = stringPreferencesKey("server_host")
        val SERVER_PORT = intPreferencesKey("server_port")
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[PreferencesKeys.THEME] ?: AppTheme.TokyoNight.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: Exception) {
            AppTheme.TokyoNight
        }
    }

    val respectExpireTimeout: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RESPECT_EXPIRE_TIMEOUT] ?: true
    }

    val notificationTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_TIMEOUT] ?: 8
    }

    val serverHost: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SERVER_HOST] ?: "127.0.0.1"
    }

    val serverPort: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SERVER_PORT] ?: 12345
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun setRespectExpireTimeout(respect: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESPECT_EXPIRE_TIMEOUT] = respect
        }
    }

    suspend fun setNotificationTimeout(timeout: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_TIMEOUT] = timeout
        }
    }

    suspend fun setServerHost(host: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_HOST] = host
        }
    }

    suspend fun setServerPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_PORT] = port
        }
    }
}
