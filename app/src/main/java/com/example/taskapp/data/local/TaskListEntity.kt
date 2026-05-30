package com.example.taskapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String = ListType.CHECKLIST.name,
    val textContent: String? = null,
    val colorArgb: Int? = null,
    val isDeleted: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val isChecked: Boolean = false,
    val position: Int = 0,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun TaskListEntity.toDomain(isNotificationEnabled: Boolean = false) = TaskList(
    id = id,
    title = title,
    type = ListType.valueOf(type),
    textContent = textContent,
    colorArgb = colorArgb,
    isNotificationEnabled = isNotificationEnabled,
    isDeleted = isDeleted,
    isArchived = isArchived,
    isLocked = isLocked,
    isChecked = isChecked,
    position = position,
    deletedAt = deletedAt,
    createdAt = createdAt
)

fun TaskList.toEntity() = TaskListEntity(
    id = id,
    title = title,
    type = type.name,
    textContent = textContent,
    colorArgb = colorArgb,
    isDeleted = isDeleted,
    isArchived = isArchived,
    isLocked = isLocked,
    isChecked = isChecked,
    position = position,
    deletedAt = deletedAt,
    createdAt = createdAt
)
