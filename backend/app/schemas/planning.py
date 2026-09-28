from typing import List, Optional
from pydantic import BaseModel


class ScheduleBlock(BaseModel):
    title: str
    category: str  # "routine", "task", "work", "commute", "event", "health"
    start_time: str  # "HH:MM"
    end_time: str    # "HH:MM"
    duration_minutes: int
    is_completed: bool = False
    task_id: Optional[int] = None


class RecommendedAction(BaseModel):
    task_id: Optional[int] = None
    title: str
    duration_minutes: int
    category: str
    action_type: str  # "task_timer", "step_walk", "meal", "break"
    reason: str  # e.g. "You have a free 90-minute block, Python is due today, and you still need 1h 10m."


class AvailabilityResponse(BaseModel):
    current_time: str
    date: str
    day_type: str  # "weekday", "weekend", "custom"
    wake_time: str
    sleep_time: str

    total_day_minutes: int
    routine_committed_minutes: int
    calendar_event_minutes: int
    available_focused_minutes: int
    required_task_minutes: int
    difference_minutes: int  # Positive: surplus, Negative: shortage
    is_feasible: bool

    conflicts: List[str] = []
    suggested_adjustments: List[str] = []
    recommended_next_action: Optional[RecommendedAction] = None
    timeline: List[ScheduleBlock] = []


class DailyOverview(BaseModel):
    date: str
    day_name: str
    overall_progress_pct: float

    # Core health & focus stats
    steps_current: int
    steps_target: int
    calories_current: float
    calories_target: float
    focus_minutes_current: int
    focus_minutes_target: int

    tasks_total: int
    tasks_completed: int

    availability: AvailabilityResponse
    recommended_next: Optional[RecommendedAction] = None
    morning_insight: Optional[str] = None
    evening_checkin: Optional[str] = None


class NaturalLanguageCommandRequest(BaseModel):
    text: str  # e.g. "I studied Python for 1 hour", "Tomorrow I have an outing for 4 hours"


class NaturalLanguageCommandResponse(BaseModel):
    action_type: str  # "session_logged", "food_logged", "outing_created", "task_created", "plan_modified"
    details: dict
    message: str
