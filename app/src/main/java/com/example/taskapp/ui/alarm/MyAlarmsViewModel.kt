package com.example.taskapp.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.alarm.AlarmRepository
import com.example.taskapp.domain.model.Alarm
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyAlarmsViewModel(
    private val repository: AlarmRepository,
    private val alarmScheduler: com.example.taskapp.notification.AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.upsertAlarm(updated)
            if (updated.isEnabled) {
                alarmScheduler.scheduleClockAlarm(updated)
            } else {
                alarmScheduler.cancelClockAlarm(updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            alarmScheduler.cancelClockAlarm(alarm.id)
        }
    }

    fun upsertAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val id = repository.upsertAlarm(alarm)
            val toSchedule = if (alarm.id == 0L) alarm.copy(id = id) else alarm
            if (toSchedule.isEnabled) {
                alarmScheduler.scheduleClockAlarm(toSchedule)
            }
        }
    }

    fun reorderAlarms(reordered: List<Alarm>) {
        viewModelScope.launch {
            repository.reorderAlarms(reordered)
        }
    }

    companion object {
        fun Factory(repository: AlarmRepository, alarmScheduler: com.example.taskapp.notification.AlarmScheduler): ViewModelProvider.Factory =
            ViewModelFactory { MyAlarmsViewModel(repository, alarmScheduler) }
    }
}
