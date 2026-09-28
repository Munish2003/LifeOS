from typing import Optional
from pydantic import BaseModel, EmailStr


class Token(BaseModel):
    access_token: str
    token_type: str


class TokenData(BaseModel):
    user_id: Optional[str] = None


class UserCreate(BaseModel):
    email: EmailStr
    username: str
    password: str
    full_name: Optional[str] = None


class UserLogin(BaseModel):
    username_or_email: str
    password: str


class ProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    daily_step_target: Optional[int] = None
    daily_calorie_target: Optional[int] = None
    target_weight: Optional[float] = None
    current_weight: Optional[float] = None
    sleep_target_hours: Optional[float] = None
    default_wake_time: Optional[str] = None
    default_sleep_time: Optional[str] = None
    timezone: Optional[str] = None


class UserOut(BaseModel):
    id: int
    email: EmailStr
    username: str
    is_active: bool

    class Config:
        from_attributes = True


class ProfileOut(BaseModel):
    id: int
    daily_step_target: int
    daily_calorie_target: int
    target_weight: Optional[float]
    current_weight: Optional[float]
    sleep_target_hours: float
    default_wake_time: str
    default_sleep_time: str
    timezone: str

    class Config:
        from_attributes = True
