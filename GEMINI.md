# SeniorSafe 프로젝트 지침

SeniorSafe 멀티 플랫폼 안전 시스템의 기본 지침입니다.

## 🏗 아키텍처
- **Android 멀티 모듈**: `app`, `core`, `feature` 계층을 따르는 클린 아키텍처를 준수합니다.
- **Backend**: FastAPI와 SQLAlchemy/Alembic을 사용합니다.
- **Contract-First**: API 변경 시 구현 전에 `docs/api-spec.md`를 먼저 업데이트해야 합니다.

## 🛠 기술 스택
- **Android**: Kotlin 2.0, Compose, Hilt, Retrofit, DataStore.
- **Backend**: Python 3.12+, FastAPI, PostgreSQL.

## 📏 일반 규칙
- **명명 규칙**: 클래스/타입은 `PascalCase`, 변수/함수는 `camelCase`(Kotlin) 또는 `snake_case`(Python/SQL)를 사용합니다.
- **문서화**: 모든 공개 함수와 복잡한 로직에는 KDoc/Docstring을 작성해야 합니다.
- **테스트**: 모든 새로운 기능이나 버그 수정에는 해당하는 단위/통합 테스트가 필요합니다.
- **커밋**: Conventional Commits 규약을 따릅니다 (feat:, fix:, chore:, docs:, refactor:).

## ⚠️ 보안
- 비밀번호나 API 키를 코드에 직접 하드코딩하지 마십시오.
- 환경 변수나 암호화된 비밀 관리 도구를 사용하십시오.
- 개인 건강 및 위치 데이터를 보호하고 엄격한 접근 제어를 구현하십시오.
