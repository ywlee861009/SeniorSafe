# 완료-002 백엔드 Device/Pairing 모델 및 API 구현

## 상태

새 기획 기준 완료.

## 구현된 내용

- `Device` 모델 추가.
  - role: `senior` 또는 `guardian`
  - install id hash
  - display name
  - fcm token
  - token hash
  - created_at, last_seen_at
- 기존 `PairingCode` 모델에 device 기반 컬럼 추가.
  - senior_device_id
  - consumed_at
- 기존 `Pairing` 모델에 device 기반 컬럼 추가.
  - senior_device_id
  - guardian_device_id
  - active
  - disconnected_at
- 기기 등록 API 추가.
  - `POST /devices/register`
  - `GET /devices/me`
  - `PUT /devices/fcm-token`
- 어르신 연결 코드 생성 API 추가.
  - `POST /pairing/codes`
- 보호자 연결 코드 사용 API 추가.
  - `POST /pairings`
- 페어링 조회 및 연결 해제 API 추가.
  - `GET /pairings`
  - `DELETE /pairings/{pairing_id}`
- device token 발급 및 인증 의존성 추가.
- Alembic migration 추가.
- 구현 당시에는 기존 user/auth 기반 API와 테스트를 유지했다.

## 현재 기준 메모

2026-05-16 이후 현재 MVP는 device token 인증 전용이다. 이후 신규 작업은 잠금해제 이벤트, 서비스 실행 내역, 미사용 알림 배치를 Device/Pairing 기준으로 추가한다.

## 확인 근거

- `backend/app/models/device.py`
- `backend/app/models/pairing.py`
- `backend/app/models/pairing_code.py`
- `backend/app/core/security.py`
- `backend/app/routers/devices.py`
- `backend/app/routers/pairing.py`
- `backend/app/services/devices_service.py`
- `backend/app/services/pairing_service.py`
- `backend/alembic/versions/202605160001_add_device_pairing_schema.py`
- `backend/tests/test_device_pairing_routes.py`

## 검증

```bash
cd backend && .venv/bin/python -m pytest
```

결과: 32 passed.
