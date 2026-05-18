# Deployment

Target: Oracle Cloud Infrastructure Always Free VM, Ubuntu, Docker Compose.

## Server Shape

Recommended free-tier target:

- Shape: `VM.Standard.A1.Flex`
- Architecture: Arm / `arm64`
- Size: 1-2 OCPU and 6-12 GB RAM is enough for this stack during development
- Boot volume: keep within the Always Free block volume allowance

## Docker Setup on Ubuntu

Install Docker Engine from Docker's official apt repository. Docker currently supports Ubuntu 22.04 LTS, 24.04 LTS, 25.10, and 26.04 LTS on `arm64`.

After Docker is installed:

```bash
git clone <repo-url>
cd SeniorSafe
cp backend/.env.example .env
```

Edit `.env` and set strong values for `POSTGRES_PASSWORD` and `SECRET_KEY`.

Firebase push notifications are needed for inactivity alerts. Place the key on the server:

```bash
cp /path/to/firebase-credentials.json backend/firebase-credentials.json
```

Then mount it into the backend service as `/app/firebase-credentials.json`.

Set the inactivity alert defaults in `.env`:

```bash
INACTIVITY_ALERT_THRESHOLD_DAYS=2
INACTIVITY_ALERT_REPEAT_HOURS=24
```

Start the stack:

```bash
docker compose up -d --build
docker compose logs -f backend
```

Check:

```bash
curl http://SERVER_PUBLIC_IP/health
```

## Scheduled Jobs

The MVP requires a daily inactivity alert batch. The batch checks senior devices whose `last_activity_at` is older than the configured threshold and sends FCM notifications to active guardians.

The implementation may run as one of:

- a backend CLI command executed by cron on the host
- a lightweight scheduler inside the backend container
- a protected internal endpoint triggered by an external scheduler

The batch must write `InactivityAlert` rows for sent and failed notifications so repeated alerts and FCM failures are auditable.

현재 미사용 알림 배치는 미구현 상태이며, `ticket/todo/004-unlock-inactivity-notification-flow.md`에서 다룬다.

## Ports

- `80`: public HTTP through Nginx
- `8000`: backend exposed for development; remove this port mapping for stricter production deployment
- `5432`: PostgreSQL is internal only

## Follow-up Production Tasks

- Add HTTPS before real users.
- Remove the public `8000:8000` mapping after Android points to the Nginx URL.
- Add database backups for the `postgres_data` Docker volume.
- Rotate `SECRET_KEY` and database credentials before deployment.
- Add a monitored daily schedule for inactivity alerts.
