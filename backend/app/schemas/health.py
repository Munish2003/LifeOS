from typing import Optional
from datetime import datetime
from pydantic import BaseModel


class StepSyncRequest(BaseModel):
    date: str  # "YYYY-MM-DD"
    steps: int
    target: int = 10000
    distance_meters: Optional[float] = 0.0
    calories_burned: Optional[float] = 0.0
    active_minutes: Optional[int] = 0


class StepRecordOut(BaseModel):
    id: int
    date: str
    steps: int
    target: int
    distance_meters: float
    calories_burned: float
    active_minutes: int
    completion_percentage: float

    class Config:
        from_attributes = True


class WeightCreate(BaseModel):
    date: str  # "YYYY-MM-DD"
    weight_kg: float
    notes: Optional[str] = None


class WeightOut(BaseModel):
    id: int
    date: str
    weight_kg: float
    notes: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
