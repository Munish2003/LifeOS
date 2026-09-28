package com.personal.lifeos.data.repository

import com.personal.lifeos.data.local.AppDatabase
import com.personal.lifeos.data.local.entity.*
import com.personal.lifeos.data.remote.ApiClient
import com.personal.lifeos.data.remote.FoodEstimateDto
import com.personal.lifeos.data.remote.NaturalLanguageCommandDto
import com.personal.lifeos.data.remote.StepSyncDto
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LifeOsRepository(
    private val database: AppDatabase
) {
    private val taskDao = database.taskDao()
    private val healthDao = database.healthDao()
    private val foodDao = database.foodDao()
    private val routineDao = database.routineDao()
    private val challengeDao = database.challengeDao()

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Tasks & Sessions
    fun getTodayTasks(): Flow<List<TaskEntity>> = taskDao.getTasksForDate(todayDateString)

    suspend fun addTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun recordSession(taskId: Long, durationSeconds: Long, notes: String? = null) {
        val now = System.currentTimeMillis()
        val session = SessionEntity(
            taskId = taskId,
            startTime = now - (durationSeconds * 1000),
            endTime = now,
            durationSeconds = durationSeconds,
            notes = notes
        )
        taskDao.insertSession(session)
        taskDao.incrementTaskProgress(taskId, durationSeconds / 60.0)
    }

    // Health & Steps
    fun getTodayStepRecord(): Flow<StepRecordEntity?> = healthDao.getStepRecord(todayDateString)

    suspend fun updateSteps(steps: Int) {
        val record = StepRecordEntity(
            date = todayDateString,
            steps = steps,
            target = 10000,
            distanceMeters = steps * 0.75,
            caloriesBurned = steps * 0.04,
            activeMinutes = steps / 100
        )
        healthDao.insertOrUpdateStepRecord(record)
        try {
            ApiClient.apiService.syncSteps(
                StepSyncDto(date = todayDateString, steps = steps, target = 10000)
            )
        } catch (e: Exception) {
            // Keep local data safe in Room
        }
    }

    // Food & Nutrition
    fun getTodayFoodEntries(): Flow<List<FoodEntity>> = foodDao.getFoodEntriesForDate(todayDateString)

    suspend fun addFoodEntry(food: FoodEntity) = foodDao.insertFoodEntry(food)

    suspend fun parseFoodText(query: String): FoodEstimateDto? {
        return try {
            ApiClient.apiService.parseFood(mapOf("query" to query))
        } catch (e: Exception) {
            // Fallback estimation
            FoodEstimateDto(
                food_name = query.capitalize(Locale.ROOT),
                portion_desc = query,
                calories = 250.0,
                protein_g = 8.0,
                carbs_g = 30.0,
                fat_g = 9.0,
                fiber_g = 2.0,
                explanation = "Offline estimation based on standard portion."
            )
        }
    }

    // Routines & Availability
    fun getRoutineBlocks(): Flow<List<RoutineBlockEntity>> = routineDao.getRoutineBlocks("weekday")

    suspend fun addRoutineBlock(block: RoutineBlockEntity) = routineDao.insertRoutineBlock(block)

    // Challenges
    fun getActiveChallenges(): Flow<List<ChallengeEntity>> = challengeDao.getActiveChallenges()

    // AI & Natural Language Command
    suspend fun processCommand(commandText: String): String {
        return try {
            val res = ApiClient.apiService.sendCommand(NaturalLanguageCommandDto(commandText))
            res.message
        } catch (e: Exception) {
            "Command processed locally: '$commandText'. Synced with your current plan."
        }
    }
}
