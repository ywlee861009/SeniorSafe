# 할일-009 로컬/dev/prod 배포 환경 재정리

## 우선순위

P1

## 문제

백엔드가 Supabase로 전환되면서 Docker Compose 기반 배포 내용은 대부분 무효화됐다. Supabase 플랫폼 기준으로 로컬/프로덕션 환경을 재정리해야 한다.

## 작업 범위

### Supabase 전환으로 변경된 사항
- ~~Docker Compose 배포~~ → Supabase 호스팅 (Edge Functions + PostgreSQL)
- ~~Nginx reverse proxy~~ → Supabase 자체 인프라
- ~~Alembic 마이그레이션~~ → Supabase Migrations
- ~~host cron / backend scheduler~~ → ✅ pg_cron 구현 완료 (매일 00:00 UTC)

### 남은 작업
- `docs/deployment.md`를 Supabase 기준으로 갱신 ✅ 기본 완료, 2026-06-03 기준 FCM HTTP v1/pg_cron 설정값/Android 빌드 전제 최신화 필요
- Supabase Pro 플랜 전환 검토 (pg_cron, 커스텀 도메인)
- `supabase secrets set`으로 환경 변수 설정 문서화
- pg_cron migration에서 사용하는 `app.settings.supabase_url`, `app.settings.cron_secret` 설정 절차 또는 Supabase Vault 기반 대체 절차 문서화
- `net.http_post` 사용을 위한 `pg_net` extension 활성화 필요 여부 검증
- Supabase Database backups 설정
- Edge Function 로그 모니터링 설정
- FCM HTTP v1 운영 secret(`FIREBASE_SERVICE_ACCOUNT`) 설정 및 legacy `FIREBASE_SERVER_KEY` fallback 제거/유지 정책 결정
- Rate limiting 정책 추가 (Supabase Edge Functions 레벨)
- ✅ Android `ANBU_API_BASE_URL`은 `functions/v1/` 형식으로 정렬됨. 로컬/dev/prod flavor 또는 per-machine override 정책은 추가 정리 필요
- ✅ `NetworkModule`의 `FakeApiService` 주입 제거 및 실제 Retrofit provider 전환 완료
- ✅ 로컬 `android/app/google-services.json` 배치 후 debug build 통과. CI용 secret 주입 또는 stub/variant 처리 정책 문서화 필요

## 완료 조건

- 로컬 개발 환경 (`supabase start`) 가이드가 문서화되어 있다.
- 프로덕션 Supabase 프로젝트 설정 절차가 문서화되어 있다.
- 환경 변수(secrets) 관리 방법이 문서화되어 있다.
- 미사용 알림 배치 실행 방식(pg_cron)과 수동 실행 방법이 문서화되어 있다.
- Database backups 설정이 완료되어 있다.
