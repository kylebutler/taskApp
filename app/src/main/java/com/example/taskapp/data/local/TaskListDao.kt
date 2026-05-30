package com.example.taskapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists WHERE isDeleted = 0 AND isArchived = 0 AND type != 'TASK' ORDER BY position ASC, createdAt DESC")
    fun getAllLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE isDeleted = 0 AND isArchived = 0 AND type = 'TASK' ORDER BY isChecked ASC, position ASC, createdAt ASC")
    fun getStandaloneTasks(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE isArchived = 1 AND isDeleted = 0 ORDER BY position ASC, createdAt DESC")
    fun getArchivedLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE id = :listId")
    fun getListById(listId: Long): Flow<TaskListEntity?>

    @Query("DELETE FROM task_lists WHERE isDeleted = 1 AND deletedAt < :timestamp")
    suspend fun deleteOldTrash(timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskListEntity): Long

    @Update
    suspend fun updateList(list: TaskListEntity)

    @Update
    suspend fun updateLists(lists: List<TaskListEntity>)

    @Delete
    suspend fun deleteList(list: TaskListEntity)
}
