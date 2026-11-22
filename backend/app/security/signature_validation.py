"""Twilio webhook signature validation (X-Twilio-Signature).

Algorithm: HMAC-SHA1 over the full public URL followed by every POST parameter,
sorted by name, as name+value. The digest is base64 encoded.
"""
import base64
import hashlib
import hmac
from typing import Mapping


def compute_twilio_signature(auth_token: str, url: str, params: Mapping[str, str]) -> str:
    data = url + "".join(f"{k}{params[k]}" for k in sorted(params))
    digest = hmac.new(auth_token.encode(), data.encode(), hashlib.sha1).digest()
    return base64.b64encode(digest).decode()


def is_valid_twilio_signature(
    auth_token: str, url: str, params: Mapping[str, str], signature: str
) -> bool:
    if not auth_token or not signature:
        return False
    expected = compute_twilio_signature(auth_token, url, params)
    return hmac.compare_digest(expected, signature)
