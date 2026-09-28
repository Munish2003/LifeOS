from typing import List, Union
from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    PROJECT_NAME: str = "Personal AI Life OS"
    API_V1_STR: str = "/api/v1"
    SECRET_KEY: str = "super-secret-key-change-this-in-production-use-random-bytes"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # Database
    DATABASE_URL: str = "sqlite+pysqlite:///./lifeos.db"

    # AI Integration
    HUGGINGFACE_API_KEY: str = ""
    DEEPGRAM_API_KEY: str = ""
    AI_MODEL_NAME: str = "meta-llama/Meta-Llama-3-8B-Instruct"

    # CORS
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    model_config = SettingsConfigDict(case_sensitive=True, env_file=".env", extra="ignore")


settings = Settings()
