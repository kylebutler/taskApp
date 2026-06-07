package com.example.taskapp.ui.alarm

import app.cash.turbine.test
import com.example.taskapp.data.repository.alarm.AlarmRepository
import com.example.taskapp.domain.model.Alarm
import com.example.taskapp.notification.AlarmScheduler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyAlarmsViewModelTest {

    private val repo = mockk<AlarmRepository>(relaxed = true)
    private val scheduler = mockk<AlarmScheduler>(relaxed = true)
    private lateinit var viewModel: MyAlarmsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getAllAlarms() } returns flowOf(emptyList())
        viewModel = MyAlarmsViewModel(repo, scheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `alarms flow should emit alarms from repository`() = runTest {
        val alarms = listOf(Alarm(id = 1, label = "Test", hour = 8, minute = 0))
        every { repo.getAllAlarms() } returns flowOf(alarms)
        
        viewModel = MyAlarmsViewModel(repo, scheduler)

        viewModel.alarms.test {
            assertEquals(emptyList<Alarm>(), awaitItem())
            assertEquals(alarms, awaitItem())
        }
    }

    @Test
    fun `reorderAlarms should call repository`() = runTest {
        val alarms = listOf(Alarm(id = 1, label = "A", hour = 8, minute = 0))
        
        viewModel.reorderAlarms(alarms)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.reorderAlarms(alarms) }
    }
}
