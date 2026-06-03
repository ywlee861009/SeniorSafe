# SeniorSafe Supabase Backend

## 구조

```
supabase/
├── config.toml                          # Supabase 로컬 설정
├── .env.example                         # 환경변수 템플릿
├── migrations/
│   ├── 20260527000001_initial_schema.sql   # 테이블 + RLS
│   ├── 20260527000002_cron_inactivity_check.sql  # 비활동 배치
│   └── 20260530000001_add_service_events.sql     # 서비스 이벤트
└── functions/
    ├── _shared/                          # 공통 유틸리티
    │   ├── cors.ts
    │   ├── supabase.ts
    │   ├── jwt.ts
    │   └── auth.ts
    ├── device-register/                  # POST - 기기 등록
    ├── fcm-token/                        # PUT - FCM 토큰 갱신
    ├── pairing-codes/                    # POST - 페어링 코드 생성 (senior)
    ├── pairing-claim/                    # POST - 페어링 코드 사용 (guardian)
    ├── pairing-disconnect/               # POST - 페어링 해제
    ├── pairings-list/                    # GET - 페어링 목록 조회
    ├── activity-events/                  # POST - 활동 이벤트 업로드 (senior)
    ├── activity-events-list/             # GET - 활동 이벤트 조회
    ├── service-events/                   # POST - 서비스 이벤트 업로드
    ├── service-events-list/              # GET - 서비스 이벤트 조회
    ├── inactivity-alerts-list/           # GET - 미사용 알림 이력 조회
    └── inactivity-check/                 # POST - 비활동 배치 (cron)
```

## API 엔드포인트 매핑

| 기존 FastAPI | Supabase Edge Function | Method |
|---|---|---|
| `POST /devices/register` | `/functions/v1/device-register` | POST |
| `GET /devices/me` | PostgREST: `/rest/v1/devices?id=eq.{id}` | GET |
| `PUT /devices/fcm-token` | `/functions/v1/fcm-token` | PUT |
| `POST /pairing/codes` | `/functions/v1/pairing-codes` | POST |
| `POST /pairings` | `/functions/v1/pairing-claim` | POST |
| `GET /pairings` | `/functions/v1/pairings-list` | GET |
| `DELETE /pairings/{id}` | `/functions/v1/pairing-disconnect` | POST |
| `POST /activity/events` | `/functions/v1/activity-events` | POST |
| `GET /activity/events/{senior}` | `/functions/v1/activity-events-list` | GET |
| `POST /activity/service-events` | `/functions/v1/service-events` | POST |
| `GET /activity/service-events/{device}` | `/functions/v1/service-events-list` | GET |
| `GET /activity/inactivity-alerts/{senior}` | `/functions/v1/inactivity-alerts-list` | GET |
| (배치) | `/functions/v1/inactivity-check` | POST |

## 설정 방법

### 1. Supabase 프로젝트 생성

1. https://supabase.com 에서 프로젝트 생성
2. Dashboard > Settings > API에서 키 확인:
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`
   - `SUPABASE_SERVICE_ROLE_KEY`
   - `SUPABASE_JWT_SECRET` (Settings > API > JWT Secret)

### 2. Supabase CLI 설치

```bash
npm install -g supabase
supabase login
supabase link --project-ref your-project-id
```

### 3. 마이그레이션 실행

```bash
supabase db push
```

### 4. Edge Functions 배포

```bash
supabase functions deploy device-register
supabase functions deploy fcm-token
supabase functions deploy pairing-codes
supabase functions deploy pairing-claim
supabase functions deploy pairing-disconnect
supabase functions deploy pairings-list
supabase functions deploy activity-events
supabase functions deploy activity-events-list
supabase functions deploy service-events
supabase functions deploy service-events-list
supabase functions deploy inactivity-alerts-list
supabase functions deploy inactivity-check
```

### 5. 환경변수 설정

```bash
supabase secrets set FIREBASE_SERVICE_ACCOUNT="$(cat service-account.json)"
# Legacy FCM fallback only. Prefer FIREBASE_SERVICE_ACCOUNT for real delivery.
supabase secrets set FIREBASE_SERVER_KEY=your-legacy-key
supabase secrets set CRON_SECRET=your-secret
```

## 인증 방식

- 기기 등록 시 커스텀 JWT 발급 (Supabase JWT Secret으로 서명)
- 이 토큰은 PostgREST와 호환 → RLS에서 `auth.uid()` = device_id
- 토큰 유효기간: 365일
- Android에서 `Authorization: Bearer <token>` 헤더로 전송

## Android 연동

Android `core:network`는 `ANBU_API_BASE_URL`을 Supabase Functions root로 두고, Retrofit 상대 경로는 Edge Function 이름을 사용한다.

```kotlin
// Base URL: https://your-project-id.supabase.co/functions/v1/
@POST("device-register")
suspend fun register(@Body request: RegisterRequest): DeviceRegisterResponse
```

로컬 Android emulator에서는 `ANBU_API_BASE_URL=http://10.0.2.2:54321/functions/v1/`로 override한다. Firebase Messaging을 빌드하려면 `android/app/google-services.json` 또는 variant별 파일이 필요하며, 실제 파일은 git에 포함하지 않는다.
