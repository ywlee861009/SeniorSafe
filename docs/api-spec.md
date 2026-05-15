# API Spec

Base URL:

```text
http://<server-host>/
```

Protected endpoints require:

```http
Authorization: Bearer <access_token>
```

## Health

`GET /health`

```json
{"status":"ok"}
```

## Auth

`POST /auth/register`

```json
{
  "email": "senior@example.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-0000-0000",
  "user_type": "senior"
}
```

`POST /auth/login`

```json
{
  "email": "senior@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "access_token": "...",
  "user_type": "senior",
  "name": "홍길동",
  "user_id": "uuid"
}
```

## Pairing

`GET /pairing/code`

Senior only.

```json
{
  "code": "A3F9K2",
  "expires_at": "2026-05-15T12:10:00Z"
}
```

`POST /pairing/connect`

Guardian only.

```json
{"code":"A3F9K2"}
```

Response:

```json
{
  "senior_id": "uuid",
  "senior_name": "홍길동"
}
```

`GET /pairing/list`

Guardian only.

```json
{
  "pairings": [
    {
      "senior_id": "uuid",
      "senior_name": "홍길동",
      "service_active": true,
      "last_fall_at": null
    }
  ]
}
```

## Fall

`POST /fall/event`

Senior only.

```json
{"detected_at":"2026-05-15T12:00:00Z"}
```

Response:

```json
{
  "event_id": "uuid",
  "status": "reported"
}
```

`POST /fall/cancel`

Senior only.

```json
{"event_id":"uuid"}
```

Response:

```json
{
  "event_id": "uuid",
  "status": "cancelled"
}
```

`GET /fall/history/{senior_id}`

Guardian only.

```json
{
  "events": [
    {
      "id": "uuid",
      "detected_at": "2026-05-15T12:00:00Z",
      "cancelled": false
    }
  ]
}
```

## Devices

`PUT /devices/token`

```json
{"fcm_token":"firebase-token"}
```

Response:

```json
{"message":"FCM token updated"}
```
