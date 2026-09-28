from typing import Dict, Any, List


class InsightsGenerator:
    """
    Generates structured morning briefing, evening check-in, night reviews,
    and smart personalized 15-day challenges.
    """

    @classmethod
    def generate_morning_brief(
        cls,
        available_minutes: int,
        required_minutes: int,
        top_tasks: List[str],
        step_target: int = 10000
    ) -> str:
        avail_h, avail_m = available_minutes // 60, available_minutes % 60
        req_h, req_m = required_minutes // 60, required_minutes % 60

        diff = available_minutes - required_minutes
        if diff >= 0:
            status = f"You have {avail_h}h {avail_m}m available for {req_h}h {req_m}m of planned work. Schedule is optimal."
        else:
            shortage = abs(diff)
            status = f"Schedule shortage of {shortage} minutes. Consider moving non-critical tasks to tomorrow."

        tasks_summary = ", ".join(top_tasks[:3]) if top_tasks else "daily routines"
        return (
            f"Good morning! Today's goal includes {step_target:,} steps and focus on {tasks_summary}. "
            f"{status}"
        )

    @classmethod
    def generate_evening_checkin(
        cls,
        steps_current: int,
        steps_target: int,
        remaining_minutes: int,
        available_minutes_left: int
    ) -> str:
        steps_rem = max(0, steps_target - steps_current)
        steps_text = (
            f"You have {steps_rem:,} steps remaining."
            if steps_rem > 0 else "Step target completed! 🎉"
        )

        if remaining_minutes <= available_minutes_left:
            feasibility = f"Remaining {remaining_minutes}m work fits smoothly into your {available_minutes_left}m available evening."
        else:
            shortage = remaining_minutes - available_minutes_left
            feasibility = f"Tight timeline! You have {shortage}m more planned work than available time tonight. Consider resting early."

        return f"Evening check-in: {steps_text} {feasibility}"

    @classmethod
    def generate_night_review(
        cls,
        completion_pct: float,
        focus_minutes: int,
        steps: int
    ) -> str:
        h, m = focus_minutes // 60, focus_minutes % 60
        return (
            f"Daily Review: Overall score {completion_pct}%! "
            f"Focused deep work: {h}h {m}m, Total steps: {steps:,}. "
            "Great consistency. Rest well for tomorrow!"
        )

    @classmethod
    def suggest_smart_challenges(
        cls,
        recent_average_steps: int,
        recent_average_focus_minutes: int
    ) -> List[Dict[str, Any]]:
        return [
            {
                "title": "15-Day Step Master Challenge",
                "description": f"Hit at least 10,000 steps daily for 15 consecutive days.",
                "duration_days": 15,
                "target_metric": "steps_daily",
                "target_value_per_day": 10000,
                "reward_badge": "👟 Consistency Master"
            },
            {
                "title": "30-Hour Python Sprint",
                "description": "Log 30 hours of focused Python learning over the next 15 days (~2h/day).",
                "duration_days": 15,
                "target_metric": "hours_total",
                "total_target_value": 30,
                "reward_badge": "🐍 Python Pro"
            },
            {
                "title": "DSA Streak: 50 Questions",
                "description": "Solve 50 Data Structure & Algorithm problems across 15 days.",
                "duration_days": 15,
                "target_metric": "dsa_questions",
                "total_target_value": 50,
                "reward_badge": "🧠 Algorithm Ace"
            }
        ]
