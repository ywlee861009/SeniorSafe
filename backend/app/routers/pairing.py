from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.schemas.pairing import (
    ConnectRequest,
    ConnectResponse,
    PairingCodeResponse,
    PairingListResponse,
)
from app.services.pairing_service import connect_senior, create_pairing_code, list_pairings

router = APIRouter()


@router.get("/code", response_model=PairingCodeResponse)
async def get_pairing_code(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> PairingCodeResponse:
    return await create_pairing_code(db, current_user)


@router.post("/connect", response_model=ConnectResponse)
async def connect(
    request: ConnectRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> ConnectResponse:
    return await connect_senior(db, current_user, request.code)


@router.get("/list", response_model=PairingListResponse)
async def get_pairing_list(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> PairingListResponse:
    return await list_pairings(db, current_user)
