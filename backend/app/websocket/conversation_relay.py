"""WebSocket relay for Twilio ConversationRelay.

Twilio does speech-to-text and text-to-speech. This server receives the caller's
words as text and answers with text.

Incoming message types handled: setup, prompt, interrupt, dtmf, error.
NOTE: Message field names follow Twilio's documented protocol but are untested
against a live account in Phase 1. Verify in Phase 4.
"""
import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.config import get_settings
from app.security.signature_validation import is_valid_twilio_signature
from app.services.ai_service import AiProvider, get_ai_provider
from app.services.call_service import receptionist_prompt
from app.services.conversation_service import ConversationStore

router = APIRouter()
log = logging.getLogger("nexus.relay")
_store = ConversationStore(ttl_sec=900)


async def handle_message(
    session_id: str, message: dict, ai: AiProvider, owner_name: str, store: ConversationStore
) -> list[dict]:
    """Pure message handler (no sockets) so it can be unit tested."""
    kind = message.get("type")

    if kind == "prompt":
        text = str(message.get("voicePrompt", "")).strip()
        if not text:
            return []
        history = store.history(session_id)
        reply = await ai.reply(history, text, system_prompt=receptionist_prompt(owner_name))
        store.append(session_id, "user", text)
        store.append(session_id, "assistant", reply)
        return [{"type": "text", "token": reply, "last": True}]

    if kind == "interrupt":
        heard = str(message.get("utteranceUntilInterrupt", "")).strip()
        if heard:
            store.append(session_id, "assistant", heard)
        return []

    if kind == "error":
        # Log only that an error happened, never its content.
        log.warning("relay reported an error")
        return []

    # setup, dtmf and unknown types need no reply.
    return []


@router.websocket("/ws/conversation-relay")
async def conversation_relay(websocket: WebSocket):
    settings = get_settings()
    if settings.call_provider != "twilio" or not settings.twilio_auth_token:
        await websocket.close(code=1008)
        return
    signature = websocket.headers.get("x-twilio-signature", "")
    if not is_valid_twilio_signature(
        settings.twilio_auth_token, settings.public_ws_url, {}, signature
    ):
        await websocket.close(code=1008)
        return

    await websocket.accept()
    session_id = f"call-{id(websocket)}"
    ai = get_ai_provider(
        settings.ai_provider,
        api_key=settings.ai_api_key,
        model=settings.ai_model,
        base_url=settings.ai_base_url,
    )
    try:
        while True:
            message = await websocket.receive_json()
            for out in await handle_message(session_id, message, ai, settings.owner_name, _store):
                await websocket.send_json(out)
    except WebSocketDisconnect:
        pass
    except Exception:
        log.exception("relay connection error")
        await websocket.close(code=1011)
    finally:
        _store.end(session_id)  # do not keep call context after the call
