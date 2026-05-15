from datetime import datetime
from typing import Literal, Optional

from pydantic import BaseModel, Field


class DeviceRegisterRequest(BaseModel):
    install_id: str = Field(min_length=8, max_length=255)
    role: Literal["senior", "guardian"]
    display_name: str = Field(min_length=1, max_length=100)
    fcm_token: Optional[str] = Field(default=None, min_length=1)


class DeviceRegisterResponse(BaseModel):
    device_id: str
    role: Literal["senior", "guardian"]
    display_name: str
    device_access_token: str


class DeviceMeResponse(BaseModel):
    device_id: str
    role: Literal["senior", "guardian"]
    display_name: str
    created_at: datetime
    last_seen_at: datetime


class FcmTokenRequest(BaseModel):
    fcm_token: str = Field(min_length=1)


class MessageResponse(BaseModel):
    message: str
