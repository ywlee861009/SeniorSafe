from collections.abc import Iterator
from types import SimpleNamespace

import pytest
from fastapi.testclient import TestClient

from app.core.database import get_db
from app.core.security import get_current_user
from app.main import app


@pytest.fixture
def client() -> Iterator[TestClient]:
    app.dependency_overrides.clear()
    app.dependency_overrides[get_db] = lambda: object()
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


def auth_as(user_type: str = "senior", user_id: str = "user-1", name: str = "테스트") -> None:
    user = SimpleNamespace(id=user_id, user_type=user_type, name=name, fcm_token=None)
    app.dependency_overrides[get_current_user] = lambda: user

