from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime, Date
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class StepRecord(Base):
    """
    Step records synced from Health Connect or native step detector.
    """
    __tablename__ = "step_records"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    date = Column(String, nullable=False, index=True)  # "YYYY-MM-DD"
    steps = Column(Integer, default=0, nullable=False)
    target = Column(Integer, default=10000, nullable=False)
    distance_meters = Column(Float, default=0.0)
    calories_burned = Column(Float, default=0.0)
    active_minutes = Column(Integer, default=0)
    synced_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="step_records")


class WeightEntry(Base):
    """
    Weight tracking entries.
    """
    __tablename__ = "weight_entries"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    date = Column(String, nullable=False)  # "YYYY-MM-DD"
    weight_kg = Column(Float, nullable=False)
    notes = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="weight_entries")
