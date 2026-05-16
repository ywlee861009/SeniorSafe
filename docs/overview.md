# SeniorSafe — 프로젝트 개요

## 목표

고령자의 휴대폰 사용 활동을 보호자가 확인할 수 있게 하는 모바일 안전망 시스템.

현재 MVP는 낙상 감지 기능을 보류하고, 어르신 휴대폰이 일정 기간 잠금해제되지 않으면 보호자에게 푸시 알림을 보내는 흐름에 집중한다. Android 앱은 잠금해제 이벤트와 서비스 실행 내역을 로컬 DB에 기록하고, 백엔드는 마지막 잠금해제 시각과 이벤트 로그를 저장한다.

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
→ 어르신 폰 잠금해제 활동 기록
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

- 어르신 앱은 휴대폰 잠금해제 이벤트를 감지하면 로컬 DB에 기록하고 백엔드에 업로드한다.
- 백엔드는 어르신 기기의 `last_unlocked_at`을 갱신하고, 모든 잠금해제 이벤트를 별도 로그로 저장한다.
- 백엔드는 매일 배치를 실행해 `last_unlocked_at` 기준 N일 이상 미사용 상태를 찾는다.
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
│   ├── 잠금해제 감지 Receiver
│   ├── 활동 모니터링 Foreground Service
│   ├── 서비스 실행 내역 로컬 DB 기록
│   └── 잠금해제 내역 로컬 DB 기록 및 백엔드 업로드
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
    ├── UnlockEvent 저장
    ├── 마지막 잠금해제 시각 관리
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
| 잠금해제 감지 | `ACTION_USER_PRESENT` BroadcastReceiver | 사용자가 잠금해제한 시점을 앱에서 기록 가능 |
| Android 서비스 | Foreground Service | 서비스 실행 상태와 생존 여부를 사용자와 개발자가 확인 가능 |
| Android 진단 로그 | Room | 서비스 실행 내역과 잠금해제 내역을 앱 재시작 이후에도 확인 |
| 백엔드 | Python FastAPI | 비동기 처리, 자동 API 문서화 |
| 데이터베이스 | PostgreSQL | 기기, 페어링, 활동 이벤트, 알림 로그 관계 표현에 적합 |
| 기기 인증 | 서버 발급 device access token | 사용자 로그인 없이 API 요청 주체 식별 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) | Android 표준, 백그라운드 알림 보장 |
| 컨테이너 | Docker + Docker Compose | 이식성, 서버 이전 용이 |
| 리버스 프록시 | Nginx | SSL 종단, 정적 파일 서빙 |

---

## 핵심 기능

### Android 현재 구현 메모

2026-05-16 기준 Android 앱은 아직 최종 온보딩 플로우가 아니라 MVP 낙상 감지 대시보드로 바로 진입한다. 이 대시보드는 센서와 foreground service 검증을 위한 임시 화면이다.

새 MVP 방향에서는 기존 낙상 감지 런타임을 제품 핵심에서 내리고, 서비스 실행 진단 구조와 Room 기반 로그 저장 구조를 잠금해제 활동 모니터링에 재사용한다.

- 낙상 감지 런타임은 `core:fall-detection` 모듈에 있으나 MVP 제품화는 보류한다.
- MVP 진단 로그는 `core:diagnostics` 모듈의 Room DB에 저장한다.
- 화면을 종료해도 foreground service가 살아 있으면 서비스 상태 전이 로그가 계속 저장된다.
- 서비스 실행 상태 표시는 저장된 boolean이 아니라 service heartbeat 기준으로 동기화한다.
- root Compose에는 edge-to-edge status bar/navigation bar padding이 적용되어 있다.
- 로그인 없는 역할 선택/기기 등록/페어링 온보딩은 아직 남은 작업이다.
- 잠금해제 이벤트 감지, 업로드, 미사용 알림 배치는 신규 작업이다.

### 어르신 앱

- 역할 선택 후 어르신 기기로 등록
- 보호자와 연결할 6자리 코드 생성
- 활동 모니터링 Foreground Service 실행
- 서비스 시작/중지/heartbeat/오류 내역을 로컬 DB에 기록
- 휴대폰 잠금해제 이벤트를 로컬 DB에 기록
- 잠금해제 이벤트를 백엔드에 업로드
- 네트워크 실패 시 로컬 미전송 이벤트를 보관하고 재시도

### 보호자 앱

- 역할 선택 후 보호자 기기로 등록
- 어르신 앱의 연결 코드 입력
- 연결된 어르신 목록 조회
- 어르신별 마지막 잠금해제 시각 조회
- N일 미사용 FCM 알림 수신
- 연결 해제

### 백엔드 API 및 배치

- 기기 등록 및 device token 발급
- FCM token 등록/갱신
- 연결 코드 생성 및 사용
- active pairing 조회/해제
- 잠금해제 이벤트 수신
- 어르신 기기 마지막 잠금해제 시각 저장
- 서비스 실행 내역 수신 및 저장
- 미사용 상태 일일 배치 실행
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

### 잠금해제 시

```text
어르신 폰                 백엔드
 │                        │
 ├─ 잠금해제 감지          │
 ├─ 로컬 DB 기록           │
 ├─ POST /activity/unlocks ─▶
 │                        ├─ UnlockEvent 저장
 │                        └─ Device.last_unlocked_at 갱신
```

### 미사용 배치 시

```text
백엔드 배치                    보호자 폰
 │                              │
 ├─ 매일 실행                    │
 ├─ 마지막 잠금해제 2일 경과 조회 │
 ├─ active pairing 보호자 조회    │
 ├─ 중복 알림 여부 확인           │
 ├─ FCM 전송 ───────────────────▶│
 │                              ├─ 알림 표시
```

알림 예시:

```text
홍길동님 휴대폰 사용 기록이 2일 동안 확인되지 않았습니다. 안부 확인이 필요할 수 있습니다.
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
├── last_unlocked_at
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

UnlockEvent
├── id
├── senior_device_id     → Device(senior)
├── unlocked_at
├── received_at
├── source               (user_present | manual | retry)
└── created_at

ServiceEvent
├── id
├── device_id            → Device
├── event_type           (started | stopped | heartbeat | error | boot_completed)
├── occurred_at
├── received_at
└── detail

InactivityAlert
├── id
├── senior_device_id     → Device(senior)
├── guardian_device_id   → Device(guardian)
├── threshold_days
├── last_unlocked_at
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
