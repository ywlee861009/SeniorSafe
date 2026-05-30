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
- `docs/deployment.md`를 Supabase 기준으로 갱신 ✅ 완료
- Supabase Pro 플랜 전환 검토 (pg_cron, 커스텀 도메인)
- `supabase secrets set`으로 환경 변수 설정 문서화
- Supabase Database backups 설정
- Edge Function 로그 모니터링 설정
- Firebase Server Key → FCM HTTP v1 API 마이그레이션 검토
- Rate limiting 정책 추가 (Supabase Edge Functions 레벨)
- Android `BASE_URL` 전환 (`10.0.2.2:8000` → Supabase URL)

## 완료 조건

- 로컬 개발 환경 (`supabase start`) 가이드가 문서화되어 있다.
- 프로덕션 Supabase 프로젝트 설정 절차가 문서화되어 있다.
- 환경 변수(secrets) 관리 방법이 문서화되어 있다.
- 미사용 알림 배치 실행 방식(pg_cron)과 수동 실행 방법이 문서화되어 있다.
- Database backups 설정이 완료되어 있다.
