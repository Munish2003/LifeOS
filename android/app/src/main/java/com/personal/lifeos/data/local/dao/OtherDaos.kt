package com.personal.lifeos.data.local.dao

import androidx.room.*
import com.personal.lifeos.data.local.entity.ChallengeEntity
import com.personal.lifeos.data.local.entity.FoodEntity
import com.personal.lifeos.data.local.entity.RoutineBlockEntity
import com.personal.lifeos.data.local.entity.StepRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM step_records WHERE date = :date")
    fun getStepRecord(date: String): Flow<StepRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStepRecord(record: StepRecordEntity)

    @Query("SELECT * FROM step_records ORDER BY date DESC LIMIT 7")
    fun getRecentStepRecords(): Flow<List<StepRecordEntity>>
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY id ASC")
    fun getFoodEntriesForDate(date: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(food: FoodEntity): Long

    @Delete
    suspend fun deleteFoodEntry(food: FoodEntity)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_blocks WHERE dayType = :dayType ORDER BY startTime ASC")
    fun getRoutineBlocks(dayType: String): Flow<List<RoutineBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBlocks(blocks: List<RoutineBlockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBlock(block: RoutineBlockEntity): Long

    @Query("DELETE FROM routine_blocks WHERE dayType = :dayType")
    suspend fun clearBlocksForDayType(dayType: String)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE isActive = 1")
    fun getActiveChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)
}
