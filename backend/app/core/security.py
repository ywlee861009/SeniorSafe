from datetime import datetime, timedelta, timezone
from uuid import UUID
import hashlib
import uuid

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.database import get_db
from app.models.device import Device

bearer_scheme = HTTPBearer()


def hash_identifier(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def create_device_access_token(device_id: UUID) -> str:
    expires_at = datetime.now(timezone.utc) + timedelta(days=settings.device_token_expire_days)
    payload = {"sub": str(device_id), "typ": "device", "jti": str(uuid.uuid4()), "exp": expires_at}
    return jwt.encode(payload, settings.secret_key, algorithm="HS256")


async def get_current_device(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
    db: AsyncSession = Depends(get_db),
) -> Device:
    try:
        payload = jwt.decode(credentials.credentials, settings.secret_key, algorithms=["HS256"])
        if payload.get("typ") != "device":
            raise ValueError
        device_id = UUID(payload["sub"])
    except (JWTError, KeyError, ValueError):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid device token")

    result = await db.execute(select(Device).where(Device.id == device_id))
    device = result.scalar_one_or_none()
    if device is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Device not found")

    device.last_seen_at = datetime.now(timezone.utc)
    await db.commit()
    await db.refresh(device)
    return device
