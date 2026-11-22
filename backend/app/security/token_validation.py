"""Validation of the user's sign-in token.

- Demo tokens ("demo-...") are accepted only when APP_ENV=demo.
- Real tokens are Firebase ID tokens, verified with Google's public keys:
  signature, expiry, issuer and audience (= your Firebase project id).
  No service account key is needed or stored.
Never log tokens.
"""
from dataclasses import dataclass
from typing import Callable, Optional

DEMO_TOKEN_PREFIX = "demo-"


class TokenError(Exception):
    pass


@dataclass(frozen=True)
class AuthUser:
    uid: str
    is_demo: bool
    phone_number: Optional[str] = None


def _firebase_verifier(token: str, project_id: str) -> dict:
    # Imported here so demo mode and tests do not need the google-auth package.
    from google.auth.transport import requests as google_requests
    from google.oauth2 import id_token

    return id_token.verify_firebase_token(token, google_requests.Request(), audience=project_id)


def verify_token(
    token: str,
    *,
    is_demo_env: bool,
    firebase_project_id: str,
    verifier: Callable[[str, str], dict] = _firebase_verifier,
) -> AuthUser:
    if not token:
        raise TokenError("Missing token")

    if token.startswith(DEMO_TOKEN_PREFIX):
        if not is_demo_env:
            raise TokenError("Demo tokens are not accepted outside demo mode")
        return AuthUser(uid=token, is_demo=True)

    if not firebase_project_id:
        raise TokenError("Real authentication is not configured")

    try:
        claims = verifier(token, firebase_project_id)
    except Exception:
        # Do not leak why verification failed.
        raise TokenError("Invalid or expired token")
    if not claims:
        raise TokenError("Invalid or expired token")

    uid = claims.get("user_id") or claims.get("sub")
    if not uid:
        raise TokenError("Invalid or expired token")
    return AuthUser(uid=str(uid), is_demo=False, phone_number=claims.get("phone_number"))
