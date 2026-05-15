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

If Firebase push notifications are needed, place the key on the server:

```bash
cp /path/to/firebase-credentials.json backend/firebase-credentials.json
```

Then mount it into the backend service as `/app/firebase-credentials.json`.

Start the stack:

```bash
docker compose up -d --build
docker compose logs -f backend
```

Check:

```bash
curl http://SERVER_PUBLIC_IP/health
```

## Ports

- `80`: public HTTP through Nginx
- `8000`: backend exposed for development; remove this port mapping for stricter production deployment
- `5432`: PostgreSQL is internal only

## Follow-up Production Tasks

- Add HTTPS before real users.
- Remove the public `8000:8000` mapping after Android points to the Nginx URL.
- Add database backups for the `postgres_data` Docker volume.
- Rotate `SECRET_KEY` and database credentials before deployment.
