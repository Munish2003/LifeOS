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
import com.personal.lifeos.service.GoogleFitManager
import com.personal.lifeos.service.StepSensorManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val overallProgressPct: Float = 0.0f,
    val stepsCurrent: Int = 0,
    val stepsTarget: Int = 10000,
    val caloriesCurrent: Double = 0.0,
    val caloriesTarget: Double = 1200.0,
    val focusMinutesCurrent: Int = 0,
    val focusMinutesTarget: Int = 180,
    val tasks: List<TaskEntity> = emptyList(),
    val availability: LocalAvailabilityResult? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = LifeOsRepository(database)
    private val availabilityEngine = LocalAvailabilityEngine()
    val stepSensorManager = StepSensorManager(application)
    val googleFitManager = GoogleFitManager(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        stepSensorManager.startListening()
        loadData()
        listenToHardwareSteps()
        if (googleFitManager.isConnected() && googleFitManager.isAutoSync()) {
            syncGoogleFit()
        }
    }

    fun syncGoogleFit(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                if (!googleFitManager.isHealthConnectAvailable()) {
                    onResult(false, "Health Connect service not available on this device.")
                    return@launch
                }
                val hasPerms = googleFitManager.hasPermissions()
                if (!hasPerms) {
                    onResult(false, "Permissions not granted yet. Tap 'Connect Google Fit' to grant access.")
                    return@launch
                }
                val fitSteps = googleFitManager.readTodaySteps()
                repository.updateSteps(fitSteps.toInt())
                googleFitManager.setConnected(true)
                if (fitSteps > 0) {
                    onResult(true, "Successfully synced ${fitSteps} actual steps from Google Fit!")
                } else {
                    onResult(true, "Google Fit synced. 0 steps recorded so far today.")
                }
            } catch (e: Exception) {
                onResult(false, "Google Fit sync error: ${e.message}")
            }
        }
    }

    private fun listenToHardwareSteps() {
        viewModelScope.launch {
            stepSensorManager.liveSteps.collect { hardwareSteps ->
                if (hardwareSteps > 0) {
                    repository.updateSteps(hardwareSteps)
                }
            }
        }
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
        val steps = stepRec?.steps ?: 0
        val stepTarget = stepRec?.target ?: 10000

        val totalCalories = foods.sumOf { it.calories }

        var focusCurrent = 0
        var focusTarget = 0
        for (t in tasks) {
            if (t.measurementType == "time_based") {
                focusCurrent += t.currentValue.toInt()
                focusTarget += t.targetValue.toInt()
            }
        }
        if (focusTarget == 0) focusTarget = 180

        val taskCompletion = if (tasks.isNotEmpty()) {
            tasks.count { it.isCompleted }.toFloat() / tasks.size
        } else 0f

        val stepCompletion = if (stepTarget > 0) (steps.toFloat() / stepTarget).coerceIn(0f, 1f) else 0f
        val focusCompletion = if (focusTarget > 0) (focusCurrent.toFloat() / focusTarget).coerceIn(0f, 1f) else 0f

        val overallProgress = if (tasks.isEmpty() && steps == 0 && focusCurrent == 0) {
            0f
        } else {
            (taskCompletion * 0.4f + focusCompletion * 0.3f + stepCompletion * 0.3f)
        }

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

    override fun onCleared() {
        super.onCleared()
        stepSensorManager.stopListening()
    }
}
