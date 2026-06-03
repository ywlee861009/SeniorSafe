# 할일-007 보호자 모니터링 기능을 페어링 기준으로 보강

## 우선순위

P1

## 문제

보호자 화면은 기존 user 관계와 placeholder 상태에 기대는 부분이 있다. 새 구조에서는 active pairing 기준으로 연결된 어르신, 마지막 잠금해제 시각, 미사용 알림 이력, 알림 탭 이동, 연결 해제를 제공해야 한다.

2026-06-03 현재 Android 보호자 모델은 `last_activity_at`, `inactivity_threshold_days`를 받을 수 있게 갱신됐고 화면도 마지막 사용 기록을 표시한다. 런타임은 실제 Retrofit/Supabase Edge Function 경계로 전환됐다. 다만 `PairingItem.serviceActive`가 `active` alias로 남아 있어 실제 서비스/활동 상태를 표현하지 않는다. 연결 해제 UI, 미사용 알림 이력, 알림 탭 상세 이동도 없다. 백엔드(Supabase)는 `pairings-list`, `activity-events-list`, `service-events-list`, `inactivity-alerts-list`를 제공하므로 남은 작업은 Android 클라이언트 중심이다.

## 작업 범위

- 보호자 홈에서 실제 Supabase `pairings-list` active pairing 목록을 조회한다. (repository/API 경계는 구현됨, 실기기 검증 필요)
- 어르신 표시 이름과 연결 시간을 보여준다.
- 어르신별 마지막 잠금해제 시각과 경과 시간을 보여준다.
- `service_active`와 `last_fall_at` 같은 피벗 전 필드를 제거하거나 실제 heartbeat/activity 계약으로 교체한다.
- 활동 모니터링 서비스 heartbeat 또는 `last_seen_at`을 구현해 표시 기준을 명확히 한다.
- 보호자-어르신 연결 해제 플로우를 `pairing-disconnect` Edge Function으로 연결한다.
- 잠금해제 이력과 미사용 알림 이력의 날짜 포맷, 빈 상태, 에러 상태, 로딩 상태를 개선한다.
- FCM 알림을 탭했을 때 관련 어르신 상세 또는 미사용 알림 이력 화면으로 이동한다.
- 여러 보호자 또는 여러 어르신 연결 범위가 정해졌다면 UI에 반영한다.

## 완료 조건

- 보호자 화면이 user가 아니라 pairing/device 기준 데이터를 보여준다.
- 가짜 서비스 상태를 보여주지 않는다.
- 보호자가 연결을 해제할 수 있다.
- Android 모델과 화면에서 낙상 중심 필드명이 일반 MVP 화면에 남아 있지 않다.
- 알림 탭 시 관련 화면으로 이동한다.
- 마지막 잠금해제 시각과 미사용 알림 시간이 사용자 친화적인 로컬 시간으로 표시된다.
