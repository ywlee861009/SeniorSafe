from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.schemas.fall import (
    FallCancelRequest,
    FallEventRequest,
    FallEventResponse,
    FallHistoryResponse,
)
from app.services.fall_service import cancel_fall_event, get_fall_history, report_fall_event

router = APIRouter()


@router.post("/event", response_model=FallEventResponse)
async def create_fall_event(
    request: FallEventRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> FallEventResponse:
    return await report_fall_event(db, current_user, request.detected_at)


@router.post("/cancel", response_model=FallEventResponse)
async def cancel_event(
    request: FallCancelRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> FallEventResponse:
    return await cancel_fall_event(db, current_user, request.event_id)


@router.get("/history/{senior_id}", response_model=FallHistoryResponse)
async def history(
    senior_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> FallHistoryResponse:
    return await get_fall_history(db, current_user, senior_id)
