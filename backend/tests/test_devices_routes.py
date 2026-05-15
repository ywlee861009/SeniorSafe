import app.routers.devices as devices_router
from tests.conftest import auth_as


def test_update_fcm_token_requires_authentication(client):
    response = client.put("/devices/token", json={"fcm_token": "token-1"})

    assert response.status_code == 403


def test_update_fcm_token_returns_message(client, monkeypatch):
    auth_as(user_type="guardian", user_id="guardian-1", name="보호자")

    async def fake_update_fcm_token(db, current_user, fcm_token):
        assert current_user.user_type == "guardian"
        assert fcm_token == "token-1"

    monkeypatch.setattr(devices_router, "update_fcm_token", fake_update_fcm_token)

    response = client.put("/devices/token", json={"fcm_token": "token-1"})

    assert response.status_code == 200
    assert response.json() == {"message": "FCM token updated"}


def test_update_fcm_token_validates_non_empty_token(client):
    auth_as(user_type="guardian")

    response = client.put("/devices/token", json={"fcm_token": ""})

    assert response.status_code == 422

