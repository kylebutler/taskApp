package com.example.taskapp.ui.detail

import android.app.Application
import com.example.taskapp.TaskApp
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.TaskItem
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelTest {

    private val app = mockk<TaskApp>(relaxed = true)
    private val repo = mockk<TaskRepository>(relaxed = true)
    private val notifRepo = mockk<NotificationRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val listId = 1L
    private lateinit var viewModel: ListDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val itemsFlow = MutableStateFlow<List<TaskItem>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getListById(listId) } returns MutableStateFlow(null)
        every { repo.getItemsForList(listId) } returns itemsFlow
        every { notifRepo.getSettingForList(listId) } returns MutableStateFlow(null)
        viewModel = ListDetailViewModel(app, repo, notifRepo, alarmScheduler, listId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `changeItemIndent should update item in repository when not first item`() = runTest {
        val item1 = TaskItem(id = 9, listId = listId, text = "Parent", indentLevel = 0)
        val item2 = TaskItem(id = 10, listId = listId, text = "Child", indentLevel = 0)
        itemsFlow.value = listOf(item1, item2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.changeItemIndent(item2, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateItem(match { it.id == 10L && it.indentLevel == 1 }) }
    }

    @Test
    fun `changeItemIndent should not indent first item`() = runTest {
        val item = TaskItem(id = 10, listId = listId, text = "First Task", indentLevel = 0)
        itemsFlow.value = listOf(item)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.changeItemIndent(item, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repo.updateItem(any()) }
    }

    @Test
    fun `changeItemIndent should allow outdenting from level 1 to 0`() = runTest {
        val item = TaskItem(id = 10, listId = listId, text = "Task", indentLevel = 1)
        itemsFlow.value = listOf(item)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.changeItemIndent(item, -1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateItem(match { it.id == 10L && it.indentLevel == 0 }) }
    }
}
