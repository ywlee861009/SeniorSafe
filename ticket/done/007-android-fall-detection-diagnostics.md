# 완료-007 Android 낙상 감지 런타임 분리 및 MVP 진단 로그

## 상태

MVP 디버깅 기반 완료.

## 구현된 내용

- `feature:mvp`에 있던 낙상 감지 핵심 로직을 `core:fall-detection` 모듈로 분리했다.
- `MvpFallDetectionManager`, `MvpFallDetectionService`, `MvpFallEventBus`를 각각 `FallDetectionManager`, `FallDetectionService`, `FallEventBus`로 정리했다.
- foreground service, sensor 권한, accelerometer feature 선언을 `core:fall-detection` manifest로 이동했다.
- MVP용 진단 로그를 `core:diagnostics` 모듈로 분리했다.
- 진단 로그는 Room DB(`seniorsafe_diagnostics.db`)에 저장하고 최근 500개를 유지한다.
- MVP 대시보드는 작은 서비스 제어 영역과 Runtime Log 콘솔 중심 UI로 변경했다.
- 서비스 시작/중지 요청, service lifecycle, wake lock, foreground 시작, sensor sample, 상태 전이, 낙상 이벤트 발행/수신, 알림 카운트다운을 로그로 남긴다.
- 서비스 실행 상태 UI는 service heartbeat 기준으로 동기화한다.
  - service가 1초마다 heartbeat를 기록한다.
  - 최근 5초 안에 heartbeat가 없으면 UI는 `보호 꺼짐`으로 표시한다.
  - OS가 service를 강제 종료해 `onDestroy`가 호출되지 않아도 stale heartbeat로 상태가 내려간다.
- `enableEdgeToEdge()` 상태에서 root Compose에 status bar/navigation bar padding을 적용했다.

## 확인 근거

- `android/core/fall-detection/`
- `android/core/diagnostics/`
- `android/feature/mvp/src/main/java/com/seniorsafe/feature/mvp/MvpDashboardScreen.kt`
- `android/feature/mvp/src/main/java/com/seniorsafe/feature/mvp/MvpDashboardViewModel.kt`
- `android/app/src/main/java/com/seniorsafe/MainActivity.kt`
- 2026-05-16 기준 `cd android && ./gradlew assembleDebug` 통과.

## 남은 메모

- `feature:senior`에는 피벗 이전 낙상 감지 서비스 구현이 남아 있어, 이후 `core:fall-detection`으로 통합해야 한다.
- 현재 알고리즘 threshold는 검증 완료 상태가 아니다. 특히 정지 판단은 magnitude 절대값이 아니라 변화량/분산 기반으로 재검토해야 한다.
- MVP Runtime Log는 진단 목적이며, 제품화 전에는 별도 debug flag 또는 build variant로 격리해야 한다.
