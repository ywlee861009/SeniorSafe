from functools import cached_property

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    postgres_user: str = "seniorsafe"
    postgres_password: str = "seniorsafe"
    postgres_db: str = "seniorsafe_db"
    postgres_host: str = "localhost"
    postgres_port: int = 5432

    secret_key: str = Field(default="change-this-to-a-long-random-secret")
    access_token_expire_minutes: int = 60
    refresh_token_expire_days: int = 30
    device_token_expire_days: int = 365

    firebase_credentials_path: str = "/app/firebase-credentials.json"

    pairing_code_expire_minutes: int = 10
    fall_cancel_window_seconds: int = 30

    @cached_property
    def database_url(self) -> str:
        return (
            f"postgresql+asyncpg://{self.postgres_user}:{self.postgres_password}"
            f"@{self.postgres_host}:{self.postgres_port}/{self.postgres_db}"
        )


settings = Settings()
