import os
import logging
import asyncio
from pathlib import Path
from typing import List, Optional
from datetime import datetime, timezone

import httpx
from fastapi import FastAPI, APIRouter, HTTPException
from starlette.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
from pydantic import BaseModel, Field
from dotenv import load_dotenv


ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / ".env")

# Mongo (used to track last-seen article for the broadcast worker)
mongo_url = os.environ["MONGO_URL"]
mongo_client = AsyncIOMotorClient(mongo_url)
db = mongo_client[os.environ["DB_NAME"]]

# Push relay setup
PUSH_BASE_URL = "https://integrations.emergentagent.com"
PUSH_KEY = os.environ.get("EMERGENT_PUSH_KEY", "placeholder")

_push_client = httpx.AsyncClient(
    base_url=PUSH_BASE_URL,
    headers={"X-Push-Key": PUSH_KEY},
    timeout=10.0,
)

# WordPress source
WP_BASE = "https://chaipanchayat.com/wp-json/wp/v2"
_wp_client = httpx.AsyncClient(base_url=WP_BASE, timeout=15.0)

# Broadcast recipient id — every device registers under this id so a single
# trigger fans out to all subscribers.
BROADCAST_USER_ID = "chai_all_subscribers"


app = FastAPI(title="Chai Panchayat API")
api_router = APIRouter(prefix="/api")


class RegisterPushBody(BaseModel):
    user_id: str
    platform: str  # "android" | "ios" | "web"
    device_token: str


class SendPushBody(BaseModel):
    title: str
    message: str
    action_url: Optional[str] = None
    image_url: Optional[str] = None


@api_router.get("/")
async def root():
    return {"service": "Chai Panchayat", "status": "ok"}


@api_router.post("/register-push", status_code=201)
async def register_push(body: RegisterPushBody):
    """Relay push registration to the Emergent push service."""
    try:
        resp = await _push_client.post(
            "/api/v1/push/users/register", json=body.model_dump()
        )
    except httpx.HTTPError as e:
        logging.warning(f"Push register upstream error: {e}")
        raise HTTPException(502, "Push provider unavailable")
    if resp.status_code == 401:
        raise HTTPException(500, "EMERGENT_PUSH_KEY missing or invalid")
    if resp.status_code >= 500:
        raise HTTPException(502, "Push provider unavailable")
    resp.raise_for_status()
    return {"status": "registered"}


async def send_push(
    recipients: List[str],
    data: dict,
    idempotency_key: Optional[str] = None,
) -> None:
    if not recipients:
        return
    if "title" not in data or "message" not in data:
        raise ValueError("data must include title and message")
    payload: dict = {"recipients": recipients, "data": data}
    if idempotency_key:
        payload["$idempotency_key"] = idempotency_key
    resp = await _push_client.post("/api/v1/push/trigger", json=payload)
    if resp.status_code == 401:
        raise HTTPException(500, "EMERGENT_PUSH_KEY missing or invalid")
    if resp.status_code >= 500:
        raise HTTPException(502, "Push provider unavailable")
    resp.raise_for_status()


@api_router.post("/broadcast-test")
async def broadcast_test(body: SendPushBody):
    """Manual trigger used for QA — broadcasts a push to all subscribers."""
    try:
        await send_push(
            recipients=[BROADCAST_USER_ID],
            data={
                "title": body.title,
                "message": body.message,
                **({"action_url": body.action_url} if body.action_url else {}),
                **({"image_url": body.image_url} if body.image_url else {}),
            },
        )
    except Exception as e:
        logging.warning(f"Broadcast failed (non-blocking): {e}")
        return {"status": "error", "detail": str(e)}
    return {"status": "sent"}


async def _fetch_latest_wp_post() -> Optional[dict]:
    try:
        resp = await _wp_client.get("/posts", params={"per_page": 1, "_embed": 1})
        resp.raise_for_status()
        arr = resp.json()
        return arr[0] if arr else None
    except Exception as e:
        logging.warning(f"WP poll failed: {e}")
        return None


async def _broadcast_loop() -> None:
    """Poll WordPress every 10 minutes; push a notification for each new post."""
    while True:
        try:
            latest = await _fetch_latest_wp_post()
            if latest:
                latest_id = int(latest.get("id", 0))
                doc = await db.push_state.find_one({"_id": "last_post"})
                seen_id = int(doc["post_id"]) if doc else 0
                if seen_id == 0:
                    # First run — record without notifying
                    await db.push_state.update_one(
                        {"_id": "last_post"},
                        {"$set": {"post_id": latest_id,
                                  "updated_at": datetime.now(timezone.utc).isoformat()}},
                        upsert=True,
                    )
                elif latest_id > seen_id:
                    title = latest.get("title", {}).get("rendered", "New story")
                    # very small excerpt
                    import re
                    excerpt_html = latest.get("excerpt", {}).get("rendered", "")
                    excerpt = re.sub(r"<[^>]+>", "", excerpt_html).strip()
                    if len(excerpt) > 140:
                        excerpt = excerpt[:137] + "..."
                    try:
                        await send_push(
                            recipients=[BROADCAST_USER_ID],
                            data={
                                "title": title,
                                "message": excerpt or "Read the latest on Chai Panchayat",
                                "action_url": f"/article/{latest_id}",
                            },
                            idempotency_key=f"post-{latest_id}",
                        )
                    except Exception as e:
                        logging.warning(f"Push send failed: {e}")
                    await db.push_state.update_one(
                        {"_id": "last_post"},
                        {"$set": {"post_id": latest_id,
                                  "updated_at": datetime.now(timezone.utc).isoformat()}},
                        upsert=True,
                    )
        except Exception as e:
            logging.warning(f"Broadcast loop iteration failed: {e}")
        await asyncio.sleep(600)  # 10 minutes


@app.on_event("startup")
async def _startup():
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    )
    asyncio.create_task(_broadcast_loop())


@app.on_event("shutdown")
async def _shutdown():
    await _push_client.aclose()
    await _wp_client.aclose()
    mongo_client.close()


app.include_router(api_router)
app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)
