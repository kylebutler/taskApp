package com.example.taskapp.ui.notifications

import app.cash.turbine.test
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.NotificationSetting
import com.example.taskapp.notification.AlarmScheduler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    private val notifRepo = mockk<NotificationRepository>(relaxed = true)
    private val taskRepo = mockk<TaskRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val listId = 1L
    private lateinit var viewModel: NotificationSettingsViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val listFlow = MutableStateFlow<com.example.taskapp.domain.model.TaskList?>(null)
    private val settingFlow = MutableStateFlow<NotificationSetting?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { taskRepo.getListById(listId) } returns listFlow
        every { notifRepo.getSettingForList(listId) } returns settingFlow
        viewModel = NotificationSettingsViewModel(notifRepo, taskRepo, alarmScheduler, listId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveSetting should save to repo and schedule alarm if enabled`() = runTest {
        // Wait for initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Update state
        viewModel.onEnabledChanged(true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveSetting()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { notifRepo.saveSetting(match { it.isEnabled }) }
        coVerify { alarmScheduler.cancel(listId) }
        coVerify { alarmScheduler.schedule(match { it.isEnabled }) }
    }

    @Test
    fun `saveSetting should cancel alarm and not schedule if disabled`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onEnabledChanged(false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveSetting()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { notifRepo.saveSetting(match { !it.isEnabled }) }
        coVerify { alarmScheduler.cancel(listId) }
        coVerify(exactly = 0) { alarmScheduler.schedule(any()) }
    }

    @Test
    fun `uiState should reflect changes in fields`() = runTest {
        viewModel.uiState.test {
            // Initial state (empty)
            awaitItem() 
            
            // Advance until first emission from flows
            testDispatcher.scheduler.advanceUntilIdle()
            var state = awaitItem()
            
            viewModel.onEnabledChanged(true)
            state = awaitItem()
            assertEquals(true, state.setting?.isEnabled)

            viewModel.onTimeChanged(14, 30)
            state = awaitItem()
            assertEquals(14, state.setting?.hour)
            assertEquals(30, state.setting?.minute)
        }
    }
}
