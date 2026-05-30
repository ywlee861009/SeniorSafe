# Repository Guidelines

## Source Documents

This file is the Codex instruction file for the repository root. It mirrors the project-wide intent from `GEMINI.md` and adds repository-specific analysis for Codex work.

## Project Structure & Module Organization

SeniorSafe is an Android senior safety app with a Supabase backend (Edge Functions + PostgreSQL). The current MVP defers fall detection and focuses on unlock activity monitoring: if a senior phone has no unlock record for N days, the backend sends a push notification to guardians.

- `supabase/`: Edge Functions (Deno/TypeScript), SQL migrations, pg_cron config, and Deno tests.
- `android/`: Kotlin multi-module Android app. `app/` owns `MainActivity` and navigation, `core/` contains shared model/network/data/ui modules, and `feature/` contains login, senior, guardian, and MVP screens.
- `docs/`: product, deployment, and API documentation.
- `design/`: UI design files and design notes.
- `ticket/`: completed and remaining work tickets.

## Folder Analysis

The repository is split into an Android client and a Supabase backend. Treat API changes as contract-first work: update `docs/api-spec.md` before changing either side of the implementation.

The Android app uses a multi-module structure with `app`, `core`, and `feature` layers. Keep app-wide navigation and application wiring in `android/app`, reusable model/network/data/UI concerns in `android/core`, and user-flow screens in `android/feature`.

The backend uses Supabase Edge Functions (Deno/TypeScript) with each function in its own directory under `supabase/functions/`. Shared utilities (auth, CORS, Supabase client, JWT) live in `supabase/functions/_shared/`. Database schema is managed via SQL migrations in `supabase/migrations/`. RLS policies enforce security at the database level.

## Planning & Tickets

Use `ticket/` as the source of truth for current work tickets. Check `ticket/README.md` for the active roadmap, `ticket/todo/` for remaining work, and `ticket/done/` for completed or historical tickets.

## Build, Test, and Development Commands

Backend (Supabase):

```bash
supabase start                      # Local Supabase stack
supabase db push                    # Apply migrations
supabase functions serve            # Serve Edge Functions locally
cd supabase/functions && deno test --config=tests/deno.json tests/ --allow-env --allow-net
```

Android:

```bash
cd android
./gradlew assembleDebug             # Build debug APK
./gradlew test                      # Run unit tests
./gradlew clean assembleDebug       # Clean rebuild
```

## Coding Style & Naming Conventions

Backend code uses TypeScript with `kebab-case` directory names for Edge Functions, `camelCase` functions/variables, and shared utilities in `_shared/`. Each function exports a `handler` and calls `serve(handler)`.

Android code uses Kotlin conventions: `PascalCase` classes, `camelCase` functions/variables, and `UPPER_SNAKE_CASE` constants. Route constants should end with `Route`, for example `seniorHomeRoute`.

## Testing Guidelines

Backend tests use Deno's built-in test runner with a mock Supabase client. Tests cover device registration, pairing restrictions, activity event persistence, service event persistence, inactivity alert batches with deduplication, and role-based access control. 52 tests currently passing.

Run:

```bash
cd supabase/functions && deno test --config=tests/deno.json tests/ --allow-env --allow-net
```

## Commit & Pull Request Guidelines

Commit history follows conventional commits, often in Korean: `feat:`, `fix:`, `test:`, `docs:`, `chore:`. Keep the subject under 72 characters and scoped to one logical change.

Pull requests should include a short summary, test results, linked ticket or issue when relevant, and screenshots for Android UI changes. Do not include secrets, `.env`, Firebase credentials, or generated build output.

## Security & Configuration Tips

Use `supabase/.env.example` as the template for local secrets. Real Firebase server keys, Supabase service role keys, and JWT secrets must stay out of git. Use `supabase secrets set` for production deployments.
