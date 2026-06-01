# Deployment

## Supabase 호스팅

백엔드는 Supabase 플랫폼(Edge Functions + PostgreSQL + pg_cron)에서 운영한다.

### 프로젝트 설정

1. [supabase.com](https://supabase.com)에서 프로젝트 생성
2. 환경 변수 확인: Dashboard → Settings → API에서 `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_JWT_SECRET` 확인

### 마이그레이션 적용

```bash
supabase link --project-ref <project-id>
supabase db push
```

마이그레이션 파일:
- `20260527000001_initial_schema.sql` — 5개 테이블 + RLS
- `20260527000002_cron_inactivity_check.sql` — pg_cron 스케줄러
- `20260530000001_add_service_events.sql` — service_events 테이블

### Edge Functions 배포

```bash
# 전체 배포
supabase functions deploy

# 개별 배포
supabase functions deploy device-register
supabase functions deploy activity-events
# ...
```

### 환경 변수 (Secrets)

```bash
supabase secrets set SUPABASE_URL=https://<project-id>.supabase.co
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
supabase secrets set SUPABASE_JWT_SECRET=<jwt-secret>
supabase secrets set FIREBASE_SERVER_KEY=<firebase-server-key>
supabase secrets set CRON_SECRET=<random-secret>
```

### pg_cron 설정

`20260527000002_cron_inactivity_check.sql` 마이그레이션이 자동으로 설정:
- 매일 00:00 UTC (09:00 KST)에 `inactivity-check` Edge Function 호출
- `CRON_SECRET`으로 인증

pg_cron 사용 가능 여부는 Supabase 플랜과 프로젝트 설정에 따라 확인한다. 사용할 수 없는 환경에서는 외부 스케줄러(GitHub Actions, cron-job.org 등)로 대체:

```bash
curl -X POST https://<project-id>.supabase.co/functions/v1/inactivity-check \
  -H "Authorization: Bearer <CRON_SECRET>" \
  -H "Content-Type: application/json"
```

## 로컬 개발

```bash
# Supabase CLI 설치
brew install supabase/tap/supabase

# 로컬 스택 시작 (PostgreSQL, Studio, Edge Functions 런타임)
supabase start

# 마이그레이션 적용
supabase db push

# Edge Function 로컬 실행
supabase functions serve

# 테스트
cd supabase/functions && deno test --config=tests/deno.json tests/ --allow-env --allow-net
```

로컬 엔드포인트:
- API: `http://localhost:54321/functions/v1/<function-name>`
- Studio: `http://localhost:54323`
- PostgreSQL: `localhost:54322`

## Android 연결

Android 앱의 `ANBU_API_BASE_URL`을 Supabase Edge Functions URL로 설정한다. 현재 `NetworkModule`은 Retrofit 대신 `FakeApiService`를 주입하고 있으므로, 실제 서버 연결 작업에서는 `BuildConfig.ANBU_API_BASE_URL` 기반 Retrofit provider와 Edge Function 경로 매핑을 함께 복구해야 한다.

```kotlin
// android/gradle.properties 또는 빌드 variant별 설정
ANBU_API_BASE_URL=https://<project-id>.supabase.co/functions/v1/
```

로컬 개발 시:
```kotlin
ANBU_API_BASE_URL=http://10.0.2.2:54321/functions/v1/
```

## Follow-up Production Tasks

- [ ] Supabase Pro 플랜 전환 (pg_cron, 커스텀 도메인 사용)
- [ ] 커스텀 도메인 설정 (Supabase Dashboard → Settings → Custom Domains)
- [ ] Database backups 설정 (Supabase Dashboard → Database → Backups)
- [ ] Edge Function 로그 모니터링 설정
- [ ] Firebase Server Key를 FCM HTTP v1 API로 마이그레이션
- [ ] Rate limiting 정책 추가 (Supabase Edge Functions 레벨)
