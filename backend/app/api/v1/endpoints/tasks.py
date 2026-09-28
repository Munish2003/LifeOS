from typing import List, Optional
from datetime import date
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.task import Task
from app.schemas.task import TaskCreate, TaskUpdate, TaskOut

router = APIRouter()


@router.get("/", response_model=List[TaskOut])
def list_tasks(
    scheduled_date: Optional[str] = Query(None),
    category: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = db.query(Task).filter(Task.user_id == current_user.id)
    if scheduled_date:
        query = query.filter(Task.scheduled_date == scheduled_date)
    if category:
        query = query.filter(Task.category == category)
    tasks = query.all()

    # Seed initial tasks if empty for today
    if not tasks and not scheduled_date:
        today_str = date.today().isoformat()
        seeds = [
            Task(user_id=current_user.id, title="Python Deep Work", category="learning", measurement_type="time_based", target_value=180.0, current_value=0.0, unit="minutes", priority=1, scheduled_date=today_str, is_flexible=False),
            Task(user_id=current_user.id, title="DSA Practice", category="learning", measurement_type="quantity_based", target_value=20.0, current_value=0.0, unit="questions", priority=2, scheduled_date=today_str, is_flexible=True),
            Task(user_id=current_user.id, title="Daily 10k Steps", category="health", measurement_type="step_based", target_value=10000.0, current_value=0.0, unit="steps", priority=1, scheduled_date=today_str, is_flexible=False),
            Task(user_id=current_user.id, title="Evening Pooja", category="personal", measurement_type="binary", target_value=1.0, current_value=0.0, unit="boolean", priority=1, scheduled_date=today_str, is_flexible=False),
        ]
        db.add_all(seeds)
        db.commit()
        tasks = seeds

    return tasks


@router.post("/", response_model=TaskOut)
def create_task(
    task_in: TaskCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    task = Task(
        user_id=current_user.id,
        title=task_in.title,
        description=task_in.description,
        category=task_in.category,
        measurement_type=task_in.measurement_type,
        target_value=task_in.target_value,
        current_value=task_in.current_value,
        unit=task_in.unit,
        priority=task_in.priority,
        importance_weight=task_in.importance_weight,
        deadline=task_in.deadline,
        scheduled_date=task_in.scheduled_date,
        recurrence=task_in.recurrence,
        is_flexible=task_in.is_flexible,
        is_completed=task_in.is_completed
    )
    db.add(task)
    db.commit()
    db.refresh(task)
    return task


@router.get("/{task_id}", response_model=TaskOut)
def get_task(task_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    task = db.query(Task).filter(Task.id == task_id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task


@router.patch("/{task_id}", response_model=TaskOut)
def update_task(
    task_id: int,
    task_update: TaskUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    task = db.query(Task).filter(Task.id == task_id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")

    for field, val in task_update.model_dump(exclude_unset=True).items():
        setattr(task, field, val)

    # Automatically check if completion threshold reached
    if task.current_value >= task.target_value and task.target_value > 0:
        task.is_completed = True

    db.commit()
    db.refresh(task)
    return task


@router.delete("/{task_id}")
def delete_task(task_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    task = db.query(Task).filter(Task.id == task_id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    db.delete(task)
    db.commit()
    return {"message": "Task deleted successfully"}
