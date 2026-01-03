#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ACTION="${1:-}"

if [[ -z "${ACTION}" ]]; then
  printf "Choose action [stop/remove]: "
  read -r ACTION
fi

if [[ "${ACTION}" != "stop" && "${ACTION}" != "remove" ]]; then
  echo "Usage: $(basename "$0") [stop|remove]"
  exit 1
fi

compose_down() {
  local compose_file="$1"
  local container_name
  container_name="$(grep -m 1 -E '^[[:space:]]*container_name:' "${compose_file}" | awk '{print $2}')"

  if [[ "${ACTION}" == "stop" ]]; then
    echo "Stopping ${compose_file}..."
    docker compose -f "${compose_file}" stop

    if [[ -n "${container_name}" ]] && docker ps --format '{{.Names}}' | grep -qx "${container_name}"; then
      docker stop "${container_name}" >/dev/null
    fi
    return
  fi

  echo "Removing ${compose_file}..."
  docker compose -f "${compose_file}" down

  if [[ -n "${container_name}" ]] && docker ps -a --format '{{.Names}}' | grep -qx "${container_name}"; then
    docker rm -f "${container_name}" >/dev/null
  fi
}

compose_down "${ROOT_DIR}/services/user-service/docker-compose.yml"
compose_down "${ROOT_DIR}/services/ai-service/docker-compose.yml"
compose_down "${ROOT_DIR}/services/activity-service/docker-compose.yml"
compose_down "${ROOT_DIR}/infra/local/docker-compose.yml"

echo "Local infrastructure is down."
