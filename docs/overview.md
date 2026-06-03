# SeniorSafe — 프로젝트 개요

## 목표

고령자의 휴대폰 사용 활동을 보호자가 확인할 수 있게 하는 모바일 안전망 시스템.

현재 MVP는 낙상 감지 기능을 보류하고, 어르신 휴대폰에서 일정 기간 활동이 감지되지 않으면 보호자에게 푸시 알림을 보내는 흐름에 집중한다. Android 앱은 잠금해제, 충전기 연결/해제 등 활동 이벤트와 서비스 실행 내역을 로컬 DB에 기록한다. 백엔드는 활동/서비스 이벤트 수신, 마지막 활동 시각 갱신, 미사용 알림 배치를 구현 완료했다. Android 런타임도 실제 Retrofit/Supabase Edge Function 경계로 전환됐지만, 어르신 활동/서비스 이벤트 업로드 worker와 실기기 E2E 검증은 아직 남아 있다.

SeniorSafe MVP는 계정 로그인 없이 동작한다. 앱을 설치한 뒤 보호자 또는 어르신 모드를 선택하고, 어르신 앱에 표시되는 연결 코드를 보호자 앱에 입력해 바로 페어링한다.

---

## 제품 계약

### MVP 기본 흐름

```text
앱 설치
→ 보호자 / 어르신 모드 선택
→ 어르신: 연결 코드 생성
→ 보호자: 연결 코드 입력
→ 페어링 완료
→ 어르신 폰 활동 기록 (잠금해제, 충전 등)
→ N일 미사용 시 보호자 알림
```

### MVP에서 하지 않는 것

- 이메일/비밀번호 회원가입
- 로그인
- refresh token
- 비밀번호 찾기
- 사용자 계정 기반 데이터 복구
- 다중 기기 계정 동기화
- 낙상 감지 정확도 검증 및 제품화

앱 삭제 후 재설치하면 새 기기로 취급한다. 기존 연결은 자동 복구하지 않으며, 다시 페어링해야 한다.

### 기기 식별 정책

- 서버의 기본 식별 단위는 사용자 계정이 아니라 설치된 앱 기기(`Device`)다.
- Android 앱은 첫 실행 시 로컬 `install_id`를 생성한다.
- 서버는 기기 등록 시 `device_id`와 `device_access_token`을 발급한다.
- 이후 보호가 필요한 API는 `Authorization: Bearer <device_access_token>`으로 호출한다.
- 로컬 저장소가 삭제되거나 앱이 재설치되면 새 기기로 등록한다.

### 페어링 정책

- 어르신 기기는 6자리 숫자 연결 코드를 생성한다.
- 연결 코드는 10분 동안 유효하다.
- 연결 코드는 일회성이다.
- MVP에서는 보호자 1명이 여러 어르신을 모니터링할 수 있다.
- MVP에서는 어르신 1명에게 여러 보호자가 연결될 수 있다.
- 보호자와 어르신은 연결을 해제할 수 있어야 한다.

### 미사용 알림 정책

- 어르신 앱은 활동 이벤트(잠금해제, 충전기 연결/해제 등)를 감지하면 로컬 DB에 기록하고 백엔드에 업로드한다.
- 백엔드는 어르신 기기의 `last_activity_at`을 갱신하고, 모든 활동 이벤트를 별도 로그로 저장한다.
- 백엔드는 매일 배치를 실행해 `last_activity_at` 기준 N일 이상 미사용 상태를 찾는다.
- MVP 기본 임계값은 2일이다.
- 조건을 만족하면 active pairing된 보호자 기기 FCM token으로 푸시를 보낸다.
- 같은 미사용 상태에 대해 중복 알림을 과도하게 보내지 않도록 `last_inactivity_alert_sent_at` 또는 알림 로그를 저장한다.
- 알림 문구는 위험을 단정하지 않고 "휴대폰 사용 기록이 확인되지 않음"으로 표현한다.

---

## 시스템 구성

