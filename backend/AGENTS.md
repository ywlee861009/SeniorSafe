# Backend Project Instructions

This file is the Codex instruction file for `backend/`. It mirrors `backend/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`backend/` is a FastAPI service for device registration, pairing, and activity-monitoring APIs. The implementation is organized around `app/routers`, `app/services`, `app/models`, `app/schemas`, and `app/core`.

Routers should stay thin and delegate business rules to services. SQLAlchemy models belong in `app/models`, Pydantic request/response contracts belong in `app/schemas`, and security/database/configuration helpers belong in `app/core`.

Tests live in `tests/` and currently focus on device and pairing behavior with mocked external effects. When API shape changes, update `docs/api-spec.md` first, then align schemas, services, routers, migrations, and tests.

## Python and FastAPI

- Use type hints for function signatures and variables.
- Follow PEP 8 naming and formatting.
- Use FastAPI dependency injection for database sessions and security.

## Database and Alembic

- Define models in `app/models/`.
- Use Alembic for all schema migrations.
- Do not use `Base.metadata.create_all()` directly in production paths.

## Testing

- Use `pytest` for unit and integration tests.
- Mock external services such as FCM in unit tests.
- Use a separate test database or the existing test harness for integration tests.
