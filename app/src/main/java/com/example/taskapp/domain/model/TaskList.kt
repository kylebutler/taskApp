package com.example.taskapp.domain.model

data class TaskList(
    val id: Long = 0,
    val title: String,
    val type: ListType = ListType.CHECKLIST,
    val textContent: String? = null,
    val colorArgb: Int? = null,
    val isNotificationEnabled: Boolean = false,
    val isDeleted: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val position: Int = 0,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