```text
Android App
├── 어르신 모드
│   ├── 기기 등록
│   ├── 연결 코드 생성
│   ├── 활동 감지 Receiver (잠금해제, 충전 상태 변화)
│   ├── 활동 모니터링 Foreground Service
│   ├── 서비스 실행 내역 로컬 DB 기록
│   └── 활동 이벤트 로컬 DB 기록 및 백엔드 업로드
│
└── 보호자 모드
    ├── 기기 등록
    ├── 연결 코드 입력
    ├── 연결된 어르신 마지막 사용 시각 조회
    └── 미사용 알림 수신

Supabase
├── Edge Functions (Deno/TypeScript)
│   ├── Device 등록/인증 (커스텀 JWT)
│   ├── PairingCode 생성/사용
│   ├── Pairing 관리
│   ├── ActivityEvent 수신/조회
│   ├── ServiceEvent 수신/조회
│   ├── 미활동 알림 배치 (pg_cron)
│   └── Firebase FCM 발송
├── PostgreSQL (RLS 적용)
└── pg_cron (매일 00:00 UTC)

Firebase FCM
```

---

## 기술 스택

| 영역 | 기술 | 선택 이유 |
|------|------|----------|
| Android | Kotlin | 구글 공식 언어, 최신 API 지원 |
| 활동 감지 | BroadcastReceiver (`ACTION_USER_PRESENT`, `ACTION_POWER_CONNECTED/DISCONNECTED`) | 잠금해제, 충전기 연결/해제 등 생존 신호를 앱에서 기록 |
| Android 서비스 | Foreground Service | 서비스 실행 상태와 생존 여부를 사용자와 개발자가 확인 가능 |
| Android 진단 로그 | Room | 서비스 실행 내역과 활동 이벤트를 앱 재시작 이후에도 확인 |
| 백엔드 | Supabase Edge Functions (Deno/TypeScript) | 서버리스, 자동 스케일링, PostgreSQL 통합 |
| 데이터베이스 | Supabase PostgreSQL (RLS) | 기기, 페어링, 활동 이벤트, 알림 로그; RLS로 DB 레벨 보안 |
| 스키마 관리 | Supabase Migrations | 선언적 마이그레이션, 로컬/프로덕션 동기화 |
| 배치 스케줄러 | pg_cron | DB 내장 cron, 매일 미활동 알림 배치 실행 |
| 기기 인증 | 커스텀 device JWT (HS256, djwt) | Supabase Auth 미사용, RLS와 호환되는 커스텀 토큰 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) | Android 표준, 백그라운드 알림 보장 |

---

## 핵심 기능

### Android 현재 구현 메모 (2026-06-03 기준)

**구현됨:**
- `RoleSelect → Pairing → Senior/Guardian Home` 진입 흐름
- 역할 선택 시 `DeviceRepository.registerCurrentDevice()` 호출 및 device token 저장 경계 구현
- `NetworkModule`이 실제 Retrofit + GsonConverterFactory를 주입하고 `BuildConfig.ANBU_API_BASE_URL`을 Supabase Functions base URL로 사용
- `ApiService`의 device/pairing/FCM 경로가 실제 Edge Function 이름과 일치
- 어르신 연결 코드 생성과 보호자 코드 입력이 `pairing-codes`, `pairing-claim`, `pairings-list` API 경계로 연결
- `ActivityMonitorService`: 잠금해제·충전 이벤트 감지 → `unlock_events` Room 저장
- 서비스 다층 생존(foreground + WakeLock + START_STICKY + `onTaskRemoved` AlarmManager 재예약 + BootReceiver + 3분 stale 자동 복구)
- 서비스 생존 상태 heartbeat → `ActivityServiceStateStore` (SharedPreferences `last_heartbeat_at`)
- MVP 디버깅 대시보드 (`feature/mvp` — 개발자 진입용, 일반 startDestination 분기 없음)
- 매일 20시 오늘의 글 로컬 알림 (`AlarmManager.setInexactRepeating`)
- MVP 진단 로그는 `core:diagnostics` 모듈의 Room DB(`seniorsafe_diagnostics.db`)에 저장
- `GuardianFcmService`가 FCM 수신 및 token refresh 시 `DeviceRepository.updateFcmToken()` 호출

