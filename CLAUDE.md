# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SeniorSafe는 Android 앱 1개(어르신/보호자 두 모드)와 FastAPI 백엔드로 구성된 고령자 안부 확인 앱이다.

현재 MVP는 낙상 감지 제품화를 보류하고, 어르신 휴대폰의 활동 기록(잠금해제, 충전기 연결/해제 등)을 백엔드에 저장한 뒤 마지막 활동 후 2일이 지나면 보호자에게 FCM 푸시를 보내는 방향이다.

설계 문서: `docs/`, UI 설계: `design/`, API 명세: `docs/api-spec.md`, 티켓: `ticket/`

---

## 빌드/테스트/실행 명령어

### 백엔드

```bash
# Docker로 전체 스택 실행 (PostgreSQL + FastAPI + Nginx)
docker compose up -d --build

# 백엔드 로그
docker compose logs -f backend

# DB 마이그레이션 (컨테이너 시작 시 자동 실행됨)
docker compose exec backend alembic upgrade head

# 테스트 (in-memory SQLite 사용, Docker 불필요)
cd backend && .venv/bin/python -m pytest

# 테스트 의존성 설치
cd backend
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-dev.txt

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

# 클린 빌드
./gradlew clean assembleDebug
```

Android SDK: compileSdk 35, minSdk 26, targetSdk 35, JVM 17, Kotlin 2.0.21, Compose BOM 2024.12.01

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Python 3.12, FastAPI 0.115, SQLAlchemy 2.0 async, Alembic |
| Database | PostgreSQL 16 |
| 푸시 알림 | Firebase Cloud Messaging |
| 기기 인증 | 서버 발급 device access token |
| 컨테이너 | Docker + Docker Compose |
| 리버스 프록시 | Nginx 1.27 |
| Android | Kotlin 2.0.21, Jetpack Compose, Hilt, Retrofit, OkHttp, FCM |
| Android 로컬 로그 | Room |

---

## 백엔드 아키텍처 규칙

### 레이어 역할

| 레이어 | 역할 | 금지 사항 |
|--------|------|----------|
| `routers/` | 요청 수신, service 호출, 응답 반환 | DB 직접 접근, 비즈니스 로직 |
| `services/` | 비즈니스 로직, DB 조작, FCM 호출 | HTTP 요청/응답 조립 |
| `models/` | 테이블 정의 | 비즈니스 로직 |
| `schemas/` | 입출력 스키마 | DB 접근 |

### 파일 네이밍

- 파일명: `snake_case.py`
- 클래스: `PascalCase`
- 함수/변수: `snake_case`
- 라우터 파일명은 기능 단위로 둔다. 현재 핵심 후보: `devices.py`, `pairing.py`, `activity.py`, `batches.py`

### 인증 방식

- 헤더: `Authorization: Bearer <device_access_token>`
- 보호된 엔드포인트: `current_device: Device = Depends(get_current_device)`
- `get_current_device`는 `core/security.py`에 정의한다.
- 사용자 계정/JWT 로그인은 현재 MVP 범위가 아니다.

### 라우터 prefix 기준

```python
app.include_router(devices_router, prefix="/devices")
app.include_router(pairing_router, prefix="/pairing")
app.include_router(pairings_router, prefix="/pairings")
app.include_router(activity_router, prefix="/activity")
```

배치 실행은 CLI, scheduler, 또는 보호된 internal endpoint 중 하나로 구현한다.

---

## 데이터 모델 요약

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
├── senior_device_id
├── expires_at
├── consumed_at
└── created_at

Pairing
├── id
├── senior_device_id
├── guardian_device_id
├── active
├── status
├── created_at
└── disconnected_at

ActivityEvent
├── id
├── senior_device_id
├── occurred_at
├── received_at
└── source               (user_present | power_connected | power_disconnected)

ServiceEvent
├── id
├── device_id
├── event_type
├── occurred_at
├── received_at
└── detail

InactivityAlert
├── id
├── senior_device_id
├── guardian_device_id
├── threshold_days
├── last_activity_at
├── sent_at
├── status
└── detail
```

낙상 관련 `FallEvent`는 현재 MVP에서 보류한다.

---

## Android 아키텍처 규칙

### 모듈 구조

```text
android/
├── build-logic/convention/
├── app/
├── core/
│   ├── model/
│   ├── network/
│   ├── datastore/
│   ├── data/
│   ├── diagnostics/
│   └── ui/
└── feature/
    ├── login/      ← 현재 MVP 진입 흐름에서는 제거/비노출 대상
    ├── senior/
    ├── guardian/
    └── mvp/
```

낙상 감지 런타임은 `core:fall-detection`에 있으나 제품화는 보류한다. 서비스 생존/heartbeat/Room 로그 패턴은 활동 모니터링 서비스에서 재사용할 수 있다.

### 의존성 방향

```text
app → feature/* → core/*
feature/* → core/*
core/* → core/* (순환 금지)
```

### 현재 MVP Android 핵심 흐름

```text
ACTION_USER_PRESENT
  → 로컬 UnlockEvent 기록
  → 백엔드 POST /activity/events 업로드
  → 실패 시 미전송 이벤트로 보관
  → 재시도 후 업로드 성공/실패 로그 기록
```

서비스 실행 내역:

```text
ActivityMonitorService
  → started / stopped / heartbeat / error 로컬 기록
  → 백엔드 POST /activity/service-events 업로드
```

### 네이밍

- 클래스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- Route 상수: `camelCase` + `Route` suffix

---

## 커밋 규칙

`/git-commit` 스킬 사용. Co-Authored-By는 `ywlee861009 <ywlee861009@gmail.com>` 고정.

---

## 작업 지시 예시

```text
"[backend] 활동 이벤트 수신 API 구현해줘"
"[backend] 미사용 알림 배치 구현해줘"
"[android] ACTION_USER_PRESENT 기록 구현해줘"
"[android] 활동 모니터링 서비스 구현해줘"
"[docs] api-spec.md 갱신해줘"
```
