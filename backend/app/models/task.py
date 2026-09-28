from sqlalchemy import Column, Integer, String, Boolean, Float, ForeignKey, DateTime
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class Task(Base):
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    title = Column(String, nullable=False)
    description = Column(String, nullable=True)

    # Categories: "health", "learning", "work", "personal", "goals", "custom"
    category = Column(String, default="learning", nullable=False)

    # Measurement Types: "time_based", "quantity_based", "distance_based", "step_based", "numeric", "binary"
    measurement_type = Column(String, default="time_based", nullable=False)

    # Targets & Progress
    # For time_based: target_value in minutes (e.g. 180 for 3 hours)
    # For quantity_based: target_value in count (e.g. 20 for 20 questions)
    # For binary: target_value = 1
    target_value = Column(Float, default=0.0, nullable=False)
    current_value = Column(Float, default=0.0, nullable=False)
    unit = Column(String, default="minutes")  # "minutes", "questions", "km", "steps", "litres", "boolean"

    # Scheduling & Priority
    priority = Column(Integer, default=3)  # 1 (Highest) to 5 (Lowest)
    importance_weight = Column(Float, default=1.0)
    deadline = Column(String, nullable=True)  # ISO string or date
    scheduled_date = Column(String, nullable=False)  # "YYYY-MM-DD"

    # Recurrence: "none", "daily", "weekdays", "weekends", "custom"
    recurrence = Column(String, default="none")
    is_flexible = Column(Boolean, default=True)  # If tight schedule, flexible tasks can be shifted
    is_completed = Column(Boolean, default=False)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    user = relationship("User", back_populates="tasks")
    sessions = relationship("TaskSession", back_populates="task", cascade="all, delete-orphan")


class TaskSession(Base):
    """
    Time tracking sessions for time-based tasks (supports pause/resume, multi-session tracking).
    """
    __tablename__ = "task_sessions"

    id = Column(Integer, primary_key=True, index=True)
    task_id = Column(Integer, ForeignKey("tasks.id"), nullable=False)
    session_number = Column(Integer, default=1)
    start_time = Column(DateTime(timezone=True), nullable=False)
    end_time = Column(DateTime(timezone=True), nullable=True)
    duration_seconds = Column(Integer, default=0)
    is_completed = Column(Boolean, default=False)
    notes = Column(String, nullable=True)

    task = relationship("Task", back_populates="sessions")
