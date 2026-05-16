# 완료-005 보호자 모드 MVP

## 상태

과거 MVP 기준 완료. 현재 잠금해제 미사용 알림 MVP 기준에서는 보호자 화면의 핵심 정보가 마지막 잠금해제 시각과 미사용 알림 이력으로 변경됨.

## 구현된 내용

- 연결된 어르신 목록을 보여주는 보호자 홈 화면.
- 어르신 페어링 코드로 연결하는 플로우.
- 낙상 이력 화면과 Repository 호출.
- FCM 토큰 갱신 및 낙상 알림 표시를 위한 Firebase Messaging Service.
- 백엔드 FCM 토큰 업데이트 API.
- 낙상 이벤트 발생 시 FCM 전송 연결부.

## 현재 기준 메모

2026-05-16 이후 보호자 FCM의 우선 대상은 낙상 알림이 아니라 어르신 휴대폰 미사용 알림입니다.

## 확인 근거

- `android/feature/guardian/`
- `android/core/data/src/main/java/com/seniorsafe/core/data/repository/PairingRepository.kt`
- `android/core/data/src/main/java/com/seniorsafe/core/data/repository/FallRepository.kt`
- `backend/app/routers/devices.py`
- `backend/app/services/fcm_service.py`
