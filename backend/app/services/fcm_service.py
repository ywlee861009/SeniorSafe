from pathlib import Path

import firebase_admin
from firebase_admin import credentials, messaging

from app.core.config import settings


def _ensure_firebase_app() -> bool:
    if firebase_admin._apps:
        return True

    credentials_path = Path(settings.firebase_credentials_path)
    if not credentials_path.is_file():
        return False

    firebase_admin.initialize_app(credentials.Certificate(str(credentials_path)))
    return True


async def send_fall_alert(tokens: list[str], senior_name: str) -> None:
    if not tokens or not _ensure_firebase_app():
        return

    message = messaging.MulticastMessage(
        tokens=tokens,
        notification=messaging.Notification(
            title="낙상 감지",
            body=f"{senior_name}님 낙상이 감지되었습니다.",
        ),
        data={"type": "fall_alert"},
    )
    messaging.send_each_for_multicast(message)
