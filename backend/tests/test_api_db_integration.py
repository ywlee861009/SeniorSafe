from datetime import datetime, timezone


def register_user(client, email: str, user_type: str) -> dict:
    response = client.post(
        "/auth/register",
        json={
            "email": email,
            "password": "password123",
            "name": "홍길동" if user_type == "senior" else "보호자",
            "phone": "010-0000-0000",
            "user_type": user_type,
        },
    )
    assert response.status_code == 200
    return response.json()


def auth_headers(auth_response: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {auth_response['access_token']}"}


def test_register_login_and_duplicate_email_use_database(db_client):
    senior = register_user(db_client, "senior@example.com", "senior")

    duplicate = db_client.post(
        "/auth/register",
        json={
            "email": "senior@example.com",
            "password": "password123",
            "name": "중복",
            "phone": "010-1111-1111",
            "user_type": "senior",
        },
    )
    assert duplicate.status_code == 409

    login = db_client.post(
        "/auth/login",
        json={"email": "senior@example.com", "password": "password123"},
    )
    assert login.status_code == 200
    assert login.json()["user_id"] == senior["user_id"]

    failed_login = db_client.post(
        "/auth/login",
        json={"email": "senior@example.com", "password": "wrong-password"},
    )
    assert failed_login.status_code == 401


def test_pairing_flow_enforces_roles_and_persists_connection(db_client):
    senior = register_user(db_client, "senior@example.com", "senior")
    guardian = register_user(db_client, "guardian@example.com", "guardian")

    guardian_code = db_client.get("/pairing/code", headers=auth_headers(guardian))
    assert guardian_code.status_code == 403

    code_response = db_client.get("/pairing/code", headers=auth_headers(senior))
    assert code_response.status_code == 200
    code = code_response.json()["code"]
    assert len(code) == 6

    senior_connect = db_client.post(
        "/pairing/connect",
        headers=auth_headers(senior),
        json={"code": code},
    )
    assert senior_connect.status_code == 403

    connect = db_client.post(
        "/pairing/connect",
        headers=auth_headers(guardian),
        json={"code": code.lower()},
    )
    assert connect.status_code == 200
    assert connect.json()["senior_id"] == senior["user_id"]

    pairings = db_client.get("/pairing/list", headers=auth_headers(guardian))
    assert pairings.status_code == 200
    assert pairings.json()["pairings"][0]["senior_id"] == senior["user_id"]


def test_fall_event_cancel_and_history_use_database(db_client, monkeypatch):
    sent_alerts = []

    async def fake_send_fall_alert(tokens, senior_name):
        sent_alerts.append((tokens, senior_name))

    monkeypatch.setattr("app.services.fall_service.send_fall_alert", fake_send_fall_alert)

    senior = register_user(db_client, "senior@example.com", "senior")
    guardian = register_user(db_client, "guardian@example.com", "guardian")
    other_guardian = register_user(db_client, "other@example.com", "guardian")

    token_response = db_client.put(
        "/devices/token",
        headers=auth_headers(guardian),
        json={"fcm_token": "fcm-token-1"},
    )
    assert token_response.status_code == 200

    code = db_client.get("/pairing/code", headers=auth_headers(senior)).json()["code"]
    connect = db_client.post(
        "/pairing/connect",
        headers=auth_headers(guardian),
        json={"code": code},
    )
    assert connect.status_code == 200

    detected_at = datetime(2026, 5, 15, 12, 0, tzinfo=timezone.utc).isoformat()
    report = db_client.post(
        "/fall/event",
        headers=auth_headers(senior),
        json={"detected_at": detected_at},
    )
    assert report.status_code == 200
    event_id = report.json()["event_id"]
    assert report.json()["status"] == "reported"
    assert sent_alerts == [(["fcm-token-1"], "홍길동")]

    other_history = db_client.get(
        f"/fall/history/{senior['user_id']}",
        headers=auth_headers(other_guardian),
    )
    assert other_history.status_code == 404

    cancel = db_client.post(
        "/fall/cancel",
        headers=auth_headers(senior),
        json={"event_id": event_id},
    )
    assert cancel.status_code == 200
    assert cancel.json() == {"event_id": event_id, "status": "cancelled"}

    history = db_client.get(
        f"/fall/history/{senior['user_id']}",
        headers=auth_headers(guardian),
    )
    assert history.status_code == 200
    assert history.json()["events"] == [
        {
            "id": event_id,
            "detected_at": "2026-05-15T12:00:00",
            "cancelled": True,
        }
    ]
