# SeniorSafe Backend

FastAPI + PostgreSQL backend for the SeniorSafe Android app.

## Local Run

```bash
cp backend/.env.example .env
docker compose up -d --build
```

API docs:

```text
http://localhost:8000/docs
```

Health check:

```text
http://localhost:8000/health
```

## Tests

```bash
cd backend
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-dev.txt
.venv/bin/python -m pytest
```

현재 테스트는 API 라우터 요청/응답 계약, 인증 보호, 서비스 역할 제한을 검증합니다. 실제 PostgreSQL을 사용하는 DB 통합 테스트는 후속 작업입니다.

## Required Secrets

Set these in the root `.env` file before deploying:

```env
POSTGRES_USER=seniorsafe
POSTGRES_PASSWORD=replace-with-a-strong-password
POSTGRES_DB=seniorsafe_db
SECRET_KEY=replace-with-a-long-random-secret
```

For Firebase push notifications, mount the Firebase Admin SDK service account file to:

```text
backend/firebase-credentials.json
```

and expose it in the backend container as `/app/firebase-credentials.json`. The backend still runs without this file, but FCM sending is skipped.

## OCI Always Free Notes

Oracle Cloud Always Free Ampere A1 provides 3,000 OCPU hours and 18,000 GB hours per month, equivalent to 4 OCPUs and 24 GB memory when used continuously in the home region. Docker Engine on Ubuntu supports `arm64`, so the Compose stack uses multi-architecture images.

Open inbound TCP 80 in both the OCI security list/network security group and the instance firewall. For HTTPS, add a certificate flow later, for example Certbot on the host or a TLS-capable reverse proxy.
