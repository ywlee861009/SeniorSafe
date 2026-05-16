# 할일-004 잠금해제 미사용 알림 플로우 구현

## 우선순위

P0

## 문제

낙상 감지 기능은 MVP 제품화 범위에서 잠시 보류한다. 대신 어르신 휴대폰이 N일 동안 잠금해제되지 않으면 보호자에게 안부 확인 푸시를 보내는 기능을 MVP 핵심으로 구현한다.

이 기능은 센서 알고리즘보다 기술 리스크가 낮고, 서비스 실행 내역과 잠금해제 내역을 DB에 남기면 실제 기기 동작을 검증하기 쉽다.

## 선행 완료 사항

- 백엔드는 Device 토큰 인증 전용으로 전환 완료.
- Device/Pairing/PairingCode 모델과 API 구현 완료.
- Android에는 Room 기반 진단 로그 저장 구조와 foreground service heartbeat 패턴이 있다.

## 작업 범위

### 백엔드

- Device 모델에 활동 상태 필드 추가:
  - `last_unlocked_at`
  - `inactivity_threshold_days` 또는 전역 기본값 사용
- 잠금해제 이벤트 모델 추가:
  - `UnlockEvent`
  - `senior_device_id`
  - `unlocked_at`
  - `received_at`
  - `source`
- 서비스 실행 이벤트 모델 추가:
  - `ServiceEvent`
  - `device_id`
  - `event_type`
  - `occurred_at`
  - `received_at`
  - `detail`
- 미사용 알림 모델 추가:
  - `InactivityAlert`
  - `senior_device_id`
  - `guardian_device_id`
  - `threshold_days`
  - `last_unlocked_at`
  - `sent_at`
  - `status`
  - `detail`
- 활동 API 구현:
  - `POST /activity/unlocks`
  - `GET /activity/unlocks/{senior_device_id}`
  - `POST /activity/service-events`
  - `GET /activity/service-events/{device_id}`
  - `GET /activity/inactivity-alerts/{senior_device_id}`
- 미사용 알림 배치 구현:
  - 기본 기준: 마지막 잠금해제 2일 경과
  - active pairing 보호자 FCM token 조회
  - 같은 미사용 상태에 대한 중복 발송 제한
  - FCM 성공/실패 로그 저장
- Alembic 마이그레이션 작성.
- pytest로 권한, 기록, 배치, 중복 발송 제한을 검증.

### Android

- 어르신 모드에서 잠금해제 감지 구현:
  - `ACTION_USER_PRESENT` receiver
  - 잠금해제 시각 로컬 DB 기록
  - 백엔드 `POST /activity/unlocks` 업로드
  - 네트워크 실패 시 미전송 이벤트 보관 및 재시도
- 활동 모니터링 foreground service 구현 또는 기존 서비스 구조 재사용:
  - 서비스 시작/중지/heartbeat/error 로컬 DB 기록
  - 주요 서비스 이벤트 백엔드 업로드
  - 부팅 후 재시작 정책 검토
- MVP 진단 화면에서 확인 가능한 내역:
  - 서비스 실행 내역
  - 잠금해제 내역
  - 업로드 성공/실패 내역
- 보호자 화면에서 마지막 잠금해제 시각 표시.
- 보호자 FCM 수신 시 미사용 알림으로 표시.

## 정책 결정

- MVP 기본 임계값은 2일이다.
- 알림 문구는 위험을 단정하지 않는다.
  - 예: `홍길동님 휴대폰 사용 기록이 2일 동안 확인되지 않았습니다. 안부 확인이 필요할 수 있습니다.`
- 배터리 방전, 네트워크 끊김, 앱 강제 종료, 제조사 백그라운드 제한 때문에 100% 안전 보장 기능으로 표현하지 않는다.
- 제품 문구는 "잠금해제되지 않음"보다 "휴대폰 사용 기록이 확인되지 않음"을 우선 사용한다.

## 완료 조건

- 어르신 폰 잠금해제 시 로컬 DB와 백엔드 DB에 이벤트가 기록된다.
- 백엔드는 어르신 기기의 `last_unlocked_at`을 갱신한다.
- 서비스 시작/중지/heartbeat/error 내역이 로컬 DB와 백엔드 DB에서 확인된다.
- 매일 배치 실행 시 마지막 잠금해제 2일 경과 어르신을 찾아 보호자에게 FCM을 보낸다.
- 같은 상태에서 중복 알림이 과도하게 발송되지 않는다.
- 보호자는 연결된 어르신의 마지막 잠금해제 시각과 미사용 알림 이력을 볼 수 있다.
- Android와 백엔드 테스트가 잠금해제 기록, 권한 제한, 배치 발송, FCM 실패를 검증한다.
