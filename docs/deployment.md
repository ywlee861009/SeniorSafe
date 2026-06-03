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
supabase secrets set FIREBASE_SERVICE_ACCOUNT="$(cat service-account.json)"
# Legacy FCM fallback only. Prefer FIREBASE_SERVICE_ACCOUNT for real delivery.
supabase secrets set FIREBASE_SERVER_KEY=<legacy-firebase-server-key>
supabase secrets set CRON_SECRET=<random-secret>
```

### pg_cron 설정

`20260527000002_cron_inactivity_check.sql` 마이그레이션이 자동으로 설정:
- 매일 00:00 UTC (09:00 KST)에 `inactivity-check` Edge Function 호출
- `CRON_SECRET`으로 인증

현재 cron migration은 `net.http_post`와 PostgreSQL runtime setting을 사용한다.

```sql
alter database postgres set app.settings.supabase_url = 'https://<project-id>.supabase.co';
alter database postgres set app.settings.cron_secret = '<random-secret>';
```

운영 적용 전에는 다음을 확인한다.

- `pg_cron`과 HTTP 호출용 `pg_net`/`net.http_post` 사용 가능 여부
- `app.settings.supabase_url`, `app.settings.cron_secret` 설정이 실제 cron 실행 세션에서 조회되는지 여부
- 필요하면 Supabase Vault 또는 외부 스케줄러로 대체

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

Android 앱의 `ANBU_API_BASE_URL`을 Supabase Edge Functions URL로 설정한다. `NetworkModule`은 실제 Retrofit client를 주입하고, `ApiService` 상대 경로는 Edge Function 이름을 사용한다.

```kotlin
// android/gradle.properties 또는 빌드 variant별 설정
ANBU_API_BASE_URL=https://<project-id>.supabase.co/functions/v1/
```

로컬 개발 시:
```kotlin
ANBU_API_BASE_URL=http://10.0.2.2:54321/functions/v1/
```

Firebase Messaging을 포함한 현재 Android app module은 `google-services` plugin이 활성화되어 있다. 따라서 debug build도 `android/app/google-services.json` 또는 variant별 `google-services.json`이 없으면 `:app:processDebugGoogleServices`에서 실패한다. 실제 파일은 git에 포함하지 않는다.

## Follow-up Production Tasks

- [ ] Supabase Pro 플랜 전환 (pg_cron, 커스텀 도메인 사용)
- [ ] 커스텀 도메인 설정 (Supabase Dashboard → Settings → Custom Domains)
- [ ] Database backups 설정 (Supabase Dashboard → Database → Backups)
- [ ] Edge Function 로그 모니터링 설정
- [ ] `FIREBASE_SERVICE_ACCOUNT` 기반 FCM HTTP v1 운영 전송 검증
- [ ] legacy `FIREBASE_SERVER_KEY` fallback 제거 또는 유지 정책 결정
- [ ] pg_cron의 `net.http_post`/runtime setting 운영 적용 방식 검증
- [ ] Rate limiting 정책 추가 (Supabase Edge Functions 레벨)
