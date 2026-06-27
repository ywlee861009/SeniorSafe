# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SeniorSafe는 Android 앱 1개(어르신/보호자 두 모드)와 Supabase 백엔드(Edge Functions + PostgreSQL)로 구성된 고령자 안부 확인 앱이다.

현재 MVP는 낙상 감지 제품화를 보류하고, 어르신 휴대폰의 활동 기록(잠금해제, 충전기 연결/해제 등)을 백엔드에 저장한 뒤 마지막 활동 후 2일이 지나면 보호자에게 FCM 푸시를 보내는 방향이다.

**구현 현황**: 백엔드 Edge Functions 12개 전부 구현 완료(기기 등록, 페어링, 활동 이벤트, 서비스 이벤트, 미활동 알림 배치). 테스트 52개 통과. Android는 활동 업로드 미연결 — 로컬 Room 기록까지만 구현됨.

설계 문서: `docs/`, UI 설계: `design/`, API 명세: `docs/api-spec.md`, 티켓: `ticket/`

---

## 빌드/테스트/실행 명령어

### 백엔드 (Supabase)

```bash
# 로컬 Supabase 스택 실행
supabase start

# 마이그레이션 적용
supabase db push

# Edge Functions 개별 배포
supabase functions deploy <function-name>

# 전체 Functions 배포
supabase functions deploy

# 테스트 (Deno, 로컬 실행)
cd supabase/functions && deno test --config=tests/deno.json tests/ --allow-env --allow-net

# API URL: http://localhost:54321/functions/v1/<function-name>
# Studio: http://localhost:54323
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
| Backend | Supabase Edge Functions (Deno/TypeScript) |
| Database | Supabase PostgreSQL (RLS 적용) |
| 스키마 관리 | Supabase Migrations |
| 배치 스케줄러 | pg_cron (매일 00:00 UTC) |
| 푸시 알림 | Firebase Cloud Messaging (Legacy HTTP API) |
| 기기 인증 | 커스텀 device JWT (HS256, djwt) + RLS |
| Android | Kotlin 2.0.21, Jetpack Compose, Hilt, Retrofit, OkHttp, FCM |
| Android 로컬 로그 | Room |

---

## 백엔드 아키텍처 규칙

### 구조

```text
supabase/
├── config.toml                    # Supabase 로컬 설정
├── .env.example                   # 환경 변수 템플릿
├── migrations/                    # PostgreSQL 마이그레이션
│   ├── 20260527000001_initial_schema.sql
│   ├── 20260527000002_cron_inactivity_check.sql
│   ├── 20260530000001_add_service_events.sql
│   └── 20260601000001_grant_table_privileges.sql
└── functions/
    ├── _shared/                   # 공유 유틸 (cors, auth, jwt, supabase client)
    ├── <function-name>/index.ts   # 각 Edge Function
    └── tests/                     # Deno 테스트 (52개)
