# 할일-009 로컬/dev/prod 배포 환경 재정리

## 우선순위

P1

## 문제

Docker Compose 배포 골격은 있지만 운영 환경으로 보기에는 아직 부족하다. 또한 새 기획에서는 Firebase, 기기 등록, FCM 발송, 페어링 코드, 잠금해제 이벤트, 미사용 알림 배치를 local/dev/prod 환경에서 각각 안정적으로 검증할 수 있어야 한다.

## 작업 범위

- 로컬 Docker Compose 실행 가이드를 새 API 흐름에 맞춰 갱신한다.
- dev 서버와 production 서버의 환경 변수 목록을 분리한다.
- 운영 compose에서 public `8000:8000` 매핑을 제거한다.
- Nginx 또는 다른 reverse proxy를 통해서만 backend에 접근하도록 한다.
- HTTPS 구성을 정한다.
  - Certbot
  - Caddy
  - Traefik
  - cloud load balancer
- 기본 `SECRET_KEY`와 DB 비밀번호를 운영에서 사용할 수 없도록 강제한다.
- Firebase credentials를 안전하게 mount한다.
- 미사용 알림 배치를 매일 실행하는 방식을 정한다.
  - host cron
  - backend scheduler
  - external scheduler + internal endpoint
- 배치 실행 로그와 실패 알림 방식을 정한다.
- PostgreSQL 백업 및 복구 절차를 추가한다.
- health check와 기본 장애 대응 runbook을 작성한다.

## 완료 조건

- local/dev/prod 실행 방법이 문서화되어 있다.
- 운영 백엔드는 reverse proxy를 통해서만 외부 접근 가능하다.
- HTTPS와 방화벽 설정이 문서화되어 있다.
- 미사용 알림 배치 실행 방식과 수동 실행 방법이 문서화되어 있다.
- 백업 복구 절차를 최소 1회 검증한다.
- 운영에서 기본 secret을 실수로 사용할 수 없다.
