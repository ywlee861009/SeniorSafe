# 완료-003 인증 및 사용자 유형 플로우

## 상태

과거 MVP 기준 완료. 현재 로그인 없는 기기 페어링 MVP에서는 진입 흐름에서 제거/비노출 대상.

## 구현된 내용

- Android 로그인 화면.
- Android 회원가입 화면.
- 회원가입 시 어르신/보호자 유형 선택.
- 백엔드 회원가입 및 로그인 API.
- Android DataStore에 JWT와 사용자 정보 저장.
- 로그인 성공 후 `user_type`에 따른 화면 이동.

## 현재 기준 메모

2026-05-16 이후 회원가입/로그인 화면은 MVP 진입 경로가 아니다. 앱은 역할 선택, device 등록, 페어링 상태를 기준으로 시작한다.

## 확인 근거

- `android/feature/login/`
- `android/core/datastore/src/main/java/com/seniorsafe/core/datastore/TokenDataStore.kt`
- `backend/app/routers/auth.py`
- `backend/app/services/auth_service.py`
