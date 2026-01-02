#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

compose_down() {
  local compose_file="$1"
  echo "Stopping ${compose_file}..."
  docker compose -f "${compose_file}" down
}

compose_down "${ROOT_DIR}/services/user-service/docker-compose.yml"
compose_down "${ROOT_DIR}/services/ai-service/docker-compose.yml"
compose_down "${ROOT_DIR}/services/activity-service/docker-compose.yml"
compose_down "${ROOT_DIR}/infra/local/docker-compose.yml"

echo "Local infrastructure is down."
