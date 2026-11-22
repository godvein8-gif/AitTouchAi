from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_env: str = "demo"
    owner_name: str = "the owner"

    firebase_project_id: str = ""

    ai_provider: str = "mock"
    ai_api_key: str = ""

    call_provider: str = "mock"
    twilio_auth_token: str = ""
    public_ws_url: str = ""
    public_webhook_url: str = ""

    rate_limit_per_minute: int = 30
    recording_enabled: bool = False

    @property
    def is_demo(self) -> bool:
        return self.app_env == "demo"


@lru_cache
def get_settings() -> Settings:
    return Settings()
