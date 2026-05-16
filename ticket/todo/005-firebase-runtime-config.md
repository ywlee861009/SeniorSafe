# 할일-005 Firebase 및 Android 런타임 설정 정리

## 우선순위

P0

## 문제

로그인을 제거해도 FCM은 계속 핵심이다. 보호자 기기가 페어링 후 어르신 휴대폰 미사용 알림을 받으려면 Firebase 설정, FCM 토큰 등록, Android 알림 권한 처리가 실제 기기에서 검증되어야 한다.

## 작업 범위

- API base URL을 BuildConfig 또는 product flavor로 분리한다.
- 로컬, dev, prod Android 빌드가 서로 다른 API URL을 쓰도록 정리한다.
- 실제 `google-services.json`은 git 밖에서 관리한다.
- FCM 토큰이 다음 시점에 서버로 동기화되는지 검증한다.
  - 보호자 기기 등록
  - 앱 재실행
  - FCM 토큰 갱신
  - 페어링 완료 후
- Android 알림 권한 요청과 거부 상태를 처리한다.
- Android 14 이상 Foreground Service 권한과 활동 모니터링 서비스 실행 동작을 검증한다.
- 미사용 알림 FCM payload 타입을 정의한다.
- 로컬/에뮬레이터/실기기 실행 가이드를 작성한다.

## 완료 조건

- 코드 수정 없이 local/dev/prod API URL을 바꿀 수 있다.
- 보호자 기기가 페어링 후 실제 FCM 미사용 알림을 받는다.
- 알림 권한 거부 상태에서도 앱 화면이 깨지지 않는다.
- Firebase 설정 절차가 문서화되어 있고 secret 파일은 git에 포함되지 않는다.
