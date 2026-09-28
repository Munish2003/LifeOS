package com.personal.lifeos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.personal.lifeos.data.local.dao.*
import com.personal.lifeos.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        TaskEntity::class,
        SessionEntity::class,
        StepRecordEntity::class,
        FoodEntity::class,
        RoutineBlockEntity::class,
        ChallengeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun healthDao(): HealthDao
    abstract fun foodDao(): FoodDao
    abstract fun routineDao(): RoutineDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_os_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // 1. Initial Tasks from newappneed.md
                val tasks = listOf(
                    TaskEntity(title = "Python Deep Work", category = "learning", measurementType = "time_based", targetValue = 180.0, currentValue = 137.0, unit = "minutes", priority = 1, scheduledDate = today, isFlexible = false),
                    TaskEntity(title = "DSA Practice", category = "learning", measurementType = "quantity_based", targetValue = 20.0, currentValue = 15.0, unit = "questions", priority = 2, scheduledDate = today, isFlexible = true),
                    TaskEntity(title = "Daily 10k Steps", category = "health", measurementType = "step_based", targetValue = 10000.0, currentValue = 7842.0, unit = "steps", priority = 1, scheduledDate = today, isFlexible = false),
                    TaskEntity(title = "Evening Pooja", category = "personal", measurementType = "binary", targetValue = 1.0, currentValue = 0.0, unit = "boolean", priority = 1, scheduledDate = today, isFlexible = false)
                )
                db.taskDao().insertTasks(tasks)

                // 2. Initial Steps
                val stepRecord = StepRecordEntity(
                    date = today,
                    steps = 7842,
                    target = 10000,
                    distanceMeters = 5881.5,
                    caloriesBurned = 313.6,
                    activeMinutes = 78
                )
                db.healthDao().insertOrUpdateStepRecord(stepRecord)

                // 3. Initial Routine Blocks (Weekday)
                val routineBlocks = listOf(
                    RoutineBlockEntity(name = "Getting Ready", category = "routine", startTime = "07:30", endTime = "08:15", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Bath & Morning Pooja", category = "spiritual", startTime = "08:15", endTime = "08:35", durationMinutes = 20, dayType = "weekday"),
                    RoutineBlockEntity(name = "Breakfast", category = "meal", startTime = "08:35", endTime = "09:00", durationMinutes = 25, dayType = "weekday"),
                    RoutineBlockEntity(name = "Morning Commute", category = "commute", startTime = "09:00", endTime = "09:30", durationMinutes = 30, dayType = "weekday"),
                    RoutineBlockEntity(name = "Office Work", category = "work", startTime = "09:30", endTime = "18:00", durationMinutes = 510, dayType = "weekday"),
                    RoutineBlockEntity(name = "Evening Commute", category = "commute", startTime = "18:00", endTime = "18:45", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Dinner", category = "meal", startTime = "19:30", endTime = "20:15", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Evening Pooja", category = "spiritual", startTime = "20:15", endTime = "20:35", durationMinutes = 20, dayType = "weekday")
                )
                db.routineDao().insertRoutineBlocks(routineBlocks)

                // 4. Initial Food
                val foods = listOf(
                    FoodEntity(date = today, time = "08:45", mealType = "breakfast", foodName = "Oats & Milk with Banana", calories = 415.0, proteinG = 14.8, carbsG = 67.0, fatG = 11.3, fiberG = 7.5),
                    FoodEntity(date = today, time = "13:15", mealType = "lunch", foodName = "2 Rotis with Paneer Bhurji & Salad", calories = 445.0, proteinG = 24.0, carbsG = 47.5, fatG = 21.0, fiberG = 6.0)
                )
                for (f in foods) {
                    db.foodDao().insertFoodEntry(f)
                }

                // 5. Initial 15-day Challenges
                val challenges = listOf(
                    ChallengeEntity(title = "15-Day Step Challenge", description = "Hit 10,000 steps daily for 15 days.", durationDays = 15, daysCompleted = 8, currentProgress = 53.3, targetMetric = "steps_daily"),
                    ChallengeEntity(title = "30-Hour Python Sprint", description = "30 hours of focused Python learning in 15 days.", durationDays = 15, daysCompleted = 6, currentProgress = 14.5, targetMetric = "hours_total")
                )
                db.challengeDao().insertChallenges(challenges)
            }
        }
    }
}
