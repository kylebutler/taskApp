package com.example.taskapp.data.repository

import com.example.taskapp.data.local.TaskAppDatabase
import com.example.taskapp.data.local.TaskItemEntity
import com.example.taskapp.data.local.TaskListEntity
import com.example.taskapp.data.local.toDomain
import com.example.taskapp.data.local.toEntity
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskItem
import com.example.taskapp.domain.model.TaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TaskRepository(private val db: TaskAppDatabase) {

    fun getAllLists(): Flow<List<TaskList>> =
        combine(
            db.taskListDao().getAllLists(),
            db.notificationSettingDao().getAllEnabledSettingsFlow()
        ) { lists, enabledSettings ->
            val enabledListIds = enabledSettings.map { it.listId }.toSet()
            lists.map { it.toDomain(isNotificationEnabled = enabledListIds.contains(it.id)) }
        }

    fun getTrashLists(): Flow<List<TaskList>> =
        db.taskListDao().getTrashLists().map { it.map { e -> e.toDomain() } }

    fun getArchivedLists(): Flow<List<TaskList>> =
        combine(
            db.taskListDao().getArchivedLists(),
            db.notificationSettingDao().getAllEnabledSettingsFlow()
        ) { lists, enabledSettings ->
            val enabledListIds = enabledSettings.map { it.listId }.toSet()
            lists.map { it.toDomain(isNotificationEnabled = enabledListIds.contains(it.id)) }
        }

    fun getListById(listId: Long): Flow<TaskList?> =
        combine(
            db.taskListDao().getListById(listId),
            db.notificationSettingDao().getSettingForList(listId)
        ) { list, setting ->
            list?.toDomain(isNotificationEnabled = setting?.isEnabled == true)
        }

    suspend fun createList(title: String, type: ListType = ListType.CHECKLIST): Long =
        db.taskListDao().insertList(TaskListEntity(title = title, type = type.name))

    suspend fun updateList(list: TaskList) =
        db.taskListDao().updateList(list.toEntity())

    suspend fun moveToTrash(list: TaskList) =
        db.taskListDao().updateList(list.copy(isDeleted = true, deletedAt = System.currentTimeMillis()).toEntity())

    suspend fun moveToArchive(list: TaskList) =
        db.taskListDao().updateList(list.copy(isArchived = true, isDeleted = false).toEntity())

    suspend fun restoreFromArchive(list: TaskList) =
        db.taskListDao().updateList(list.copy(isArchived = false).toEntity())

    suspend fun restoreList(list: TaskList) =
        db.taskListDao().updateList(list.copy(isDeleted = false, deletedAt = null).toEntity())

    suspend fun permanentlyDeleteList(list: TaskList) =
        db.taskListDao().deleteList(list.toEntity())

    suspend fun reorderLists(lists: List<TaskList>) {
        db.taskListDao().updateLists(
            lists.mapIndexed { index, list -> list.copy(position = index).toEntity() }
        )
    }

    suspend fun deleteList(list: TaskList) =
        moveToTrash(list)

    suspend fun cleanOldTrash() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        db.taskListDao().deleteOldTrash(thirtyDaysAgo)
    }

    fun getItemsForList(listId: Long): Flow<List<TaskItem>> =
        db.taskItemDao().getItemsForList(listId).map { it.map { e -> e.toDomain() } }

    suspend fun addItem(listId: Long, text: String, position: Int): Long =
        db.taskItemDao().insertItem(TaskItemEntity(listId = listId, text = text, position = position))

    suspend fun updateItem(item: TaskItem) =
        db.taskItemDao().updateItem(item.toEntity())

    suspend fun deleteItem(item: TaskItem) =
        db.taskItemDao().deleteItem(item.toEntity())

    suspend fun reorderItems(items: List<TaskItem>) {
        db.taskItemDao().updateItems(
            items.mapIndexed { index, item -> item.copy(position = index).toEntity() }
        )
    }

    suspend fun syncItems(listId: Long, items: List<TaskItem>) {
        db.taskItemDao().deleteItemsForList(listId)
        db.taskItemDao().insertItems(items.map { it.toEntity() })
    }
}
