# 완료-001 로그인 없는 페어링 제품 계약 확정

## 상태

새 기획 기준 완료.

## 결정된 내용

- SeniorSafe MVP는 이메일/비밀번호 로그인 없이 동작한다.
- 앱 첫 실행 시 보호자 또는 어르신 모드를 선택한다.
- 서버의 기본 식별 단위는 사용자 계정이 아니라 설치된 앱 기기(`Device`)다.
- Android는 로컬 `install_id`를 생성하고, 서버는 `device_id`와 `device_access_token`을 발급한다.
- 어르신 앱은 6자리 숫자 연결 코드를 생성한다.
- 보호자 앱은 연결 코드를 입력해 어르신 기기와 페어링한다.
- 연결 코드는 10분 만료, 일회성 사용으로 운영한다.
- 앱 삭제 후 재설치는 새 기기로 취급하고, 기존 페어링은 자동 복구하지 않는다.
- MVP에서는 보호자 1명이 여러 어르신을 볼 수 있고, 어르신 1명에게 여러 보호자가 연결될 수 있다.

## MVP 제외 범위

- 회원가입
- 로그인
- refresh token
- 비밀번호 찾기
- 사용자 계정 기반 데이터 복구
- 다중 기기 계정 동기화

## 변경된 문서

- `docs/overview.md`
- `docs/api-spec.md`
- `ticket/README.md`
- `AGENTS.md`

## 확인 근거

- `docs/overview.md`에 제품 흐름, 기기 식별 정책, 페어링 정책, MVP 제외 범위를 명시했다.
- `docs/api-spec.md`를 device token, device, pairing, fall event 기준 API 계약으로 갱신했다.
- `ticket/README.md`의 추천 우선순위에서 이 티켓을 제거했다.
