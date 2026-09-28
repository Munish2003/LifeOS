from typing import List, Dict, Any


class AnalyticsEngine:
    """
    Deterministic aggregation engine for Daily, Weekly, and Monthly metrics.
    No LLM hallucinations: pure math & statistics.
    """

    @staticmethod
    def calculate_daily_metrics(
        tasks: List[Dict[str, Any]],
        step_record: Dict[str, Any],
        food_entries: List[Dict[str, Any]],
        step_target: int = 10000,
        calorie_target: float = 1200.0
    ) -> Dict[str, Any]:
        tasks_total = len(tasks)
        tasks_completed = sum(1 for t in tasks if t.get("is_completed", False))

        focus_time_minutes = 0
        focus_target_minutes = 0
        for t in tasks:
            if t.get("measurement_type") == "time_based":
                focus_time_minutes += int(t.get("current_value", 0))
                focus_target_minutes += int(t.get("target_value", 0))

        steps = step_record.get("steps", 0) if step_record else 0
        total_calories = sum(float(f.get("calories", 0)) for f in food_entries)
        total_protein = sum(float(f.get("protein_g", 0)) for f in food_entries)
        total_carbs = sum(float(f.get("carbs_g", 0)) for f in food_entries)
        total_fat = sum(float(f.get("fat_g", 0)) for f in food_entries)

        # Progress weights: 40% tasks, 30% focus time, 30% steps
        task_pct = (tasks_completed / tasks_total) if tasks_total > 0 else 1.0
        focus_pct = (focus_time_minutes / focus_target_minutes) if focus_target_minutes > 0 else 1.0
        step_pct = min(1.0, steps / step_target) if step_target > 0 else 1.0

        overall_progress = round((task_pct * 0.4 + min(1.0, focus_pct) * 0.3 + step_pct * 0.3) * 100, 1)

        return {
            "tasks_total": tasks_total,
            "tasks_completed": tasks_completed,
            "tasks_completion_pct": round(task_pct * 100, 1),
            "focus_time_minutes": focus_time_minutes,
            "focus_target_minutes": focus_target_minutes,
            "steps": steps,
            "step_target": step_target,
            "step_completion_pct": round(step_pct * 100, 1),
            "calories_consumed": round(total_calories, 1),
            "calorie_target": calorie_target,
            "protein_g": round(total_protein, 1),
            "carbs_g": round(total_carbs, 1),
            "fat_g": round(total_fat, 1),
            "overall_progress_pct": overall_progress
        }

    @staticmethod
    def calculate_weekly_summary(
        daily_records: List[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """
        Aggregates last 7 days of daily records.
        """
        if not daily_records:
            return {
                "days_tracked": 0,
                "total_steps": 0,
                "average_steps": 0,
                "total_focus_minutes": 0,
                "average_focus_minutes": 0,
                "total_calories": 0,
                "average_calories": 0,
                "task_completion_rate": 0.0,
                "best_step_day": None,
                "best_focus_day": None
            }

        total_steps = sum(d.get("steps", 0) for d in daily_records)
        total_focus = sum(d.get("focus_time_minutes", 0) for d in daily_records)
        total_calories = sum(d.get("calories_consumed", 0.0) for d in daily_records)
        total_tasks_completed = sum(d.get("tasks_completed", 0) for d in daily_records)
        total_tasks = sum(d.get("tasks_total", 0) for d in daily_records)

        num_days = len(daily_records)
        best_step_day = max(daily_records, key=lambda x: x.get("steps", 0))
        best_focus_day = max(daily_records, key=lambda x: x.get("focus_time_minutes", 0))

        completion_rate = round((total_tasks_completed / total_tasks * 100), 1) if total_tasks > 0 else 0.0

        return {
            "days_tracked": num_days,
            "total_steps": total_steps,
            "average_steps": int(total_steps / num_days),
            "total_focus_minutes": total_focus,
            "average_focus_minutes": int(total_focus / num_days),
            "total_calories": round(total_calories, 1),
            "average_calories": round(total_calories / num_days, 1),
            "task_completion_rate": completion_rate,
            "best_step_day": {"date": best_step_day.get("date"), "steps": best_step_day.get("steps", 0)},
            "best_focus_day": {"date": best_focus_day.get("date"), "focus_minutes": best_focus_day.get("focus_time_minutes", 0)}
        }
