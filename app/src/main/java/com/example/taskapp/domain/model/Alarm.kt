package com.example.taskapp.domain.model

data class Alarm(
    val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: Int = 0, // Bitmask: bit 0=Sun, bit 1=Mon, ..., bit 6=Sat
    val isOneTime: Boolean = true,
    val isSilent: Boolean = false,
    val position: Int = 0
)
