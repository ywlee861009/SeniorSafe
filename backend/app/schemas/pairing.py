from datetime import datetime

from pydantic import BaseModel, Field


class PairingCodeResponse(BaseModel):
    code: str
    expires_at: datetime


class ConnectRequest(BaseModel):
    code: str = Field(min_length=6, max_length=6)


class ConnectResponse(BaseModel):
    senior_id: str
    senior_name: str


class PairingItem(BaseModel):
    senior_id: str
    senior_name: str
    service_active: bool
    last_fall_at: datetime | None


class PairingListResponse(BaseModel):
    pairings: list[PairingItem]
