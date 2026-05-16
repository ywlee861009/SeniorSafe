# API Spec

Base URL:

```text
http://<server-host>/
```

MVP는 사용자 로그인 없이 동작한다. 보호가 필요한 엔드포인트는 기기 등록 시 발급받은 device token을 사용한다.

```http
Authorization: Bearer <device_access_token>
```

## Health

`GET /health`

```json
{"status":"ok"}
```

## Devices

### Register Device

`POST /devices/register`

요청:

```json
{
  "install_id": "local-install-uuid",
  "role": "senior",
  "display_name": "홍길동",
  "fcm_token": "firebase-token"
}
```

응답:

```json
{
  "device_id": "uuid",
  "role": "senior",
  "display_name": "홍길동",
  "device_access_token": "..."
}
```

정책:

- `role`은 `senior` 또는 `guardian`이다.
- `install_id`는 Android 로컬 저장소에서 생성한다.
- 앱 삭제/재설치로 `install_id`가 사라지면 새 기기로 등록한다.

### Update FCM Token

`PUT /devices/fcm-token`

인증: device token 필요.

```json
{"fcm_token":"firebase-token"}
```

응답:

```json
{"message":"FCM token updated"}
```

### Get Current Device

`GET /devices/me`

인증: device token 필요.

```json
{
  "device_id": "uuid",
  "role": "guardian",
  "display_name": "보호자",
  "created_at": "2026-05-16T00:00:00Z",
  "last_seen_at": "2026-05-16T00:10:00Z",
  "last_activity_at": null,
  "inactivity_threshold_days": 2
}
```

## Pairing

### Create Pairing Code

`POST /pairing/codes`

인증: senior device token 필요.

응답:

```json
{
  "code": "482913",
  "expires_at": "2026-05-16T00:10:00Z",
  "expires_in_seconds": 600
}
```

정책:

- 코드는 6자리 숫자다.
- 코드는 10분 동안 유효하다.
- 코드는 일회성이다.

### Claim Pairing Code

`POST /pairings`

인증: guardian device token 필요.

요청:

```json
{"code":"482913"}
```

응답:

```json
{
  "pairing_id": "uuid",
  "senior_device_id": "uuid",
  "senior_display_name": "홍길동",
  "created_at": "2026-05-16T00:05:00Z"
}
```

오류:

- 만료된 코드
- 이미 사용된 코드
- 존재하지 않는 코드
- 보호자가 아닌 기기의 요청

### List Pairings

`GET /pairings`

인증: device token 필요.

보호자 응답:

```json
{
  "pairings": [
    {
      "pairing_id": "uuid",
      "senior_device_id": "uuid",
      "senior_display_name": "홍길동",
      "last_seen_at": "2026-05-16T00:05:00Z",
      "last_activity_at": "2026-05-16T08:30:00Z",
      "inactivity_threshold_days": 2,
      "active": true
    }
  ]
}
```

어르신 응답:

```json
{
  "pairings": [
    {
      "pairing_id": "uuid",
      "guardian_device_id": "uuid",
      "guardian_display_name": "보호자",
      "active": true
    }
  ]
}
```

### Disconnect Pairing

`DELETE /pairings/{pairing_id}`

인증: pairing에 속한 senior 또는 guardian device token 필요.

응답:

```json
{
  "pairing_id": "uuid",
  "active": false
}
```

## Activity

### Record Activity Event

`POST /activity/events`

인증: senior device token 필요.

어르신 기기에서 활동 신호(잠금해제, 충전기 연결/해제 등)를 감지했을 때 호출한다. 서버는 이벤트를 저장하고 해당 기기의 `last_activity_at`을 갱신한다.

요청:

```json
{
  "occurred_at": "2026-05-16T08:30:00Z",
  "source": "user_present"
}
```

응답:

```json
{
  "event_id": "uuid",
  "last_activity_at": "2026-05-16T08:30:00Z"
}
```

정책:

- `source` 값: `user_present`(잠금해제), `power_connected`(충전기 연결), `power_disconnected`(충전기 해제). 향후 `step_detected` 등 추가 가능.
- `source` 기본값은 `user_present`다.
- 같은 이벤트가 재전송될 수 있으므로 서버는 가까운 시간대 중복 기록 정책을 정해야 한다.
- 네트워크 실패 후 Android가 재시도할 수 있다.
- guardian 기기는 이 API를 호출할 수 없다.

### List Activity Events

`GET /activity/events/{senior_device_id}`

인증: 해당 senior와 active pairing된 guardian device token 또는 senior 본인 device token 필요.

쿼리:

```text
limit=50
```

응답:

```json
{
  "events": [
    {
      "id": "uuid",
      "occurred_at": "2026-05-16T08:30:00Z",
      "received_at": "2026-05-16T08:30:05Z",
      "source": "user_present"
    }
  ]
}
```

### Record Service Event

`POST /activity/service-events`

인증: device token 필요.

Android foreground service 실행 내역, heartbeat, 부팅 후 재시작 시도, 오류 등을 서버에 남긴다.

요청:

```json
{
  "event_type": "heartbeat",
  "occurred_at": "2026-05-16T08:31:00Z",
  "detail": "monitor service heartbeat"
}
```

응답:

```json
{
  "event_id": "uuid"
}
```

정책:

- MVP에서는 모든 서비스 이벤트를 저장하되, heartbeat는 저장량이 커질 수 있으므로 보존 기간 또는 샘플링 정책을 둔다.
- `event_type` 후보: `started`, `stopped`, `heartbeat`, `boot_completed`, `unlock_upload_failed`, `error`.

### List Service Events

`GET /activity/service-events/{device_id}`

인증: 대상 기기 본인 또는 active pairing 관계의 상대 기기 필요.

응답:

```json
{
  "events": [
    {
      "id": "uuid",
      "event_type": "started",
      "occurred_at": "2026-05-16T08:00:00Z",
      "received_at": "2026-05-16T08:00:02Z",
      "detail": "monitor service started"
    }
  ]
}
```

## Inactivity Alerts

### Run Inactivity Alert Batch

`POST /internal/batches/inactivity-alerts/run`

인증: 운영용 internal token 또는 관리자 실행 환경 필요.

일일 배치에서 호출하거나, 운영자가 수동 실행할 수 있는 내부 API다. MVP 기본 임계값은 2일이다.

요청:

```json
{
  "dry_run": false,
  "now": "2026-05-18T09:00:00Z"
}
```

응답:

```json
{
  "checked_senior_count": 12,
  "alert_created_count": 2,
  "push_sent_count": 2,
  "push_failed_count": 0
}
```

정책:

- active pairing된 보호자에게만 FCM을 발송한다.
- 같은 미사용 상태에 대해 반복 알림 간격을 제한한다.
- FCM 실패도 `InactivityAlert` 로그에 남긴다.
- 실제 운영에서는 HTTP API 대신 CLI/스케줄러로 실행해도 된다.

### List Inactivity Alerts

`GET /activity/inactivity-alerts/{senior_device_id}`

인증: 해당 senior와 active pairing된 guardian device token 또는 senior 본인 device token 필요.

응답:

```json
{
  "alerts": [
    {
      "id": "uuid",
      "senior_device_id": "uuid",
      "guardian_device_id": "uuid",
      "threshold_days": 2,
      "last_activity_at": "2026-05-16T08:30:00Z",
      "sent_at": "2026-05-18T09:00:00Z",
      "status": "sent"
    }
  ]
}
```

## Deferred: Fall

낙상 감지 API는 현재 MVP 범위에서 보류한다. 기존 낙상 감지 진단 구현은 향후 센서 알고리즘 검증 재개 시 참고한다.
