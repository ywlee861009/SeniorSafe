from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_device, get_current_user
from app.models.device import Device
from app.models.user import User
from app.schemas.device import (
    DeviceMeResponse,
    DeviceRegisterRequest,
    DeviceRegisterResponse,
    FcmTokenRequest,
    MessageResponse,
)
from app.services.devices_service import register_device, update_device_fcm_token, update_fcm_token

router = APIRouter()


@router.post("/register", response_model=DeviceRegisterResponse)
async def register(
    request: DeviceRegisterRequest,
    db: AsyncSession = Depends(get_db),
) -> DeviceRegisterResponse:
    return await register_device(db, request)


@router.get("/me", response_model=DeviceMeResponse)
async def me(current_device: Device = Depends(get_current_device)) -> DeviceMeResponse:
    return DeviceMeResponse(
        device_id=str(current_device.id),
        role=current_device.role,
        display_name=current_device.display_name,
        created_at=current_device.created_at,
        last_seen_at=current_device.last_seen_at,
    )


@router.put("/fcm-token", response_model=MessageResponse)
async def update_device_token(
    request: FcmTokenRequest,
    current_device: Device = Depends(get_current_device),
    db: AsyncSession = Depends(get_db),
) -> MessageResponse:
    await update_device_fcm_token(db, current_device, request.fcm_token)
    return MessageResponse(message="FCM token updated")


@router.put("/token", response_model=MessageResponse)
async def update_token(
    request: FcmTokenRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> MessageResponse:
    await update_fcm_token(db, current_user, request.fcm_token)
    return MessageResponse(message="FCM token updated")
