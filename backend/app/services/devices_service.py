from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User


async def update_fcm_token(db: AsyncSession, user: User, fcm_token: str) -> None:
    user.fcm_token = fcm_token
    await db.commit()
