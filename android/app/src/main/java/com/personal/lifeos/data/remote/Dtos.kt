package com.personal.lifeos.data.remote

data class StepSyncDto(
    val date: String,
    val steps: Int,
    val target: Int = 10000,
    val distance_meters: Double? = null,
    val calories_burned: Double? = null,
    val active_minutes: Int? = null
)

data class FoodDto(
    val date: String,
    val time: String? = null,
    val meal_type: String = "lunch",
    val food_name: String,
    val portion_desc: String? = null,
    val quantity: Double = 1.0,
    val calories: Double,
    val protein_g: Double = 0.0,
    val carbs_g: Double = 0.0,
    val fat_g: Double = 0.0,
    val fiber_g: Double = 0.0,
    val source: String = "manual"
)

data class FoodParseQuery(
    val query: String
)

data class FoodEstimateDto(
    val food_name: String,
    val portion_desc: String,
    val calories: Double,
    val protein_g: Double,
    val carbs_g: Double,
    val fat_g: Double,
    val fiber_g: Double,
    val explanation: String
)

data class RecommendedActionDto(
    val task_id: Long?,
    val title: String,
    val duration_minutes: Int,
    val category: String,
    val action_type: String,
    val reason: String
)

data class AvailabilityResponseDto(
    val current_time: String,
    val date: String,
    val available_focused_minutes: Int,
    val required_task_minutes: Int,
    val difference_minutes: Int,
    val is_feasible: Boolean,
    val conflicts: List<String>,
    val suggested_adjustments: List<String>,
    val recommended_next_action: RecommendedActionDto?
)

data class DailyOverviewDto(
    val date: String,
    val day_name: String,
    val overall_progress_pct: Double,
    val steps_current: Int,
    val steps_target: Int,
    val calories_current: Double,
    val calories_target: Double,
    val focus_minutes_current: Int,
    val focus_minutes_target: Int,
    val tasks_total: Int,
    val tasks_completed: Int,
    val availability: AvailabilityResponseDto,
    val recommended_next: RecommendedActionDto?,
    val morning_insight: String?,
    val evening_checkin: String?
)

data class NaturalLanguageCommandDto(
    val text: String
)

data class CommandResponseDto(
    val action_type: String,
    val message: String
)
