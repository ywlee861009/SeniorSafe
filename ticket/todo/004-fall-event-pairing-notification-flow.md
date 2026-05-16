# 할일-004 낙상 이벤트/취소/FCM을 페어링 기준으로 재구성

## 우선순위

P0

## 문제

낙상 이벤트와 보호자 알림은 제품 핵심이다. User 기반 낙상 API는 이미 제거되었으므로, Device/Pairing 기준의 낙상 이벤트 API를 새로 구현해야 한다.

## 선행 완료 사항

- User 모델, FallEvent 모델, fall 라우터/서비스/스키마 전부 제거 완료
- 백엔드는 Device 토큰 인증 전용으로 전환 완료

## 작업 범위

### 백엔드

- FallEvent 모델 신규 작성: `senior_device_id` FK 기준
- 낙상 API 구현:
  - `POST /fall/events` — 어르신 기기가 낙상 보고 (device 인증)
  - `POST /fall/events/cancelled` — 어르신 기기가 오감지 취소 기록 (device 인증)
  - `GET /fall/history/{senior_device_id}` — 어르신 본인 또는 active pairing 보호자가 이력 조회 (device 인증)
- FCM 발송 대상: active pairing의 보호자 device fcm_token
- 이벤트 상태: reported / cancelled / notify_failed
- Alembic 마이그레이션 작성

### Android

- 30초 취소 정책 확정 및 구현
  - Android가 30초 대기 후 이벤트를 전송할지
  - 즉시 pending 이벤트를 만들고 취소 시 cancel할지
- 취소된 오감지가 보호자에게 전송되지 않도록 보장
- 센서 중복 감지로 같은 카운트다운이 여러 번 열리지 않도록 방지
- 낙상 이력 화면에서 보고됨/취소됨/알림 전송 실패 상태 구분

## 완료 조건

- 페어링된 보호자에게만 낙상 FCM이 발송된다.
- 페어링이 없으면 낙상 이벤트 처리 결과가 명확히 기록되거나 사용자에게 안내된다.
- 30초 안에 취소한 이벤트는 보호자에게 알림이 가지 않는다.
- Android와 백엔드 테스트가 취소/미취소/보호자 없음/FCM 실패를 검증한다.
- 수동 테스트 절차가 문서화되어 있다.
