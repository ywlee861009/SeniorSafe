from datetime import datetime, timezone

import app.routers.fall as fall_router
from app.schemas.fall import FallEventResponse, FallHistoryResponse
from tests.conftest import auth_as


def test_report_fall_requires_authentication(client):
    response = client.post("/fall/event", json={"detected_at": "2026-05-15T12:00:00Z"})

    assert response.status_code == 403


def test_report_fall_event_returns_event_id(client, monkeypatch):
    auth_as(user_type="senior", user_id="senior-1", name="홍길동")

    async def fake_report_fall_event(db, current_user, detected_at):
        assert current_user.user_type == "senior"
        assert detected_at == datetime(2026, 5, 15, 12, 0, tzinfo=timezone.utc)
        return FallEventResponse(event_id="event-1", status="reported")

    monkeypatch.setattr(fall_router, "report_fall_event", fake_report_fall_event)

    response = client.post("/fall/event", json={"detected_at": "2026-05-15T12:00:00Z"})

    assert response.status_code == 200
    assert response.json() == {"event_id": "event-1", "status": "reported"}


def test_cancel_fall_event_returns_cancelled_status(client, monkeypatch):
    auth_as(user_type="senior", user_id="senior-1", name="홍길동")

    async def fake_cancel_fall_event(db, current_user, event_id):
        assert current_user.user_type == "senior"
        assert event_id == "event-1"
        return FallEventResponse(event_id=event_id, status="cancelled")

    monkeypatch.setattr(fall_router, "cancel_fall_event", fake_cancel_fall_event)

    response = client.post("/fall/cancel", json={"event_id": "event-1"})

    assert response.status_code == 200
    assert response.json() == {"event_id": "event-1", "status": "cancelled"}


def test_fall_history_passes_senior_id_to_service(client, monkeypatch):
    auth_as(user_type="guardian", user_id="guardian-1", name="보호자")

    async def fake_get_fall_history(db, current_user, senior_id):
        assert current_user.user_type == "guardian"
        assert senior_id == "senior-1"
        return FallHistoryResponse(events=[])

    monkeypatch.setattr(fall_router, "get_fall_history", fake_get_fall_history)

    response = client.get("/fall/history/senior-1")

    assert response.status_code == 200
    assert response.json() == {"events": []}

