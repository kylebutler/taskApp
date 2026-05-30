package com.example.taskapp.ui.tasks

import app.cash.turbine.test
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList
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
class MyTasksViewModelTest {

    private val repo = mockk<TaskRepository>(relaxed = true)
    private lateinit var viewModel: MyTasksViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getStandaloneTasks() } returns flowOf(emptyList())
        viewModel = MyTasksViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tasks flow should emit standalone tasks from repository`() = runTest {
        val tasks = listOf(
            TaskList(id = 1, title = "Task 1", type = ListType.TASK),
            TaskList(id = 2, title = "Task 2", type = ListType.TASK)
        )
        every { repo.getStandaloneTasks() } returns flowOf(tasks)
        
        viewModel = MyTasksViewModel(repo)

        viewModel.tasks.test {
            assertEquals(emptyList<TaskList>(), awaitItem())
            assertEquals(tasks, awaitItem())
        }
    }

    @Test
    fun `createTask should call repository createList with TASK type`() = runTest {
        viewModel.createTask("Fix sink")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.createList("Fix sink", ListType.TASK) }
    }

    @Test
    fun `toggleTask should update task isChecked state`() = runTest {
        val task = TaskList(id = 1, title = "Task", isChecked = false)
        
        viewModel.toggleTask(task)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateList(match { it.id == 1L && it.isChecked }) }
    }

    @Test
    fun `updateTaskTitle should call repository updateList with new title`() = runTest {
        val task = TaskList(id = 1, title = "Old Title")
        
        viewModel.updateTaskTitle(task, "New Title")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.updateList(match { it.id == 1L && it.title == "New Title" }) }
    }

    @Test
    fun `deleteTask should move task to trash`() = runTest {
        val task = TaskList(id = 1, title = "To Delete")
        
        viewModel.deleteTask(task)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.moveToTrash(match { it.id == 1L }) }
    }

    @Test
    fun `reorderTasks should call repository reorderLists`() = runTest {
        val tasks = listOf(
            TaskList(id = 2, title = "Task 2"),
            TaskList(id = 1, title = "Task 1")
        )
        
        viewModel.reorderTasks(tasks)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.reorderLists(tasks) }
    }
}
