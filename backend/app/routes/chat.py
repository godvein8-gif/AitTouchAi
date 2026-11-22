from typing import Literal, Optional

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from app.deps import current_user, get_ai, get_store, rate_limit
from app.security.token_validation import AuthUser
from app.services.ai_service import AiProvider, AiProviderError
from app.services.conversation_service import ConversationStore

router = APIRouter(prefix="/chat", tags=["chat"])


class HistoryItem(BaseModel):
    role: Literal["user", "assistant"]
    content: str = Field(min_length=1, max_length=4000)


class ChatRequest(BaseModel):
    session_id: str = Field(default="default", min_length=1, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")
    message: str = Field(min_length=1, max_length=2000)
    # The app sends recent messages itself, so the server keeps nothing between requests.
    history: Optional[list[HistoryItem]] = Field(default=None, max_length=20)


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
    if body.history is not None:
        history = [h.model_dump() for h in body.history]
        key = None
    else:
        # Sessions are namespaced by user so one user cannot read another's context.
        key = f"{user.uid}:{body.session_id}"
        history = store.history(key)

    try:
        reply = await ai.reply(history, body.message)
    except AiProviderError as exc:
        raise HTTPException(status_code=502, detail=str(exc))

    if key is not None:
        store.append(key, "user", body.message)
        store.append(key, "assistant", reply)
    return ChatResponse(reply=reply, mock=ai.is_mock)
