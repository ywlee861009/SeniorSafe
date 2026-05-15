# 할일-005 운영 배포 강화

## 우선순위

P1

## 문제

Docker Compose 배포 골격은 있지만 운영 환경으로 보기에는 아직 부족합니다. 현재 설정은 백엔드 `8000` 포트를 외부에 노출하고, 기본 secret fallback이 있으며, HTTPS와 백업 자동화가 없습니다.

## 작업 범위

- Certbot, Caddy, Traefik, cloud load balancer 중 하나로 HTTPS 구성.
- 운영 compose에서 public `8000:8000` 매핑 제거.
- 기본 `SECRET_KEY`와 DB 비밀번호를 운영에서 사용할 수 없도록 강제.
- Firebase credentials를 안전하게 mount.
- PostgreSQL 백업 및 복구 절차 추가.
- 기본 로그 보관 및 health check 대응 runbook 작성.

## 완료 조건

- 운영 배포 문서에 HTTPS와 방화벽 설정이 포함된다.
- 백엔드는 의도한 reverse proxy를 통해서만 외부 접근 가능하다.
- 백업 복구 절차를 최소 1회 검증한다.
- 운영에서 기본 secret을 실수로 사용할 수 없다.
