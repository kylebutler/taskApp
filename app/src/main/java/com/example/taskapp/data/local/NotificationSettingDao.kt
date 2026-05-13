package com.example.taskapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSettingDao {
    @Query("SELECT * FROM notification_settings WHERE listId = :listId")
    fun getSettingForList(listId: Long): Flow<NotificationSettingEntity?>

    @Query("SELECT * FROM notification_settings WHERE isEnabled = 1")
    suspend fun getAllEnabledSettings(): List<NotificationSettingEntity>

    @Query("SELECT * FROM notification_settings WHERE isEnabled = 1")
    fun getAllEnabledSettingsFlow(): Flow<List<NotificationSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(setting: NotificationSettingEntity): Long

    @Delete
    suspend fun deleteSetting(setting: NotificationSettingEntity)
}
