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
    version = 2,
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
                    .fallbackToDestructiveMigration()
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
                        populateCleanProductionData(database)
                    }
                }
            }

            private suspend fun populateCleanProductionData(db: AppDatabase) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // 1. Clean starter tasks for the user (All start at 0 progress so user actually tracks their real day)
                val cleanTasks = listOf(
                    TaskEntity(title = "Python Deep Work", category = "learning", measurementType = "time_based", targetValue = 180.0, currentValue = 0.0, unit = "minutes", priority = 1, scheduledDate = today, isFlexible = false),
                    TaskEntity(title = "DSA Practice", category = "learning", measurementType = "quantity_based", targetValue = 10.0, currentValue = 0.0, unit = "questions", priority = 2, scheduledDate = today, isFlexible = true),
                    TaskEntity(title = "Daily 10k Steps", category = "health", measurementType = "step_based", targetValue = 10000.0, currentValue = 0.0, unit = "steps", priority = 1, scheduledDate = today, isFlexible = false),
                    TaskEntity(title = "Evening Pooja", category = "personal", measurementType = "binary", targetValue = 1.0, currentValue = 0.0, unit = "boolean", priority = 1, scheduledDate = today, isFlexible = false)
                )
                db.taskDao().insertTasks(cleanTasks)

                // 2. Real zero-baseline Step record (Real phone hardware sensor will increment this!)
                val initialStepRecord = StepRecordEntity(
                    date = today,
                    steps = 0,
                    target = 10000,
                    distanceMeters = 0.0,
                    caloriesBurned = 0.0,
                    activeMinutes = 0
                )
                db.healthDao().insertOrUpdateStepRecord(initialStepRecord)

                // 3. User's Personal Routine Blocks (Editable in Schedule screen)
                val routineBlocks = listOf(
                    RoutineBlockEntity(name = "Getting Ready & Bath", category = "routine", startTime = "07:30", endTime = "08:15", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Morning Pooja & Breakfast", category = "spiritual", startTime = "08:15", endTime = "09:00", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Morning Commute", category = "commute", startTime = "09:00", endTime = "09:30", durationMinutes = 30, dayType = "weekday"),
                    RoutineBlockEntity(name = "Office Work", category = "work", startTime = "09:30", endTime = "18:00", durationMinutes = 510, dayType = "weekday"),
                    RoutineBlockEntity(name = "Evening Commute", category = "commute", startTime = "18:00", endTime = "18:45", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Dinner & Family", category = "meal", startTime = "19:30", endTime = "20:15", durationMinutes = 45, dayType = "weekday"),
                    RoutineBlockEntity(name = "Evening Pooja", category = "spiritual", startTime = "20:15", endTime = "20:35", durationMinutes = 20, dayType = "weekday")
                )
                db.routineDao().insertRoutineBlocks(routineBlocks)

                // 4. Fresh Challenges (Starting at 0 days completed)
                val challenges = listOf(
                    ChallengeEntity(title = "15-Day 10,000 Step Challenge", description = "Hit 10,000 steps every day for 15 days.", durationDays = 15, daysCompleted = 0, currentProgress = 0.0, targetMetric = "steps_daily"),
                    ChallengeEntity(title = "30-Hour Python Sprint", description = "Complete 30 hours of focused Python learning in 15 days.", durationDays = 15, daysCompleted = 0, currentProgress = 0.0, targetMetric = "hours_total")
                )
                db.challengeDao().insertChallenges(challenges)
            }
        }
    }
}
