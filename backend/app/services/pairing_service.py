import secrets
import string
from datetime import datetime, timedelta, timezone
from uuid import UUID

from fastapi import HTTPException
from sqlalchemy import desc, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.models.device import Device
from app.models.pairing import Pairing
from app.models.pairing_code import PairingCode
from app.schemas.pairing import (
    ClaimPairingResponse,
    DevicePairingItem,
    DevicePairingListResponse,
    DisconnectPairingResponse,
    PairingCodeResponse,
)

DEVICE_CODE_ALPHABET = string.digits


def _as_aware_utc(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value


async def create_device_pairing_code(db: AsyncSession, device: Device) -> PairingCodeResponse:
    if device.role != "senior":
        raise HTTPException(status_code=403, detail="Only senior devices can create pairing codes")

    await db.execute(
        PairingCode.__table__.delete().where(
            PairingCode.senior_device_id == device.id,
            PairingCode.consumed_at.is_(None),
        )
    )

    expires_at = datetime.now(timezone.utc) + timedelta(minutes=settings.pairing_code_expire_minutes)
    for _ in range(10):
        code = "".join(secrets.choice(DEVICE_CODE_ALPHABET) for _ in range(6))
        existing = await db.get(PairingCode, code)
        if existing is None:
            pairing_code = PairingCode(
                code=code,
                senior_device_id=device.id,
                expires_at=expires_at,
            )
            db.add(pairing_code)
            await db.commit()
            return PairingCodeResponse(
                code=code,
                expires_at=expires_at,
                expires_in_seconds=settings.pairing_code_expire_minutes * 60,
            )

    raise HTTPException(status_code=500, detail="Failed to generate pairing code")


async def claim_device_pairing_code(db: AsyncSession, guardian: Device, code: str) -> ClaimPairingResponse:
    if guardian.role != "guardian":
        raise HTTPException(status_code=403, detail="Only guardian devices can connect seniors")

    now = datetime.now(timezone.utc)
    result = await db.execute(
        select(PairingCode, Device)
        .join(Device, Device.id == PairingCode.senior_device_id)
        .where(PairingCode.code == code, PairingCode.consumed_at.is_(None))
    )
    row = result.one_or_none()
    if row is None:
        raise HTTPException(status_code=404, detail="Invalid pairing code")

    pairing_code, senior = row
    if _as_aware_utc(pairing_code.expires_at) <= now:
        raise HTTPException(status_code=404, detail="Expired pairing code")

    existing = await db.execute(
        select(Pairing).where(
            Pairing.senior_device_id == senior.id,
            Pairing.guardian_device_id == guardian.id,
        )
    )
    pairing = existing.scalar_one_or_none()
    if pairing is None:
        pairing = Pairing(
            senior_device_id=senior.id,
            guardian_device_id=guardian.id,
            status="active",
            active=True,
        )
        db.add(pairing)
        await db.flush()
    else:
        pairing.status = "active"
        pairing.active = True
        pairing.disconnected_at = None

    pairing_code.consumed_at = now
    await db.commit()
    await db.refresh(pairing)

    return ClaimPairingResponse(
        pairing_id=str(pairing.id),
        senior_device_id=str(senior.id),
        senior_display_name=senior.display_name,
        created_at=pairing.created_at,
    )


async def list_device_pairings(db: AsyncSession, device: Device) -> DevicePairingListResponse:
    if device.role == "guardian":
        result = await db.execute(
            select(Pairing, Device)
            .join(Device, Device.id == Pairing.senior_device_id)
            .where(Pairing.guardian_device_id == device.id, Pairing.active.is_(True))
            .order_by(desc(Pairing.created_at))
        )
        return DevicePairingListResponse(
            pairings=[
                DevicePairingItem(
                    pairing_id=str(pairing.id),
                    senior_device_id=str(senior.id),
                    senior_display_name=senior.display_name,
                    last_seen_at=senior.last_seen_at,
                    active=pairing.active,
                )
                for pairing, senior in result.all()
            ]
        )

    result = await db.execute(
        select(Pairing, Device)
        .join(Device, Device.id == Pairing.guardian_device_id)
        .where(Pairing.senior_device_id == device.id, Pairing.active.is_(True))
        .order_by(desc(Pairing.created_at))
    )
    return DevicePairingListResponse(
        pairings=[
            DevicePairingItem(
                pairing_id=str(pairing.id),
                guardian_device_id=str(guardian.id),
                guardian_display_name=guardian.display_name,
                active=pairing.active,
            )
            for pairing, guardian in result.all()
        ]
    )


async def disconnect_device_pairing(
    db: AsyncSession,
    device: Device,
    pairing_id: str,
) -> DisconnectPairingResponse:
    try:
        parsed_pairing_id = UUID(pairing_id)
    except ValueError:
        raise HTTPException(status_code=404, detail="Pairing not found")

    result = await db.execute(select(Pairing).where(Pairing.id == parsed_pairing_id))
    pairing = result.scalar_one_or_none()
    if pairing is None:
        raise HTTPException(status_code=404, detail="Pairing not found")

    if device.id not in (pairing.senior_device_id, pairing.guardian_device_id):
        raise HTTPException(status_code=404, detail="Pairing not found")

    pairing.active = False
    pairing.status = "disconnected"
    pairing.disconnected_at = datetime.now(timezone.utc)
    await db.commit()

    return DisconnectPairingResponse(pairing_id=str(pairing.id), active=pairing.active)
