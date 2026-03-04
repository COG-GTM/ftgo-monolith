#!/usr/bin/env bash
# Build a specific FTGO microservice
# Usage: ./deploy/scripts/build-service.sh <service-name>
# Example: ./deploy/scripts/build-service.sh ftgo-order-service

set -euo pipefail

SERVICE_NAME="${1:?Usage: $0 <service-name>}"
SERVICE_DIR="services/${SERVICE_NAME}"

if [ ! -d "${SERVICE_DIR}" ]; then
  echo "Error: Service directory '${SERVICE_DIR}' not found."
  echo "Available services:"
  ls -1 services/
  exit 1
fi

echo "Building ${SERVICE_NAME}..."
./gradlew ":services-${SERVICE_NAME}:build" --no-daemon

echo "Build complete for ${SERVICE_NAME}"
