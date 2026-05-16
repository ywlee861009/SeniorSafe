# 완료-004 어르신 모드 MVP

## 상태

과거 MVP 기준 완료. 현재 잠금해제 미사용 알림 MVP 기준에서는 낙상 감지 제품화가 보류됨.

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

2026-05-16 이후 어르신 모드의 우선순위는 낙상 감지 서비스가 아니라 잠금해제 이벤트 감지, 서비스 실행 내역 기록, 백엔드 업로드입니다.
