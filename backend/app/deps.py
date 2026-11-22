from fastapi import Depends, HTTPException, Request

from app.config import Settings, get_settings
from app.security.rate_limit import RateLimiter
from app.security.token_validation import AuthUser, TokenError
from app.services.ai_service import AiProvider, get_ai_provider
from app.services.auth_service import AuthService
from app.services.conversation_service import ConversationStore

_limiters: dict[str, RateLimiter] = {}
_store = ConversationStore()


def _limiter(name: str, limit: int) -> RateLimiter:
    if name not in _limiters:
        _limiters[name] = RateLimiter(limit)
    return _limiters[name]


def rate_limit(name: str, per_minute_override: int | None = None):
    def dependency(request: Request, settings: Settings = Depends(get_settings)):
        limit = per_minute_override or settings.rate_limit_per_minute
        client = request.client.host if request.client else "unknown"
        if not _limiter(name, limit).allow(client):
            raise HTTPException(status_code=429, detail="Too many requests. Try again later.")

    return dependency


def get_auth_service(settings: Settings = Depends(get_settings)) -> AuthService:
    return AuthService(settings.is_demo, settings.firebase_project_id)


def current_user(
    request: Request, auth: AuthService = Depends(get_auth_service)
) -> AuthUser:
    header = request.headers.get("authorization", "")
    token = header[7:] if header.lower().startswith("bearer ") else ""
    try:
        return auth.authenticate(token)
    except TokenError as exc:
        raise HTTPException(status_code=401, detail=str(exc))


def get_ai(settings: Settings = Depends(get_settings)) -> AiProvider:
    try:
        return get_ai_provider(settings.ai_provider)
    except ValueError as exc:
        raise HTTPException(status_code=503, detail=str(exc))


def get_store() -> ConversationStore:
    return _store
