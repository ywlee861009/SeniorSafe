# SeniorSafe — 프로젝트 개요

## 목표

고령자의 휴대폰 사용 활동을 보호자가 확인할 수 있게 하는 모바일 안전망 시스템.

현재 MVP는 낙상 감지 기능을 보류하고, 어르신 휴대폰에서 일정 기간 활동이 감지되지 않으면 보호자에게 푸시 알림을 보내는 흐름에 집중한다. Android 앱은 잠금해제, 충전기 연결/해제 등 활동 이벤트와 서비스 실행 내역을 로컬 DB에 기록하고, 백엔드는 마지막 활동 시각과 이벤트 로그를 저장한다(업로드는 `ticket/todo/004`에서 신규 구현 예정).

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

Nginx
└── FastAPI Backend
    ├── Device 등록/인증
    ├── PairingCode 생성/사용
    ├── Pairing 관리
    ├── ActivityEvent 저장
    ├── 마지막 활동 시각 관리
    ├── 미사용 배치 실행
    └── Firebase FCM 발송

PostgreSQL
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
| 백엔드 | Python FastAPI | 비동기 처리, 자동 API 문서화 |
| 데이터베이스 | PostgreSQL | 기기, 페어링, 활동 이벤트, 알림 로그 관계 표현에 적합 |
| 기기 인증 | 서버 발급 device access token | 사용자 로그인 없이 API 요청 주체 식별 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) | Android 표준, 백그라운드 알림 보장 |
| 컨테이너 | Docker + Docker Compose | 이식성, 서버 이전 용이 |
| 리버스 프록시 | Nginx | SSL 종단, 정적 파일 서빙 |

---

## 핵심 기능

### Android 현재 구현 메모 (2026-05-18 기준)

**구현됨:**
- `RoleSelect → Pairing → Senior/Guardian Home` 진입 흐름
- `ActivityMonitorService`: 잠금해제·충전 이벤트 감지 → `unlock_events` Room 저장
- 서비스 다층 생존(foreground + WakeLock + START_STICKY + `onTaskRemoved` AlarmManager 재예약 + BootReceiver + 3분 stale 자동 복구)
- 서비스 생존 상태 heartbeat → `ActivityServiceStateStore` (SharedPreferences `last_heartbeat_at`)
- MVP 디버깅 대시보드 (`feature/mvp` — 개발자 진입용, 일반 startDestination 분기 없음)
- 매일 20시 오늘의 글 로컬 알림 (`AlarmManager.setInexactRepeating`)
- MVP 진단 로그는 `core:diagnostics` 모듈의 Room DB(`seniorsafe_diagnostics.db`)에 저장

**미구현:**
- 활동 이벤트 백엔드 업로드 (`ApiService`에 `/activity/events` 없음, 호출자 없음)
- 서비스 이벤트 백엔드 업로드 (`/activity/service-events` 미연결)
- 미전송 이벤트 재전송 루프 (Room DAO에 `getPendingUpload`/`markUploaded` 정의만 있고 호출자 없음)
- device 등록/device_access_token 발급 호출 (OkHttp Interceptor 없음, `TokenDataStore`는 user JWT용)
- 실제 페어링 코드 서버 등록 (현재 `PairingCodeViewModel`이 로컬 `Random.nextInt` 난수 생성, 서버 등록 없음)
- 페어링 완료 서버 확인 (`markPaired`는 로컬 `PairingStatus.PAIRED` 토글만 수행)
- FCM 보호자 알림 수신 (`google-services.json` 미등록, `google-services` 플러그인 주석)

**기타:**
- `core:fall-detection`은 dependency graph orphan — APK 미포함. `feature/senior/.service/FallDetectionService`도 호출 트리거 없음(둘 다 dead). 정리 대상: `ticket/todo/008`
- login/register 화면 코드 잔존(dead route — `AppNavHost`에 등록되나 `MainActivity.toStartDestination`이 분기하지 않음)
- `BASE_URL = "http://10.0.2.2:8000/"` 하드코딩 (`core/network/NetworkModule.kt`)

### 어르신 앱

구현됨:
- 역할 선택 → 어르신 홈 진입
- 연결 코드 화면 (로컬 난수, mock)
- 활동 모니터링 Foreground Service 실행 (서비스 생존 다층 방어)
- 서비스 시작/중지/heartbeat/오류 내역을 로컬 DB에 기록
- 활동 이벤트(잠금해제, 충전기 연결/해제)를 로컬 DB에 기록
- 매일 저녁 8시 "오늘의 글" 로컬 푸시 발송·열람 기록

미구현:
- 기기 등록 및 device access token 발급 호출
- 실제 페어링 코드 서버 등록 (현재 mock)
- 활동 이벤트 백엔드 업로드 및 재전송

### 보호자 앱

구현됨:
- 역할 선택 → 보호자 홈 진입
- 연결 코드 입력 (`pairings` API 호출)
- 연결된 어르신 목록 조회
- 연결 해제

미구현:
- 기기 등록 및 device access token 발급 호출
- 어르신별 마지막 활동 시각 조회 (백엔드 미구현)
- N일 미사용 FCM 알림 수신 (`google-services.json` 미등록)

### 백엔드 API 및 배치

구현됨:
- 기기 등록 및 device token 발급 (`/devices/register`)
- FCM token 등록/갱신 (`/devices/fcm-token`)
- 내 기기 정보 조회 (`/devices/me`)
- 연결 코드 생성 (`/pairing/codes`)
- active pairing 연결/조회/해제 (`/pairings`)

미구현:
- 활동 이벤트 수신 (`/activity/events`)
- 서비스 실행 내역 수신 (`/activity/service-events`)
- 미사용 알림 배치 실행 (`/internal/batches/inactivity-alerts/run`)
- 보호자 FCM 발송

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
 ├─ [미구현] POST /activity/events ──▶
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
├── id
├── code_hash
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
├── event_type           (started | stopped | heartbeat | error | boot_completed | daily_content_opened)
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

## 환경 변수 (.env)

```text
# Database
POSTGRES_USER=seniorsafe
POSTGRES_PASSWORD=
POSTGRES_DB=seniorsafe_db

# App
SECRET_KEY=
DEVICE_TOKEN_EXPIRE_DAYS=365
PAIRING_CODE_EXPIRE_MINUTES=10
INACTIVITY_ALERT_THRESHOLD_DAYS=2
INACTIVITY_ALERT_REPEAT_HOURS=24

# Firebase
FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
```

---

## 다음 문서

- [`api-spec.md`](./api-spec.md) — REST API 엔드포인트 명세
- [`deployment.md`](./deployment.md) — Docker 기반 서버 배포 가이드
