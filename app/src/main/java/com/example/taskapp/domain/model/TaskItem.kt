package com.example.taskapp.domain.model

data class TaskItem(
    val id: Long = 0,
    val listId: Long,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0
)
