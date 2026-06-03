# 완료-004 잠금해제 미사용 알림 플로우 구현

## 우선순위

P0

## 진행 현황 (2026-06-03)

백엔드와 Android 코드 경로 구현 완료. `2026-06-03 cd android && ./gradlew assembleDebug` 통과.

- ✅ `inactivity-check`가 FCM HTTP v1(OAuth2, `FIREBASE_SERVICE_ACCOUNT`)로 발송하도록 적용 (legacy는 fallback)
- ✅ `GuardianFcmService` 수신/표시 + 보호자 홈 `POST_NOTIFICATIONS` 권한 요청 추가 — 보호자 미사용 알림 수신 경로 코드 완비
- ✅ Android activity/service 업로드 DTO를 백엔드 배치 계약(`{events:[...]}` → `{accepted}`)에 맞게 정렬
- ✅ `ActivityRepository.uploadPendingEvents()` 추가: pending activity/service event를 업로드하고 성공 시 `uploaded=true` 처리
- ✅ `ServiceEventDao` pending upload/mark uploaded API 추가
- ✅ `ActivityMonitorService`에서 잠금해제/충전 이벤트 기록 직후와 heartbeat 주기마다 pending 업로드 시도
- ✅ WorkManager 기반 OS 친화적 백그라운드 재시도 정책 도입
- ✅ 보호자 미사용 알림 전체 이력 화면 추가
- ✅ Android `ActivityMonitorService`의 `task_removed` 로컬 service event는 백엔드 허용값에 맞춰 `error`로 기록하도록 정렬
- ✅ 보호자 홈에서 `pairings-list`의 마지막 활동 시각을 표시하고, `inactivity-alerts-list` 최근 1건을 어르신별로 조회해 미사용 알림 상태를 표시
- ⚠️ `today_message_opened` 같은 로컬 전용 service event는 백엔드 service lifecycle 허용값 밖이라 현재 업로드 대상에서 제외됨. 별도 콘텐츠 이벤트 API를 만들지 여부 결정 필요
- 참고: 실기기 E2E는 어르신 `last_activity_at`을 수동 세팅하면 활동 업로드 없이도 푸시 경로 검증 가능

## 문제

낙상 감지 기능은 MVP 제품화 범위에서 잠시 보류한다. 대신 어르신 휴대폰이 N일 동안 잠금해제되지 않으면 보호자에게 안부 확인 푸시를 보내는 기능을 MVP 핵심으로 구현한다.

이 기능은 센서 알고리즘보다 기술 리스크가 낮고, 서비스 실행 내역과 잠금해제 내역을 DB에 남기면 실제 기기 동작을 검증하기 쉽다.

2026-06-03 현재 백엔드는 범용 활동 이벤트 Edge Function인 `activity-events`를 구현했다. Android 로컬 Room 테이블 이름은 `unlock_events`지만 실제로는 `user_present`, `power_connected`, `power_disconnected`를 모두 저장하므로, 서버 모델/API 명칭은 `ActivityEvent`와 `last_activity_at`으로 맞춘다. 남은 핵심은 Android 로컬 이벤트를 Supabase 배치 요청(`{ "events": [...] }`)으로 업로드하고 성공 시 로컬 `uploaded` 상태를 갱신하는 것이다.

## 선행 완료 사항

- 백엔드는 Device 토큰 인증 전용으로 전환 완료.
- Device/Pairing/PairingCode 모델과 API 구현 완료.
- Android에는 Room 기반 진단 로그 저장 구조와 foreground service heartbeat 패턴이 있다.
- Android에는 `core:activity` 기반 잠금해제 이벤트 로컬 DB, 서비스 이벤트 로컬 DB, foreground activity monitor service가 있다.
- Android 어르신 홈에서 활동 모니터링 시작/중지, 최근 잠금해제 시각, 오늘 사용 기록 수를 표시한다.
- 오늘의 글 화면, 매일 저녁 8시 로컬 알림 예약, 알림 탭 시 오늘의 글 route 이동 구조가 구현됐다.

## 작업 범위

### 백엔드 ✅ 전부 구현 완료 (Supabase Edge Functions)

