from pydantic import BaseModel, Field


class FcmTokenRequest(BaseModel):
    fcm_token: str = Field(min_length=1)


class MessageResponse(BaseModel):
    message: str
