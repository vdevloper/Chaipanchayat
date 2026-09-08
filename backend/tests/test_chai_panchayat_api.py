"""Chai Panchayat backend API tests.

Endpoints under test:
- GET  /api/                (health)
- POST /api/register-push   (push registration relay)
- POST /api/broadcast-test  (push broadcast helper)

Note: EMERGENT_PUSH_KEY is 'placeholder' in preview. The push endpoints are
expected to fail upstream with a 500 whose detail is
'EMERGENT_PUSH_KEY missing or invalid'. That's the accepted placeholder-state
behavior for this MVP.
"""
import os
import pytest
import requests

BASE_URL = os.environ["EXPO_PUBLIC_BACKEND_URL"].rstrip("/") if os.environ.get("EXPO_PUBLIC_BACKEND_URL") else None
if not BASE_URL:
    # Fall back to reading frontend/.env directly so tests still work when
    # executed from a shell where EXPO_PUBLIC_BACKEND_URL is not exported.
    from pathlib import Path
    env_file = Path("/app/frontend/.env")
    if env_file.exists():
        for line in env_file.read_text().splitlines():
            if line.startswith("EXPO_PUBLIC_BACKEND_URL="):
                BASE_URL = line.split("=", 1)[1].strip().strip('"').rstrip("/")
                break
assert BASE_URL, "EXPO_PUBLIC_BACKEND_URL is required"


@pytest.fixture
def api_client():
    s = requests.Session()
    s.headers.update({"Content-Type": "application/json"})
    return s


# --- Health ------------------------------------------------------------------
class TestHealth:
    def test_root_ok(self, api_client):
        r = api_client.get(f"{BASE_URL}/api/", timeout=15)
        assert r.status_code == 200, r.text
        body = r.json()
        assert body.get("service") == "Chai Panchayat"
        assert body.get("status") == "ok"


# --- Push registration -------------------------------------------------------
class TestRegisterPush:
    def _payload(self):
        return {
            "user_id": "chai_all_subscribers",
            "platform": "web",
            "device_token": "TEST_devtoken_abc123",
        }

    def test_register_push_placeholder_or_success(self, api_client):
        r = api_client.post(
            f"{BASE_URL}/api/register-push", json=self._payload(), timeout=20
        )
        # Accept 201 success OR 500 placeholder-state
        assert r.status_code in (201, 500, 502), r.text
        if r.status_code == 500:
            detail = r.json().get("detail", "")
            assert "EMERGENT_PUSH_KEY" in detail, f"Unexpected 500 detail: {detail}"
        elif r.status_code == 201:
            assert r.json().get("status") == "registered"

    def test_register_push_validation(self, api_client):
        # Missing required fields -> 422
        r = api_client.post(f"{BASE_URL}/api/register-push", json={"user_id": "x"}, timeout=15)
        assert r.status_code == 422


# --- Broadcast helper --------------------------------------------------------
class TestBroadcastTest:
    def test_broadcast_placeholder_or_success(self, api_client):
        body = {"title": "TEST_title", "message": "TEST_message"}
        r = api_client.post(f"{BASE_URL}/api/broadcast-test", json=body, timeout=20)
        # Endpoint always returns 200; the payload indicates success/error.
        assert r.status_code == 200, r.text
        body = r.json()
        assert body.get("status") in ("sent", "error")
        if body.get("status") == "error":
            assert "EMERGENT_PUSH_KEY" in body.get("detail", "") or "Push provider" in body.get("detail", "")

    def test_broadcast_validation(self, api_client):
        # Missing 'message' -> 422
        r = api_client.post(f"{BASE_URL}/api/broadcast-test", json={"title": "x"}, timeout=15)
        assert r.status_code == 422
