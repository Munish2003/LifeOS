package com.personal.lifeos.data.local.dao

import androidx.room.*
import com.personal.lifeos.data.local.entity.SessionEntity
import com.personal.lifeos.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE scheduledDate = :date ORDER BY priority ASC, id ASC")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET currentValue = currentValue + :addedValue, isCompleted = CASE WHEN (currentValue + :addedValue) >= targetValue THEN 1 ELSE isCompleted END WHERE id = :taskId")
    suspend fun incrementTaskProgress(taskId: Long, addedValue: Double)

    // Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM task_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<SessionEntity>>
}
