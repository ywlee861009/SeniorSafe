from uuid import UUID

from fastapi import HTTPException
from sqlalchemy import desc, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.fall_event import FallEvent
from app.models.pairing import Pairing
from app.models.user import User
from app.schemas.fall import FallEventResponse, FallHistoryItem, FallHistoryResponse
from app.services.fcm_service import send_fall_alert


async def report_fall_event(db: AsyncSession, senior: User, detected_at) -> FallEventResponse:
    if senior.user_type != "senior":
        raise HTTPException(status_code=403, detail="Only senior users can report fall events")

    event = FallEvent(senior_id=senior.id, detected_at=detected_at)
    db.add(event)
    await db.flush()

    result = await db.execute(
        select(User.fcm_token)
        .join(Pairing, Pairing.guardian_id == User.id)
        .where(Pairing.senior_id == senior.id, Pairing.status == "active", User.fcm_token.is_not(None))
    )
    tokens = [token for token in result.scalars().all() if token]
    await db.commit()
    await db.refresh(event)

    await send_fall_alert(tokens, senior.name)
    return FallEventResponse(event_id=str(event.id), status="reported")


async def cancel_fall_event(db: AsyncSession, senior: User, event_id: str) -> FallEventResponse:
    if senior.user_type != "senior":
        raise HTTPException(status_code=403, detail="Only senior users can cancel fall events")

    try:
        parsed_event_id = UUID(event_id)
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid event_id")

    event = await db.get(FallEvent, parsed_event_id)
    if event is None or event.senior_id != senior.id:
        raise HTTPException(status_code=404, detail="Fall event not found")

    event.cancelled = True
    await db.commit()
    return FallEventResponse(event_id=str(event.id), status="cancelled")


async def get_fall_history(db: AsyncSession, guardian: User, senior_id: str) -> FallHistoryResponse:
    if guardian.user_type != "guardian":
        raise HTTPException(status_code=403, detail="Only guardian users can view fall history")

    try:
        parsed_senior_id = UUID(senior_id)
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid seniorId")

    pairing = await db.execute(
        select(Pairing).where(
            Pairing.guardian_id == guardian.id,
            Pairing.senior_id == parsed_senior_id,
            Pairing.status == "active",
        )
    )
    if pairing.scalar_one_or_none() is None:
        raise HTTPException(status_code=404, detail="Pairing not found")

    result = await db.execute(
        select(FallEvent)
        .where(FallEvent.senior_id == parsed_senior_id)
        .order_by(desc(FallEvent.detected_at))
        .limit(100)
    )
    events = result.scalars().all()
    return FallHistoryResponse(
        events=[
            FallHistoryItem(id=str(event.id), detected_at=event.detected_at, cancelled=event.cancelled)
            for event in events
        ]
    )
