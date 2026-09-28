package com.personal.lifeos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.personal.lifeos.data.local.AppDatabase
import com.personal.lifeos.data.local.entity.FoodEntity
import com.personal.lifeos.data.local.entity.RoutineBlockEntity
import com.personal.lifeos.data.local.entity.StepRecordEntity
import com.personal.lifeos.data.local.entity.TaskEntity
import com.personal.lifeos.data.repository.LifeOsRepository
import com.personal.lifeos.domain.engine.LocalAvailabilityEngine
import com.personal.lifeos.domain.engine.LocalAvailabilityResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val overallProgressPct: Float = 0.72f,
    val stepsCurrent: Int = 7842,
    val stepsTarget: Int = 10000,
    val caloriesCurrent: Double = 860.0,
    val caloriesTarget: Double = 1200.0,
    val focusMinutesCurrent: Int = 137, // 2h 17m
    val focusMinutesTarget: Int = 180,  // 3h
    val tasks: List<TaskEntity> = emptyList(),
    val availability: LocalAvailabilityResult? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = LifeOsRepository(database)
    private val availabilityEngine = LocalAvailabilityEngine()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getTodayTasks(),
                repository.getTodayStepRecord(),
                repository.getTodayFoodEntries(),
                repository.getRoutineBlocks()
            ) { tasks, stepRec, foods, routines ->
                calculateHomeState(tasks, stepRec, foods, routines)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateHomeState(
        tasks: List<TaskEntity>,
        stepRec: StepRecordEntity?,
        foods: List<FoodEntity>,
        routines: List<RoutineBlockEntity>
    ): HomeUiState {
        val steps = stepRec?.steps ?: 7842
        val stepTarget = stepRec?.target ?: 10000

        val totalCalories = if (foods.isNotEmpty()) foods.sumOf { it.calories } else 860.0

        var focusCurrent = 0
        var focusTarget = 0
        for (t in tasks) {
            if (t.measurementType == "time_based") {
                focusCurrent += t.currentValue.toInt()
                focusTarget += t.targetValue.toInt()
            }
        }
        if (focusTarget == 0) focusTarget = 180
        if (focusCurrent == 0) focusCurrent = 137

        val taskCompletion = if (tasks.isNotEmpty()) {
            tasks.count { it.isCompleted }.toFloat() / tasks.size
        } else 0.5f

        val stepCompletion = (steps.toFloat() / stepTarget).coerceIn(0f, 1f)
        val focusCompletion = (focusCurrent.toFloat() / focusTarget).coerceIn(0f, 1f)
        val overallProgress = (taskCompletion * 0.4f + focusCompletion * 0.3f + stepCompletion * 0.3f)

        val avail = availabilityEngine.calculate(routines, tasks)

        return HomeUiState(
            isLoading = false,
            overallProgressPct = overallProgress,
            stepsCurrent = steps,
            stepsTarget = stepTarget,
            caloriesCurrent = totalCalories,
            caloriesTarget = 1200.0,
            focusMinutesCurrent = focusCurrent,
            focusMinutesTarget = focusTarget,
            tasks = tasks,
            availability = avail
        )
    }
}
