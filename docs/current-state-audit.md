# Current State Audit

점검일: 2026-06-01

## 요약

백엔드는 Supabase Edge Functions 기준 MVP 계약이 대부분 구현되어 있다. 기기 등록, device JWT, 페어링, 활동/서비스 이벤트 저장, 미사용 알림 배치, 알림 이력 조회까지 코드와 마이그레이션이 존재한다.

Android는 사용자 흐름과 로컬 활동 기록이 상당 부분 구현되어 있고, 2026-06-01 기준 네트워크 연동의 핵심 블로커가 해소되었다. `NetworkModule`은 이제 `FakeApiService` 대신 실제 Retrofit client를 `ApiService`로 제공하며(`FakeApiService`는 삭제), `ApiService` 경로도 실제 Edge Function 이름과 일치한다. 따라서 역할 선택, 기기 등록, 연결 코드 생성, 보호자 연결, FCM token 갱신은 코드 경계상 실제 Supabase로 전송될 수 있는 상태다. 남은 것은 (1) `google-services.json` 배치와 실제 프로젝트 base URL 설정, (2) Supabase 배포 및 `FIREBASE_SERVICE_ACCOUNT` secret 등록, (3) 어르신 활동/서비스 이벤트 업로드(배치 계약) 연결, (4) 실기기 E2E 검증이다.

## Backend

구현됨:

- `device-register`: 설치 ID 해시 기반 device upsert, custom device JWT 발급, token hash 저장
- `fcm-token`: 인증된 device의 FCM token 갱신
- `pairing-codes`: senior device 전용 6자리 코드 생성, 10분 만료
- `pairing-claim`: guardian device 전용 코드 사용 및 pairing 생성
- `pairings-list`: 보호자는 연결된 어르신 목록과 `last_activity_at` 조회, 어르신은 연결된 보호자 조회
- `pairing-disconnect`: pairing 참여 device의 연결 해제
- `activity-events`: senior 활동 이벤트 배치 저장 및 `devices.last_activity_at` 갱신
- `activity-events-list`: senior 본인 또는 active pairing guardian 조회
- `service-events`: service lifecycle event 배치 저장
- `service-events-list`: 본인 또는 active pairing guardian 조회
- `inactivity-check`: `last_activity_at` 기준 threshold 초과 senior를 찾아 guardian FCM 발송, 중복 방지, sent/skipped/failed 기록
- `inactivity-alerts-list`: senior/guardian 알림 이력 조회
- 마이그레이션: `devices`, `pairing_codes`, `pairings`, `activity_events`, `service_events`, `inactivity_alerts`, RLS, pg_cron

남은 백엔드/운영 리스크:

- 이번 점검 환경에서는 `deno` 실행 파일이 없어 백엔드 테스트를 로컬 재실행하지 못함
- pairing code 생성/입력 rate limit 없음
- device token rotation/폐기 정책 없음
- ✅ (해소) FCM HTTP v1(OAuth2, `FIREBASE_SERVICE_ACCOUNT`)을 주 전송 경로로 적용. 종료된 legacy(`FIREBASE_SERVER_KEY`)는 fallback으로만 잔존. 배포 시 `FIREBASE_SERVICE_ACCOUNT` secret 등록 필요
- 운영 로그에서 token/개인정보 노출 여부 점검 필요
- pg_cron 사용 가능 여부는 실제 Supabase 플랜/프로젝트 설정에서 확인 필요

## Android

구현됨:

