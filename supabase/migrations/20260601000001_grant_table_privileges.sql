-- Grant table privileges to Supabase roles.
--
-- 신규 프로젝트(2026-06-01 생성)에서 public 스키마 테이블에 대한 권한이
-- service_role/anon/authenticated 에게 자동 부여되지 않아, Edge Functions가
-- service_role 키로 붙어도 RLS 우회 이전에 "permission denied for table"(42501)로
-- 실패했다. 권한을 명시적으로 부여한다.

grant usage on schema public to anon, authenticated, service_role;

-- service_role: Edge Functions 전용. RLS를 우회하므로 전체 DML 권한 필요.
grant all privileges on all tables in schema public to service_role;
grant all privileges on all sequences in schema public to service_role;

-- anon/authenticated: GRANT 레벨 DML 허용. 실제 행 접근은 RLS가 강제한다.
grant select, insert, update, delete on all tables in schema public to anon, authenticated;

-- 이후 생성되는 테이블/시퀀스도 동일 권한을 상속하도록 기본 권한 설정.
alter default privileges in schema public
  grant all privileges on tables to service_role;
alter default privileges in schema public
  grant all privileges on sequences to service_role;
alter default privileges in schema public
  grant select, insert, update, delete on tables to anon, authenticated;
