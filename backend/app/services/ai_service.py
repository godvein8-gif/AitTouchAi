"""AI provider abstraction. Swap providers without touching routes."""
from abc import ABC, abstractmethod
from typing import Sequence

Message = dict  # {"role": "user" | "assistant", "content": str}

HINDI_HINTS = {"kaise", "kya", "hai", "mujhe", "batao", "namaste", "aap", "kaun", "kyun", "madad"}


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


def get_ai_provider(name: str) -> AiProvider:
    if name == "mock":
        return MockAiProvider()
    # Phase 2: add a real provider here, reading its key from settings (never from the app).
    raise ValueError(f"Unknown or unconfigured AI provider: {name}")