```

### Edge Function 네이밍

- 디렉토리: `kebab-case` (예: `activity-events-list`)
- 핸들러: `export const handler` + `serve(handler)` 패턴
- 공유 모듈: `_shared/` 하위에 배치

### 인증 방식

- 커스텀 device JWT (HS256, DEVICE_JWT_SECRET으로 서명, 365일 만료) — `SUPABASE_` 접두사는 함수 secret으로 예약되어 쓸 수 없음
- 헤더: `Authorization: Bearer <device_access_token>`
- `getDeviceFromRequest(req)` → `DeviceInfo { id, role, display_name }` 반환
- Edge Functions는 `service_role` key로 RLS 우회
- RLS가 DB 레이어에서 보안 강제 (5개 테이블 적용)
- 사용자 계정/Supabase Auth 로그인은 MVP 범위가 아니다

> **Android 구현 현황** (2026-06-28): device_access_token 발급/저장/주입 경계 구현됨. `device-register` 응답 토큰을 `TokenDataStore.saveDeviceAuth()`로 저장하고(저장 시 `TokenCipher`로 AES/GCM 암호화), `NetworkModule`의 OkHttp Interceptor가 `getDeviceAccessToken()`을 `Authorization: Bearer`로 주입한다. JWT 서명 secret은 `DEVICE_JWT_SECRET`(`SUPABASE_` 접두사는 예약어라 사용 불가). `NetworkModule`은 실제 Retrofit을 `ApiService`로 제공하며(`FakeApiService` 삭제), `ApiService` 경로는 실제 Edge Function 이름과 일치한다. 어르신 활동/서비스 이벤트 배치 업로드도 연결 완료(`ActivityMonitorService` + `ActivityUploadWorker`). 잔여: 실기기 E2E(`ticket/todo/005` Firebase 런타임 설정).

### Edge Function 구현 상태

| Function | 구현 상태 |
|---|---|
| `device-register` | ✅ 기기 등록 + 토큰 발급 |
| `fcm-token` | ✅ FCM 토큰 갱신 |
| `pairing-codes` | ✅ 페어링 코드 생성 (시니어) |
| `pairing-claim` | ✅ 페어링 코드 사용 (보호자) |
| `pairing-disconnect` | ✅ 페어링 해제 |
| `pairings-list` | ✅ 페어링 목록 (역할별) |
| `activity-events` | ✅ 활동 이벤트 수신 (시니어) |
| `activity-events-list` | ✅ 활동 이벤트 조회 |
| `service-events` | ✅ 서비스 이벤트 수신 |
| `service-events-list` | ✅ 서비스 이벤트 조회 |
| `inactivity-check` | ✅ 미활동 알림 배치 (pg_cron, 중복 방지 포함) |
| `inactivity-alerts-list` | ✅ 미사용 알림 이력 조회 |

배치 실행은 pg_cron (매일 00:00 UTC)으로 `inactivity-check` Edge Function을 HTTP 호출한다.

---

## 데이터 모델 요약 (Supabase PostgreSQL)

6개 테이블, 전부 RLS 적용. 마이그레이션 4개로 관리.

```text
devices              ← 기기 등록/인증
├── id (UUID PK), install_id_hash, role, display_name
├── fcm_token, token_hash, created_at, last_seen_at
├── last_activity_at, inactivity_threshold_days (default 2)

pairing_codes        ← 6자리 페어링 코드 (10분 만료)
├── code (PK), senior_device_id (FK), expires_at, consumed_at

pairings             ← 어르신-보호자 연결 관계
├── id (UUID PK), senior_device_id (FK), guardian_device_id (FK)
├── status (pending|active|disconnected), active, disconnected_at

activity_events      ← 어르신 활동 이벤트 (잠금해제, 충전 등)
├── id (UUID PK), senior_device_id (FK)
├── occurred_at, received_at, source (user_present|power_connected|power_disconnected)

service_events       ← 서비스 생명주기 이벤트
├── id (UUID PK), device_id (FK)
├── event_type (started|stopped|heartbeat|error), occurred_at, received_at, detail

inactivity_alerts    ← 미활동 알림 발송 이력
├── id (UUID PK), senior_device_id (FK), guardian_device_id (FK)
├── threshold_days, last_activity_at, sent_at, status (sent|skipped|failed), detail
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
│   ├── util/
│   ├── activity/   ← 활동 모니터링 서비스 + Room 기록 + 배치 업로드
│   └── ui/
└── feature/
    ├── senior/
    ├── guardian/
    └── mvp/
```

낙상 감지 제품화는 보류 상태이며, 관련 런타임 코드(`core:fall-detection` 모듈, `FallDetectionService`)는 이미 제거되어 트리에 존재하지 않는다 — 남은 것은 티켓 문서(`ticket/done/007`, `ticket/todo/008`)뿐이다. 서비스 생존/heartbeat/Room 로그 패턴은 `core:activity`의 활동 모니터링 서비스에서 재사용했다.

### 의존성 방향

```text
app → feature/* → core/*
feature/* → core/*
core/* → core/* (순환 금지)
```

### 현재 MVP Android 핵심 흐름

```text
ACTION_USER_PRESENT / POWER_CONNECTED / POWER_DISCONNECTED
  → ActivityRepository.recordUnlock(now, source)
  → Room unlock_events 테이블 insert (uploaded=false)
  → 백엔드 POST activity-events(배치 {events:[...]}) 업로드 + 실패 시 재전송
  (ActivityMonitorService가 unlock/power/heartbeat 시 uploadPendingEvents() 호출, ActivityUploadWorker(WorkManager)가 재전송 담당)
```

서비스 실행 내역:

```text
ActivityMonitorService
  → started / stopped / heartbeat / error 로컬 기록 (Room service_events + DiagnosticsLogStore)
  → 백엔드 POST service-events(배치 {events:[...]}) 업로드
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
