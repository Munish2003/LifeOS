from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    profile = relationship("Profile", back_populates="user", uselist=False, cascade="all, delete-orphan")
    routines = relationship("Routine", back_populates="user", cascade="all, delete-orphan")
    tasks = relationship("Task", back_populates="user", cascade="all, delete-orphan")
    calendar_events = relationship("CalendarEvent", back_populates="user", cascade="all, delete-orphan")
    goals = relationship("Goal", back_populates="user", cascade="all, delete-orphan")
    challenges = relationship("Challenge", back_populates="user", cascade="all, delete-orphan")
    step_records = relationship("StepRecord", back_populates="user", cascade="all, delete-orphan")
    food_entries = relationship("FoodEntry", back_populates="user", cascade="all, delete-orphan")
    weight_entries = relationship("WeightEntry", back_populates="user", cascade="all, delete-orphan")
    daily_summaries = relationship("DailySummary", back_populates="user", cascade="all, delete-orphan")


class Profile(Base):
    __tablename__ = "profiles"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False, unique=True)
    full_name = Column(String, nullable=True)
    daily_step_target = Column(Integer, default=10000)
    daily_calorie_target = Column(Integer, default=1200)
    target_weight = Column(Float, nullable=True)
    current_weight = Column(Float, nullable=True)
    sleep_target_hours = Column(Float, default=7.5)
    default_wake_time = Column(String, default="07:00")
    default_sleep_time = Column(String, default="23:30")
    timezone = Column(String, default="Asia/Kolkata")

    user = relationship("User", back_populates="profile")
