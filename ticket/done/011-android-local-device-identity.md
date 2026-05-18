# 할일-011 Android 로컬 기기 식별 상태 구현

완료일: 2026-05-18

## 우선순위

P0

## 전제

아직 서버 API가 없다고 가정한다. Android 앱 안에서 먼저 앱 설치 단위의 UUID와 역할 상태를 저장하고, 나중에 서버 기기 등록 API와 자연스럽게 연결될 수 있는 구조로 만든다.

## 문제

새 MVP는 로그인 없이 동작해야 한다. 기존 `TokenDataStore`는 auth token, user type, user name 중심이라 새 구조와 맞지 않는다. 앱 최초 실행 시 UUID를 만들고, 어르신/보호자 역할 선택 결과를 로컬에 저장해야 한다.

## 작업 범위

- 앱 설치 단위 `localDeviceId`를 UUID v4로 생성한다.
- 이미 저장된 UUID가 있으면 재생성하지 않는다.
- `role` 값을 저장한다.
  - `senior`
  - `guardian`
- 서버가 없는 동안 사용할 로컬 페어링 상태를 저장한다.
  - `unpaired`
  - `paired`
- 기존 `TokenDataStore`를 확장하거나 새 `DeviceDataStore`를 만든다.
- 앱 삭제 후 재설치는 새 기기로 취급한다는 동작을 주석 또는 문서에 남긴다.
- 이후 서버 연동 시 `localDeviceId`와 `role`을 device 등록 요청에 넘길 수 있게 API 경계를 정리한다.

## 완료 조건

- 최초 실행 시 UUID가 생성되어 DataStore에 저장된다.
- 앱을 다시 열어도 같은 UUID를 사용한다.
- 역할 선택 결과가 로컬에 저장된다.
- 로그인 토큰 없이 앱 시작 목적지를 결정할 수 있다.
- 서버 API 없이도 이후 UI 티켓들이 로컬 상태로 동작할 수 있다.

## 완료 기록

- `core:model`에 `DeviceRole`, `PairingStatus`, `LocalDeviceState`, `DeviceRegistrationDraft`를 추가했다.
- `core:datastore`에 `DeviceDataStore`를 추가해 설치 단위 UUID, 역할, 로컬 페어링 상태를 저장한다.
- 서버 기기 등록 연동을 위해 `getRegistrationDraft()` 경계를 마련했다.
- 앱 삭제 또는 앱 데이터 삭제 후 재설치는 새 기기로 취급한다는 주석을 코드에 남겼다.
- 검증: `cd android && ./gradlew assembleDebug` 통과.
