# SeniorSafe — 프로젝트 개요

## 목표

고령자의 낙상(Fall)을 스마트폰 가속도 센서로 실시간 감지하고, 보호자에게 즉시 푸시 알림을 전송하는 모바일 안전망 시스템.

---

## 시스템 구성

```
┌──────────────────────────────────────────────────────────────────┐
│                        Android App                               │
│                                                                  │
│   ┌─────────────────────┐      ┌──────────────────────────────┐  │
│   │   어른(Senior) 모드  │      │     보호자(Guardian) 모드    │  │
│   │                     │      │                              │  │
│   │ • 낙상 감지 Service  │      │ • 낙상 알림 수신 (FCM)       │  │
│   │ • 상태 모니터링 UI   │      │ • 연결된 어른 목록           │  │
│   │ • 긴급 연락처 설정   │      │ • 낙상 이력 조회             │  │
│   └────────┬────────────┘      └───────────────┬──────────────┘  │
└────────────│───────────────────────────────────│─────────────────┘
             │ HTTPS REST API                    │ FCM Push
             ▼                                   ▲
┌──────────────────────────────────────────────────────────────────┐
│                     Docker Compose                               │
│                                                                  │
│   ┌──────────────┐   ┌──────────────┐   ┌─────────────────────┐ │
│   │    Nginx     │   │   FastAPI    │   │     PostgreSQL       │ │
│   │ (Reverse     │──▶│  (Backend)   │──▶│     (Database)      │ │
│   │  Proxy)      │   │   :8000      │   │       :5432         │ │
│   │  :80/:443    │   └──────┬───────┘   └─────────────────────┘ │
│   └──────────────┘          │                                    │
└────────────────────────────│────────────────────────────────────┘
                             │ Firebase Admin SDK
                             ▼
                    ┌─────────────────┐
                    │  Firebase FCM   │
                    │  (Google 서버)  │
                    └─────────────────┘
```

---

## 기술 스택

| 영역 | 기술 | 선택 이유 |
|------|------|----------|
| Android | Kotlin | 구글 공식 언어, 최신 API 지원 |
| 낙상 감지 | Android SensorManager (Accelerometer) | 별도 하드웨어 불필요 |
| 백엔드 | Python FastAPI | 비동기 처리, 자동 API 문서화 |
| 데이터베이스 | PostgreSQL | 어른-보호자 관계 표현에 적합 |
| 인증 | JWT (Access + Refresh Token) | Stateless, 모바일 친화적 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) | Android 표준, 백그라운드 알림 보장 |
| 컨테이너 | Docker + Docker Compose | 이식성, 서버 이전 용이 |
| 리버스 프록시 | Nginx | SSL 종단, 정적 파일 서빙 |

---

## 핵심 기능

### 어른(Senior) 앱
- **낙상 감지 Foreground Service**: 화면이 꺼져도 가속도 센서를 지속 모니터링
- **낙상 감지 알고리즘**: 자유낙하(low-G) → 충격(high-G) → 정지 패턴 감지
- **오감지 취소**: 감지 후 30초 카운트다운, 본인이 취소 가능
- **연결 코드 발급**: 보호자와 페어링할 6자리 코드 생성

### 보호자(Guardian) 앱
- **FCM 푸시 알림**: 어른 낙상 시 즉시 알림 수신 (앱 꺼진 상태에서도)
- **낙상 이력 조회**: 연결된 어른의 낙상 기록 목록
- **다중 연결**: 보호자 1명이 여러 어른을 동시에 모니터링 가능

### 백엔드 API
- **회원가입 / 로그인**: 어른 / 보호자 타입 구분
- **페어링**: 어른이 발급한 코드로 보호자가 연결 요청
- **낙상 이벤트 수신**: 어른 앱으로부터 낙상 신호 수신 → 연결된 보호자에게 FCM 전송
- **FCM 토큰 갱신**: 앱 설치/재설치 시 토큰 업데이트

---

## 사용자 플로우

### 최초 설정
```
어른                          보호자
 │                              │
 ├─ 앱 설치 & 회원가입           ├─ 앱 설치 & 회원가입
 ├─ "연결 코드 발급" 탭          │
 ├─ 6자리 코드 확인              │
 │  (예: A3F9K2)               │
 │       └──── 코드 공유 ───────▶│
 │                              ├─ 코드 입력 → 페어링 완료
 ├─ 낙상 감지 서비스 시작         │
```

### 낙상 감지 시
```
어른 폰                 백엔드                  보호자 폰
 │                        │                        │
 ├─ 가속도 이상 감지        │                        │
 ├─ 30초 카운트다운 시작    │                        │
 │  (취소 없으면 전송)      │                        │
 ├─ POST /fall/event ────▶│                        │
 │                        ├─ 연결된 보호자 조회       │
 │                        ├─ FCM 전송 ────────────▶│
 │                        │                        ├─ 알림 표시
 │                        │                        │  "홍길동님 낙상 감지"
```

---

## 데이터 모델 (개요)

```
User
├── id
├── email
├── password_hash
├── name
├── phone
├── user_type          (senior | guardian)
├── fcm_token          (FCM 기기 토큰)
└── created_at

Pairing
├── id
├── senior_id          → User(senior)
├── guardian_id        → User(guardian)
├── status             (pending | active | disconnected)
└── created_at

FallEvent
├── id
├── senior_id          → User(senior)
├── detected_at
├── cancelled          (오감지로 취소 여부)
└── acknowledged_at    (보호자가 확인한 시각)

PairingCode
├── code               (6자리 영숫자)
├── senior_id          → User(senior)
└── expires_at         (발급 후 10분 유효)
```

---

## 디렉토리 구조

```
SeniorSafe/
├── docs/
│   ├── overview.md          ← 이 문서
│   ├── api-spec.md          (API 명세)
│   ├── fall-detection.md    (낙상 감지 알고리즘 상세)
│   └── deployment.md        (배포 가이드)
│
├── backend/
│   ├── app/
│   │   ├── main.py
│   │   ├── core/            (config, security, db)
│   │   ├── models/          (SQLAlchemy ORM)
│   │   ├── schemas/         (Pydantic)
│   │   ├── routers/         (auth, pairing, fall, devices)
│   │   └── services/        (fcm_service, auth_service)
│   ├── alembic/             (DB 마이그레이션)
│   ├── Dockerfile
│   └── requirements.txt
│
├── android/
│   └── app/src/main/
│       ├── java/com/seniorsafe/
│       │   ├── ui/          (login, senior, guardian 화면)
│       │   ├── service/     (FallDetectionService)
│       │   ├── data/        (api, repository)
│       │   └── firebase/    (FCM 수신 처리)
│       └── res/
│
├── nginx/
│   └── nginx.conf
│
└── docker-compose.yml
```

---

## 환경 변수 (.env)

```
# Database
POSTGRES_USER=seniorsafe
POSTGRES_PASSWORD=
POSTGRES_DB=seniorsafe_db

# JWT
SECRET_KEY=
ACCESS_TOKEN_EXPIRE_MINUTES=60
REFRESH_TOKEN_EXPIRE_DAYS=30

# Firebase
FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json

# App
PAIRING_CODE_EXPIRE_MINUTES=10
FALL_CANCEL_WINDOW_SECONDS=30
```

---

## 다음 문서

- [`api-spec.md`](./api-spec.md) — REST API 엔드포인트 전체 명세
- [`fall-detection.md`](./fall-detection.md) — 낙상 감지 알고리즘 상세 설명
- [`deployment.md`](./deployment.md) — Docker 기반 서버 배포 가이드
