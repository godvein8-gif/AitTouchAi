"""AI provider abstraction. Swap providers without touching routes."""
from abc import ABC, abstractmethod
from typing import Sequence

Message = dict  # {"role": "user" | "assistant", "content": str}

HINDI_HINTS = {"kaise", "kya", "hai", "mujhe", "batao", "namaste", "aap", "kaun", "kyun", "madad"}

CHAT_SYSTEM_PROMPT = (
    "You are NEXUS AI, a helpful personal assistant. Reply in the language the user writes in "
    "(Hindi, Hinglish or English). Be clear and concise. If you do not know something, say so."
)


class AiProviderError(Exception):
    """The AI service failed. The message is safe to show to the user."""


class AiProvider(ABC):
    is_mock: bool = False

    @abstractmethod
    async def reply(self, history: Sequence[Message], user_text: str, *, system_prompt: str = "") -> str:
        ...


class MockAiProvider(AiProvider):
    """Scripted DEMO replies. Not a real language model."""

    is_mock = True

    async def reply(self, history, user_text, *, system_prompt=""):
        words = {w.strip(".,!?").lower() for w in user_text.split()}
        is_hindi = any("ऀ" <= ch <= "ॿ" for ch in user_text) or bool(words & HINDI_HINTS)
        quote = user_text.strip()[:80]
        if is_hindi:
            return f'Namaste! Ye ek DEMO jawab hai, asli AI abhi juda nahi hai. Aapne kaha: "{quote}"'
        return f'This is a DEMO reply. A real AI is not connected yet. You said: "{quote}"'


def build_chat_payload(model: str, history: Sequence[Message], user_text: str, system_prompt: str) -> dict:
    messages = [{"role": "system", "content": system_prompt or CHAT_SYSTEM_PROMPT}]
    messages += [{"role": m["role"], "content": m["content"]} for m in history]
    messages.append({"role": "user", "content": user_text})
    return {"model": model, "messages": messages, "max_tokens": 600}


class OpenRouterProvider(AiProvider):
    """Real AI through OpenRouter (OpenAI-compatible chat completions API)."""

    is_mock = False

    def __init__(self, api_key: str, model: str, base_url: str, timeout: float = 30.0):
        if not api_key:
            raise ValueError("AI_API_KEY is not set")
        self.api_key = api_key
        self.model = model
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    async def reply(self, history, user_text, *, system_prompt=""):
        import httpx  # imported here so the mock works without the package

        payload = build_chat_payload(self.model, history, user_text, system_prompt)
        headers = {"Authorization": f"Bearer {self.api_key}", "Content-Type": "application/json"}
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                resp = await client.post(f"{self.base_url}/chat/completions", json=payload, headers=headers)
        except httpx.HTTPError:
            raise AiProviderError("The AI service could not be reached. Please try again.")
        if resp.status_code != 200:
            # Never include the response body or key in the message.
            raise AiProviderError(f"The AI service returned an error ({resp.status_code}).")
        try:
            return resp.json()["choices"][0]["message"]["content"].strip()
        except (KeyError, IndexError, TypeError, ValueError):
            raise AiProviderError("The AI service sent an unexpected reply.")


def get_ai_provider(name: str, *, api_key: str = "", model: str = "", base_url: str = "") -> AiProvider:
    if name == "mock":
        return MockAiProvider()
    if name == "openrouter":
        return OpenRouterProvider(api_key, model or "openrouter/auto", base_url or "https://openrouter.ai/api/v1")
    raise ValueError(f"Unknown AI provider: {name}")
