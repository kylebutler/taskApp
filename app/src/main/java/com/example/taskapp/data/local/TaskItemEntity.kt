package com.example.taskapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskapp.domain.model.TaskItem

@Entity(
    tableName = "task_items",
    foreignKeys = [ForeignKey(
        entity = TaskListEntity::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["listId"])]
)
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0,
    val indentLevel: Int = 0
)

fun TaskItemEntity.toDomain() = TaskItem(id, listId, text, isChecked, position, indentLevel)
fun TaskItem.toEntity() = TaskItemEntity(id, listId, text, isChecked, position, indentLevel)
