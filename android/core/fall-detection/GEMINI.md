# 낙상 감지(Fall Detection) 지침

## 🧪 알고리즘
- 센서 데이터 처리는 배터리 소모를 최소화하도록 효율적이어야 합니다.
- 적절한 샘플링 속도(예: `SENSOR_DELAY_UI`)와 함께 `SensorManager`를 사용합니다.

## 🛡 서비스 라이프사이클
- `FallDetectionService`는 반드시 Foreground Service로 실행되어야 합니다.
- 시스템에 의해 종료되지 않도록 적절한 알림(Notification) 관리를 수행합니다.
- 화면이 꺼진 상태에서도 감지가 지속되도록 Wake Lock 처리에 유의합니다.
