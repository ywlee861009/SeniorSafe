# API Spec

Base URL (Supabase Edge Functions):

```text
https://<project-id>.supabase.co/functions/v1/
```

MVP는 사용자 로그인 없이 동작한다. 보호가 필요한 엔드포인트는 기기 등록 시 발급받은 커스텀 device JWT를 사용한다.

```http
Authorization: Bearer <device_access_token>
```

> **구현 상태 표기**: ✅ 구현됨 — 백엔드 Edge Function 및 Android 클라이언트 연동 모두 완료. JWT 서명 secret은 `DEVICE_JWT_SECRET`을 사용한다(`SUPABASE_` 접두사는 함수 secret 예약어).

## Devices ✅ 구현됨

### Register Device ✅ 구현됨

`POST /functions/v1/device-register`

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

### Update FCM Token ✅ 구현됨

`PUT /functions/v1/fcm-token`

인증: device token 필요.

```json
{"fcm_token":"firebase-token"}
```

응답:

```json
{"message":"FCM token updated"}
```

### Get Current Device ✅ 구현됨

`GET /rest/v1/devices` (PostgREST, RLS가 자기 기기만 반환)

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

## Pairing ✅ 구현됨

### Create Pairing Code ✅ 구현됨

`POST /functions/v1/pairing-codes`

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

### Claim Pairing Code ✅ 구현됨

`POST /functions/v1/pairing-claim`

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

### List Pairings ✅ 구현됨

`GET /functions/v1/pairings-list`

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

### Disconnect Pairing ✅ 구현됨

`POST /functions/v1/pairing-disconnect`

인증: pairing에 속한 senior 또는 guardian device token 필요.

응답:

```json
{
  "pairing_id": "uuid",
  "active": false
}
```

## Activity ✅ 구현됨

### Record Activity Events ✅ 구현됨

`POST /functions/v1/activity-events`

인증: senior device token 필요.

어르신 기기에서 활동 신호(잠금해제, 충전기 연결/해제 등)를 배치로 업로드한다. 서버는 이벤트를 저장하고 해당 기기의 `last_activity_at`을 갱신한다.

요청:

```json
{
  "events": [
    { "occurred_at": "2026-05-16T08:30:00Z", "source": "user_present" },
    { "occurred_at": "2026-05-16T09:00:00Z", "source": "power_connected" }
  ]
}
```

응답:

```json
{
  "accepted": 2
}
```

정책:

- `source` 값: `user_present`(잠금해제), `power_connected`(충전기 연결), `power_disconnected`(충전기 해제).
- 배치 전송 — `events` 배열로 여러 이벤트를 한 번에 보낸다.
- `last_activity_at`은 전송된 이벤트 중 가장 최근 `occurred_at`으로 갱신된다.
- guardian 기기는 이 API를 호출할 수 없다 (403).

### List Activity Events ✅ 구현됨

`GET /functions/v1/activity-events-list?senior_device_id=<uuid>&limit=50&offset=0`

인증: senior 본인 또는 active pairing된 guardian device token 필요.

- Senior: `senior_device_id` 파라미터 불필요 (자동으로 본인)
- Guardian: `senior_device_id` 필수, 페어링 확인 후 조회

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
  ],
  "total": 100,
  "limit": 50,
  "offset": 0
}
```

### Record Service Events ✅ 구현됨

`POST /functions/v1/service-events`

인증: device token 필요 (모든 역할).

Android foreground service 실행 내역, heartbeat, 오류 등을 배치로 업로드한다.

요청:

```json
{
  "events": [
    { "event_type": "started", "occurred_at": "2026-05-16T08:00:00Z" },
    { "event_type": "heartbeat", "occurred_at": "2026-05-16T08:05:00Z" },
    { "event_type": "error", "occurred_at": "2026-05-16T08:06:00Z", "detail": "sensor failure" }
  ]
}
```

응답:

```json
{
  "accepted": 3
}
```

정책:

- `event_type` 값: `started`, `stopped`, `heartbeat`, `error`.
- `detail`은 선택 필드.

### List Service Events ✅ 구현됨

`GET /functions/v1/service-events-list?device_id=<uuid>&limit=50&offset=0`

인증: 본인 기기 또는 active pairing된 guardian이 senior의 이벤트 조회 가능.

- `device_id` 생략 시 본인 기기의 이벤트 조회.

응답:

```json
{
  "events": [
    {
      "id": "uuid",
      "event_type": "started",
      "occurred_at": "2026-05-16T08:00:00Z",
      "received_at": "2026-05-16T08:00:02Z",
      "detail": null
    }
  ],
  "total": 50,
  "limit": 50,
  "offset": 0
}
```

## Inactivity Alerts ✅ 구현됨

### Run Inactivity Alert Batch ✅ 백엔드 구현됨

`POST /functions/v1/inactivity-check`

인증: `CRON_SECRET` Bearer token (pg_cron에서 자동 호출).

pg_cron이 매일 00:00 UTC에 자동 실행한다. 수동 실행도 가능.

응답:

```json
{
  "checked": 12,
  "alerts_sent": 2,
  "alerts_skipped": 1,
  "alerts_failed": 0,
  "alerts_deduplicated": 3
}
```

정책:

- active pairing된 보호자에게만 FCM을 발송한다.
- 중복 방지: 마지막 `sent` 알림의 `last_activity_at`이 현재와 동일하면 재발송하지 않는다.
- FCM 토큰 없는 보호자는 `skipped` 상태로 기록한다.
- FCM 실패도 `failed` 상태로 `inactivity_alerts` 테이블에 기록한다.

### List Inactivity Alerts ✅ 구현됨

`GET /functions/v1/inactivity-alerts-list?senior_device_id=<uuid>&limit=50&offset=0`

인증: guardian 또는 senior device token 필요.

- Guardian: 본인 대상 알림 조회, `senior_device_id`로 필터 가능 (페어링 확인)
- Senior: 본인 관련 알림 조회

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
      "status": "sent",
      "detail": null
    }
  ],
  "total": 5,
  "limit": 50,
  "offset": 0
}
```

## Deferred: Fall

낙상 감지 API는 현재 MVP 범위에서 보류한다. 기존 낙상 감지 진단 구현은 향후 센서 알고리즘 검증 재개 시 참고한다.
