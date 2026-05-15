from datetime import datetime, timedelta, timezone

from app.models.pairing_code import PairingCode


def register_device(client, install_id: str, role: str, display_name: str) -> dict:
    response = client.post(
        "/devices/register",
        json={
            "install_id": install_id,
            "role": role,
            "display_name": display_name,
            "fcm_token": f"fcm-{install_id}",
        },
    )
    assert response.status_code == 200
    return response.json()


def device_headers(device_response: dict) -> dict[str, str]:
    return {"Authorization": f"Bearer {device_response['device_access_token']}"}


def test_register_device_and_get_current_device(db_client):
    senior = register_device(db_client, "senior-install-1", "senior", "홍길동")

    response = db_client.get("/devices/me", headers=device_headers(senior))

    assert response.status_code == 200
    body = response.json()
    assert body["device_id"] == senior["device_id"]
    assert body["role"] == "senior"
    assert body["display_name"] == "홍길동"


def test_register_device_reuses_install_id_and_rotates_token(db_client):
    first = register_device(db_client, "same-install-id", "senior", "홍길동")
    second = register_device(db_client, "same-install-id", "senior", "김길동")

    assert second["device_id"] == first["device_id"]
    assert second["display_name"] == "김길동"
    assert second["device_access_token"] != first["device_access_token"]


def test_device_pairing_flow_enforces_roles_and_persists_connection(db_client):
    senior = register_device(db_client, "senior-install-1", "senior", "홍길동")
    guardian = register_device(db_client, "guardian-install-1", "guardian", "보호자")

    guardian_code = db_client.post("/pairing/codes", headers=device_headers(guardian))
    assert guardian_code.status_code == 403

    code_response = db_client.post("/pairing/codes", headers=device_headers(senior))
    assert code_response.status_code == 200
    code = code_response.json()["code"]
    assert code.isdigit()
    assert len(code) == 6
    assert code_response.json()["expires_in_seconds"] == 600

    senior_claim = db_client.post(
        "/pairings",
        headers=device_headers(senior),
        json={"code": code},
    )
    assert senior_claim.status_code == 403

    claim = db_client.post(
        "/pairings",
        headers=device_headers(guardian),
        json={"code": code},
    )
    assert claim.status_code == 200
    assert claim.json()["senior_device_id"] == senior["device_id"]

    pairings = db_client.get("/pairings", headers=device_headers(guardian))
    assert pairings.status_code == 200
    assert pairings.json()["pairings"][0]["senior_device_id"] == senior["device_id"]


def test_pairing_code_is_one_time_use(db_client):
    senior = register_device(db_client, "senior-install-1", "senior", "홍길동")
    guardian = register_device(db_client, "guardian-install-1", "guardian", "보호자")
    other_guardian = register_device(db_client, "guardian-install-2", "guardian", "다른 보호자")
    code = db_client.post("/pairing/codes", headers=device_headers(senior)).json()["code"]

    first_claim = db_client.post("/pairings", headers=device_headers(guardian), json={"code": code})
    assert first_claim.status_code == 200

    second_claim = db_client.post("/pairings", headers=device_headers(other_guardian), json={"code": code})
    assert second_claim.status_code == 404


def test_expired_and_invalid_pairing_codes_are_rejected(db_client):
    senior = register_device(db_client, "senior-install-1", "senior", "홍길동")
    guardian = register_device(db_client, "guardian-install-1", "guardian", "보호자")
    code = db_client.post("/pairing/codes", headers=device_headers(senior)).json()["code"]

    from app.core.database import get_db
    from app.main import app

    async def expire_code() -> None:
        async for db in app.dependency_overrides[get_db]():
            pairing_code = await db.get(PairingCode, code)
            pairing_code.expires_at = datetime.now(timezone.utc) - timedelta(seconds=1)
            await db.commit()

    import asyncio

    asyncio.run(expire_code())

    expired = db_client.post("/pairings", headers=device_headers(guardian), json={"code": code})
    assert expired.status_code == 404

    invalid = db_client.post("/pairings", headers=device_headers(guardian), json={"code": "000000"})
    assert invalid.status_code == 404


def test_disconnect_pairing_requires_participant(db_client):
    senior = register_device(db_client, "senior-install-1", "senior", "홍길동")
    guardian = register_device(db_client, "guardian-install-1", "guardian", "보호자")
    other_guardian = register_device(db_client, "guardian-install-2", "guardian", "다른 보호자")
    code = db_client.post("/pairing/codes", headers=device_headers(senior)).json()["code"]
    pairing = db_client.post("/pairings", headers=device_headers(guardian), json={"code": code}).json()

    forbidden = db_client.delete(
        f"/pairings/{pairing['pairing_id']}",
        headers=device_headers(other_guardian),
    )
    assert forbidden.status_code == 404

    disconnected = db_client.delete(
        f"/pairings/{pairing['pairing_id']}",
        headers=device_headers(guardian),
    )
    assert disconnected.status_code == 200
    assert disconnected.json() == {"pairing_id": pairing["pairing_id"], "active": False}

    pairings = db_client.get("/pairings", headers=device_headers(guardian))
    assert pairings.status_code == 200
    assert pairings.json() == {"pairings": []}
