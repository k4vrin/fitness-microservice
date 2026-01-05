#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

compose_up() {
  local compose_file="$1"
  local container_name
  container_name="$(grep -m 1 -E '^[[:space:]]*container_name:' "${compose_file}" | awk '{print $2}')"

  if [[ -n "${container_name}" ]] && docker ps -a --format '{{.Names}}' | grep -qx "${container_name}"; then
    if docker inspect -f '{{.State.Running}}' "${container_name}" | grep -qx "true"; then
      echo "Container ${container_name} already running; skipping ${compose_file}."
      return
    fi

    echo "Starting existing container ${container_name} (from ${compose_file})..."
    docker start "${container_name}" >/dev/null
    return
  fi

  echo "Starting ${compose_file}..."
  docker compose -f "${compose_file}" up -d
}

compose_up "${ROOT_DIR}/infra/local/rabbitmq/docker-compose.yml"
compose_up "${ROOT_DIR}/infra/local/keycloak/docker-compose.yml"
compose_up "${ROOT_DIR}/services/activity-service/docker-compose.yml"
compose_up "${ROOT_DIR}/services/ai-service/docker-compose.yml"
compose_up "${ROOT_DIR}/services/user-service/docker-compose.yml"

echo "Local infrastructure is up."
