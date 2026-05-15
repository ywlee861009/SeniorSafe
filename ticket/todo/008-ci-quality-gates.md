# 할일-008 CI 품질 게이트 추가

## 우선순위

P2

## 문제

이번 분석에서는 빌드와 문법 검사를 수동으로 실행했습니다. 앞으로는 변경사항 병합 전에 같은 검증이 자동으로 실행되어야 합니다.

## 작업 범위

- Android debug build CI workflow 추가.
- TODO-001에서 백엔드 테스트가 생긴 뒤 backend lint/test workflow 추가.
- Gradle 및 Python dependency cache 설정.
- 로컬 pre-merge 명령 문서화.
- 필요 시 Android는 ktlint/detekt, 백엔드는 ruff/mypy 같은 정적 분석 도입 검토.

## 완료 조건

- Android 컴파일 오류가 CI에서 실패로 잡힌다.
- 백엔드 테스트 실패가 CI에서 실패로 잡힌다.
- README 또는 티켓 문서에 로컬에서 실행할 동일한 명령이 적혀 있다.
