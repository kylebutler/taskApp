package com.example.taskapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskapp.MainActivity
import com.example.taskapp.R
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskItem
import com.example.taskapp.domain.model.TaskList

object NotificationHelper {

    const val CHANNEL_ID = "task_list_notifications"
    const val ALARM_CHANNEL_ID = "alarm_notifications"

    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_ID,
                "Task List Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Scheduled reminders for your task lists" }
            manager.createNotificationChannel(taskChannel)

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Clock Alarms"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(null, audioAttributes) // Sound will be set per-notification for flexibility
            }
            manager.createNotificationChannel(alarmChannel)
        }
    }

    fun showNotification(context: Context, list: TaskList, items: List<TaskItem>) {
        val notificationId = list.id.toInt()
        val isStandaloneTask = list.type == ListType.TASK

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(list.title)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)

        if (isStandaloneTask) {
            builder.setContentText("Reminder")
        } else {
            val bodyText = items.joinToString("\n") { item ->
                val prefix = if (item.isChecked) "\u2611" else "\u2610"
                "$prefix ${item.text}"
            }.ifEmpty { "No tasks" }

            val style = NotificationCompat.BigTextStyle()
                .bigText(bodyText)
                .setBigContentTitle(list.title)

            val summary = if (items.isEmpty()) "No tasks"
            else "${items.count { it.isChecked }}/${items.size} completed"

            builder.setContentText(summary)
            builder.setStyle(style)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_list_id", list.id)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.setContentIntent(tapPendingIntent)
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun buildClockAlarmNotification(context: Context, alarmId: Long, label: String): androidx.core.app.NotificationCompat.Builder {
        val stopIntent = Intent(context, ClockAlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
            putExtra("ALARM_ID", alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, (alarmId + 1000).toInt(), stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ClockAlarmReceiver::class.java).apply {
            action = "SNOOZE_ALARM"
            putExtra("ALARM_ID", alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, (alarmId + 2000).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(context, com.example.taskapp.ui.alarm.ringing.AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, (alarmId + 5000).toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alarm: $label")
            .setContentText("Wake up!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)
            .addAction(R.drawable.ic_notification, "Snooze", snoozePendingIntent)
    }

    fun showClockAlarmNotification(context: Context, alarmId: Long, label: String, ringtoneUri: Uri?, vibrate: Boolean) {
        val builder = buildClockAlarmNotification(context, alarmId, label)

        if (vibrate) {
            builder.setVibrate(longArrayOf(0, 500, 500, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        if (ringtoneUri != null) {
            builder.setSound(ringtoneUri)
        }

        val notification = builder.build()
        notification.flags = notification.flags or NotificationCompat.FLAG_INSISTENT

        NotificationManagerCompat.from(context).notify(alarmId.toInt(), notification)
    }

    fun showSnoozeNotification(context: Context, alarmId: Long, label: String, nextTriggerTime: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alarm snoozed")
            .setContentText("$label will trigger at $nextTriggerTime")
            .setAutoCancel(true)
        
        NotificationManagerCompat.from(context).notify((alarmId + 4000).toInt(), builder.build())
    }

    fun updateIfActive(context: Context, list: TaskList, items: List<TaskItem>) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val isActive = manager.activeNotifications.any { it.id == list.id.toInt() }
            if (isActive) showNotification(context, list, items)
        } catch (e: Exception) { }
    }
}
