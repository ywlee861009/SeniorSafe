# Repository Guidelines

## Source Documents

This file is the Codex instruction file for the repository root. It mirrors the project-wide intent from `GEMINI.md` and adds repository-specific analysis for Codex work.

## Project Structure & Module Organization

SeniorSafe is an Android senior safety app with a FastAPI backend. The current MVP defers fall detection and focuses on unlock activity monitoring: if a senior phone has no unlock record for N days, the backend sends a push notification to guardians.

- `backend/`: FastAPI app, SQLAlchemy async models, Alembic migrations, and pytest tests.
- `android/`: Kotlin multi-module Android app. `app/` owns `MainActivity` and navigation, `core/` contains shared model/network/data/ui modules, and `feature/` contains login, senior, guardian, and MVP screens.
- `docs/`: product, deployment, and API documentation.
- `design/`: UI design files and design notes.
- `ticket/`: completed and remaining work tickets.
- `nginx/` and `docker-compose.yml`: local deployment stack.

## Folder Analysis

The repository is split into an Android client and a FastAPI backend. Treat API changes as contract-first work: update `docs/api-spec.md` before changing either side of the implementation.

The Android app uses a multi-module structure with `app`, `core`, and `feature` layers. Keep app-wide navigation and application wiring in `android/app`, reusable model/network/data/UI concerns in `android/core`, and user-flow screens in `android/feature`.

The backend uses thin FastAPI routers, service-layer business logic, SQLAlchemy async models, Alembic migrations, and pytest coverage. Do not bypass the service/model boundaries for request handling or persistence.

## Planning & Tickets

Use `ticket/` as the source of truth for current work tickets. Check `ticket/README.md` for the active roadmap, `ticket/todo/` for remaining work, and `ticket/done/` for completed or historical tickets.

## Build, Test, and Development Commands

Backend:

```bash
docker compose up -d --build        # Run PostgreSQL, backend, and Nginx
docker compose logs -f backend      # Tail backend logs
cd backend && .venv/bin/python -m pytest
```

Install backend test dependencies with:

```bash
cd backend
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-dev.txt
```

Android:

```bash
cd android
./gradlew assembleDebug             # Build debug APK
./gradlew test                      # Run unit tests
./gradlew clean assembleDebug       # Clean rebuild
```

## Coding Style & Naming Conventions

Backend code uses `snake_case.py`, `snake_case` functions, and `PascalCase` classes. Keep routers thin: request handling belongs in `app/routers`, business logic in `app/services`, persistence in `app/models`, and Pydantic contracts in `app/schemas`.

Android code uses Kotlin conventions: `PascalCase` classes, `camelCase` functions/variables, and `UPPER_SNAKE_CASE` constants. Route constants should end with `Route`, for example `seniorHomeRoute`.

## Testing Guidelines

Backend tests use `pytest` and FastAPI `TestClient`. DB integration tests run against in-memory SQLite, so no backend server is required. Name tests `test_*.py` and cover device registration, pairing restrictions, unlock activity persistence, service event persistence, inactivity alert batches, and Firebase/FCM side effects with mocks.

Run:

```bash
cd backend && .venv/bin/python -m pytest
```

## Commit & Pull Request Guidelines

Commit history follows conventional commits, often in Korean: `feat:`, `fix:`, `test:`, `docs:`, `chore:`. Keep the subject under 72 characters and scoped to one logical change.

Pull requests should include a short summary, test results, linked ticket or issue when relevant, and screenshots for Android UI changes. Do not include secrets, `.env`, Firebase credentials, or generated build output.

## Security & Configuration Tips

Use `backend/.env.example` as the template for local secrets. Real Firebase files and production credentials must stay out of git. Production deployment should avoid default `SECRET_KEY` and database passwords.
