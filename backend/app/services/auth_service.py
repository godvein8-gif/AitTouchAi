from app.security.token_validation import AuthUser, TokenError, verify_token


class AuthService:
    def __init__(self, is_demo_env: bool, firebase_project_id: str):
        self.is_demo_env = is_demo_env
        self.firebase_project_id = firebase_project_id

    def authenticate(self, token: str) -> AuthUser:
        """Raises TokenError when the token is not acceptable."""
        return verify_token(
            token,
            is_demo_env=self.is_demo_env,
            firebase_project_id=self.firebase_project_id,
        )


__all__ = ["AuthService", "TokenError"]
