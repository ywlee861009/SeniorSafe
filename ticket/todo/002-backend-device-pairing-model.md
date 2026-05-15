# 할일-002 백엔드 Device/Pairing 모델 및 API 구현

## 우선순위

P0

## 문제

기존 백엔드는 사용자 계정과 인증 role을 중심으로 동작한다. 새 기획에서는 설치된 앱 기기와 기기 간 페어링 관계를 중심으로 낙상 알림, 이력, 보호자 화면을 처리해야 한다.

## 작업 범위

- `Device` 모델 추가 또는 기존 `User` 모델 대체 전략 결정.
  - role: `senior` 또는 `guardian`
  - install id 또는 서버 발급 device id
  - display name
  - fcm token
  - created_at, last_seen_at
- `PairingCode` 모델 추가.
  - code
  - senior_device_id
  - expires_at
  - consumed_at
  - attempt 제한 또는 rate limit 고려
- `Pairing` 모델 추가.
  - senior_device_id
  - guardian_device_id
  - active
  - created_at
  - disconnected_at
- 기기 등록 API 구현.
- 어르신 연결 코드 생성 API 구현.
- 보호자 연결 코드 사용 API 구현.
- 페어링 조회 및 연결 해제 API 구현.
- Alembic migration 추가.
- 기존 user/auth 기반 API가 새 모델과 충돌하지 않도록 정리한다.

## 완료 조건

- 로그인 없이 기기를 등록할 수 있다.
- 어르신 기기가 일회성 연결 코드를 생성할 수 있다.
- 보호자 기기가 연결 코드를 입력해 어르신 기기와 페어링된다.
- 만료/사용 완료/잘못된 코드는 안전하게 거절된다.
- 백엔드 테스트가 정상/실패/만료/중복 사용 케이스를 검증한다.
