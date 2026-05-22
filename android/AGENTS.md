# Android Project Instructions

This file is the Codex instruction file for `android/`. It mirrors `android/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/` is the Kotlin multi-module client. The included modules are `:app`, `:core:model`, `:core:network`, `:core:datastore`, `:core:data`, `:core:diagnostics`, `:core:util`, `:core:activity`, `:core:ui`, `:feature:login`, `:feature:senior`, `:feature:guardian`, and `:feature:mvp`.

`app` owns the Android entry point, Hilt application setup, top-level navigation, and application resources. `core:*` modules own reusable concerns and must stay independent from specific user flows. `feature:*` modules own screens, view models, and per-flow navigation hooks.

Build configuration is centralized through `gradle/libs.versions.toml` and convention plugins under `build-logic`. Add dependencies through version catalogs and existing convention plugins before adding one-off module configuration.

## Module Structure

- `:app`: entry point, DI application setup, and global navigation.
- `:core:*`: shared logic, data, services, utilities, and UI components. Do not put feature-specific workflow logic here.
- `:feature:*`: individual user flows. Feature modules should not depend on other feature modules.

## UI and Compose

- Use `SeniorSafeTheme` from `:core:ui`.
- Prefer Material 3 components.
- Use `ViewModel`, `StateFlow`, and `collectAsStateWithLifecycle` for screen state.

## Dependency Injection

- Use Hilt for DI.
- Bind interfaces in `@Module` classes with `@Binds` or `@Provides`.

## Build System

- Manage versions in `gradle/libs.versions.toml`.
- Use convention plugins in `build-logic` for consistent module setup.
