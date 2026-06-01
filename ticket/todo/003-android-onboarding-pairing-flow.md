# 할일-003 Android 역할 선택 및 페어링 온보딩 구현

## 우선순위

P0

## 문제

Android 앱의 로컬 역할 선택/어르신 목업 페어링 UI는 구현됐다. 남은 작업은 서버 기기 등록 API와 실제 페어링 API를 연결하고, 보호자 코드 입력 플로우와 실패 상태를 새 device/pairing 계약 기준으로 완성하는 것이다.

2026-06-01 현재 백엔드는 새 device/pairing API가 Supabase Edge Functions로 구현되어 있다. Android는 `DeviceRepository`, device token 저장, OkHttp 인증 interceptor, pairing repository 경계가 생겼지만 `NetworkModule`이 여전히 `FakeApiService`를 주입한다. 또한 Android `ApiService` 경로는 `devices/register`, `pairing/codes`, `pairings` 같은 FastAPI 스타일 이름이고, 실제 Edge Function 이름(`device-register`, `pairing-codes`, `pairing-claim`, `pairings-list`, `pairing-disconnect`)과 맞지 않는다. 이 티켓의 핵심은 Android 런타임을 fake에서 실제 Supabase 계약으로 전환하는 것이다.

## 선행 완료 사항

- `done/011`: 로컬 설치 UUID, 역할, 페어링 상태 저장 구현 완료.
- `done/012`: 앱 시작 목적지를 로컬 역할/페어링 상태 기준으로 결정하도록 변경 완료.
- `done/013`: 어르신 6자리 연결 코드 목업 UI와 수동 페어링 완료 액션 구현 완료.
- `done/014`: 어르신 홈에서 활동 모니터링 상태/최근 잠금해제/오늘 사용 기록 표시 완료.
- `done/015`: 오늘의 글 화면과 로컬 알림 예약 구조 구현 완료.

## 작업 범위

- Android 기기 등록 API 연동:
  - `FakeApiService` 주입 제거 또는 빌드 타입별 fake/real 전환
  - `BuildConfig.ANBU_API_BASE_URL` 기반 Retrofit provider 복구
  - 로컬 `localDeviceId`와 `role`을 서버 `POST /functions/v1/device-register` 요청에 연결
  - 서버 발급 device token 저장 경계 점검
  - `TokenDataStore`의 user JWT 호환 필드를 device auth 전용 모델로 정리할지 결정
  - OkHttp `Authorization: Bearer <device_access_token>` interceptor 실서버 호출 검증
  - 앱 재실행 시 `devices/me` 또는 로컬 상태와 서버 상태 동기화
- 어르신 모드 서버 페어링 연동:
  - fake 6자리 코드 생성을 `POST /functions/v1/pairing-codes` 호출로 교체
  - 만료 시간/재생성 UI를 서버 응답 기준으로 표시
  - 페어링 완료 상태를 `GET /functions/v1/pairings-list`의 active pairing 기준으로 반영
- 보호자 모드 서버 페어링 연동:
  - 연결 코드 입력을 현재 백엔드 계약인 `POST /functions/v1/pairing-claim` 호출로 연결
  - 연결 목록 조회를 `GET /functions/v1/pairings-list`로 연결
  - 연결 해제를 `POST /functions/v1/pairing-disconnect`로 연결
  - 실패/만료/이미 사용된 코드 상태 표시
  - 페어링 완료 후 보호자 홈 이동
  - 페어링 완료 후 마지막 잠금해제 시각 조회 준비
- 로그인, 회원가입, 비밀번호 입력 화면을 제거하거나 접근되지 않게 정리한다.
- MVP 대시보드/낙상 화면이 일반 사용자 진입 흐름에서 노출되지 않도록 정리한다.
- `docs/api-spec.md`, Android models, Retrofit interface, repository DTO 이름을 같은 계약으로 맞춘다.

## 완료 조건

- 새 설치 후 로그인 없이 역할 선택부터 시작한다.
- 어르신 코드 생성과 보호자 코드 입력으로 실제 백엔드 페어링이 완료된다.
- 앱을 다시 열어도 페어링된 홈 화면으로 진입한다.
- Android 런타임이 실제 Supabase Edge Functions를 호출한다.
- Android `ApiService`에 피벗 전 `auth/*`, `fall/*`, `pairing/connect`, `pairing/list`, `devices/token` 엔드포인트가 일반 MVP 경로에서 남아 있지 않다.
- 어르신 모드 진입 후 잠금해제 활동 모니터링을 시작할 수 있다.
- 어르신 홈에서 오늘의 글을 열 수 있다.
- 페어링 실패 상태가 사용자에게 명확히 표시된다.
- 기존 로그인 화면이 MVP 진입 흐름에 남아 있지 않다.
