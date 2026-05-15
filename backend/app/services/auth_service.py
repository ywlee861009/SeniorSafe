from fastapi import HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import create_access_token, hash_password, verify_password
from app.models.user import User
from app.schemas.auth import AuthResponse, LoginRequest, RegisterRequest

VALID_USER_TYPES = {"senior", "guardian"}


async def register_user(db: AsyncSession, request: RegisterRequest) -> AuthResponse:
    if request.user_type not in VALID_USER_TYPES:
        raise HTTPException(status_code=400, detail="Invalid user_type")

    existing = await db.execute(select(User).where(User.email == request.email))
    if existing.scalar_one_or_none() is not None:
        raise HTTPException(status_code=409, detail="Email already registered")

    user = User(
        email=str(request.email),
        password_hash=hash_password(request.password),
        name=request.name,
        phone=request.phone,
        user_type=request.user_type,
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)

    return AuthResponse(
        access_token=create_access_token(user.id),
        user_type=user.user_type,
        name=user.name,
        user_id=str(user.id),
    )


async def login_user(db: AsyncSession, request: LoginRequest) -> AuthResponse:
    result = await db.execute(select(User).where(User.email == request.email, User.is_active.is_(True)))
    user = result.scalar_one_or_none()
    if user is None or not verify_password(request.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    return AuthResponse(
        access_token=create_access_token(user.id),
        user_type=user.user_type,
        name=user.name,
        user_id=str(user.id),
    )
