from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class FoodEntry(Base):
    """
    Food and calorie tracking entries with macro nutrients.
    """
    __tablename__ = "food_entries"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    date = Column(String, nullable=False, index=True)  # "YYYY-MM-DD"
    time = Column(String, nullable=True)  # "HH:MM"
    meal_type = Column(String, default="lunch")  # "breakfast", "lunch", "dinner", "snack"

    food_name = Column(String, nullable=False)
    portion_desc = Column(String, nullable=True)  # e.g. "2 rotis and 1 bowl paneer"
    quantity = Column(Float, default=1.0)

    # Nutritional breakdown
    calories = Column(Float, nullable=False)
    protein_g = Column(Float, default=0.0)
    carbs_g = Column(Float, default=0.0)
    fat_g = Column(Float, default=0.0)
    fiber_g = Column(Float, default=0.0)

    # Logging origin: "manual", "text_ai", "image_ai", "ocr"
    source = Column(String, default="manual")
    image_url = Column(String, nullable=True)
    is_confirmed_by_user = Column(Integer, default=1)  # 1 = confirmed

    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="food_entries")
