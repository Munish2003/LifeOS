from typing import List, Optional
from pydantic import BaseModel, Field


class RoutineBlockBase(BaseModel):
    name: str  # "Office", "Commute", "Meals", "Bath", "Pooja", "Outing"
    category: str = "routine"  # "work", "commute", "routine", "spiritual", "meal", "outing"
    start_time: str = Field(..., pattern=r"^\d{2}:\d{2}$")  # "09:30"
    end_time: str = Field(..., pattern=r"^\d{2}:\d{2}$")    # "18:00"
    duration_minutes: int
    is_flexible: bool = False
    priority: int = 1


class RoutineBlockCreate(RoutineBlockBase):
    pass


class RoutineBlockOut(RoutineBlockBase):
    id: int
    routine_id: int

    class Config:
        from_attributes = True


class RoutineCreate(BaseModel):
    name: str
    day_type: str  # "weekday", "weekend", "custom"
    specific_date: Optional[str] = None
    blocks: List[RoutineBlockCreate] = []


class RoutineOut(BaseModel):
    id: int
    name: str
    day_type: str
    specific_date: Optional[str] = None
    is_active: bool
    blocks: List[RoutineBlockOut] = []

    class Config:
        from_attributes = True


class CalendarEventCreate(BaseModel):
    external_event_id: Optional[str] = None
    title: str
    start_datetime: str
    end_datetime: str
    duration_minutes: int
    is_all_day: bool = False


class CalendarEventOut(CalendarEventCreate):
    id: int

    class Config:
        from_attributes = True


class OutingPlanRequest(BaseModel):
    title: str = "Outing"
    date: str  # YYYY-MM-DD
    start_time: str  # HH:MM
    duration_hours: float
    includes_travel: bool = True
