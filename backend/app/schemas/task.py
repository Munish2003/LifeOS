from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel


class TaskBase(BaseModel):
    title: str
    description: Optional[str] = None
    category: str = "learning"  # "health", "learning", "work", "personal", "goals"
    measurement_type: str = "time_based"  # "time_based", "quantity_based", "distance_based", "step_based", "numeric", "binary"
    target_value: float = 0.0
    current_value: float = 0.0
    unit: str = "minutes"
    priority: int = 3
    importance_weight: float = 1.0
    deadline: Optional[str] = None
    scheduled_date: str
    recurrence: str = "none"
    is_flexible: bool = True
    is_completed: bool = False


class TaskCreate(TaskBase):
    pass


class TaskUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    category: Optional[str] = None
    target_value: Optional[float] = None
    current_value: Optional[float] = None
    priority: Optional[int] = None
    deadline: Optional[str] = None
    scheduled_date: Optional[str] = None
    is_flexible: Optional[bool] = None
    is_completed: Optional[bool] = None


class SessionCreate(BaseModel):
    task_id: int
    duration_seconds: int
    notes: Optional[str] = None


class SessionOut(BaseModel):
    id: int
    task_id: int
    session_number: int
    start_time: datetime
    end_time: Optional[datetime]
    duration_seconds: int
    is_completed: bool
    notes: Optional[str]

    class Config:
        from_attributes = True


class TaskOut(TaskBase):
    id: int
    created_at: Optional[datetime] = None
    sessions: List[SessionOut] = []

    class Config:
        from_attributes = True
