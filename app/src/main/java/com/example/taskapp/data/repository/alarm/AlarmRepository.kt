package com.example.taskapp.data.repository.alarm

import com.example.taskapp.data.local.AlarmDao
import com.example.taskapp.data.local.toDomain
import com.example.taskapp.data.local.toEntity
import com.example.taskapp.domain.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepository(private val alarmDao: AlarmDao) {

    fun getAllAlarms(): Flow<List<Alarm>> =
        alarmDao.getAllAlarms().map { entities -> entities.map { it.toDomain() } }

    suspend fun getAlarmById(id: Long): Alarm? =
        alarmDao.getAlarmById(id)?.toDomain()

    suspend fun upsertAlarm(alarm: Alarm): Long =
        alarmDao.upsertAlarm(alarm.toEntity())

    suspend fun deleteAlarm(alarm: Alarm) =
        alarmDao.deleteAlarm(alarm.toEntity())

    suspend fun reorderAlarms(alarms: List<Alarm>) {
        alarmDao.updateAlarms(
            alarms.mapIndexed { index, alarm -> alarm.copy(position = index).toEntity() }
        )
    }
}
