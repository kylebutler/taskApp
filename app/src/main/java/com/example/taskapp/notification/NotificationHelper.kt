package com.example.taskapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task List Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Scheduled reminders for your task lists" }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, list: TaskList, items: List<TaskItem>) {
        val notificationId = list.id.toInt()

        val isStandaloneTask = list.type == ListType.TASK

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(list.title)
            .setAutoCancel(false)

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

    // Re-post the notification in place if one for this list is already in the shade.
    // Posting with the same ID silently updates the existing notification.
    fun updateIfActive(context: Context, list: TaskList, items: List<TaskItem>) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val isActive = manager.activeNotifications.any { it.id == list.id.toInt() }
            if (isActive) showNotification(context, list, items)
        } catch (e: Exception) {
            // Some devices or API levels might fail here; ignore as it's just an update
        }
    }
}
