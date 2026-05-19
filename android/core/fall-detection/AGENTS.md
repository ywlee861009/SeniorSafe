# Fall Detection Module Instructions

This file is the Codex instruction file for `android/core/fall-detection/`. It mirrors `android/core/fall-detection/GEMINI.md` and adds folder-specific analysis.

## Folder Analysis

`android/core/fall-detection/` owns fall-detection service infrastructure: service lifecycle, service state storage, event bus, and manager APIs. The current MVP prioritizes unlock/activity monitoring, so avoid expanding fall-detection scope unless the active ticket explicitly calls for it.

Keep sensor and foreground-service behavior battery-aware and lifecycle-safe. Public APIs should be small enough for feature modules to start, stop, and observe detection state without taking ownership of sensor internals.

## Algorithm

- Process sensor data efficiently to minimize battery use.
- Use `SensorManager` with an appropriate sampling rate such as `SENSOR_DELAY_UI`.

## Service Lifecycle

- `FallDetectionService` must run as a foreground service.
- Manage notifications so the system is less likely to terminate the service unexpectedly.
- Be careful with wake lock behavior so detection can continue while the screen is off.
