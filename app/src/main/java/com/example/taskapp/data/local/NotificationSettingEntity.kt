package com.example.taskapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskapp.domain.model.IntervalUnit
import com.example.taskapp.domain.model.NotificationFrequency
import com.example.taskapp.domain.model.NotificationSetting

@Entity(
    tableName = "notification_settings",
    foreignKeys = [ForeignKey(
        entity = TaskListEntity::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["listId"], unique = true)]
)
data class NotificationSettingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val isEnabled: Boolean = false,
    val frequency: String = "DAILY",
    val hour: Int = 9,
    val minute: Int = 0,
    val weekDaysMask: Int = 0b0000010,
    val oneTimeEpochMillis: Long = 0L,
    val intervalValue: Int = 1,
    val intervalUnit: String = "DAYS"
)

fun NotificationSettingEntity.toDomain() = NotificationSetting(
    id, listId, isEnabled,
    NotificationFrequency.valueOf(frequency),
    hour, minute, weekDaysMask, oneTimeEpochMillis,
    intervalValue, IntervalUnit.valueOf(intervalUnit)
)

fun NotificationSetting.toEntity() = NotificationSettingEntity(
    id, listId, isEnabled, frequency.name, hour, minute,
    weekDaysMask, oneTimeEpochMillis, intervalValue, intervalUnit.name
)
