# 완료-001 백엔드 API MVP

## 상태

MVP 기준 완료.

## 구현된 내용

- FastAPI 앱 진입점과 `/health` 엔드포인트.
- 인증, 페어링, 낙상 이벤트, 기기 토큰 업데이트 라우터.
- `get_current_user` 기반 JWT 보호 엔드포인트.
- 사용자, 페어링, 페어링 코드, 낙상 이벤트 SQLAlchemy async 모델.
- Alembic 초기 스키마 마이그레이션.
- `CLAUDE.md` 기준에 맞춘 `services/` 비즈니스 로직 분리.

## 확인 근거

- `backend/app/main.py`
- `backend/app/routers/*.py`
- `backend/app/services/*.py`
- `backend/alembic/versions/202605150001_initial_schema.py`
- 2026-05-15 기준 `python3 -m compileall backend/app` 통과.
