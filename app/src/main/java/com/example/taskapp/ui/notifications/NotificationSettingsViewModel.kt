package com.example.taskapp.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.IntervalUnit
import com.example.taskapp.domain.model.NotificationFrequency
import com.example.taskapp.domain.model.NotificationSetting
import com.example.taskapp.notification.AlarmScheduler
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val setting: NotificationSetting? = null,
    val listTitle: String = "",
    val isSaving: Boolean = false
)

class NotificationSettingsViewModel(
    private val notifRepo: NotificationRepository,
    private val taskRepo: TaskRepository,
    private val alarmScheduler: AlarmScheduler,
    private val listId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                taskRepo.getListById(listId),
                notifRepo.getSettingForList(listId)
            ) { list, setting ->
                NotificationSettingsUiState(
                    setting = setting ?: NotificationSetting(listId = listId),
                    listTitle = list?.title ?: ""
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEnabledChanged(enabled: Boolean) =
        _uiState.update { it.copy(setting = it.setting?.copy(isEnabled = enabled)) }

    fun onFrequencyChanged(frequency: NotificationFrequency) =
        _uiState.update { it.copy(setting = it.setting?.copy(frequency = frequency)) }

    fun onTimeChanged(hour: Int, minute: Int) =
        _uiState.update { it.copy(setting = it.setting?.copy(hour = hour, minute = minute)) }

    fun onWeekDayToggled(bit: Int) =
        _uiState.update { state ->
            val mask = state.setting?.weekDaysMask ?: 0
            state.copy(setting = state.setting?.copy(weekDaysMask = mask xor bit))
        }

    fun onOneTimeDateTimeChanged(epochMillis: Long) =
        _uiState.update { it.copy(setting = it.setting?.copy(oneTimeEpochMillis = epochMillis)) }

    fun onIntervalValueChanged(value: Int) =
        _uiState.update { it.copy(setting = it.setting?.copy(intervalValue = value.coerceAtLeast(1))) }

    fun onIntervalUnitChanged(unit: IntervalUnit) =
        _uiState.update { it.copy(setting = it.setting?.copy(intervalUnit = unit)) }

    fun saveSetting() {
        val setting = _uiState.value.setting ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            notifRepo.saveSetting(setting)
            alarmScheduler.cancel(listId)
            if (setting.isEnabled) alarmScheduler.schedule(setting)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    companion object {
        fun Factory(
            notifRepo: NotificationRepository,
            taskRepo: TaskRepository,
            alarmScheduler: AlarmScheduler,
            listId: Long
        ): ViewModelProvider.Factory = ViewModelFactory {
            NotificationSettingsViewModel(notifRepo, taskRepo, alarmScheduler, listId)
        }
    }
}
