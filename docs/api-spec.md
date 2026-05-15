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
  "last_seen_at": "2026-05-16T00:10:00Z"
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
      "last_fall_at": null,
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

## Fall

### Report Fall Event

`POST /fall/events`

인증: senior device token 필요.

```json
{"detected_at":"2026-05-16T00:00:00Z"}
```

응답:

```json
{
  "event_id": "uuid",
  "status": "reported",
  "notified_guardian_count": 1
}
```

정책:

- MVP 기본 정책은 Android가 30초 카운트다운 종료 후 이벤트를 전송하는 방식이다.
- 카운트다운 중 어르신이 취소하면 서버에 이벤트를 만들지 않는다.
- 이벤트 전송 후 FCM 실패가 발생하면 `notify_failed` 상태로 기록한다.

### Record Cancelled Local Detection

`POST /fall/events/cancelled`

인증: senior device token 필요.

카운트다운 중 취소된 오감지를 서버 이력에 남기고 싶을 때 사용한다. 보호자에게 FCM을 보내지 않는다.

```json
{
  "detected_at": "2026-05-16T00:00:00Z",
  "cancelled_at": "2026-05-16T00:00:12Z"
}
```

응답:

```json
{
  "event_id": "uuid",
  "status": "cancelled"
}
```

### List Fall History

`GET /fall/history/{senior_device_id}`

인증: 해당 senior와 active pairing된 guardian device token 또는 senior 본인 device token 필요.

```json
{
  "events": [
    {
      "id": "uuid",
      "detected_at": "2026-05-16T00:00:00Z",
      "status": "reported",
      "notified_guardian_count": 1
    }
  ]
}
```
