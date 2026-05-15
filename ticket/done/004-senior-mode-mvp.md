# 완료-004 어르신 모드 MVP

## 상태

MVP 기준 완료.

## 구현된 내용

- 어르신 홈 화면과 서비스 켜기/끄기 제어.
- Foreground 낙상 감지 서비스.
- 가속도 센서 기반 낙상 감지 상태 머신.
- 페어링 코드 화면과 ViewModel.
- 낙상 감지 후 카운트다운 화면.
- 낙상 이벤트 보고 Repository 연동.

## 확인 근거

- `android/feature/senior/src/main/java/com/seniorsafe/feature/senior/`
- `android/feature/senior/src/main/java/com/seniorsafe/feature/senior/service/`
- `backend/app/services/pairing_service.py`
- `backend/app/services/fall_service.py`

## 메모

기능은 구현되어 있지만, 실제 알림 취소 흐름과 런타임 정확성은 별도 todo 티켓에서 다룹니다.
