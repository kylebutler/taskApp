package com.example.taskapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskItemDao {
    @Query("SELECT * FROM task_items WHERE listId = :listId ORDER BY position ASC")
    fun getItemsForList(listId: Long): Flow<List<TaskItemEntity>>

    @Query("""
        SELECT task_items.* FROM task_items 
        INNER JOIN task_lists ON task_items.listId = task_lists.id 
        WHERE task_lists.isDeleted = 0 AND task_lists.isArchived = 0 
        ORDER BY task_items.isChecked ASC, task_lists.position ASC, task_items.position ASC
    """)
    fun getAllActiveItems(): Flow<List<TaskItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TaskItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TaskItemEntity>)

    @Query("DELETE FROM task_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: Long)

    @Update
    suspend fun updateItem(item: TaskItemEntity)

    @Update
    suspend fun updateItems(items: List<TaskItemEntity>)

    @Delete
    suspend fun deleteItem(item: TaskItemEntity)
}
