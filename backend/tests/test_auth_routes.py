from fastapi import HTTPException

import app.routers.auth as auth_router
from app.schemas.auth import AuthResponse


def test_register_returns_auth_response(client, monkeypatch):
    async def fake_register_user(db, request):
        assert request.email == "senior@example.com"
        assert request.user_type == "senior"
        return AuthResponse(
            access_token="access-token",
            user_type="senior",
            name=request.name,
            user_id="user-id",
        )

    monkeypatch.setattr(auth_router, "register_user", fake_register_user)

    response = client.post(
        "/auth/register",
        json={
            "email": "senior@example.com",
            "password": "password123",
            "name": "홍길동",
            "phone": "010-0000-0000",
            "user_type": "senior",
        },
    )

    assert response.status_code == 200
    assert response.json() == {
        "access_token": "access-token",
        "user_type": "senior",
        "name": "홍길동",
        "user_id": "user-id",
    }


def test_register_rejects_short_password(client):
    response = client.post(
        "/auth/register",
        json={
            "email": "senior@example.com",
            "password": "short",
            "name": "홍길동",
            "phone": "010-0000-0000",
            "user_type": "senior",
        },
    )

    assert response.status_code == 422


def test_register_surfaces_duplicate_email(client, monkeypatch):
    async def fake_register_user(db, request):
        raise HTTPException(status_code=409, detail="Email already registered")

    monkeypatch.setattr(auth_router, "register_user", fake_register_user)

    response = client.post(
        "/auth/register",
        json={
            "email": "senior@example.com",
            "password": "password123",
            "name": "홍길동",
            "phone": "010-0000-0000",
            "user_type": "senior",
        },
    )

    assert response.status_code == 409
    assert response.json() == {"detail": "Email already registered"}


def test_login_surfaces_invalid_credentials(client, monkeypatch):
    async def fake_login_user(db, request):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    monkeypatch.setattr(auth_router, "login_user", fake_login_user)

    response = client.post(
        "/auth/login",
        json={"email": "senior@example.com", "password": "wrong-password"},
    )

    assert response.status_code == 401
    assert response.json() == {"detail": "Invalid email or password"}

