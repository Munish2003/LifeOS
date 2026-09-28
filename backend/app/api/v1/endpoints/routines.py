from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.routine import Routine, RoutineBlock, CalendarEvent
from app.schemas.routine import (
    RoutineCreate, RoutineOut, RoutineBlockCreate, RoutineBlockOut,
    CalendarEventCreate, CalendarEventOut, OutingPlanRequest
)

router = APIRouter()


@router.get("/", response_model=List[RoutineOut])
def get_routines(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    routines = db.query(Routine).filter(Routine.user_id == current_user.id).all()
    # If user has no routine yet, seed standard default routine from spec
    if not routines:
        default_routine = Routine(
            user_id=current_user.id,
            name="Regular Weekday",
            day_type="weekday",
            is_active=True
        )
        db.add(default_routine)
        db.commit()
        db.refresh(default_routine)

        # Seed default routine blocks from newappneed.md
        blocks = [
            RoutineBlock(routine_id=default_routine.id, name="Getting Ready", category="routine", start_time="07:30", end_time="08:15", duration_minutes=45, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Bath & Morning Pooja", category="spiritual", start_time="08:15", end_time="08:35", duration_minutes=20, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Breakfast", category="meal", start_time="08:35", end_time="09:00", duration_minutes=25, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Morning Commute", category="commute", start_time="09:00", end_time="09:30", duration_minutes=30, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Office Work", category="work", start_time="09:30", end_time="18:00", duration_minutes=510, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Evening Commute", category="commute", start_time="18:00", end_time="18:45", duration_minutes=45, is_flexible=False),
            RoutineBlock(routine_id=default_routine.id, name="Dinner", category="meal", start_time="19:30", end_time="20:15", duration_minutes=45, is_flexible=True),
            RoutineBlock(routine_id=default_routine.id, name="Evening Pooja", category="spiritual", start_time="20:15", end_time="20:35", duration_minutes=20, is_flexible=False),
        ]
        db.add_all(blocks)
        db.commit()
        routines = [default_routine]

    return routines


@router.post("/", response_model=RoutineOut)
def create_routine(routine_in: RoutineCreate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    routine = Routine(
        user_id=current_user.id,
        name=routine_in.name,
        day_type=routine_in.day_type,
        is_active=True
    )
    db.add(routine)
    db.commit()
    db.refresh(routine)

    for b in routine_in.blocks:
        block = RoutineBlock(
            routine_id=routine.id,
            name=b.name,
            category=b.category,
            start_time=b.start_time,
            end_time=b.end_time,
            duration_minutes=b.duration_minutes,
            is_flexible=b.is_flexible,
            priority=b.priority
        )
        db.add(block)
    db.commit()
    db.refresh(routine)
    return routine


@router.post("/outing", response_model=RoutineBlockOut)
def schedule_outing(
    outing: OutingPlanRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Creates an outing block (Section 11) and updates availability immediately.
    """
    routine = db.query(Routine).filter(Routine.user_id == current_user.id).first()
    if not routine:
        routine = Routine(user_id=current_user.id, name="Active Schedule", day_type="weekday")
        db.add(routine)
        db.commit()
        db.refresh(routine)

    # Calculate end time
    start_h, start_m = map(int, outing.start_time.split(":"))
    total_mins = int(outing.duration_hours * 60)
    end_mins = (start_h * 60 + start_m + total_mins) % (24 * 60)
    end_time_str = f"{end_mins // 60:02d}:{end_mins % 60:02d}"

    block = RoutineBlock(
        routine_id=routine.id,
        name=outing.title,
        category="outing",
        start_time=outing.start_time,
        end_time=end_time_str,
        duration_minutes=total_mins,
        is_flexible=False,
        priority=1
    )
    db.add(block)
    db.commit()
    db.refresh(block)
    return block


@router.get("/calendar-events", response_model=List[CalendarEventOut])
def get_calendar_events(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return db.query(CalendarEvent).filter(CalendarEvent.user_id == current_user.id).all()


@router.post("/calendar-events", response_model=CalendarEventOut)
def sync_calendar_event(
    event_in: CalendarEventCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    event = CalendarEvent(
        user_id=current_user.id,
        external_event_id=event_in.external_event_id,
        title=event_in.title,
        start_datetime=event_in.start_datetime,
        end_datetime=event_in.end_datetime,
        duration_minutes=event_in.duration_minutes,
        is_all_day=event_in.is_all_day
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event
