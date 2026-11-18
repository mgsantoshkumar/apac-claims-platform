#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Created .env from .env.example"
fi

echo "Starting infrastructure and applications via Docker Compose..."
docker compose up --build -d

cat <<EOF

Chubb APAC Claims MVP is starting.

  UI:                 http://localhost:\${FRONTEND_PORT:-14200}
  Claims API/Swagger: http://localhost:\${CLAIMS_SERVICE_PORT:-18081}/swagger-ui.html
  Notifications API:  http://localhost:\${NOTIFICATION_SERVICE_PORT:-18082}/swagger-ui.html

Demo users (password: password)
  claimant1  CLAIMANT
  adjuster1  ADJUSTER
  manager1   MANAGER

EOF
