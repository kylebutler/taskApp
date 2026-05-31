package com.example.taskapp

import android.app.Application
import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.data.repository.settings.UserPreferencesRepository
import com.example.taskapp.notification.NotificationHelper

class TaskApp : Application() {
    val database by lazy { TaskAppDatabase.getInstance(this) }
    val taskRepository by lazy { TaskRepository(database) }
    val notificationRepository by lazy { NotificationRepository(database) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.createNotificationChannel(this)
        } catch (e: Exception) {
            // Ignore if channel creation fails
        }
    }
}
