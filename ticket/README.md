# SeniorSafe 티켓

최종 점검일: 2026-05-16

## 기획 피벗

SeniorSafe는 로그인/회원가입 중심 MVP에서 로그인 없는 기기 페어링 MVP로 방향을 변경한다.

새 기본 흐름:

```text
앱 설치
→ 보호자 / 어르신 모드 선택
→ 어르신은 연결 코드 생성
→ 보호자는 연결 코드 입력
→ 페어링 완료
→ 낙상 감지와 보호자 알림 사용
```

사용자는 계정을 만들지 않는다. 서버는 사용자 계정 대신 설치된 앱 기기와 페어링 관계를 기준으로 동작한다.

## 현재 진행상황

- Android 멀티 모듈 앱, 어르신/보호자 MVP 화면, 낙상 감지 서비스, 백엔드 API, Docker Compose 골격은 존재한다.
- 기존 구현에는 로그인, 사용자 role, auth token, user 기반 보호자-어르신 관계가 포함되어 있다.
- 새 방향에서는 기존 인증/사용자 흐름을 걷어내거나 호환 계층으로 축소해야 한다.
- 낙상 이벤트, 이력, FCM 발송은 device/pairing 기준으로 다시 정리해야 한다.

## 폴더 기준

- `todo/`: 새 기획 기준으로 앞으로 해야 할 작업이다.
- `done/`: 피벗 이전 구현 완료 기록이다. 그대로 참고하되, 새 제품 계약의 완료 상태로 보지는 않는다.

## 추천 우선순위

1. `todo/002-backend-device-pairing-model.md`
2. `todo/003-android-onboarding-pairing-flow.md`
3. `todo/004-fall-event-pairing-notification-flow.md`
4. `todo/005-firebase-runtime-config.md`
5. `todo/006-sessionless-device-security.md`
6. `todo/007-guardian-monitoring-pairing-features.md`
7. `todo/008-sensor-algorithm-validation.md`
8. `todo/009-local-dev-and-prod-deployment.md`
9. `todo/010-ci-quality-gates.md`

## 핵심 결정사항

- 계정 로그인은 MVP 범위에서 제거한다.
- 앱 설치 단위의 `device`를 서버 식별 단위로 사용한다.
- 페어링 코드는 짧은 만료 시간을 가진 일회성 코드로 운영한다.
- 앱 삭제 후 재설치는 새 기기로 취급한다.
- 데이터 복구, 다중 기기 계정 동기화, 비밀번호 찾기는 MVP 이후로 미룬다.
