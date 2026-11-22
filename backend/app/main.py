import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.routes import auth, calls, chat, health
from app.websocket import conversation_relay

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")

app = FastAPI(title="NEXUS AI Backend", version="0.1.0")

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(chat.router)
app.include_router(calls.router)
app.include_router(conversation_relay.router)


@app.exception_handler(Exception)
async def unhandled(_: Request, exc: Exception):
    # Never echo internal details or request content to the client.
    logging.getLogger("nexus").error("unhandled error: %s", type(exc).__name__)
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})
