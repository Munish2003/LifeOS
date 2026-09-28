from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.engine.analytics_engine import AnalyticsEngine

router = APIRouter()


@router.get("/weekly")
def get_weekly_analytics(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Returns weekly aggregated statistics matching Section 28:
    - Average steps (e.g. 9,774/day)
    - Total steps (e.g. 68,420)
    - Learning hours (e.g. 18h 42m)
    - Task completion rate (e.g. 84%)
    - Breakdown by subject (Python 8h20m, DSA 4h10m)
    """
    # Sample structured 7-day performance data
    demo_daily_records = [
        {"date": "2026-09-22", "steps": 10240, "focus_time_minutes": 190, "calories_consumed": 1180, "tasks_completed": 4, "tasks_total": 5},
        {"date": "2026-09-23", "steps": 9850, "focus_time_minutes": 180, "calories_consumed": 1210, "tasks_completed": 3, "tasks_total": 4},
        {"date": "2026-09-24", "steps": 11200, "focus_time_minutes": 210, "calories_consumed": 1150, "tasks_completed": 5, "tasks_total": 5},
        {"date": "2026-09-25", "steps": 8900, "focus_time_minutes": 150, "calories_consumed": 1290, "tasks_completed": 4, "tasks_total": 4},
        {"date": "2026-09-26", "steps": 10500, "focus_time_minutes": 240, "calories_consumed": 1120, "tasks_completed": 4, "tasks_total": 4},
        {"date": "2026-09-27", "steps": 9888, "focus_time_minutes": 120, "calories_consumed": 1300, "tasks_completed": 3, "tasks_total": 4},
        {"date": "2026-09-28", "steps": 7842, "focus_time_minutes": 137, "calories_consumed": 860, "tasks_completed": 2, "tasks_total": 4},
    ]

    summary = AnalyticsEngine.calculate_weekly_summary(demo_daily_records)
    summary["category_breakdown"] = {
        "Python": "8h 20m",
        "DSA": "4h 10m",
        "General Learning": "6h 12m"
    }
    return summary


@router.get("/monthly")
def get_monthly_analytics(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Returns monthly progress matching Section 29:
    - Monthly progress & goal trends
    - Habit consistency
    - Weight trends (79.5 kg -> target 75 kg)
    """
    return {
        "month": "September 2026",
        "total_steps": 284500,
        "average_steps_daily": 10160,
        "total_focus_hours": 76.5,
        "task_completion_rate": 86.4,
        "consistency_streak_days": 18,
        "weight_progress": {
            "starting_weight": 81.2,
            "current_weight": 79.5,
            "target_weight": 75.0,
            "change_kg": -1.7
        },
        "challenges_completed": 2
    }
