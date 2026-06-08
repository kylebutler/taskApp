package com.example.taskapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskapp.domain.model.Alarm

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: Int = 0,
    val isOneTime: Boolean = true,
    val isSilent: Boolean = false,
    val position: Int = 0
)

fun AlarmEntity.toDomain() = Alarm(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    isEnabled = isEnabled,
    daysOfWeek = daysOfWeek,
    isOneTime = isOneTime,
    isSilent = isSilent,
    position = position
)

fun Alarm.toEntity() = AlarmEntity(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    isEnabled = isEnabled,
    daysOfWeek = daysOfWeek,
    isOneTime = isOneTime,
    isSilent = isSilent,
    position = position
)
