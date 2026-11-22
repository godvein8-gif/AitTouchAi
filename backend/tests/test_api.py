from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)
AUTH = {"Authorization": "Bearer demo-test-user"}


def test_health():
    r = client.get("/health")
    assert r.status_code == 200 and r.json()["status"] == "ok"


def test_chat_requires_token():
    assert client.post("/chat", json={"session_id": "a", "message": "hi"}).status_code == 401


def test_chat_demo_reply_is_marked_mock():
    r = client.post("/chat", json={"session_id": "a", "message": "hello"}, headers=AUTH)
    assert r.status_code == 200 and r.json()["mock"] is True


def test_chat_validates_input():
    r = client.post("/chat", json={"session_id": "bad id!", "message": "hi"}, headers=AUTH)
    assert r.status_code == 422


def test_auth_verify_demo_token():
    r = client.post("/auth/verify", json={"id_token": "demo-abc"})
    assert r.status_code == 200 and r.json()["demo"] is True


def test_incoming_webhook_disabled_by_default():
    assert client.post("/calls/incoming", data={"CallSid": "x"}).status_code == 503


def test_simulated_call_is_demo():
    r = client.post("/calls/simulate", headers=AUTH)
    assert r.status_code == 200 and r.json()["demo"] is True
