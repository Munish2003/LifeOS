from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, Time, Date
from sqlalchemy.orm import relationship
from app.core.database import Base


class Routine(Base):
    """
    Routines define weekly/day-type templates (e.g. Weekday, Weekend, Holiday, Custom Date).
    """
    __tablename__ = "routines"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    name = Column(String, nullable=False)  # e.g., "Regular Weekday", "Weekend", "WFH Day"
    day_type = Column(String, nullable=False)  # "weekday", "weekend", "custom"
    specific_date = Column(Date, nullable=True)  # For specific custom date or outing
    is_active = Column(Boolean, default=True)

    user = relationship("User", back_populates="routines")
    blocks = relationship("RoutineBlock", back_populates="routine", cascade="all, delete-orphan")


class RoutineBlock(Base):
    """
    Routine blocks represent non-negotiable or planned commitments in a day:
    e.g. Office, Commute, Getting Ready, Bath, Meals, Pooja, Sleep preparation, Outing.
    """
    __tablename__ = "routine_blocks"

    id = Column(Integer, primary_key=True, index=True)
    routine_id = Column(Integer, ForeignKey("routines.id"), nullable=False)
    name = Column(String, nullable=False)  # "Office", "Commute", "Pooja", "Meals", "Outing"
    category = Column(String, default="routine")  # "work", "commute", "routine", "spiritual", "meal", "outing"
    start_time = Column(String, nullable=False)  # "HH:MM" e.g. "09:30"
    end_time = Column(String, nullable=False)  # "HH:MM" e.g. "18:00"
    duration_minutes = Column(Integer, nullable=False)  # Calculated or fixed minutes
    is_flexible = Column(Boolean, default=False)
    priority = Column(Integer, default=1)  # 1: strict/mandatory, 5: flexible

    routine = relationship("Routine", back_populates="blocks")


class CalendarEvent(Base):
    """
    Calendar events synced from Android Calendar Provider.
    """
    __tablename__ = "calendar_events"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    external_event_id = Column(String, nullable=True)
    title = Column(String, nullable=False)
    start_datetime = Column(String, nullable=False)  # ISO string: "2026-09-28T14:00:00"
    end_datetime = Column(String, nullable=False)  # ISO string: "2026-09-28T15:30:00"
    duration_minutes = Column(Integer, nullable=False)
    is_all_day = Column(Boolean, default=False)

    user = relationship("User", back_populates="calendar_events")
