"""Small in-memory sliding-window rate limiter (single process).

Good enough for Phase 1. For several server instances use a shared store such as Redis.
"""
import time
from collections import defaultdict, deque


class RateLimiter:
    def __init__(self, limit: int, window_sec: int = 60, clock=time.monotonic):
        self.limit = limit
        self.window = window_sec
        self.clock = clock
        self._hits: dict[str, deque] = defaultdict(deque)

    def allow(self, key: str) -> bool:
        now = self.clock()
        hits = self._hits[key]
        while hits and now - hits[0] > self.window:
            hits.popleft()
        if len(hits) >= self.limit:
            return False
        hits.append(now)
        return True