- ✅ Device 모델에 `last_activity_at`, `inactivity_threshold_days` 포함 (initial migration)
- ✅ `activity_events` 테이블 + RLS (initial migration)
- ✅ `service_events` 테이블 + RLS (20260530000001 migration)
- ✅ `inactivity_alerts` 테이블 (initial migration)
- ✅ 활동 이벤트 수신 (`activity-events` Edge Function)
- ✅ 활동 이벤트 조회 (`activity-events-list` Edge Function, 페이지네이션)
- ✅ 서비스 이벤트 수신 (`service-events` Edge Function)
- ✅ 서비스 이벤트 조회 (`service-events-list` Edge Function, 페이지네이션)
- ✅ 미사용 알림 이력 조회 (`inactivity-alerts-list` Edge Function)
- ✅ 미사용 알림 배치 (`inactivity-check` Edge Function, pg_cron 매일 00:00 UTC)
  - ✅ 중복 발송 방지 (last_activity_at 변경 없으면 스킵)
  - ✅ FCM 성공/실패/스킵 로그 저장
- ✅ Supabase Migrations 3개 작성
- ✅ Deno 테스트 스위트 작성 (권한, 기록, 배치, 중복 방지 검증)

### Android

- 기존 `core:activity` 로컬 기록을 백엔드 API와 연결:
  - ✅ 잠금해제/충전 이벤트 백엔드 `POST /functions/v1/activity-events` 업로드
  - ✅ 서비스 이벤트 백엔드 `POST /functions/v1/service-events` 업로드
  - ✅ 네트워크 실패 시 미전송 이벤트 보관 및 다음 이벤트/heartbeat에서 재시도
  - ✅ 업로드 성공 시 로컬 `uploaded` 상태 갱신
  - ✅ `ServiceEventDao`에도 pending upload 조회와 mark uploaded API 추가
- 활동 모니터링 foreground service 보강:
  - ✅ heartbeat/error 로컬 기록 및 백엔드 업로드
  - ✅ 주요 서비스 이벤트 백엔드 업로드
  - ✅ 부팅 후 재시작 경계와 WorkManager 업로드 재시도 경로 연결
- 오늘의 글 로컬 알림 보강:
  - ✅ 알림 발송 시각 로컬 이벤트 기록 추가
  - ✅ 열람 이벤트는 로컬 전용 service event로 기록하고 백엔드 lifecycle 업로드 대상에서 제외
- MVP 진단 화면에서 확인 가능한 내역:
  - ✅ 서비스 실행 내역
  - ✅ 잠금해제 내역
  - ✅ 업로드 성공/실패 내역
  - ✅ 매일 콘텐츠 알림 발송/열람 내역
- ✅ 보호자 화면에서 마지막 잠금해제 시각 표시.
- ✅ 보호자 화면에서 최근 미사용 알림 이력 1건 표시.
- ✅ 보호자 화면에서 미사용 알림 전체 이력 표시.
- ✅ 보호자 FCM 수신 시 미사용 알림으로 표시.

## 정책 결정

- MVP 기본 임계값은 2일이다.
- 알림 문구는 위험을 단정하지 않는다.
  - 예: `홍길동님 휴대폰 사용 기록이 2일 동안 확인되지 않았습니다. 안부 확인이 필요할 수 있습니다.`
- 배터리 방전, 네트워크 끊김, 앱 강제 종료, 제조사 백그라운드 제한 때문에 100% 안전 보장 기능으로 표현하지 않는다.
- 제품 문구는 "잠금해제되지 않음"보다 "휴대폰 사용 기록이 확인되지 않음"을 우선 사용한다.
- 어르신 앱 재방문 유도를 위해 매일 저녁 8시 로컬 푸시를 보낸다.
- 매일 콘텐츠는 안전 기능을 가장하지 않고, 앱을 자연스럽게 열어보게 하는 보조 콘텐츠로 둔다.

## 완료 조건

- 어르신 폰 잠금해제 시 로컬 DB와 백엔드 DB에 이벤트가 기록된다.
- 백엔드는 어르신 기기의 `last_activity_at`을 갱신한다.
- 서비스 시작/중지/heartbeat/error 내역이 로컬 DB와 백엔드 DB에서 확인된다.
- 매일 저녁 8시 오늘의 글 로컬 푸시가 예약되고, 탭하면 오늘의 글 화면으로 이동한다.
- 오늘의 글 발송/열람 내역이 로컬 DB에서 확인된다.
- 매일 배치 실행 시 마지막 잠금해제 2일 경과 어르신을 찾아 보호자에게 FCM을 보낸다.
- 같은 상태에서 중복 알림이 과도하게 발송되지 않는다.
- 보호자는 연결된 어르신의 마지막 잠금해제 시각과 미사용 알림 이력을 볼 수 있다.
- Android와 백엔드 테스트가 잠금해제 기록, 권한 제한, 배치 발송, FCM 실패를 검증한다.
