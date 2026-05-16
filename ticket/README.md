# SeniorSafe 티켓

최종 점검일: 2026-05-16

## 기획 피벗

SeniorSafe는 로그인/회원가입 중심 MVP에서 로그인 없는 기기 페어링 MVP로 방향을 변경한다. 2026-05-16 현재 낙상 감지 제품화는 보류하고, 어르신 휴대폰의 잠금해제 활동 기록 기반 안부 확인 알림을 MVP 핵심으로 둔다.

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
- 잠금해제 이벤트, 서비스 실행 내역, 미사용 알림 배치 API는 신규 구현 필요 (todo/004).
- DB 마이그레이션(users/fall_events 테이블 drop, pairing User FK 칼럼 drop) 별도 필요.

### Android

- 앱 진입점은 아직 새 온보딩/페어링 플로우가 아니라 MVP 낙상 감지 대시보드로 바로 진입한다.
- `enableEdgeToEdge()` 환경에서 루트 Compose에 status bar/navigation bar padding을 적용했다.
- 낙상 감지 런타임을 `feature:mvp`에서 `core:fall-detection` 모듈로 분리했지만, 제품화는 보류한다.
- MVP 디버깅 로그 저장을 `core:diagnostics` 모듈로 분리하고 Room DB(`seniorsafe_diagnostics.db`)에 최근 500개 로그를 저장한다.
- MVP 대시보드는 작은 서비스 제어 영역과 Runtime Log 콘솔 중심 UI로 변경했다.
- 서비스 실행 상태 UI는 service heartbeat 기준으로 동기화한다.
- Android 빌드 검증: 2026-05-16 기준 `cd android && ./gradlew assembleDebug` 통과.

## Android 남은 핵심 작업

- `todo/003`: MVP 대시보드 직행을 제거하고, 로그인 없는 역할 선택/기기 등록/페어링 온보딩을 구현해야 한다.
- `todo/004`: 잠금해제 이벤트 보고, 서비스 실행 내역 저장, 미사용 배치, FCM 발송을 device/pairing 기준으로 신규 구현해야 한다.
- `todo/008`: 낙상 감지 알고리즘은 MVP에서 보류하고, 향후 재개 조건과 검증 범위만 문서화한다.
- `feature:senior`에 남은 기존 낙상 감지 서비스 구현은 `core:fall-detection`으로 통합해 중복을 제거해야 한다.

## 폴더 기준

- `todo/`: 새 기획 기준으로 앞으로 해야 할 작업이다.
- `done/`: 피벗 이전 구현 완료 기록이다. 그대로 참고하되, 새 제품 계약의 완료 상태로 보지는 않는다.

## 추천 우선순위

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
- 페어링 코드는 짧은 만료 시간을 가진 일회성 코드로 운영한다.
- 낙상 감지는 MVP 제품화 범위에서 보류한다.
- MVP 핵심 알림은 "마지막 잠금해제 2일 경과" 기준의 보호자 FCM이다.
- 서비스 실행 내역과 잠금해제 관련 내역은 Android 로컬 DB와 백엔드 DB에 모두 기록한다.
- 앱 삭제 후 재설치는 새 기기로 취급한다.
- 데이터 복구, 다중 기기 계정 동기화, 비밀번호 찾기는 MVP 이후로 미룬다.
