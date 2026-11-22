"""Call provider abstraction, call prompt rules and DEMO call simulation."""
from abc import ABC, abstractmethod
from xml.sax.saxutils import quoteattr


def receptionist_prompt(owner_name: str) -> str:
    return (
        f"You are NEXUS AI, an AI assistant answering calls for {owner_name}. "
        "Say clearly that you are an AI at the start. Never pretend to be the owner. "
        "Never invent personal information and never share private details. "
        "Never make commitments or confirm meetings on the owner's behalf. "
        "Never ask for passwords, OTPs or payment details. "
        "Offer to take a callback message with the caller's name and reason. "
        "Reply in the caller's language (Hindi or English). Keep answers short."
    )


def greeting(owner_name: str) -> str:
    return (
        f"Namaste! Main NEXUS AI hoon, {owner_name} ka AI assistant. "
        "Ye call ek AI se ho rahi hai. Bataiye, main aapki kya madad kar sakta hoon?"
    )


class CallProvider(ABC):
    name: str

    @abstractmethod
    def incoming_call_response(self, owner_name: str, ws_url: str) -> str:
        """Provider-specific response (TwiML for Twilio) that connects the call."""


class TwilioConversationRelayProvider(CallProvider):
    """Builds the TwiML that connects a call to the WebSocket relay.

    NOTE: Not tested against a live Twilio account in Phase 1. Verify attribute
    names against Twilio's current ConversationRelay documentation in Phase 4.
    """

    name = "twilio"

    def incoming_call_response(self, owner_name: str, ws_url: str) -> str:
        return (
            '<?xml version="1.0" encoding="UTF-8"?>'
            "<Response><Connect>"
            f"<ConversationRelay url={quoteattr(ws_url)} "
            f"welcomeGreeting={quoteattr(greeting(owner_name))} language=\"hi-IN\" />"
            "</Connect></Response>"
        )


def simulated_call() -> dict:
    """A fictional DEMO call. No real caller or provider is involved."""
    return {
        "demo": True,
        "caller": "DEMO Caller",
        "summary": "Simulated call: caller asked for the owner; AI took a message.",
        "callback_message": "DEMO: please call back.",
    }
