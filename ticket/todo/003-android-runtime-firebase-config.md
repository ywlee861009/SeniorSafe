# 할일-003 Android 런타임 및 Firebase 설정 완료

## 우선순위

P0

## 문제

Android 앱은 빌드되지만 실제 실행/운영 설정은 아직 개발 환경 중심입니다. API base URL은 에뮬레이터 호스트로 하드코딩되어 있고, Firebase 설정은 예시 파일만 있으며, 알림/Foreground Service 권한은 실기기 검증이 필요합니다.

## 작업 범위

- API base URL을 BuildConfig 또는 product flavor로 이동.
- 실제 `google-services.json`은 git 밖에서 관리하도록 절차 확정.
- 로그인, 앱 재설치, FCM 토큰 갱신 시점의 토큰 등록 검증.
- Android 알림 권한 요청 및 거부 상태 처리.
- Android 14 이상에서 Foreground Service 권한과 실행 동작 검증.
- 로컬/에뮬레이터/실기기 실행 가이드 작성.

## 완료 조건

- 코드 수정 없이 debug/release 빌드가 서로 다른 API URL을 사용할 수 있다.
- 보호자 기기가 로그인 및 토큰 동기화 후 FCM 낙상 알림을 받는다.
- 권한 거부 상태가 앱에서 어색하게 깨지지 않는다.
- Firebase와 백엔드 설정에 필요한 값이 문서화되어 있다.
