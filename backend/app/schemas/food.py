from typing import Optional
from datetime import datetime
from pydantic import BaseModel


class FoodCreate(BaseModel):
    date: str  # "YYYY-MM-DD"
    time: Optional[str] = None  # "HH:MM"
    meal_type: str = "lunch"  # "breakfast", "lunch", "dinner", "snack"
    food_name: str
    portion_desc: Optional[str] = None
    quantity: float = 1.0
    calories: float
    protein_g: float = 0.0
    carbs_g: float = 0.0
    fat_g: float = 0.0
    fiber_g: float = 0.0
    source: str = "manual"  # "manual", "text_ai", "image_ai", "ocr"


class FoodParseRequest(BaseModel):
    query: str  # e.g. "2 rotis and 1 bowl paneer" or OCR label text


class FoodEstimateResponse(BaseModel):
    food_name: str
    portion_desc: str
    calories: float
    protein_g: float
    carbs_g: float
    fat_g: float
    fiber_g: float
    confidence: float
    is_estimate: bool = True
    explanation: str


class FoodEntryOut(FoodCreate):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True


class DailyNutritionSummary(BaseModel):
    date: str
    target_calories: float
    consumed_calories: float
    remaining_calories: float
    total_protein_g: float
    total_carbs_g: float
    total_fat_g: float
    total_fiber_g: float
