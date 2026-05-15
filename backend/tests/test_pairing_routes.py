from datetime import datetime, timezone

import app.routers.pairing as pairing_router
from app.schemas.pairing import ConnectResponse, PairingCodeResponse, PairingListResponse
from tests.conftest import auth_as


def test_pairing_code_requires_authentication(client):
    response = client.get("/pairing/code")

    assert response.status_code == 403


def test_pairing_code_rejects_invalid_token(client):
    response = client.get("/pairing/code", headers={"Authorization": "Bearer invalid-token"})

    assert response.status_code == 401


def test_get_pairing_code_returns_code_for_authenticated_user(client, monkeypatch):
    auth_as(user_type="senior", user_id="senior-1", name="어르신")

    async def fake_create_pairing_code(db, current_user):
        assert current_user.user_type == "senior"
        return PairingCodeResponse(
            code="A3F9K2",
            expires_at=datetime(2026, 5, 15, 12, 10, tzinfo=timezone.utc),
        )

    monkeypatch.setattr(pairing_router, "create_pairing_code", fake_create_pairing_code)

    response = client.get("/pairing/code")

    assert response.status_code == 200
    assert response.json()["code"] == "A3F9K2"
    assert response.json()["expires_at"].startswith("2026-05-15T12:10:00")


def test_connect_senior_passes_code_to_service(client, monkeypatch):
    auth_as(user_type="guardian", user_id="guardian-1", name="보호자")

    async def fake_connect_senior(db, current_user, code):
        assert current_user.user_type == "guardian"
        assert code == "A3F9K2"
        return ConnectResponse(senior_id="senior-1", senior_name="홍길동")

    monkeypatch.setattr(pairing_router, "connect_senior", fake_connect_senior)

    response = client.post("/pairing/connect", json={"code": "A3F9K2"})

    assert response.status_code == 200
    assert response.json() == {"senior_id": "senior-1", "senior_name": "홍길동"}


def test_connect_senior_validates_code_length(client):
    auth_as(user_type="guardian")

    response = client.post("/pairing/connect", json={"code": "ABC"})

    assert response.status_code == 422


def test_pairing_list_returns_pairings(client, monkeypatch):
    auth_as(user_type="guardian", user_id="guardian-1", name="보호자")

    async def fake_list_pairings(db, current_user):
        assert current_user.user_type == "guardian"
        return PairingListResponse(pairings=[])

    monkeypatch.setattr(pairing_router, "list_pairings", fake_list_pairings)

    response = client.get("/pairing/list")

    assert response.status_code == 200
    assert response.json() == {"pairings": []}
