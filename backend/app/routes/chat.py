from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from app.deps import current_user, get_ai, get_store, rate_limit
from app.security.token_validation import AuthUser
from app.services.ai_service import AiProvider, AiProviderError
from app.services.conversation_service import ConversationStore

router = APIRouter(prefix="/chat", tags=["chat"])


class ChatRequest(BaseModel):
    session_id: str = Field(min_length=1, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")
    message: str = Field(min_length=1, max_length=2000)


class ChatResponse(BaseModel):
    reply: str
    mock: bool


@router.post("", response_model=ChatResponse, dependencies=[Depends(rate_limit("chat"))])
async def chat(
    body: ChatRequest,
    user: AuthUser = Depends(current_user),
    ai: AiProvider = Depends(get_ai),
    store: ConversationStore = Depends(get_store),
):
    # Sessions are namespaced by user so one user cannot read another's context.
    key = f"{user.uid}:{body.session_id}"
    history = store.history(key)
    try:
        reply = await ai.reply(history, body.message)
    except AiProviderError as exc:
        raise HTTPException(status_code=502, detail=str(exc))
    store.append(key, "user", body.message)
    store.append(key, "assistant", reply)
    return ChatResponse(reply=reply, mock=ai.is_mock)
