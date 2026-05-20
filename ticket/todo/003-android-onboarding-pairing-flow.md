# 할일-003 Android 역할 선택 및 페어링 온보딩 구현

## 우선순위

P0

## 문제

Android 앱의 로컬 역할 선택/어르신 목업 페어링 UI는 구현됐다. 남은 작업은 서버 기기 등록 API와 실제 페어링 API를 연결하고, 보호자 코드 입력 플로우와 실패 상태를 새 device/pairing 계약 기준으로 완성하는 것이다.

2026-05-20 현재 백엔드는 새 device/pairing API가 구현되어 있고 pytest가 통과한다. 반면 Android `core:network`/`core:data`는 아직 피벗 전 API(`auth/*`, `pairing/code`, `pairing/connect`, `pairing/list`, `fall/*`, `devices/token`)와 user JWT 기반 `TokenDataStore`를 참조한다. 이 티켓의 핵심은 Android 네트워크/데이터 계층을 현재 백엔드 계약과 맞추는 것이다.

## 선행 완료 사항

- `done/011`: 로컬 설치 UUID, 역할, 페어링 상태 저장 구현 완료.
- `done/012`: 앱 시작 목적지를 로컬 역할/페어링 상태 기준으로 결정하도록 변경 완료.
- `done/013`: 어르신 6자리 연결 코드 목업 UI와 수동 페어링 완료 액션 구현 완료.
- `done/014`: 어르신 홈에서 활동 모니터링 상태/최근 잠금해제/오늘 사용 기록 표시 완료.
- `done/015`: 오늘의 글 화면과 로컬 알림 예약 구조 구현 완료.

## 작업 범위

- Android 기기 등록 API 연동:
  - 로컬 `localDeviceId`와 `role`을 서버 `POST /devices/register` 요청에 연결
  - 서버 발급 device token 저장 경계 확정
  - 기존 user JWT용 `TokenDataStore`를 device access token 저장 용도로 교체하거나 별도 `DeviceTokenDataStore`를 추가
  - OkHttp `Authorization: Bearer <device_access_token>` interceptor 추가
  - 앱 재실행 시 `devices/me` 또는 로컬 상태와 서버 상태 동기화
- 어르신 모드 서버 페어링 연동:
  - 목업 6자리 코드 생성을 `POST /pairing/codes` 호출로 교체
  - 만료 시간/재생성 UI를 서버 응답 기준으로 표시
  - 페어링 완료 상태를 `GET /pairings`의 active pairing 기준으로 반영
- 보호자 모드 서버 페어링 연동:
  - 연결 코드 입력을 현재 백엔드 계약인 `POST /pairings` 호출로 연결
  - 연결 목록 조회를 `GET /pairings`로 연결
  - 연결 해제를 `DELETE /pairings/{pairing_id}`로 연결
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
- Android `ApiService`에 피벗 전 `auth/*`, `fall/*`, `pairing/connect`, `pairing/list`, `devices/token` 엔드포인트가 일반 MVP 경로에서 남아 있지 않다.
- 어르신 모드 진입 후 잠금해제 활동 모니터링을 시작할 수 있다.
- 어르신 홈에서 오늘의 글을 열 수 있다.
- 페어링 실패 상태가 사용자에게 명확히 표시된다.
- 기존 로그인 화면이 MVP 진입 흐름에 남아 있지 않다.
