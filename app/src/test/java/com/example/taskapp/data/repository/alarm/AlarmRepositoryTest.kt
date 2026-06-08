package com.example.taskapp.data.repository.alarm

import com.example.taskapp.data.local.AlarmDao
import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.domain.model.Alarm
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AlarmRepositoryTest {

    private val db = mockk<TaskAppDatabase>()
    private val alarmDao = mockk<AlarmDao>(relaxed = true)
    private lateinit var repository: AlarmRepository

    @Before
    fun setup() {
        every { db.alarmDao() } returns alarmDao
        repository = AlarmRepository(alarmDao)
    }

    @Test
    fun `reorderAlarms should update positions correctly`() = runTest {
        val alarms = listOf(
            Alarm(id = 10, label = "A", hour = 8, minute = 0),
            Alarm(id = 20, label = "B", hour = 9, minute = 0)
        )

        repository.reorderAlarms(alarms)

        coVerify {
            alarmDao.updateAlarms(match {
                it[0].id == 10L && it[0].position == 0 &&
                it[1].id == 20L && it[1].position == 1
            })
        }
    }
}
