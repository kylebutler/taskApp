package com.example.taskapp.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taskapp.TaskApp
import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.local.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        if (alarmId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            "STOP_ALARM" -> {
                notificationManager.cancel(alarmId.toInt())
                context.stopService(Intent(context, AlarmService::class.java))
                handleStopAlarm(context, alarmId)
            }
            "SNOOZE_ALARM" -> {
                notificationManager.cancel(alarmId.toInt())
                context.stopService(Intent(context, AlarmService::class.java))
                snoozeAlarm(context, alarmId)
            }
            else -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = TaskAppDatabase.getInstance(context)
                        val alarm = db.alarmDao().getAlarmById(alarmId)
                        if (alarm != null && alarm.isEnabled) {
                            // Start the alarm service to handle sound and vibration
                            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                                putExtra("ALARM_ID", alarmId)
                                putExtra("ALARM_LABEL", alarm.label)
                                putExtra("IS_SILENT", alarm.isSilent)
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                            
                            if (!alarm.isOneTime) {
                                AlarmScheduler(context).scheduleClockAlarm(alarm.toDomain())
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun handleStopAlarm(context: Context, alarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = TaskAppDatabase.getInstance(context)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            if (alarm != null && alarm.isOneTime) {
                db.alarmDao().upsertAlarm(alarm.copy(isEnabled = false))
                AlarmScheduler(context).cancelClockAlarm(alarmId)
            }
        }
    }

    private fun snoozeAlarm(context: Context, alarmId: Long) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as TaskApp
                val db = TaskAppDatabase.getInstance(context)
                val alarm = db.alarmDao().getAlarmById(alarmId) ?: return@launch
                
                val minutes = app.userPreferencesRepository.snoozeDuration.first()
                val triggerAt = System.currentTimeMillis() + minutes * 60 * 1000
                
                val nextTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(triggerAt))
                NotificationHelper.showSnoozeNotification(context, alarmId, alarm.label, nextTimeStr)

                val intent = Intent(context, ClockAlarmReceiver::class.java).apply {
                    putExtra("ALARM_ID", alarmId)
                    action = "com.example.taskapp.CLOCK_ALARM_SNOOZE_$alarmId"
                }
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    context, (alarmId + 3000).toInt(), intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
