package com.example.taskapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.settings.ThemePreference
import com.example.taskapp.data.repository.settings.UserPreferencesRepository
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = repository.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.OS)

    val snoozeDuration: StateFlow<Int> = repository.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 5)

    val alarmRingtoneUri: StateFlow<String?> = repository.alarmRingtoneUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setThemePreference(themePreference: ThemePreference) {
        viewModelScope.launch {
            repository.setThemePreference(themePreference)
        }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            repository.setSnoozeDuration(minutes)
        }
    }

    fun setAlarmRingtoneUri(uri: String) {
        viewModelScope.launch {
            repository.setAlarmRingtoneUri(uri)
        }
    }

    companion object {
        fun Factory(repository: UserPreferencesRepository): ViewModelProvider.Factory =
            ViewModelFactory { SettingsViewModel(repository) }
    }
}
