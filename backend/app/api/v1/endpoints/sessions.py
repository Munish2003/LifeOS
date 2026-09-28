from datetime import datetime, timezone
from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.task import Task, TaskSession
from app.schemas.task import SessionCreate, SessionOut

router = APIRouter()


@router.post("/", response_model=SessionOut)
def record_session(
    session_in: SessionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    task = db.query(Task).filter(Task.id == session_in.task_id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")

    session_count = db.query(TaskSession).filter(TaskSession.task_id == task.id).count()

    now = datetime.now(timezone.utc)
    new_session = TaskSession(
        task_id=task.id,
        session_number=session_count + 1,
        start_time=now,
        end_time=now,
        duration_seconds=session_in.duration_seconds,
        is_completed=True,
        notes=session_in.notes
    )
    db.add(new_session)

    # Increment task current_value if time_based
    if task.measurement_type == "time_based":
        added_minutes = session_in.duration_seconds / 60.0
        task.current_value += added_minutes
        if task.current_value >= task.target_value:
            task.is_completed = True

    db.commit()
    db.refresh(new_session)
    return new_session


@router.get("/task/{task_id}", response_model=List[SessionOut])
def get_task_sessions(
    task_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    task = db.query(Task).filter(Task.id == task_id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")

    return db.query(TaskSession).filter(TaskSession.task_id == task_id).order_by(TaskSession.start_time.desc()).all()