**미구현/잔여:**
- 활동 이벤트 백엔드 업로드 호출자 없음. 현재 Android DTO도 백엔드 배치 계약(`{ "events": [...] }` → `{ "accepted": N }`)과 다름
- 서비스 이벤트 백엔드 업로드 호출자 없음. 현재 Android DTO도 백엔드 배치 계약과 다름
- 미전송 이벤트 재전송 루프 없음 (`UnlockEventDao`에만 `getPendingUpload`/`markUploaded` 존재, 서비스 이벤트 DAO에는 업로드 상태 처리 없음)
- 실제 FCM 런타임 설정 미완료 (`google-services` plugin은 활성화됐고 `android/app/google-services.json` 실제 파일 배치 필요)
- 보호자 미사용 알림 탭 시 상세 화면 라우팅 없음

**기타:**
- 현재 저장소에는 `core:fall-detection` 모듈이 존재하지 않는다. 낙상 감지 완료 티켓은 과거 기록으로만 남아 있으며 일반 MVP 앱 흐름에는 낙상 기능 진입점이 없다. 정리 대상: `ticket/todo/008`
- login/register 화면 코드 잔존(dead route — `AppNavHost`에 등록되나 `MainActivity.toStartDestination`이 분기하지 않음)
- `core:network`의 fallback 기본값은 아직 `http://10.0.2.2:8000/`이지만, 저장소의 `android/gradle.properties`가 Supabase Functions URL을 제공한다. 머신별 override 또는 product flavor 정책은 추가 정리 대상이다.

### 어르신 앱

구현됨:
- 역할 선택 → 어르신 홈 진입
- 서버 연결 코드 화면 (`pairing-codes` API 경계)
- 활동 모니터링 Foreground Service 실행 (서비스 생존 다층 방어)
- 서비스 시작/중지/heartbeat/오류 내역을 로컬 DB에 기록
- 활동 이벤트(잠금해제, 충전기 연결/해제)를 로컬 DB에 기록
- 매일 저녁 8시 "오늘의 글" 로컬 푸시 발송·열람 기록

미구현:
- 실기기 기준 기기 등록/페어링 성공·실패·만료·재사용 상태 검증
- 활동 이벤트 백엔드 업로드 및 재전송

### 보호자 앱

구현됨:
- 역할 선택 → 보호자 홈 진입
- 연결 코드 입력 UI 및 실제 `pairing-claim` repository/API 경계
- 연결된 어르신 목록 조회 UI 및 실제 `pairings-list` repository/API 경계
- FCM service 수신/표시 및 token refresh hook

미구현:
- 연결 해제 버튼/플로우
- 어르신별 미사용 알림 이력 화면
- N일 미사용 FCM 실기기 수신 검증 (`google-services.json` 미등록)

### 백엔드 API 및 배치 (Supabase Edge Functions)

전체 구현 완료 (12개 Edge Function, Deno 테스트 스위트 존재):
- 기기 등록 및 device JWT 발급 (`device-register`)
- FCM token 갱신 (`fcm-token`)
- 내 기기 정보 조회 (PostgREST + RLS)
- 연결 코드 생성/사용 (`pairing-codes`, `pairing-claim`)
- 페어링 목록/해제 (`pairings-list`, `pairing-disconnect`)
- 활동 이벤트 수신/조회 (`activity-events`, `activity-events-list`)
- 서비스 이벤트 수신/조회 (`service-events`, `service-events-list`)
- 미활동 알림 배치 (`inactivity-check` — pg_cron, 중복 방지 포함)
- 미활동 알림 이력 조회 (`inactivity-alerts-list`)
- 보호자 FCM 발송 (HTTP v1 via `FIREBASE_SERVICE_ACCOUNT`, legacy `FIREBASE_SERVER_KEY`는 fallback/test 호환)

---

## 사용자 플로우

### 최초 설정

```text
어르신 앱                         보호자 앱
 │                                  │
 ├─ 앱 설치                         ├─ 앱 설치
 ├─ "어르신 모드" 선택               ├─ "보호자 모드" 선택
 ├─ 기기 등록                       ├─ 기기 등록
 ├─ 연결 코드 생성                   │
 ├─ 6자리 코드 표시                  │
 │  예: 482913                      │
 │       └──── 코드 공유 ───────────▶│
 │                                  ├─ 코드 입력
 │                                  ├─ 페어링 완료
 ├─ 보호자 연결 완료 확인             │
 ├─ 활동 모니터링 시작                │
```

