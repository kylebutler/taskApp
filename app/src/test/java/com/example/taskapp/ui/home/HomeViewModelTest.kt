package com.example.taskapp.ui.home

import app.cash.turbine.test
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList
import io.mockk.coEvery
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
class HomeViewModelTest {

    private val repo = mockk<TaskRepository>(relaxed = true)
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getAllLists() } returns flowOf(emptyList())
        viewModel = HomeViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `lists flow should emit lists from repository`() = runTest {
        val lists = listOf(
            TaskList(id = 1, title = "List 1", type = ListType.CHECKLIST),
            TaskList(id = 2, title = "List 2", type = ListType.TEXT)
        )
        every { repo.getAllLists() } returns flowOf(lists)

        // Re-create ViewModel to pick up the new flow
        viewModel = HomeViewModel(repo)

        viewModel.lists.test {
            assertEquals(emptyList<TaskList>(), awaitItem())
            assertEquals(lists, awaitItem())
        }
    }

    @Test
    fun `createList should call repository createList`() = runTest {
        val title = "New List"
        val type = ListType.CHECKLIST

        viewModel.createList(title, type)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.createList(title, type) }
    }

    @Test
    fun `moveToTrash should call repository moveToTrash`() = runTest {
        val list = TaskList(id = 1, title = "To delete")

        viewModel.moveToTrash(list)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.moveToTrash(list) }
    }

    @Test
    fun `reorderLists should call repository reorderLists`() = runTest {
        val lists = listOf(
            TaskList(id = 2, title = "Second"),
            TaskList(id = 1, title = "First")
        )

        viewModel.reorderLists(lists)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.reorderLists(lists) }
    }
}
