import logging

from fastapi import APIRouter, Depends, HTTPException, Request, Response

from app.config import Settings, get_settings
from app.deps import current_user, rate_limit
from app.security.signature_validation import is_valid_twilio_signature
from app.security.token_validation import AuthUser
from app.services.call_service import TwilioConversationRelayProvider, simulated_call

router = APIRouter(prefix="/calls", tags=["calls"])
log = logging.getLogger("nexus.calls")


async def _validated_form(request: Request, settings: Settings) -> dict:
    if settings.call_provider != "twilio":
        raise HTTPException(status_code=503, detail="Cloud call provider is not enabled")
    if not settings.twilio_auth_token or not settings.public_webhook_url:
        raise HTTPException(status_code=503, detail="Cloud call provider is not configured")
    form = {k: str(v) for k, v in (await request.form()).items()}
    url = settings.public_webhook_url.rstrip("/") + request.url.path
    signature = request.headers.get("x-twilio-signature", "")
    if not is_valid_twilio_signature(settings.twilio_auth_token, url, form, signature):
        raise HTTPException(status_code=403, detail="Invalid signature")
    return form


@router.post("/incoming")
async def incoming(request: Request, settings: Settings = Depends(get_settings)):
    """Twilio voice webhook. Returns TwiML that connects the call to the relay."""
    await _validated_form(request, settings)
    if not settings.public_ws_url.startswith("wss://"):
        raise HTTPException(status_code=503, detail="PUBLIC_WS_URL must be a wss:// address")
    xml = TwilioConversationRelayProvider().incoming_call_response(
        settings.owner_name, settings.public_ws_url
    )
    return Response(content=xml, media_type="text/xml")


@router.post("/status")
async def status_callback(request: Request, settings: Settings = Depends(get_settings)):
    form = await _validated_form(request, settings)
    # Log only the status, never caller numbers or content.
    log.info("call status: %s", form.get("CallStatus", "unknown"))
    return Response(status_code=204)


@router.post("/simulate", dependencies=[Depends(rate_limit("simulate", 10))])
def simulate(user: AuthUser = Depends(current_user), settings: Settings = Depends(get_settings)):
    """DEMO only. Returns a fictional call. Never a real call."""
    if not settings.is_demo:
        raise HTTPException(status_code=404, detail="Not available")
    return simulated_call()
