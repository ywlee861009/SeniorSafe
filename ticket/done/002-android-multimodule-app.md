# 완료-002 Android 멀티모듈 앱 골격

## 상태

과거 MVP 기준 완료. 현재 잠금해제 미사용 알림 MVP 기준에서는 로그인 시작 라우팅을 역할 선택/기기 등록 라우팅으로 대체해야 함.

## 구현된 내용

- `app`, `core/*`, `feature/*`로 나뉜 Gradle 멀티모듈 구조.
- Android application/library, Compose, Hilt, feature 모듈용 Convention Plugin.
- Single Activity + Compose 기반 앱 구조.
- `AppNavHost` 기반 화면 라우팅.
- 저장된 토큰/사용자 타입에 따라 로그인, 어르신 홈, 보호자 홈으로 시작 화면 결정.
- 공통 model, network, datastore, data repository, UI 모듈.

## 확인 근거

- `android/settings.gradle.kts`
- `android/build-logic/convention/`
- `android/app/src/main/java/com/seniorsafe/MainActivity.kt`
- `android/app/src/main/java/com/seniorsafe/navigation/AppNavHost.kt`
- 2026-05-15 기준 `./gradlew :app:assembleDebug` 통과.

## 현재 기준 메모

2026-05-16 이후 앱 시작 화면은 로그인 여부가 아니라 로컬 device 등록/역할/페어링 상태를 기준으로 결정한다.
