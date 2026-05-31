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

    fun setThemePreference(themePreference: ThemePreference) {
        viewModelScope.launch {
            repository.setThemePreference(themePreference)
        }
    }

    companion object {
        fun Factory(repository: UserPreferencesRepository): ViewModelProvider.Factory =
            ViewModelFactory { SettingsViewModel(repository) }
    }
}
