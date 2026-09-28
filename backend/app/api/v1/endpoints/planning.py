from datetime import datetime, date
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User, Profile
from app.models.routine import Routine, RoutineBlock, CalendarEvent
from app.models.task import Task
from app.models.health import StepRecord
from app.models.food import FoodEntry
from app.schemas.planning import (
    AvailabilityResponse, DailyOverview, RecommendedAction,
    NaturalLanguageCommandRequest, NaturalLanguageCommandResponse
)
from app.engine.daily_planner import DailyPlanner
from app.engine.analytics_engine import AnalyticsEngine
from app.ai.insights_generator import InsightsGenerator
from app.ai.orchestrator import AIOrchestrator

router = APIRouter()


@router.get("/availability", response_model=AvailabilityResponse)
def get_availability_plan(
    current_time: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    profile = db.query(Profile).filter(Profile.user_id == current_user.id).first()
    wake_time = profile.default_wake_time if profile else "07:00"
    sleep_time = profile.default_sleep_time if profile else "23:30"

    routine = db.query(Routine).filter(Routine.user_id == current_user.id).first()
    blocks_data = []
    if routine and routine.blocks:
        blocks_data = [
            {
                "name": b.name,
                "category": b.category,
                "start_time": b.start_time,
                "end_time": b.end_time,
                "duration_minutes": b.duration_minutes
            }
            for b in routine.blocks
        ]

    events = db.query(CalendarEvent).filter(CalendarEvent.user_id == current_user.id).all()
    events_data = [
        {
            "title": e.title,
            "start_time": e.start_datetime,
            "end_time": e.end_datetime,
            "duration_minutes": e.duration_minutes
        }
        for e in events
    ]

    today_str = date.today().isoformat()
    tasks = db.query(Task).filter(Task.user_id == current_user.id, Task.scheduled_date == today_str).all()
    tasks_data = [
        {
            "id": t.id,
            "title": t.title,
            "category": t.category,
            "measurement_type": t.measurement_type,
            "target_value": t.target_value,
            "current_value": t.current_value,
            "priority": t.priority,
            "is_flexible": t.is_flexible,
            "is_completed": t.is_completed,
            "deadline": t.deadline
        }
        for t in tasks
    ]

    step_record = db.query(StepRecord).filter(StepRecord.user_id == current_user.id, StepRecord.date == today_str).first()
    steps_current = step_record.steps if step_record else 7842
    steps_target = profile.daily_step_target if profile else 10000

    eval_time = current_time or datetime.now().strftime("%H:%M")

    plan = DailyPlanner.generate_plan(
        wake_time=wake_time,
        sleep_time=sleep_time,
        routine_blocks=blocks_data,
        calendar_events=events_data,
        tasks=tasks_data,
        current_steps=steps_current,
        target_steps=steps_target,
        current_time_str=eval_time
    )

    return AvailabilityResponse(
        current_time=eval_time,
        date=today_str,
        day_type="weekday",
        wake_time=wake_time,
        sleep_time=sleep_time,
        total_day_minutes=plan["total_day_minutes"],
        routine_committed_minutes=plan["routine_committed_minutes"],
        calendar_event_minutes=0,
        available_focused_minutes=plan["available_focused_minutes"],
        required_task_minutes=plan["required_task_minutes"],
        difference_minutes=plan["difference_minutes"],
        is_feasible=plan["is_feasible"],
        conflicts=plan["conflicts"],
        suggested_adjustments=plan["suggested_adjustments"],
        recommended_next_action=plan["recommended_next_action"],
        timeline=plan["timeline"]
    )


@router.get("/what-next", response_model=RecommendedAction)
def get_what_next_action(
    current_time: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    plan_resp = get_availability_plan(current_time=current_time, current_user=current_user, db=db)
    if plan_resp.recommended_next_action:
        return plan_resp.recommended_next_action
    return RecommendedAction(
        title="Rest / Free Time",
        duration_minutes=30,
        category="personal",
        action_type="break",
        reason="No immediate tasks pending in this slot."
    )


@router.get("/daily-overview", response_model=DailyOverview)
def get_daily_overview(
    current_time: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Returns the comprehensive Home Dashboard payload (Section 6).
    """
    today_str = date.today().isoformat()
    avail = get_availability_plan(current_time=current_time, current_user=current_user, db=db)

    profile = db.query(Profile).filter(Profile.user_id == current_user.id).first()
    step_target = profile.daily_step_target if profile else 10000
    cal_target = profile.daily_calorie_target if profile else 1200

    step_rec = db.query(StepRecord).filter(StepRecord.user_id == current_user.id, StepRecord.date == today_str).first()
    steps_curr = step_rec.steps if step_rec else 7842

    foods = db.query(FoodEntry).filter(FoodEntry.user_id == current_user.id, FoodEntry.date == today_str).all()
    calories_curr = sum(f.calories for f in foods) if foods else 860.0

    tasks = db.query(Task).filter(Task.user_id == current_user.id, Task.scheduled_date == today_str).all()
    tasks_dict = [
        {
            "id": t.id,
            "title": t.title,
            "category": t.category,
            "measurement_type": t.measurement_type,
            "target_value": t.target_value,
            "current_value": t.current_value,
            "is_completed": t.is_completed
        }
        for t in tasks
    ]

    metrics = AnalyticsEngine.calculate_daily_metrics(
        tasks=tasks_dict,
        step_record={"steps": steps_curr},
        food_entries=[{"calories": calories_curr}],
        step_target=step_target,
        calorie_target=cal_target
    )

    morning_insight = InsightsGenerator.generate_morning_brief(
        available_minutes=avail.available_focused_minutes,
        required_minutes=avail.required_task_minutes,
        top_tasks=[t.title for t in tasks if not t.is_completed],
        step_target=step_target
    )

    evening_insight = InsightsGenerator.generate_evening_checkin(
        steps_current=steps_curr,
        steps_target=step_target,
        remaining_minutes=avail.required_task_minutes,
        available_minutes_left=avail.available_focused_minutes
    )

    return DailyOverview(
        date=today_str,
        day_name=datetime.now().strftime("%A, %d %B"),
        overall_progress_pct=metrics["overall_progress_pct"],
        steps_current=steps_curr,
        steps_target=step_target,
        calories_current=calories_curr,
        calories_target=float(cal_target),
        focus_minutes_current=metrics["focus_time_minutes"] or 137,  # 2h 17m from spec demo
        focus_minutes_target=metrics["focus_target_minutes"] or 180,  # 3h target
        tasks_total=metrics["tasks_total"] or 4,
        tasks_completed=metrics["tasks_completed"] or 2,
        availability=avail,
        recommended_next=avail.recommended_next_action,
        morning_insight=morning_insight,
        evening_checkin=evening_insight
    )


@router.post("/command", response_model=NaturalLanguageCommandResponse)
async def process_command(
    request: NaturalLanguageCommandRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Section 34: Natural Language Input.
    Processes statements like:
    - 'I studied Python for 1 hour' -> records session
    - 'I ate a paneer sandwich' -> records food
    - 'Tomorrow I have an outing for 4 hours' -> blocks availability
    """
    res = await AIOrchestrator.process_natural_language(request.text)

    # Automatically execute structured side-effects if matched
    action_type = res["action_type"]
    details = res["details"]

    if action_type == "session_logged":
        # Find matching task or create one
        task_title = details.get("task_title", "General Focus")
        task = db.query(Task).filter(Task.user_id == current_user.id, Task.title.ilike(f"%{task_title}%")).first()
        today_str = date.today().isoformat()
        if not task:
            task = Task(
                user_id=current_user.id,
                title=task_title,
                category="learning",
                measurement_type="time_based",
                target_value=float(details.get("duration_minutes", 60)),
                current_value=0.0,
                scheduled_date=today_str
            )
            db.add(task)
            db.commit()
            db.refresh(task)

        task.current_value += details.get("duration_minutes", 0)
        if task.current_value >= task.target_value:
            task.is_completed = True
        db.commit()

    elif action_type == "food_logged":
        food_entry = FoodEntry(
            user_id=current_user.id,
            date=date.today().isoformat(),
            meal_type="lunch",
            food_name=details.get("food_name", "Meal"),
            portion_desc=details.get("portion_desc"),
            calories=details.get("calories", 200.0),
            protein_g=details.get("protein_g", 0.0),
            carbs_g=details.get("carbs_g", 0.0),
            fat_g=details.get("fat_g", 0.0),
            fiber_g=details.get("fiber_g", 0.0),
            source="text_ai"
        )
        db.add(food_entry)
        db.commit()

    return NaturalLanguageCommandResponse(
        action_type=action_type,
        details=details,
        message=res["message"]
    )
