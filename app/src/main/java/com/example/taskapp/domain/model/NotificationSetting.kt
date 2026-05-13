package com.example.taskapp.domain.model

enum class NotificationFrequency { DAILY, WEEKLY, ONE_TIME, CUSTOM_INTERVAL }
enum class IntervalUnit { HOURS, DAYS, WEEKS }

data class NotificationSetting(
    val id: Long = 0,
    val listId: Long,
    val isEnabled: Boolean = false,
    val frequency: NotificationFrequency = NotificationFrequency.DAILY,
    val hour: Int = 9,
    val minute: Int = 0,
    // Bitmask: bit 0=Sun, bit 1=Mon, ... bit 6=Sat. WEEKLY only.
    val weekDaysMask: Int = 0b0000010,
    // First (or only) fire time epoch millis. ONE_TIME and CUSTOM_INTERVAL.
    val oneTimeEpochMillis: Long = 0L,
    // CUSTOM_INTERVAL: repeat every intervalValue * intervalUnit
    val intervalValue: Int = 1,
    val intervalUnit: IntervalUnit = IntervalUnit.DAYS
)
