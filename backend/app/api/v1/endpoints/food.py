from typing import List, Optional
from datetime import date
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.food import FoodEntry
from app.schemas.food import (
    FoodCreate, FoodEntryOut, FoodParseRequest,
    FoodEstimateResponse, DailyNutritionSummary
)
from app.ai.food_parser import FoodParser

router = APIRouter()


@router.post("/parse", response_model=FoodEstimateResponse)
def parse_food(request: FoodParseRequest):
    """
    Parses natural language food string or OCR text into estimated calories & macros.
    Section 17 & 18.
    """
    parsed = FoodParser.parse_text(request.query)
    return FoodEstimateResponse(**parsed)


@router.post("/", response_model=FoodEntryOut)
def log_food_entry(
    food_in: FoodCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    entry = FoodEntry(
        user_id=current_user.id,
        date=food_in.date,
        time=food_in.time,
        meal_type=food_in.meal_type,
        food_name=food_in.food_name,
        portion_desc=food_in.portion_desc,
        quantity=food_in.quantity,
        calories=food_in.calories,
        protein_g=food_in.protein_g,
        carbs_g=food_in.carbs_g,
        fat_g=food_in.fat_g,
        fiber_g=food_in.fiber_g,
        source=food_in.source,
        is_confirmed_by_user=1
    )
    db.add(entry)
    db.commit()
    db.refresh(entry)
    return entry


@router.get("/day", response_model=List[FoodEntryOut])
def get_food_for_date(
    date_str: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    target_date = date_str or date.today().isoformat()
    entries = db.query(FoodEntry).filter(
        FoodEntry.user_id == current_user.id,
        FoodEntry.date == target_date
    ).all()

    # Seed demo food entry from spec if empty
    if not entries and not date_str:
        seeds = [
            FoodEntry(user_id=current_user.id, date=target_date, time="08:45", meal_type="breakfast", food_name="Oats & Milk with Banana", calories=415.0, protein_g=14.8, carbs_g=67.0, fat_g=11.3, fiber_g=7.5, source="manual"),
            FoodEntry(user_id=current_user.id, date=target_date, time="13:15", meal_type="lunch", food_name="2 Rotis with Paneer Bhurji & Salad", calories=445.0, protein_g=24.0, carbs_g=47.5, fat_g=21.0, fiber_g=6.0, source="text_ai"),
        ]
        db.add_all(seeds)
        db.commit()
        entries = seeds

    return entries


@router.get("/summary/today", response_model=DailyNutritionSummary)
def get_daily_nutrition_summary(
    date_str: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    target_date = date_str or date.today().isoformat()
    entries = db.query(FoodEntry).filter(
        FoodEntry.user_id == current_user.id,
        FoodEntry.date == target_date
    ).all()

    target_cal = 1200.0  # From product spec
    consumed_cal = sum(e.calories for e in entries)
    total_prot = sum(e.protein_g for e in entries)
    total_carbs = sum(e.carbs_g for e in entries)
    total_fat = sum(e.fat_g for e in entries)
    total_fiber = sum(e.fiber_g for e in entries)

    return DailyNutritionSummary(
        date=target_date,
        target_calories=target_cal,
        consumed_calories=round(consumed_cal, 1),
        remaining_calories=round(max(0.0, target_cal - consumed_cal), 1),
        total_protein_g=round(total_prot, 1),
        total_carbs_g=round(total_carbs, 1),
        total_fat_g=round(total_fat, 1),
        total_fiber_g=round(total_fiber, 1)
    )
