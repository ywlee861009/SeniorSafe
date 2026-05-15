from datetime import datetime

from pydantic import BaseModel


class FallEventRequest(BaseModel):
    detected_at: datetime


class FallCancelRequest(BaseModel):
    event_id: str


class FallEventResponse(BaseModel):
    event_id: str
    status: str


class FallHistoryItem(BaseModel):
    id: str
    detected_at: datetime
    cancelled: bool


class FallHistoryResponse(BaseModel):
    events: list[FallHistoryItem]
