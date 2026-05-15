# 완료-003 인증 및 사용자 유형 플로우

## 상태

MVP 기준 완료.

## 구현된 내용

- Android 로그인 화면.
- Android 회원가입 화면.
- 회원가입 시 어르신/보호자 유형 선택.
- 백엔드 회원가입 및 로그인 API.
- Android DataStore에 JWT와 사용자 정보 저장.
- 로그인 성공 후 `user_type`에 따른 화면 이동.

## 확인 근거

- `android/feature/login/`
- `android/core/datastore/src/main/java/com/seniorsafe/core/datastore/TokenDataStore.kt`
- `backend/app/routers/auth.py`
- `backend/app/services/auth_service.py`
