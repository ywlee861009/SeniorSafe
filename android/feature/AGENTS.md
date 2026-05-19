# Feature Module Instructions

This file is the Codex instruction file for `android/feature/`. It mirrors `android/feature/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/feature/` contains user-facing flows: login/register, senior screens, guardian screens, and the MVP dashboard. Each feature module should own its screens, view models, and navigation extension points for that flow.

Feature modules should communicate outward through callbacks and shared core APIs, not by depending on sibling feature modules. Cross-feature navigation belongs in `:app` through the app-level navigation host.

## Purpose

Implement specific user screens and workflows.

## Structure

- `navigation/`: `NavGraphBuilder` extension functions and route constants.
- `[Feature]Screen.kt`: main UI entry point for a screen.
- `[Feature]ViewModel.kt`: screen state and logic.

## Rules

- Feature modules must remain independent from each other.
- The `app` module coordinates movement between features through callbacks.
- Access data only through repositories from `core:data`.
