from collections.abc import Iterator
from types import SimpleNamespace

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine
from sqlalchemy.pool import StaticPool

from app.core.database import get_db
from app.core.security import get_current_user
from app.main import app
from app.models import FallEvent, Pairing, PairingCode, User
from app.models.base import Base


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


@pytest.fixture
def db_client() -> Iterator[TestClient]:
    engine = create_async_engine(
        "sqlite+aiosqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    SessionLocal = async_sessionmaker(engine, expire_on_commit=False)

    async def override_get_db():
        async with SessionLocal() as session:
            yield session

    async def create_schema() -> None:
        async with engine.begin() as connection:
            await connection.run_sync(Base.metadata.create_all)

    async def drop_schema() -> None:
        async with engine.begin() as connection:
            await connection.run_sync(Base.metadata.drop_all)
        await engine.dispose()

    import asyncio

    # Ensure all model modules are imported before create_all sees metadata.
    _ = (FallEvent, Pairing, PairingCode, User)
    asyncio.run(create_schema())
    app.dependency_overrides.clear()
    app.dependency_overrides[get_db] = override_get_db

    with TestClient(app) as test_client:
        yield test_client

    app.dependency_overrides.clear()
    asyncio.run(drop_schema())
