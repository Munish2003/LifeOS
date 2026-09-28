from typing import List, Optional
from datetime import date
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.health import StepRecord, WeightEntry
from app.schemas.health import StepSyncRequest, StepRecordOut, WeightCreate, WeightOut

router = APIRouter()


@router.post("/steps/sync", response_model=StepRecordOut)
def sync_steps(
    step_in: StepSyncRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    record = db.query(StepRecord).filter(
        StepRecord.user_id == current_user.id,
        StepRecord.date == step_in.date
    ).first()

    if not record:
        record = StepRecord(
            user_id=current_user.id,
            date=step_in.date,
            steps=step_in.steps,
            target=step_in.target,
            distance_meters=step_in.distance_meters or (step_in.steps * 0.75),
            calories_burned=step_in.calories_burned or (step_in.steps * 0.04),
            active_minutes=step_in.active_minutes or int(step_in.steps / 100)
        )
        db.add(record)
    else:
        record.steps = step_in.steps
        record.target = step_in.target
        record.distance_meters = step_in.distance_meters or record.distance_meters
        record.calories_burned = step_in.calories_burned or record.calories_burned
        record.active_minutes = step_in.active_minutes or record.active_minutes

    db.commit()
    db.refresh(record)

    pct = round((record.steps / record.target * 100), 1) if record.target > 0 else 100.0
    return StepRecordOut(
        id=record.id,
        date=record.date,
        steps=record.steps,
        target=record.target,
        distance_meters=record.distance_meters,
        calories_burned=record.calories_burned,
        active_minutes=record.active_minutes,
        completion_percentage=pct
    )


@router.get("/steps/today", response_model=StepRecordOut)
def get_today_steps(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    today_str = date.today().isoformat()
    record = db.query(StepRecord).filter(
        StepRecord.user_id == current_user.id,
        StepRecord.date == today_str
    ).first()

    if not record:
        record = StepRecord(
            user_id=current_user.id,
            date=today_str,
            steps=7842,  # Initial demo step count from specification
            target=10000,
            distance_meters=5881.5,
            calories_burned=313.6,
            active_minutes=78
        )
        db.add(record)
        db.commit()
        db.refresh(record)

    pct = round((record.steps / record.target * 100), 1) if record.target > 0 else 100.0
    return StepRecordOut(
        id=record.id,
        date=record.date,
        steps=record.steps,
        target=record.target,
        distance_meters=record.distance_meters,
        calories_burned=record.calories_burned,
        active_minutes=record.active_minutes,
        completion_percentage=pct
    )


@router.post("/weight", response_model=WeightOut)
def log_weight(
    weight_in: WeightCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    entry = WeightEntry(
        user_id=current_user.id,
        date=weight_in.date,
        weight_kg=weight_in.weight_kg,
        notes=weight_in.notes
    )
    db.add(entry)
    db.commit()
    db.refresh(entry)
    return entry


@router.get("/weight/history", response_model=List[WeightOut])
def get_weight_history(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return db.query(WeightEntry).filter(WeightEntry.user_id == current_user.id).order_by(WeightEntry.date.desc()).all()
