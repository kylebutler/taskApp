package com.example.taskapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.taskapp.domain.model.IntervalUnit
import com.example.taskapp.domain.model.NotificationFrequency
import com.example.taskapp.domain.model.NotificationSetting
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(setting: NotificationSetting) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return
        when (setting.frequency) {
            NotificationFrequency.DAILY -> scheduleDailyAlarm(setting)
            NotificationFrequency.WEEKLY -> scheduleWeeklyAlarms(setting)
            NotificationFrequency.ONE_TIME -> scheduleOneTimeAlarm(setting)
            NotificationFrequency.CUSTOM_INTERVAL -> scheduleCustomIntervalAlarm(setting)
        }
    }

    private fun scheduleDailyAlarm(setting: NotificationSetting) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextOccurrence(setting.hour, setting.minute, dayOfWeek = null),
            buildPendingIntent(setting.listId, setting.listId.toInt())
        )
    }

    private fun scheduleWeeklyAlarms(setting: NotificationSetting) {
        for (dayIndex in 0..6) {
            val bit = 1 shl dayIndex
            if ((setting.weekDaysMask and bit) == 0) continue
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextOccurrence(setting.hour, setting.minute, dayOfWeek = dayIndex + 1),
                buildPendingIntent(
                    setting.listId,
                    requestCode = (setting.listId * 10 + dayIndex).toInt(),
                    dayIndex = dayIndex
                )
            )
        }
    }

    private fun scheduleOneTimeAlarm(setting: NotificationSetting) {
        if (setting.oneTimeEpochMillis <= System.currentTimeMillis()) return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            setting.oneTimeEpochMillis,
            buildPendingIntent(setting.listId, setting.listId.toInt())
        )
    }

    private fun scheduleCustomIntervalAlarm(setting: NotificationSetting) {
        val firstFire = if (setting.oneTimeEpochMillis > System.currentTimeMillis())
            setting.oneTimeEpochMillis
        else
            System.currentTimeMillis() + intervalMillis(setting.intervalValue, setting.intervalUnit)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            firstFire,
            buildPendingIntent(setting.listId, setting.listId.toInt())
        )
    }

    // Called from NotificationReceiver to reschedule the next custom-interval fire.
    fun scheduleNextCustomInterval(listId: Long, intervalValue: Int, intervalUnit: IntervalUnit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis(intervalValue, intervalUnit),
            buildPendingIntent(listId, listId.toInt())
        )
    }

    fun cancel(listId: Long) {
        alarmManager.cancel(buildPendingIntent(listId, listId.toInt()))
        for (dayIndex in 0..6) {
            alarmManager.cancel(
                buildPendingIntent(listId, (listId * 10 + dayIndex).toInt(), dayIndex)
            )
        }
    }

    private fun buildPendingIntent(listId: Long, requestCode: Int, dayIndex: Int = -1): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_LIST_ID, listId)
            putExtra(NotificationReceiver.EXTRA_DAY_INDEX, dayIndex)
            // Unique action ensures each PendingIntent is distinct by content
            action = "com.example.taskapp.NOTIFY_$requestCode"
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextOccurrence(hour: Int, minute: Int, dayOfWeek: Int?): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (dayOfWeek != null) {
            val current = cal.get(Calendar.DAY_OF_WEEK)
            var daysUntil = (dayOfWeek - current + 7) % 7
            if (daysUntil == 0 && cal.timeInMillis <= System.currentTimeMillis()) daysUntil = 7
            cal.add(Calendar.DAY_OF_YEAR, daysUntil)
        } else {
            if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun intervalMillis(value: Int, unit: IntervalUnit): Long = when (unit) {
        IntervalUnit.HOURS -> AlarmManager.INTERVAL_HOUR * value
        IntervalUnit.DAYS -> AlarmManager.INTERVAL_DAY * value
        IntervalUnit.WEEKS -> AlarmManager.INTERVAL_DAY * 7L * value
    }
}
