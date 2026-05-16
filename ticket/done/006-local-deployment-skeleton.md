# 완료-006 로컬 배포 골격

## 상태

MVP 배포 골격 기준 완료. 현재 잠금해제 미사용 알림 MVP에서는 배치 실행 설정이 추가로 필요함.

## 구현된 내용

- PostgreSQL, FastAPI 백엔드, Nginx로 구성된 Docker Compose 스택.
- 백엔드 Dockerfile.
- 컨테이너 시작 시 Alembic 마이그레이션 후 Uvicorn 실행.
- Nginx reverse proxy 설정.
- 환경 변수 예시 파일.
- 배포 문서와 API/프로젝트 개요 문서.

## 확인 근거

- `docker-compose.yml`
- `backend/Dockerfile`
- `backend/.env.example`
- `nginx/nginx.conf`
- `docs/overview.md`
- `docs/api-spec.md`
- `docs/deployment.md`

## 현재 기준 메모

2026-05-16 이후 배포 환경에는 미사용 알림 배치를 매일 실행하는 방식과 배치 실패를 확인할 수 있는 운영 로그가 필요하다.
