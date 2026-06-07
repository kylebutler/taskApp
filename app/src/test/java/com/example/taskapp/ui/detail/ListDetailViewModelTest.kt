package com.example.taskapp.ui.detail

import app.cash.turbine.test
import com.example.taskapp.TaskApp
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.TaskItem
import com.example.taskapp.domain.model.TaskList
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    private val listFlow = MutableStateFlow<TaskList?>(TaskList(id = listId, title = "Test List"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getListById(listId) } returns listFlow
        every { repo.getItemsForList(listId) } returns itemsFlow
        every { notifRepo.getSettingForList(listId) } returns MutableStateFlow(null)
        viewModel = ListDetailViewModel(app, repo, notifRepo, alarmScheduler, listId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addItem should call repository when text is not blank`() = runTest {
        viewModel.setNewItemText("New Task")
        viewModel.addItem()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.addItem(listId, "New Task", 0) }
    }

    @Test
    fun `toggleItem should call repository with flipped checked state`() = runTest {
        val item = TaskItem(id = 10, listId = listId, text = "Task", isChecked = false)
        
        viewModel.toggleItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateItem(match { it.id == 10L && it.isChecked }) }
    }

    @Test
    fun `undo should update UI state correctly`() = runTest {
        val initialItems = listOf(TaskItem(id = 10, listId = listId, text = "Original"))
        itemsFlow.value = initialItems
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Action
        viewModel.updateItemText(initialItems[0], "Updated")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue("canUndo should be true", viewModel.uiState.value.canUndo)
        
        viewModel.undo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse("canUndo should be false after undoing one action", viewModel.uiState.value.canUndo)
        assertTrue("canRedo should be true after undo", viewModel.uiState.value.canRedo)
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
}
