# Core Module Instructions

This file is the Codex instruction file for `android/core/`. It mirrors `android/core/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/core/` contains reusable Android modules that support the app without owning a specific user journey. Keep these modules feature-neutral and avoid references to feature modules.

Current core modules include model definitions, Retrofit networking, DataStore-backed local persistence, repository/data mapping, diagnostics, utility code, activity monitoring, fall detection, and shared UI. Each module should keep a narrow responsibility and expose only the API needed by app or feature modules.

## Purpose

Provide reusable, feature-neutral components and data layers.

## Constraints

- Do not place logic that is specific only to the Senior or Guardian flow in core modules.
- Keep each core module isolated with one clear responsibility.

## Main Submodules

- `core:model`: pure Kotlin data classes, DTOs, and domain models.
- `core:network`: Retrofit services and API configuration.
- `core:data`: repositories and data synchronization logic.
- `core:ui`: shared design system, theme, and reusable UI components.
- `core:util`: general-purpose utility functions.
- `core:datastore`: local DataStore-backed token and device persistence.
- `core:activity`: unlock/activity monitoring service and local activity records.
- `core:fall-detection`: fall detection service and state/event plumbing.
- `core:diagnostics`: local diagnostics logging and database support.