### 활동 감지 시

```text
어르신 폰                  백엔드
 │                         │
 ├─ 활동 감지               │
 │  (잠금해제/충전 연결/해제) │
 ├─ 로컬 DB 기록            │
 ├─ Room unlock_events insert
 ├─ [Android 미연결] POST /functions/v1/activity-events ──▶
 │                         ├─ ActivityEvent 저장
 │                         └─ Device.last_activity_at 갱신
```

### 매일 콘텐츠 알림

```text
어르신 폰
 │
 ├─ 매일 저녁 8시 로컬 푸시 표시
 │  예: 오늘의 글이 도착했어요
 ├─ 어르신이 알림 탭
 ├─ 앱 실행 및 오늘의 글 표시
 └─ 열람 내역 로컬 DB 기록
```

이 기능은 어르신이 앱을 자연스럽게 다시 열도록 돕는 보조 장치다. MVP에서는 앱 내장 콘텐츠로 시작하고, 이후 서버에서 매일 콘텐츠를 내려받는 방식으로 확장할 수 있다.

### 미사용 배치 시

```text
백엔드 배치                    보호자 폰
 │                              │
 ├─ 매일 실행                    │
 ├─ 마지막 활동 2일 경과 조회     │
 ├─ active pairing 보호자 조회    │
 ├─ 중복 알림 여부 확인           │
 ├─ FCM 전송 ───────────────────▶│
 │                              ├─ 알림 표시
```

알림 예시:

```text
홍길동님 휴대폰 활동이 2일 동안 확인되지 않았습니다. 안부 확인이 필요할 수 있습니다.
```

### 앱 삭제/재설치

```text
앱 삭제
→ 로컬 install_id와 device token 삭제
→ 재설치 후 새 device로 등록
→ 기존 페어링은 자동 복구하지 않음
→ 연결 코드로 다시 페어링
```

---

## 데이터 모델 (개요)

```text
Device
├── id
├── install_id_hash
├── role                 (senior | guardian)
├── display_name
├── fcm_token
├── token_hash
├── created_at
├── last_seen_at
├── last_activity_at
└── inactivity_threshold_days

PairingCode
├── code
├── senior_device_id     → Device(senior)
├── expires_at
├── consumed_at
└── created_at

Pairing
├── id
├── senior_device_id     → Device(senior)
├── guardian_device_id   → Device(guardian)
├── active
├── created_at
└── disconnected_at

ActivityEvent
├── id
├── senior_device_id     → Device(senior)
├── occurred_at
├── received_at
├── source               (user_present | power_connected | power_disconnected)
└── created_at

ServiceEvent
├── id
├── device_id            → Device
├── event_type           (started | stopped | heartbeat | error)
├── occurred_at
├── received_at
└── detail

InactivityAlert
├── id
├── senior_device_id     → Device(senior)
├── guardian_device_id   → Device(guardian)
├── threshold_days
├── last_activity_at
├── sent_at
├── status               (sent | skipped | failed)
└── detail
```

낙상 관련 `FallEvent`는 MVP 제품 범위에서 보류한다. 기존 구현과 진단 로그는 향후 센서 알고리즘 검증이 재개될 때 참고한다.

---

## 환경 변수

```text
# Supabase
SUPABASE_URL=https://<project-id>.supabase.co
SUPABASE_ANON_KEY=<anon-key>
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
SUPABASE_JWT_SECRET=<jwt-secret>

# Firebase
FIREBASE_SERVICE_ACCOUNT=<firebase-service-account-json>
FIREBASE_SERVER_KEY=<legacy-fallback-key>

# Batch
CRON_SECRET=<random-secret-for-pg-cron>
```

---

## 다음 문서

- [`api-spec.md`](./api-spec.md) — REST API 엔드포인트 명세
- [`current-state-audit.md`](./current-state-audit.md) — Android/Backend 현재 구현 전수조사
- [`deployment.md`](./deployment.md) — Supabase 기반 배포 가이드
