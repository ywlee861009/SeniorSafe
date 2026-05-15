# 할일-010 CI 품질 게이트 추가

## 우선순위

P2

## 문제

현재 빌드와 테스트는 수동 실행 중심이다. 로그인 없는 페어링 구조로 큰 변경이 들어가면 Android 컴파일, 백엔드 테스트, 마이그레이션 문제가 병합 전에 자동으로 잡혀야 한다.

## 작업 범위

- Android debug build CI workflow 추가.
- Android unit test workflow 추가.
- 백엔드 pytest workflow 추가.
- Alembic migration 검증 또는 최소 import/metadata 검증 추가.
- Gradle 및 Python dependency cache 설정.
- 로컬 pre-merge 명령 문서화.
- 필요 시 Android는 ktlint/detekt, 백엔드는 ruff/mypy 같은 정적 분석 도입 검토.

## 완료 조건

- Android 컴파일 오류가 CI에서 실패로 잡힌다.
- 백엔드 테스트 실패가 CI에서 실패로 잡힌다.
- 마이그레이션 또는 모델 import 오류가 CI에서 잡힌다.
- README 또는 티켓 문서에 로컬에서 실행할 동일한 명령이 적혀 있다.
