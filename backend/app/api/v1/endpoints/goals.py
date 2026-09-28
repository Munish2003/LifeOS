from typing import List
from datetime import date, timedelta
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.v1.endpoints.auth import get_current_user
from app.models.user import User
from app.models.goal import Goal, Challenge

router = APIRouter()


@router.get("/challenges")
def get_challenges(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    challenges = db.query(Challenge).filter(Challenge.user_id == current_user.id).all()
    if not challenges:
        today = date.today()
        end = today + timedelta(days=15)
        # Seed 15-day challenges from spec
        seed_challenges = [
            Challenge(
                user_id=current_user.id,
                title="15-Day Step Challenge",
                description="Hit 10,000 steps daily for 15 days.",
                duration_days=15,
                target_metric="steps_daily",
                target_value_per_day=10000.0,
                days_completed=8,  # From spec example (8/15)
                current_progress=53.3,
                start_date=today.isoformat(),
                end_date=end.isoformat(),
                is_active=True
            ),
            Challenge(
                user_id=current_user.id,
                title="Python Sprint Challenge",
                description="30 hours of focused Python learning in 15 days.",
                duration_days=15,
                target_metric="hours_total",
                total_target_value=30.0,
                days_completed=6,
                current_progress=14.5,
                start_date=today.isoformat(),
                end_date=end.isoformat(),
                is_active=True
            )
        ]
        db.add_all(seed_challenges)
        db.commit()
        challenges = seed_challenges

    return challenges


@router.get("/")
def get_goals(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    goals = db.query(Goal).filter(Goal.user_id == current_user.id).all()
    if not goals:
        seeds = [
            Goal(user_id=current_user.id, title="10,000 Steps Daily", frequency="daily", category="health", metric="steps", target_value=10000.0, current_value=7842.0),
            Goal(user_id=current_user.id, title="1,200 kcal Daily Intake", frequency="daily", category="health", metric="calories", target_value=1200.0, current_value=860.0),
            Goal(user_id=current_user.id, title="3h Python Mastery Daily", frequency="daily", category="learning", metric="hours", target_value=3.0, current_value=2.28),
            Goal(user_id=current_user.id, title="1h DSA Practice Daily", frequency="daily", category="learning", metric="hours", target_value=1.0, current_value=0.75),
            Goal(user_id=current_user.id, title="Target Weight: 75 kg", frequency="long_term", category="health", metric="kg", target_value=75.0, current_value=79.5),
        ]
        db.add_all(seeds)
        db.commit()
        goals = seeds
    return goals
