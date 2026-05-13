package com.example.taskapp.data.repository

import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.local.toDomain
import com.example.taskapp.data.local.toEntity
import com.example.taskapp.domain.model.NotificationSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(private val db: TaskAppDatabase) {

    fun getSettingForList(listId: Long): Flow<NotificationSetting?> =
        db.notificationSettingDao().getSettingForList(listId).map { it?.toDomain() }

    suspend fun saveSetting(setting: NotificationSetting) =
        db.notificationSettingDao().upsertSetting(setting.toEntity())

    suspend fun getAllEnabledSettings(): List<NotificationSetting> =
        db.notificationSettingDao().getAllEnabledSettings().map { it.toDomain() }
}
