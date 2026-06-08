package com.example.taskapp.data.repository.settings

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemePreference {
    LIGHT, DARK, OS
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
        val ALARM_RINGTONE_URI = stringPreferencesKey("alarm_ringtone_uri")
    }

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemePreference.OS.name
            ThemePreference.valueOf(themeName)
        }

    val snoozeDuration: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION] ?: 5
        }

    val alarmRingtoneUri: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ALARM_RINGTONE_URI] ?: Settings.System.DEFAULT_ALARM_ALERT_URI.toString()
        }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themePreference.name
        }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION] = minutes
        }
    }

    suspend fun setAlarmRingtoneUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_RINGTONE_URI] = uri
        }
    }
}
