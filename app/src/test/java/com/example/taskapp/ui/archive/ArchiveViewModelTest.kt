package com.example.taskapp.ui.archive

import app.cash.turbine.test
import com.example.taskapp.data.repository.TaskRepository
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
class ArchiveViewModelTest {

    private val repo = mockk<TaskRepository>(relaxed = true)
    private lateinit var viewModel: ArchiveViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getArchivedLists() } returns flowOf(emptyList())
        viewModel = ArchiveViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `lists flow should emit archived lists`() = runTest {
        val archivedLists = listOf(TaskList(id = 1, title = "Archived", isArchived = true))
        every { repo.getArchivedLists() } returns flowOf(archivedLists)
        
        viewModel = ArchiveViewModel(repo)

        viewModel.lists.test {
            assertEquals(emptyList<TaskList>(), awaitItem())
            assertEquals(archivedLists, awaitItem())
        }
    }

    @Test
    fun `restoreList should call repository restoreFromArchive`() = runTest {
        val list = TaskList(id = 1, title = "Archived", isArchived = true)
        
        viewModel.restoreList(list)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.restoreFromArchive(list) }
    }

    @Test
    fun `deleteList should call repository moveToTrash`() = runTest {
        val list = TaskList(id = 1, title = "Archived", isArchived = true)
        
        viewModel.deleteList(list)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.moveToTrash(list) }
    }
}
