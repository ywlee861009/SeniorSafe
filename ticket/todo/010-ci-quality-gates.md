# 할일-010 CI 품질 게이트 추가

## 우선순위

P2

## 문제

현재 빌드와 테스트는 수동 실행 중심이다. 로그인 없는 페어링과 잠금해제 미사용 알림 구조로 큰 변경이 들어가면 Android 컴파일, 백엔드 테스트, 마이그레이션, 배치 문제가 병합 전에 자동으로 잡혀야 한다.

2026-05-20 현재 확인된 수동 검증은 `cd backend && .venv/bin/python -m pytest`이며 device/pairing 테스트 6개가 통과한다. Android는 2026-05-18 기준 `./gradlew assembleDebug` 통과 기록이 ticket README에 남아 있으나 현재 CI는 없다.

## 작업 범위

- Android debug build CI workflow 추가.
- Android unit test workflow 추가.
- 백엔드 pytest workflow 추가.
- 우선 현재 통과 중인 device/pairing pytest를 CI 기준선으로 등록한다.
- Alembic migration 검증 또는 최소 import/metadata 검증 추가.
- 미사용 알림 배치 테스트가 CI에서 실행되도록 추가.
- Gradle 및 Python dependency cache 설정.
- 로컬 pre-merge 명령 문서화.
- 필요 시 Android는 ktlint/detekt, 백엔드는 ruff/mypy 같은 정적 분석 도입 검토.

## 완료 조건

- Android 컴파일 오류가 CI에서 실패로 잡힌다.
- 백엔드 테스트 실패가 CI에서 실패로 잡힌다.
- 마이그레이션 또는 모델 import 오류가 CI에서 잡힌다.
- 잠금해제 이벤트와 미사용 알림 배치 회귀가 테스트에서 잡힌다.
- README 또는 티켓 문서에 로컬에서 실행할 동일한 명령이 적혀 있다.
