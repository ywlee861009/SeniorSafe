# Backend 프로젝트 지침

## 🐍 Python 및 FastAPI
- 모든 함수 시그니처와 변수에 Type Hint를 사용합니다.
- PEP 8 스타일 가이드를 준수합니다. (black, isort, flake8 사용 권장)
- 데이터베이스 세션 및 보안을 위해 FastAPI의 의존성 주입(DI)을 활용합니다.

## 📂 디렉토리 구조
- `app/routers/`: API 엔드포인트 정의 및 요청 처리.
- `app/services/`: 비즈니스 로직 및 외부 서비스 연동.
- `app/models/`: SQLAlchemy 데이터베이스 모델.
- `app/schemas/`: Pydantic 요청/응답 스키마.

## 🗄 데이터베이스 및 Alembic
- 모델은 `app/models/`에 정의합니다.
- 모든 스키마 마이그레이션에는 `Alembic`을 사용합니다.
- 운영 환경에서 `Base.metadata.create_all()`을 직접 사용하지 마십시오.

## 📝 API 문서화 및 설계
- 모든 엔드포인트는 `docs/api-spec.md`에 먼저 정의되어야 합니다.
- FastAPI의 자동 생성 OpenAPI 문서를 적극 활용하며, `summary`와 `description`을 상세히 작성합니다.

## 🧪 테스트
- 단위 및 통합 테스트에는 `pytest`를 사용합니다.
- 단위 테스트 시 외부 서비스(예: FCM)는 Mock 처리합니다.
- 통합 테스트에는 별도의 테스트용 데이터베이스를 사용합니다.
