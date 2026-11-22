"""Validation of the user's sign-in token.

Phase 1: only the clearly fake demo token is accepted, and only when APP_ENV=demo.
Phase 2: verify Firebase ID tokens here (signature, expiry, audience = project id).
Never log tokens.
"""
from dataclasses import dataclass

DEMO_TOKEN_PREFIX = "demo-"


class TokenError(Exception):
    pass


@dataclass(frozen=True)
class AuthUser:
    uid: str
    is_demo: bool


def verify_token(token: str, *, is_demo_env: bool, firebase_project_id: str) -> AuthUser:
    if not token:
        raise TokenError("Missing token")

    if token.startswith(DEMO_TOKEN_PREFIX):
        if not is_demo_env:
            raise TokenError("Demo tokens are not accepted outside demo mode")
        return AuthUser(uid=token, is_demo=True)

    if not firebase_project_id:
        raise TokenError("Real authentication is not configured")

    # Phase 2: real Firebase ID token verification goes here.
    raise TokenError("Real token verification is not implemented yet")
