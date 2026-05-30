package com.example.taskapp.data.repository

import com.example.taskapp.data.local.NotificationSettingDao
import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.local.TaskItemDao
import com.example.taskapp.data.local.TaskListDao
import com.example.taskapp.domain.model.TaskList
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    private val db = mockk<TaskAppDatabase>()
    private val taskListDao = mockk<TaskListDao>(relaxed = true)
    private val taskItemDao = mockk<TaskItemDao>(relaxed = true)
    private val notificationSettingDao = mockk<NotificationSettingDao>(relaxed = true)
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        every { db.taskListDao() } returns taskListDao
        every { db.taskItemDao() } returns taskItemDao
        every { db.notificationSettingDao() } returns notificationSettingDao
        repository = TaskRepository(db)
    }

    @Test
    fun `moveToTrash should set isDeleted to true and set deletedAt`() = runTest {
        val list = TaskList(id = 1, title = "Test List")
        
        repository.moveToTrash(list)

        coVerify { 
            taskListDao.updateList(match { 
                it.id == 1L && it.isDeleted && it.deletedAt != null 
            }) 
        }
    }

    @Test
    fun `moveToArchive should set isArchived to true and isDeleted to false`() = runTest {
        val list = TaskList(id = 1, title = "Test List", isDeleted = true)
        
        repository.moveToArchive(list)

        coVerify { 
            taskListDao.updateList(match { 
                it.id == 1L && it.isArchived && !it.isDeleted 
            }) 
        }
    }

    @Test
    fun `cleanOldTrash should call dao with timestamp from 30 days ago`() = runTest {
        val now = System.currentTimeMillis()
        // We can't easily mock System.currentTimeMillis() with mockk without static mock,
        // but we can verify it's roughly 30 days ago.
        val expectedThreshold = now - (30L * 24 * 60 * 60 * 1000)
        
        repository.cleanOldTrash()

        coVerify { 
            taskListDao.deleteOldTrash(match { 
                it in (expectedThreshold - 1000)..(expectedThreshold + 1000)
            }) 
        }
    }

    @Test
    fun `reorderLists should update positions correctly`() = runTest {
        val lists = listOf(
            TaskList(id = 10, title = "A"),
            TaskList(id = 20, title = "B")
        )

        repository.reorderLists(lists)

        coVerify {
            taskListDao.updateLists(match {
                it[0].id == 10L && it[0].position == 0 &&
                it[1].id == 20L && it[1].position == 1
            })
        }
    }
}
