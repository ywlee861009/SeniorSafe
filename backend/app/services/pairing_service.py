import secrets
import string
from datetime import datetime, timedelta, timezone

from fastapi import HTTPException
from sqlalchemy import and_, desc, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.models.fall_event import FallEvent
from app.models.pairing import Pairing
from app.models.pairing_code import PairingCode
from app.models.user import User
from app.schemas.pairing import ConnectResponse, PairingCodeResponse, PairingItem, PairingListResponse

CODE_ALPHABET = string.ascii_uppercase + string.digits


async def create_pairing_code(db: AsyncSession, user: User) -> PairingCodeResponse:
    if user.user_type != "senior":
        raise HTTPException(status_code=403, detail="Only senior users can create pairing codes")

    await db.execute(
        PairingCode.__table__.delete().where(PairingCode.senior_id == user.id)
    )

    expires_at = datetime.now(timezone.utc) + timedelta(minutes=settings.pairing_code_expire_minutes)
    for _ in range(10):
        code = "".join(secrets.choice(CODE_ALPHABET) for _ in range(6))
        existing = await db.get(PairingCode, code)
        if existing is None:
            pairing_code = PairingCode(code=code, senior_id=user.id, expires_at=expires_at)
            db.add(pairing_code)
            await db.commit()
            return PairingCodeResponse(code=code, expires_at=expires_at)

    raise HTTPException(status_code=500, detail="Failed to generate pairing code")


async def connect_senior(db: AsyncSession, guardian: User, code: str) -> ConnectResponse:
    if guardian.user_type != "guardian":
        raise HTTPException(status_code=403, detail="Only guardian users can connect seniors")

    result = await db.execute(
        select(PairingCode, User)
        .join(User, User.id == PairingCode.senior_id)
        .where(PairingCode.code == code.upper(), PairingCode.expires_at > datetime.now(timezone.utc))
    )
    row = result.one_or_none()
    if row is None:
        raise HTTPException(status_code=404, detail="Invalid or expired pairing code")

    pairing_code, senior = row
    existing = await db.execute(
        select(Pairing).where(
            and_(Pairing.senior_id == senior.id, Pairing.guardian_id == guardian.id)
        )
    )
    pairing = existing.scalar_one_or_none()
    if pairing is None:
        pairing = Pairing(senior_id=senior.id, guardian_id=guardian.id, status="active")
        db.add(pairing)
    else:
        pairing.status = "active"

    await db.delete(pairing_code)
    await db.commit()

    return ConnectResponse(senior_id=str(senior.id), senior_name=senior.name)


async def list_pairings(db: AsyncSession, guardian: User) -> PairingListResponse:
    if guardian.user_type != "guardian":
        raise HTTPException(status_code=403, detail="Only guardian users can list pairings")

    last_fall_subquery = (
        select(FallEvent.senior_id, func.max(FallEvent.detected_at).label("last_fall_at"))
        .group_by(FallEvent.senior_id)
        .subquery()
    )
    result = await db.execute(
        select(User, last_fall_subquery.c.last_fall_at)
        .join(Pairing, Pairing.senior_id == User.id)
        .outerjoin(last_fall_subquery, last_fall_subquery.c.senior_id == User.id)
        .where(Pairing.guardian_id == guardian.id, Pairing.status == "active")
        .order_by(desc(Pairing.created_at))
    )

    return PairingListResponse(
        pairings=[
            PairingItem(
                senior_id=str(senior.id),
                senior_name=senior.name,
                service_active=True,
                last_fall_at=last_fall_at,
            )
            for senior, last_fall_at in result.all()
        ]
    )
