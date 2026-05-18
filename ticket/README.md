# SeniorSafe 티켓

최종 점검일: 2026-05-18

## 기획 피벗

SeniorSafe는 로그인/회원가입 중심 MVP에서 로그인 없는 기기 페어링 MVP로 방향을 변경했다. 낙상 감지 제품화는 보류하고, 어르신 휴대폰의 잠금해제 활동 기록 기반 안부 확인 알림을 MVP 핵심으로 둔다.

새 기본 흐름:

```text
앱 설치
→ 보호자 / 어르신 모드 선택
→ 어르신은 연결 코드 생성
→ 보호자는 연결 코드 입력
→ 페어링 완료
→ 어르신 폰 잠금해제 활동 기록
→ 마지막 잠금해제 2일 경과 시 보호자 알림
```

사용자는 계정을 만들지 않는다. 서버는 사용자 계정 대신 설치된 앱 기기와 페어링 관계를 기준으로 동작한다.

## 현재 진행상황

### 백엔드

- User 기반 API(auth, fall, user-pairing) 전면 제거 완료. Device 토큰 인증 전용.
- 남은 API: health, devices(register/me/fcm-token), pairing(codes), pairings(claim/list/disconnect)
- 잠금해제 이벤트, 서비스 실행 내역, 미사용 알림 배치 API는 신규 구현 필요 (`todo/004`).
- DB 마이그레이션(users/fall_events 테이블 drop, pairing User FK 칼럼 drop) 별도 필요.

### Android

- 앱 진입점은 로컬 `DeviceDataStore` 기준 역할 선택/페어링 상태로 결정된다.
- 새 설치 후 로그인 없이 `RoleSelectScreen`에서 어르신/보호자 역할을 선택한다.
- 어르신 플로우는 서버 없이도 로컬 6자리 연결 코드 목업, 수동 페어링 완료, 어르신 홈 진입까지 동작한다.
- 어르신 홈은 낙상 중심 UI가 아니라 보호자 연결 상태, 활동 모니터링 상태, 최근 잠금해제 시각, 오늘 사용 기록 수를 표시한다.
- 어르신 홈에서 활동 모니터링 서비스를 시작/중지할 수 있다.
- 오늘의 글 화면과 매일 저녁 8시 로컬 알림 예약/탭 이동 구조가 추가됐다.
- 낙상 감지 런타임은 `core:fall-detection` 모듈에 남아 있으나 MVP 제품 흐름에서는 숨김/보류 상태다.
- MVP 디버깅 로그 저장은 `core:diagnostics` 모듈의 Room DB(`seniorsafe_diagnostics.db`)를 사용한다.
- Android 빌드 검증: 2026-05-18 기준 `cd android && ./gradlew assembleDebug` 통과.

## 완료된 최신 Android 티켓

- `done/011-android-local-device-identity.md`
- `done/012-android-role-select-entrypoint.md`
- `done/013-android-senior-pairing-mock-ui.md`
- `done/014-android-senior-home-ui.md`
- `done/015-android-today-message-ui.md`

## Android 남은 핵심 작업

- `todo/003`: 서버 기기 등록/실제 페어링 API 연동, 보호자 코드 입력 실패 상태, 기존 로그인 화면 접근 정리까지 포함한 통합 온보딩 완성.
- `todo/004`: 잠금해제 이벤트/서비스 이벤트 백엔드 업로드, 미전송 재시도, 미사용 알림 배치, 보호자 FCM 발송.
- `todo/005`: Firebase 런타임 설정, API base URL 환경 분리, FCM token 등록/갱신/권한 처리.
- `todo/006`: 로그인 없는 기기 단위 인증, rate limit, pairing/device 권한 정책 구현.
- `todo/007`: 보호자 화면을 device/pairing 기준 모니터링 화면으로 보강.
- `todo/008`: 낙상 감지 보류 상태와 재개 조건 문서화/검증.
- `todo/009`: local/dev/prod 배포 환경, HTTPS, batch 실행, 백업/복구 정리.
- `todo/010`: Android/Backend CI 품질 게이트 추가.

## 폴더 기준

- `todo/`: 새 기획 기준으로 앞으로 해야 할 작업이다.
- `done/`: 완료된 구현 또는 과거 완료 기록이다. 피벗 이전 티켓은 참고 기록이며, 새 제품 계약의 완료 상태로 해석하지 않는다.

## 추천 우선순위

서버 API와 Android 로컬 UI 사이의 남은 연결을 먼저 닫는다.

1. `todo/003-android-onboarding-pairing-flow.md`
2. `todo/004-unlock-inactivity-notification-flow.md`
3. `todo/005-firebase-runtime-config.md`
4. `todo/006-sessionless-device-security.md`
5. `todo/007-guardian-monitoring-pairing-features.md`
6. `todo/008-fall-detection-deferred-validation.md`
7. `todo/009-local-dev-and-prod-deployment.md`
8. `todo/010-ci-quality-gates.md`

## 핵심 결정사항

- 계정 로그인은 MVP 범위에서 제거한다.
- 앱 설치 단위의 `device`를 서버 식별 단위로 사용한다.
- Android 로컬 설치 UUID는 앱 데이터 삭제 또는 앱 재설치 시 새 기기로 취급한다.
- 페어링 코드는 짧은 만료 시간을 가진 일회성 코드로 운영한다.
- 낙상 감지는 MVP 제품화 범위에서 보류한다.
- MVP 핵심 알림은 "마지막 잠금해제 2일 경과" 기준의 보호자 FCM이다.
- 서비스 실행 내역과 잠금해제 관련 내역은 Android 로컬 DB와 백엔드 DB에 모두 기록한다.
- 데이터 복구, 다중 기기 계정 동기화, 비밀번호 찾기는 MVP 이후로 미룬다.
