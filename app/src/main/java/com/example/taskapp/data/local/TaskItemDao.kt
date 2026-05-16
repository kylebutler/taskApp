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
