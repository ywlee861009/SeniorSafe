# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

시니어 낙상 감지 앱. Android 앱 1개(어르신/보호자 두 모드) + FastAPI 백엔드.

설계 문서: `docs/`, UI 설계: `design/`, API 명세: `docs/api-spec.md`

---

## 빌드/테스트/실행 명령어

### 백엔드

```bash
# Docker로 전체 스택 실행 (PostgreSQL + FastAPI + Nginx)
docker-compose up -d --build

# 백엔드 로그
docker-compose logs -f backend

# DB 마이그레이션 (컨테이너 시작 시 자동 실행됨)
docker-compose exec backend alembic upgrade head

# 테스트 (in-memory SQLite 사용, Docker 불필요)
cd backend && python3 -m pytest

# 단일 테스트 파일 실행
cd backend && python3 -m pytest tests/test_auth_routes.py

# 테스트 의존성 설치
pip install -r backend/requirements-dev.txt

# API 문서: http://localhost:8000/docs
# 헬스체크: GET http://localhost:8000/health
```

### Android

```bash
cd android

# 빌드
./gradlew assembleDebug

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :feature:senior:test

# 클린 빌드
./gradlew clean assembleDebug
```

**Android SDK**: compileSdk 35, minSdk 26, targetSdk 35, JVM 17, Kotlin 2.0.21, Compose BOM 2024.12.01

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Python 3.12, FastAPI 0.115, SQLAlchemy 2.0 (async + asyncpg), Alembic |
| Database | PostgreSQL 16 |
| 푸시 알림 | Firebase Cloud Messaging (firebase-admin SDK) |
| 인증 | JWT HS256 (python-jose) + bcrypt |
| 컨테이너 | Docker + Docker Compose |
| 리버스 프록시 | Nginx 1.27 |
| Android | Kotlin 2.0.21, Jetpack Compose, Hilt 2.53.1, Retrofit 2.11, OkHttp 4.12, FCM |

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

- 파일명: `snake_case.py`, 클래스: `PascalCase`, 함수/변수: `snake_case`
- 라우터 파일명 = 기능 단위: `auth.py`, `pairing.py`, `fall.py`, `devices.py`

### DB 세션 패턴

```python
async def endpoint(db: AsyncSession = Depends(get_db)):
    ...
```

### 에러 응답 형식 — 전 엔드포인트 통일

```python
raise HTTPException(status_code=400, detail="에러 메시지")
```

### 인증 방식

- 헤더: `Authorization: Bearer <access_token>`
- 보호된 엔드포인트: `current_user: User = Depends(get_current_user)`
- `get_current_user`는 `core/security.py`에 정의

### 라우터 prefix 규칙

```python
app.include_router(auth_router,    prefix="/auth")
app.include_router(pairing_router, prefix="/pairing")
app.include_router(fall_router,    prefix="/fall")
app.include_router(devices_router, prefix="/devices")
```

---

## 데이터 모델 요약

```
User         — id (UUID), email, password_hash, name, phone, user_type ("senior"|"guardian"), fcm_token
Pairing      — id, senior_id → User, guardian_id → User, status ("pending"|"active"|"disconnected")
FallEvent    — id, senior_id → User, detected_at, cancelled (bool), acknowledged_at
PairingCode  — code (6자리), senior_id → User, expires_at
```

---

## 환경변수

`backend/.env.example` 참조. `.env` 파일은 절대 커밋하지 않는다.

---

## Android 아키텍처 규칙

### 모듈 구조 (멀티모듈)

```
android/
├── build-logic/convention/   ← Convention 플러그인 (보일러플레이트 관리)
├── app/                      ← SingleActivity, AppNavHost, HiltAndroidApp
├── core/
│   ├── model/    ← 데이터 클래스 (API request/response). 의존성 없음
│   ├── network/  ← ApiService(Retrofit), NetworkModule(Hilt)
│   ├── datastore/← TokenDataStore (DataStore Preferences)
│   ├── data/     ← Repository 구현체 (AuthRepository, FallRepository, PairingRepository)
│   └── ui/       ← Compose 테마, 공통 컴포넌트 (SeniorPrimaryButton 등)
└── feature/
    ├── login/    ← LoginScreen, RegisterScreen + ViewModel + navigation
    ├── senior/   ← SeniorHomeScreen, FallAlertScreen, PairingCodeScreen + FallDetectionService
    ├── guardian/ ← GuardianHomeScreen, ConnectSeniorScreen, FallHistoryScreen + FCM 서비스
    └── mvp/      ← MVP 테스트/데모 화면
```

### 의존성 방향 (절대 역방향 금지)

```
app → feature/* → core/*
feature/* → core/*
core/* → core/* (model만 의존 가능, 순환 금지)
```

### Convention 플러그인 사용법

모듈 `build.gradle.kts`는 플러그인 선언만:
```kotlin
plugins {
    alias(libs.plugins.seniorsafe.android.feature)  // library + compose + hilt 전부 포함
}
android { namespace = "com.seniorsafe.feature.xxx" }
```

| 플러그인 | 포함 내용 |
|---------|---------|
| `seniorsafe.android.application` | AGP application + kotlin |
| `seniorsafe.android.library` | AGP library + kotlin |
| `seniorsafe.android.compose` | Compose BOM + Material3 |
| `seniorsafe.android.feature` | library + compose + hilt + navigation + viewmodel |
| `seniorsafe.android.hilt` | Hilt + KSP |

### 화면 추가 절차

1. `feature/{name}/src/main/java/.../XxxScreen.kt` — Composable
2. `feature/{name}/src/main/java/.../XxxViewModel.kt` — @HiltViewModel
3. `feature/{name}/src/main/java/.../navigation/XxxNavigation.kt` — NavGraphBuilder extension
4. `app/navigation/AppNavHost.kt` — 라우트 등록

### Navigation (Single Activity)

- `app/navigation/AppNavHost.kt`가 유일한 NavHost
- 각 feature는 `NavGraphBuilder.xxxGraph(...)` extension으로 라우트 노출
- 화면 전환 콜백은 람다로 주입 (feature → app 의존성 역전 방지)

### 낙상 이벤트 흐름

```
FallDetectionService.onFallDetected()
  → FallRepository.publishFallDetected()  (SharedFlow)
  → SeniorHomeViewModel.fallDetectedEvent 수신
  → LaunchedEffect → onFallDetected() 콜백
  → AppNavHost → navigate(fallAlertRoute)
```

### 네이밍 (Android/Kotlin)

- 클래스: `PascalCase`, 함수/변수: `camelCase`, 상수: `UPPER_SNAKE_CASE`
- Route 상수: `camelCase` + `Route` suffix (예: `seniorHomeRoute`)

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
