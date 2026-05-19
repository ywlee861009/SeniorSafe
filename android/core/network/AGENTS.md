# Network Module Instructions

This file is the Codex instruction file for `android/core/network/`. It mirrors `android/core/network/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/core/network/` owns Retrofit API declarations and network object provisioning. Keep HTTP endpoint definitions in `ApiService.kt` and DI setup in `di/NetworkModule.kt`.

This module should expose low-level network contracts, not repository behavior or screen-oriented state. API changes should follow `docs/api-spec.md` first, then update DTO/model usage and downstream repositories.

## API Definitions

- Define all endpoints in `ApiService.kt`.
- Use `suspend` functions for all network calls.
- For authenticated MVP calls, use an explicit `@Header("Authorization") token: String` parameter.

## Setup

- Provide Retrofit instances through Hilt `NetworkModule`.
- Use Gson for JSON parsing.
- Include `HttpLoggingInterceptor` in debug builds.