- `MainActivity`가 `DeviceDataStore`의 role/pairing status로 시작 route 결정
- `RoleSelectScreen`에서 senior/guardian 선택
- `DeviceRepository.registerCurrentDevice()`가 local install ID, role, display name으로 device 등록 요청을 만들고 token 저장
- `TokenDataStore`에 device access token/device id 저장 경계 존재
- OkHttp interceptor가 저장된 device token을 Bearer header로 추가
- `PairingCodeViewModel`이 pairing code 조회, active pairing 확인 후 local paired 저장
- `ConnectSeniorViewModel`이 code claim 후 local paired 저장
- senior home에서 활동 모니터링 service 상태, 최근 활동 이벤트, 오늘의 글 표시
- `ActivityMonitorService`가 `ACTION_USER_PRESENT`, `ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`를 로컬 Room `unlock_events`에 기록
- service lifecycle event를 로컬 Room `service_events`에 기록
- foreground service 생존 보강: WakeLock, `START_STICKY`, task removed restart alarm, boot receiver, heartbeat state store
- 오늘의 글 로컬 알림 스케줄러와 알림 탭 route 구조
- `GuardianFcmService`가 FCM 수신 알림 표시 및 token refresh hook 제공

미구현/불일치:

- ✅ (해소) `NetworkModule`이 실제 Retrofit + GsonConverterFactory를 주입하도록 전환, `FakeApiService` 삭제
- ✅ (해소) `BuildConfig.ANBU_API_BASE_URL`을 Retrofit `baseUrl`에 연결
- ✅ (해소) Android `ApiService` 경로를 실제 Edge Function 이름과 일치시킴
- ✅ (해소) `NetworkModule` 실 Retrofit 전환, `FakeApiService` 삭제
- ✅ (해소) `ANBU_API_BASE_URL` 기본값을 `http://10.0.2.2:54321/functions/v1/`로 정정
- ✅ (해소) google-services plugin 활성화 — 단, `google-services.json` 실제 파일은 여전히 배치 필요(빌드 전제)
- ✅ (해소) 보호자 홈에 Android 13+ `POST_NOTIFICATIONS` 런타임 권한 요청 추가
- (잔여) Android activity/service request DTO가 백엔드 배치 계약(`{ "events": [...] }`)과 다름 — 호출자 없어 경로만 정정, `TODO(ticket-004)` 표시
- (잔여) `ActivityRepository`가 로컬 기록만 수행하고 업로드 worker/retry가 없음
- (잔여) `UnlockEventDao`에는 pending upload API가 있으나 호출자가 없음
- (잔여) `ServiceEventDao`에는 pending upload/mark uploaded API가 없음
- (잔여) 보호자 홈은 마지막 활동 시각을 표시하지만 연결 해제/알림 이력/상세 화면 없음
- (잔여) login/register 화면은 route에 남아 있지만 일반 시작 분기에서는 접근되지 않음
- (잔여) `getCurrentDevice`는 PostgREST 응답(배열)과 모델이 불일치하나 UI 호출자 없음

## Tickets

현재 우선순위는 다음 순서가 맞다.

1. `todo/003`: Android fake API 제거, Supabase Edge Functions 실연동
2. `todo/004`: Android 활동/서비스 이벤트 업로드, retry, FCM 수신 검증
3. `todo/005`: Firebase 및 Android runtime 설정
4. `todo/007`: 보호자 monitoring UI/연결 해제/알림 이력
5. `todo/006`: rate limit, token rotation 등 sessionless 보안 보강
6. `todo/009`: 운영 배포와 백업/로그/스케줄러 검증
7. `todo/010`: Android/Deno CI
8. `todo/008`: 낙상 감지 보류 상태 문서화와 dead-code 정리

## Contract Corrections

- 실제 Edge Function endpoint는 FastAPI-style path가 아니라 function name이다. (2026-06-01 Android `ApiService`도 이 이름으로 정렬 완료)
  - `device-register`
  - `fcm-token`
  - `pairing-codes`
  - `pairing-claim`
  - `pairings-list`
  - `pairing-disconnect`
  - `activity-events`
  - `activity-events-list`
  - `service-events`
  - `service-events-list`
  - `inactivity-check`
  - `inactivity-alerts-list`
- `activity-events`와 `service-events` POST body는 단건이 아니라 `events` 배열 배치다.
- `service_events.event_type` DB 허용값은 `started`, `stopped`, `heartbeat`, `error`다.
- 현재 저장소에는 `core:fall-detection` 모듈이 없다. 낙상 감지는 MVP 범위 밖이며 과거 티켓은 참고 기록이다.
