import uuid
from datetime import datetime
from typing import Optional

from sqlalchemy import Boolean, CheckConstraint, DateTime, ForeignKey, String, UniqueConstraint, Uuid, func
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base


class Pairing(Base):
    __tablename__ = "pairings"
    __table_args__ = (
        CheckConstraint("status in ('pending', 'active', 'disconnected')", name="ck_pairings_status"),
        UniqueConstraint("senior_id", "guardian_id", name="uq_pairings_senior_guardian"),
        UniqueConstraint("senior_device_id", "guardian_device_id", name="uq_pairings_senior_guardian_device"),
    )

    id: Mapped[uuid.UUID] = mapped_column(Uuid(as_uuid=True), primary_key=True, default=uuid.uuid4)
    senior_id: Mapped[Optional[uuid.UUID]] = mapped_column(Uuid(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=True)
    guardian_id: Mapped[Optional[uuid.UUID]] = mapped_column(Uuid(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=True)
    senior_device_id: Mapped[Optional[uuid.UUID]] = mapped_column(Uuid(as_uuid=True), ForeignKey("devices.id", ondelete="CASCADE"), nullable=True, index=True)
    guardian_device_id: Mapped[Optional[uuid.UUID]] = mapped_column(Uuid(as_uuid=True), ForeignKey("devices.id", ondelete="CASCADE"), nullable=True, index=True)
    status: Mapped[str] = mapped_column(String(20), default="active", server_default="active")
    active: Mapped[bool] = mapped_column(Boolean, default=True, server_default="true")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    disconnected_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
