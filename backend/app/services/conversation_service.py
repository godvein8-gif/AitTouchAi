"""Short-lived in-memory conversation context.

Nothing is written to disk. Sessions expire after a timeout so transcripts are not
kept longer than needed.
"""
import time
from typing import Dict, List

MAX_TURNS = 20


class ConversationStore:
    def __init__(self, ttl_sec: int = 1800, clock=time.monotonic):
        self.ttl = ttl_sec
        self.clock = clock
        self._data: Dict[str, dict] = {}

    def _purge(self) -> None:
        now = self.clock()
        for key in [k for k, v in self._data.items() if now - v["touched"] > self.ttl]:
            del self._data[key]

    def history(self, session_id: str) -> List[dict]:
        self._purge()
        entry = self._data.get(session_id)
        return list(entry["messages"]) if entry else []

    def append(self, session_id: str, role: str, content: str) -> None:
        self._purge()
        entry = self._data.setdefault(session_id, {"messages": [], "touched": self.clock()})
        entry["messages"].append({"role": role, "content": content})
        entry["messages"] = entry["messages"][-MAX_TURNS:]
        entry["touched"] = self.clock()

    def end(self, session_id: str) -> None:
        self._data.pop(session_id, None)
