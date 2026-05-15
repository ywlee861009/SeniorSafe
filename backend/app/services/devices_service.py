from datetime import datetime, timezone

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.security import create_device_access_token, hash_identifier
from app.models.device import Device
from app.models.user import User
from app.schemas.device import DeviceRegisterRequest, DeviceRegisterResponse


async def register_device(db: AsyncSession, request: DeviceRegisterRequest) -> DeviceRegisterResponse:
    install_id_hash = hash_identifier(request.install_id)
    result = await db.execute(select(Device).where(Device.install_id_hash == install_id_hash))
    device = result.scalar_one_or_none()
    now = datetime.now(timezone.utc)

    if device is None:
        device = Device(
            install_id_hash=install_id_hash,
            role=request.role,
            display_name=request.display_name,
            fcm_token=request.fcm_token,
            last_seen_at=now,
        )
        db.add(device)
        await db.flush()
    else:
        device.role = request.role
        device.display_name = request.display_name
        device.fcm_token = request.fcm_token
        device.last_seen_at = now

    token = create_device_access_token(device.id)
    device.token_hash = hash_identifier(token)
    await db.commit()
    await db.refresh(device)

    return DeviceRegisterResponse(
        device_id=str(device.id),
        role=device.role,
        display_name=device.display_name,
        device_access_token=token,
    )


async def update_device_fcm_token(db: AsyncSession, device: Device, fcm_token: str) -> None:
    device.fcm_token = fcm_token
    device.last_seen_at = datetime.now(timezone.utc)
    await db.commit()


async def update_fcm_token(db: AsyncSession, user: User, fcm_token: str) -> None:
    user.fcm_token = fcm_token
    await db.commit()
