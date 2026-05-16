# SeniorSafe — 프로젝트 개요

## 목표

고령자의 낙상(Fall)을 스마트폰 가속도 센서로 실시간 감지하고, 연결된 보호자에게 즉시 푸시 알림을 전송하는 모바일 안전망 시스템.

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
→ 낙상 감지와 보호자 알림 사용
```

### MVP에서 하지 않는 것

- 이메일/비밀번호 회원가입
- 로그인
- refresh token
- 비밀번호 찾기
- 사용자 계정 기반 데이터 복구
- 다중 기기 계정 동기화

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

---

## 시스템 구성

```text
Android App
├── 어르신 모드
│   ├── 기기 등록
│   ├── 연결 코드 생성
│   ├── 낙상 감지 Foreground Service
│   └── 오감지 취소 UI
│
└── 보호자 모드
    ├── 기기 등록
    ├── 연결 코드 입력
    ├── 낙상 알림 수신
    └── 연결된 어르신 낙상 이력 조회

Nginx
└── FastAPI Backend
    ├── Device 등록/인증
    ├── PairingCode 생성/사용
    ├── Pairing 관리
    ├── FallEvent 저장
    └── Firebase FCM 발송

PostgreSQL
Firebase FCM
```

---

## 기술 스택

| 영역 | 기술 | 선택 이유 |
|------|------|----------|
| Android | Kotlin | 구글 공식 언어, 최신 API 지원 |
| 낙상 감지 | Android SensorManager (Accelerometer) | 별도 하드웨어 불필요 |
| Android 진단 로그 | Room | MVP 센서/서비스 로그를 앱 재시작 이후에도 확인 |
| 백엔드 | Python FastAPI | 비동기 처리, 자동 API 문서화 |
| 데이터베이스 | PostgreSQL | 기기, 페어링, 낙상 이벤트 관계 표현에 적합 |
| 기기 인증 | 서버 발급 device access token | 사용자 로그인 없이 API 요청 주체 식별 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) | Android 표준, 백그라운드 알림 보장 |
| 컨테이너 | Docker + Docker Compose | 이식성, 서버 이전 용이 |
| 리버스 프록시 | Nginx | SSL 종단, 정적 파일 서빙 |

---

## 핵심 기능

### Android 현재 구현 메모

2026-05-16 기준 Android 앱은 아직 최종 온보딩 플로우가 아니라 MVP 낙상 감지 대시보드로 바로 진입한다. 이 대시보드는 센서와 foreground service 검증을 위한 임시 화면이다.

- 낙상 감지 런타임은 `core:fall-detection` 모듈에 있다.
- MVP 진단 로그는 `core:diagnostics` 모듈의 Room DB에 저장한다.
- 화면을 종료해도 foreground service가 살아 있으면 센서 샘플과 상태 전이 로그가 계속 저장된다.
- 서비스 실행 상태 표시는 저장된 boolean이 아니라 service heartbeat 기준으로 동기화한다.
- root Compose에는 edge-to-edge status bar/navigation bar padding이 적용되어 있다.
- 로그인 없는 역할 선택/기기 등록/페어링 온보딩은 아직 남은 작업이다.

### 어르신 앱

- 역할 선택 후 어르신 기기로 등록
- 보호자와 연결할 6자리 코드 생성
- 낙상 감지 Foreground Service 실행
- 자유낙하(low-G) → 충격(high-G) → 정지 패턴 감지
- 감지 후 30초 카운트다운 동안 오감지 취소
- 취소하지 않으면 백엔드에 낙상 이벤트 보고

### 보호자 앱

- 역할 선택 후 보호자 기기로 등록
- 어르신 앱의 연결 코드 입력
- 연결된 어르신 목록 조회
- FCM 낙상 알림 수신
- 낙상 이력 조회
- 연결 해제

### 백엔드 API

- 기기 등록 및 device token 발급
- FCM token 등록/갱신
- 연결 코드 생성 및 사용
- active pairing 조회/해제
- 낙상 이벤트 수신 및 보호자 FCM 발송
- 낙상 이력 조회

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
 ├─ 낙상 감지 시작                   │
```

### 낙상 감지 시

```text
어르신 폰                 백엔드                  보호자 폰
 │                        │                        │
 ├─ 가속도 이상 감지        │                        │
 ├─ 30초 카운트다운 시작    │                        │
 │  취소 없으면 전송        │                        │
 ├─ POST /fall/events ───▶│                        │
 │                        ├─ active pairing 조회     │
 │                        ├─ 보호자 FCM token 조회   │
 │                        ├─ FCM 전송 ────────────▶│
 │                        │                        ├─ 알림 표시
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
└── last_seen_at

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

FallEvent
├── id
├── senior_device_id     → Device(senior)
├── detected_at
├── status               (pending | reported | cancelled | notify_failed)
├── cancelled_at
└── created_at
```

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
FALL_CANCEL_WINDOW_SECONDS=30

# Firebase
FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
```

---

## 다음 문서

- [`api-spec.md`](./api-spec.md) — REST API 엔드포인트 명세
- [`deployment.md`](./deployment.md) — Docker 기반 서버 배포 가이드
