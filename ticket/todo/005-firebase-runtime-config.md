# 할일-005 Firebase 및 Android 런타임 설정 정리

## 우선순위

P0

## 진행 현황 (2026-06-03)

코드 측 설정은 대부분 적용. 잔여는 실제 자격증명 파일 배치와 인프라/실기기 검증(운영자 수동 작업).

- ✅ google-services plugin 활성화 (`android/app/build.gradle.kts` 주석 해제) — 단, `android/app/google-services.json`이 없으면 빌드 실패
- ✅ `google-services.json`(`android/.gitignore`)·서비스 계정 JSON(`*-firebase-adminsdk-*.json`)·Supabase CLI 캐시(`supabase/.temp/`) git 제외
- ✅ `ANBU_API_BASE_URL`을 `functions/v1` 형식으로 정정 + 머신별 override 안내 주석
- ✅ 보호자 홈 Android 13+ `POST_NOTIFICATIONS` 런타임 권한 요청 추가 (거부해도 화면 정상)
- ✅ 미사용 알림 FCM payload 타입 정의: `data.type = "inactivity_alert"` (+ `senior_name`, `days_inactive`)
- ✅ `.env.example`에 `FIREBASE_SERVICE_ACCOUNT` 추가 + secret 설정법 주석
- ⬜ (잔여, 운영자) 실제 `google-services.json` 배치, `ANBU_API_BASE_URL`을 실 프로젝트 URL로
- ⬜ (잔여, 운영자) `supabase functions deploy` + `supabase secrets set FIREBASE_SERVICE_ACCOUNT=...`
- ⬜ (잔여) FCM 토큰 동기화 시점(등록/재실행/갱신/페어링 후) 실기기 검증
- ⬜ (잔여) local/dev/prod product flavor 분리 (`ticket/todo/009`와 연계), 실행 가이드 문서화
- ⚠️ 2026-06-03 현재 `cd android && ./gradlew assembleDebug`는 `android/app/google-services.json` 누락으로 실패한다. 실제 파일 또는 빌드 variant별 대체 파일 배치 후 재검증 필요

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
