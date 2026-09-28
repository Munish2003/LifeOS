package com.personal.lifeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecordEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val steps: Int = 0,
    val target: Int = 10000,
    val distanceMeters: Double = 0.0,
    val caloriesBurned: Double = 0.0,
    val activeMinutes: Int = 0,
    val isSynced: Boolean = false
)

@Entity(tableName = "food_entries")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val time: String? = null,
    val mealType: String = "lunch", // breakfast, lunch, dinner, snack
    val foodName: String,
    val portionDesc: String? = null,
    val quantity: Double = 1.0,
    val calories: Double,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0,
    val source: String = "manual",
    val isSynced: Boolean = false
)

@Entity(tableName = "routine_blocks")
data class RoutineBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "routine", // work, commute, routine, spiritual, meal, outing
    val startTime: String, // HH:MM
    val endTime: String,   // HH:MM
    val durationMinutes: Int,
    val isFlexible: Boolean = false,
    val dayType: String = "weekday"
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val durationDays: Int = 15,
    val daysCompleted: Int = 0,
    val currentProgress: Double = 0.0,
    val targetMetric: String,
    val isActive: Boolean = true
)
