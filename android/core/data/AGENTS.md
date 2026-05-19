# Data Module Instructions

This file is the Codex instruction file for `android/core/data/`. It mirrors `android/core/data/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/core/data/` owns repositories that coordinate network calls, local data sources, and mapping between network/domain models. Feature modules should consume this module instead of directly calling `core:network` or local stores.

Repository APIs should describe app/domain behavior rather than raw transport details. Keep implementation details internal where possible and map network failures into domain-specific results or clear exceptions.

## Repository Pattern

- All data access should go through repositories.
- Repositories should map between network DTOs and domain models.
- Keep implementations `internal` when possible and expose only interfaces or repository classes that callers need.

## Synchronization and State

- Use `Flow` when exposing reactive streams from local sources such as DataStore or Room.
- Handle network errors gracefully and map them to domain-specific results or exceptions.
