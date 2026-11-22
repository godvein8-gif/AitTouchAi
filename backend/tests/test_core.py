import asyncio

from app.security.rate_limit import RateLimiter
from app.security.signature_validation import compute_twilio_signature, is_valid_twilio_signature
from app.security.token_validation import TokenError, verify_token
from app.services.ai_service import MockAiProvider
from app.services.call_service import (
    TwilioConversationRelayProvider,
    greeting,
    receptionist_prompt,
)
from app.services.conversation_service import ConversationStore
from app.websocket.conversation_relay import handle_message


def run(coro):
    return asyncio.run(coro)


def test_signature_roundtrip_and_tamper():
    params = {"CallSid": "CA123", "From": "+10000000000"}
    sig = compute_twilio_signature("secret", "https://x.example/calls/incoming", params)
    assert is_valid_twilio_signature("secret", "https://x.example/calls/incoming", params, sig)
    assert not is_valid_twilio_signature("secret", "https://x.example/calls/incoming", {**params, "From": "+2"}, sig)
    assert not is_valid_twilio_signature("wrong", "https://x.example/calls/incoming", params, sig)
    assert not is_valid_twilio_signature("secret", "https://x.example/calls/incoming", params, "")


def test_demo_token_only_in_demo_env():
    assert verify_token("demo-1", is_demo_env=True, firebase_project_id="").is_demo
    for env_demo in (False,):
        try:
            verify_token("demo-1", is_demo_env=env_demo, firebase_project_id="p")
            assert False, "should reject"
        except TokenError:
            pass


def test_rate_limiter_blocks_then_recovers():
    now = [0.0]
    rl = RateLimiter(2, 60, clock=lambda: now[0])
    assert rl.allow("a") and rl.allow("a")
    assert not rl.allow("a")
    now[0] = 61
    assert rl.allow("a")


def test_conversation_store_expires():
    now = [0.0]
    store = ConversationStore(ttl_sec=10, clock=lambda: now[0])
    store.append("s", "user", "hi")
    assert len(store.history("s")) == 1
    now[0] = 11
    assert store.history("s") == []


def test_mock_ai_flags_demo_and_language():
    ai = MockAiProvider()
    assert ai.is_mock
    assert "Namaste" in run(ai.reply([], "mujhe madad chahiye"))
    assert "DEMO" in run(ai.reply([], "hello there"))


def test_greeting_announces_ai_and_uses_configured_owner():
    g = greeting("Owner Name")
    assert "NEXUS AI" in g and "AI" in g and "Owner Name" in g


def test_prompt_contains_safety_rules():
    p = receptionist_prompt("X").lower()
    for phrase in ("never pretend", "otp", "never make commitments"):
        assert phrase in p


def test_twiml_escapes_attributes():
    xml = TwilioConversationRelayProvider().incoming_call_response('A "B" & C', "wss://h/ws")
    assert "&amp;" in xml and 'url="wss://h/ws"' in xml


def test_relay_prompt_returns_text_and_keeps_context():
    store = ConversationStore()
    out = run(handle_message("c1", {"type": "prompt", "voicePrompt": "hello"}, MockAiProvider(), "Owner", store))
    assert out[0]["type"] == "text" and out[0]["last"] is True
    assert len(store.history("c1")) == 2


def test_relay_ignores_empty_and_unknown():
    store = ConversationStore()
    assert run(handle_message("c", {"type": "prompt", "voicePrompt": " "}, MockAiProvider(), "O", store)) == []
    assert run(handle_message("c", {"type": "setup"}, MockAiProvider(), "O", store)) == []


def test_real_token_verified_with_injected_verifier():
    good = lambda t, p: {"user_id": "u1", "phone_number": "+910000000000"}
    user = verify_token("real.token", is_demo_env=False, firebase_project_id="proj", verifier=good)
    assert user.uid == "u1" and not user.is_demo


def test_real_token_failure_is_generic():
    def bad(t, p):
        raise ValueError("secret detail")
    try:
        verify_token("real.token", is_demo_env=False, firebase_project_id="proj", verifier=bad)
        assert False
    except TokenError as e:
        assert "secret" not in str(e)


def test_real_token_needs_project_id():
    try:
        verify_token("real.token", is_demo_env=False, firebase_project_id="")
        assert False
    except TokenError:
        pass


def test_openrouter_payload_and_key_required():
    from app.services.ai_service import OpenRouterProvider, build_chat_payload, get_ai_provider
    payload = build_chat_payload("m", [{"role": "user", "content": "a"}], "b", "")
    assert payload["messages"][0]["role"] == "system" and payload["messages"][-1]["content"] == "b"
    try:
        OpenRouterProvider("", "m", "https://x")
        assert False
    except ValueError:
        pass
    assert get_ai_provider("mock").is_mock
