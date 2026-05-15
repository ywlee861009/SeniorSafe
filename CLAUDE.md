# SeniorSafe — CLAUDE.md

시니어 낙상 감지 앱. Android 앱 1개(어르신/보호자 두 모드) + FastAPI 백엔드.

설계 문서: `docs/`, UI 설계: `design/`, API 명세: `docs/api-spec.md`

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Python 3.12, FastAPI, SQLAlchemy 2.0 (async), Alembic |
| Database | PostgreSQL 16 |
| 푸시 알림 | Firebase Cloud Messaging (firebase-admin SDK) |
| 인증 | JWT (python-jose) + bcrypt |
| 컨테이너 | Docker + Docker Compose |
| 리버스 프록시 | Nginx |
| Android | Kotlin, Retrofit2, OkHttp, FCM |

---

## 디렉토리 구조

```
SeniorSafe/
├── CLAUDE.md
├── docker-compose.yml
├── docs/
├── design/
├── nginx/
│   └── nginx.conf
└── backend/
    ├── Dockerfile
    ├── requirements.txt
    ├── .env.example
    ├── alembic/
    └── app/
        ├── main.py
        ├── core/
        │   ├── config.py       ← 환경변수 (pydantic-settings)
        │   ├── database.py     ← DB 세션, 엔진
        │   └── security.py     ← JWT 생성/검증, 비밀번호 해싱
        ├── models/             ← SQLAlchemy ORM (테이블 정의)
        ├── schemas/            ← Pydantic (요청/응답 스키마)
        ├── routers/            ← HTTP 레이어만 (라우팅, 의존성 주입)
        └── services/           ← 비즈니스 로직 전담
```

---

## 백엔드 아키텍처 규칙

### 레이어 역할 — 절대 혼용 금지

| 레이어 | 역할 | 금지 사항 |
|--------|------|----------|
| `routers/` | 요청 수신 → service 호출 → 응답 반환 | DB 직접 접근, 비즈니스 로직 |
| `services/` | 비즈니스 로직, DB 조작, FCM 호출 | HTTP 관련 코드 |
| `models/` | 테이블 정의만 | 로직 |
| `schemas/` | 입출력 스키마만 | DB 접근 |

### 파일 네이밍 (백엔드)

- 파일명: `snake_case.py`
- 클래스: `PascalCase`
- 함수/변수: `snake_case`
- 라우터 파일명 = 기능 단위: `auth.py`, `pairing.py`, `fall.py`, `devices.py`

### DB 세션 패턴

```python
# routers에서 항상 이 방식으로 세션 주입
async def endpoint(db: AsyncSession = Depends(get_db)):
    ...
```

### 에러 응답 형식 — 전 엔드포인트 통일

```python
raise HTTPException(status_code=400, detail="에러 메시지")
# 응답: {"detail": "에러 메시지"}
```

### 인증 방식

- 헤더: `Authorization: Bearer <access_token>`
- 보호된 엔드포인트: `current_user: User = Depends(get_current_user)`
- `get_current_user`는 `core/security.py`에 정의

### 라우터 prefix 규칙

```python
# main.py에 등록
app.include_router(auth_router,    prefix="/auth")
app.include_router(pairing_router, prefix="/pairing")
app.include_router(fall_router,    prefix="/fall")
app.include_router(devices_router, prefix="/devices")
```

---

## 데이터 모델 요약

```
User
  id (UUID), email, password_hash, name, phone
  user_type: "senior" | "guardian"
  fcm_token, is_active, created_at

Pairing
  id, senior_id → User, guardian_id → User
  status: "pending" | "active" | "disconnected"
  created_at

FallEvent
  id, senior_id → User
  detected_at, cancelled (bool), acknowledged_at

PairingCode
  code (6자리), senior_id → User, expires_at
```

---

## 환경변수 (.env)

```
POSTGRES_USER=seniorsafe
POSTGRES_PASSWORD=
POSTGRES_DB=seniorsafe_db
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

SECRET_KEY=
ACCESS_TOKEN_EXPIRE_MINUTES=60
REFRESH_TOKEN_EXPIRE_DAYS=30

FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json

PAIRING_CODE_EXPIRE_MINUTES=10
FALL_CANCEL_WINDOW_SECONDS=30
```

`.env` 파일은 절대 커밋하지 않는다. `.env.example`만 관리.

---

## 로컬 실행

```bash
# 전체 실행
docker-compose up -d

# 백엔드 로그 확인
docker-compose logs -f backend

# DB 마이그레이션
docker-compose exec backend alembic upgrade head

# API 문서
http://localhost:8000/docs
```

---

## Android 아키텍처 규칙

### 패키지 구조

```
com.seniorsafe/
├── ui/
│   ├── login/         ← LoginActivity, RegisterActivity
│   ├── senior/        ← SeniorHomeActivity
│   └── guardian/      ← GuardianHomeActivity, FallHistoryActivity, ConnectSeniorActivity
├── service/
│   ├── FallDetectionService.kt   ← Foreground Service
│   └── FallDetectionManager.kt  ← 낙상 감지 알고리즘
├── data/
│   ├── api/
│   │   ├── ApiService.kt         ← Retrofit 인터페이스
│   │   └── ApiClient.kt          ← Retrofit 인스턴스
│   ├── model/                    ← 데이터 클래스 (API request/response)
│   └── repository/
│       ├── AuthRepository.kt
│       ├── PairingRepository.kt
│       └── FallRepository.kt
└── firebase/
    └── MyFirebaseMessagingService.kt  ← FCM 토큰 갱신, 알림 수신
```

### 네이밍 (Android)

- 클래스: `PascalCase`
- 함수/변수: `camelCase`
- 리소스 파일: `snake_case` (예: `activity_senior_home.xml`, `item_senior_card.xml`)
- 상수: `UPPER_SNAKE_CASE`

### 화면 이동 규칙

로그인 성공 후 `user_type`에 따라 분기:
```kotlin
if (user.userType == "senior") → SeniorHomeActivity
if (user.userType == "guardian") → GuardianHomeActivity
```

이전 화면으로 돌아갈 수 없는 전환: `Intent.FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK`

### 낙상 감지 알고리즘 위치

`FallDetectionManager.kt`에서만 구현. 알고리즘 상세: `docs/fall-detection.md`

### SharedPreferences 키

```kotlin
object PrefKeys {
    const val ACCESS_TOKEN = "access_token"
    const val USER_TYPE    = "user_type"
    const val USER_NAME    = "user_name"
}
```

---

## 커밋 규칙

`/git-commit` 스킬 사용. Co-Authored-By는 `ywlee861009 <ywlee861009@gmail.com>` 고정.

---

## 작업 지시 방법

기능 요청 시 아래 형식으로 말하면 바로 착수:

```
"[backend] 낙상 이벤트 수신 API 구현해줘"
"[android] FallDetectionService 구현해줘"
"[docker] docker-compose 초기 세팅해줘"
"[docs] api-spec.md 작성해줘"
```

태그 없이 말해도 되지만, 태그가 있으면 더 빠르게 파악 가능.
