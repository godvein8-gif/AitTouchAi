from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from app.deps import get_auth_service, rate_limit
from app.services.auth_service import AuthService, TokenError

router = APIRouter(prefix="/auth", tags=["auth"])


class VerifyRequest(BaseModel):
    id_token: str = Field(min_length=1, max_length=4096)


@router.post("/verify", dependencies=[Depends(rate_limit("auth", 10))])
def verify(body: VerifyRequest, auth: AuthService = Depends(get_auth_service)):
    try:
        user = auth.authenticate(body.id_token)
    except TokenError as exc:
        raise HTTPException(status_code=401, detail=str(exc))
    return {"uid": user.uid, "demo": user.is_demo}
