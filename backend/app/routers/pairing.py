from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_device, get_current_user
from app.models.device import Device
from app.models.user import User
from app.schemas.pairing import (
    ClaimPairingRequest,
    ClaimPairingResponse,
    ConnectRequest,
    ConnectResponse,
    DevicePairingListResponse,
    DisconnectPairingResponse,
    PairingCodeResponse,
    PairingListResponse,
)
from app.services.pairing_service import (
    claim_device_pairing_code,
    connect_senior,
    create_device_pairing_code,
    create_pairing_code,
    disconnect_device_pairing,
    list_device_pairings,
    list_pairings,
)

router = APIRouter()
pairings_router = APIRouter()


@router.post("/codes", response_model=PairingCodeResponse)
async def post_pairing_code(
    current_device: Device = Depends(get_current_device),
    db: AsyncSession = Depends(get_db),
) -> PairingCodeResponse:
    return await create_device_pairing_code(db, current_device)


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


@pairings_router.post("", response_model=ClaimPairingResponse)
async def claim_pairing(
    request: ClaimPairingRequest,
    current_device: Device = Depends(get_current_device),
    db: AsyncSession = Depends(get_db),
) -> ClaimPairingResponse:
    return await claim_device_pairing_code(db, current_device, request.code)


@pairings_router.get("", response_model=DevicePairingListResponse)
async def get_device_pairings(
    current_device: Device = Depends(get_current_device),
    db: AsyncSession = Depends(get_db),
) -> DevicePairingListResponse:
    return await list_device_pairings(db, current_device)


@pairings_router.delete("/{pairing_id}", response_model=DisconnectPairingResponse)
async def disconnect_pairing(
    pairing_id: str,
    current_device: Device = Depends(get_current_device),
    db: AsyncSession = Depends(get_db),
) -> DisconnectPairingResponse:
    return await disconnect_device_pairing(db, current_device, pairing_id)
