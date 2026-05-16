from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class PairingCodeResponse(BaseModel):
    code: str
    expires_at: datetime
    expires_in_seconds: Optional[int] = None


class ClaimPairingRequest(BaseModel):
    code: str = Field(min_length=6, max_length=6)


class ClaimPairingResponse(BaseModel):
    pairing_id: str
    senior_device_id: str
    senior_display_name: str
    created_at: datetime


class DevicePairingItem(BaseModel):
    pairing_id: str
    active: bool
    senior_device_id: Optional[str] = None
    senior_display_name: Optional[str] = None
    guardian_device_id: Optional[str] = None
    guardian_display_name: Optional[str] = None
    last_seen_at: Optional[datetime] = None
    last_fall_at: Optional[datetime] = None


class DevicePairingListResponse(BaseModel):
    pairings: list[DevicePairingItem]


class DisconnectPairingResponse(BaseModel):
    pairing_id: str
    active: bool
