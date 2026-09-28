package com.personal.lifeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val category: String = "learning", // health, learning, work, personal, goals
    val measurementType: String = "time_based", // time_based, quantity_based, distance_based, step_based, numeric, binary
    val targetValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val unit: String = "minutes",
    val priority: Int = 3,
    val deadline: String? = null,
    val scheduledDate: String, // YYYY-MM-DD
    val recurrence: String = "none",
    val isFlexible: Boolean = true,
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)

@Entity(tableName = "task_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val sessionNumber: Int = 1,
    val startTime: Long, // Epoch ms
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val notes: String? = null,
    val isSynced: Boolean = false
)
