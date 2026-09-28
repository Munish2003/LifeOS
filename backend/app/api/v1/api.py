from fastapi import APIRouter
from app.api.v1.endpoints import (
    auth, routines, tasks, sessions, health, food, goals, planning, analytics
)

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["Authentication & Profile"])
api_router.include_router(routines.router, prefix="/routines", tags=["Routines & Calendar"])
api_router.include_router(tasks.router, prefix="/tasks", tags=["Tasks"])
api_router.include_router(sessions.router, prefix="/sessions", tags=["Timer & Sessions"])
api_router.include_router(health.router, prefix="/health", tags=["Health & Steps"])
api_router.include_router(food.router, prefix="/food", tags=["Food & Calories"])
api_router.include_router(goals.router, prefix="/goals", tags=["Goals & Challenges"])
api_router.include_router(planning.router, prefix="/planning", tags=["Planning & Intelligence"])
api_router.include_router(analytics.router, prefix="/analytics", tags=["Analytics"])
