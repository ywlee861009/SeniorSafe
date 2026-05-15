# SeniorSafe 티켓

최종 점검일: 2026-05-15

## 현재 진행상황

SeniorSafe는 현재 MVP 구현 단계입니다.

- Android 앱은 `./gradlew :app:assembleDebug` 기준 빌드에 성공했습니다.
- 백엔드는 `python3 -m compileall backend/app` 기준 Python 모듈 문법/임포트 검증을 통과했습니다.
- 핵심 제품 흐름은 코드상 구현되어 있습니다: 인증, 어르신/보호자 모드, 페어링, 낙상 감지 서비스, 낙상 이벤트 보고, 낙상 이력, FCM 알림 연결부, Docker Compose 배포 골격.
- 백엔드 자동화 테스트는 `backend/.venv/bin/python -m pytest` 기준 통과했습니다.
- 운영 전환을 위해 낙상 알림/취소 플로우 정합성, Android 실기기 검증, Firebase 설정, 토큰/세션 처리, 보안, HTTPS, 백업, 센서 오탐 튜닝이 남아 있습니다.

## 폴더 기준

- `todo/`: 앞으로 해야 할 작업입니다. 바로 착수할 수 있도록 범위와 완료 조건을 적었습니다.
- `done/`: 이번 분석에서 구현 완료로 확인한 작업입니다.

## 추천 우선순위

1. `todo/002-fall-alert-flow-correctness.md`
2. `todo/003-android-runtime-firebase-config.md`
3. `todo/004-auth-session-hardening.md`
4. `todo/005-production-deployment-hardening.md`
5. `todo/006-sensor-algorithm-validation.md`
