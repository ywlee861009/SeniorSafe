from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.schemas.device import FcmTokenRequest, MessageResponse
from app.services.devices_service import update_fcm_token

router = APIRouter()


@router.put("/token", response_model=MessageResponse)
async def update_token(
    request: FcmTokenRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> MessageResponse:
    await update_fcm_token(db, current_user, request.fcm_token)
    return MessageResponse(message="FCM token updated")
