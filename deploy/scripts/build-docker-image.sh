#!/usr/bin/env bash
# Build Docker image for a specific FTGO microservice
# Usage: ./deploy/scripts/build-docker-image.sh <service-name> [tag]
# Example: ./deploy/scripts/build-docker-image.sh ftgo-order-service v1.0.0

set -euo pipefail

SERVICE_NAME="${1:?Usage: $0 <service-name> [tag]}"
TAG="${2:-latest}"
SERVICE_DIR="services/${SERVICE_NAME}"

if [ ! -d "${SERVICE_DIR}" ]; then
  echo "Error: Service directory '${SERVICE_DIR}' not found."
  exit 1
fi

IMAGE_NAME="ftgo/${SERVICE_NAME}:${TAG}"

echo "Building Docker image: ${IMAGE_NAME}"
docker build -t "${IMAGE_NAME}" -f "${SERVICE_DIR}/docker/Dockerfile" .

echo "Docker image built: ${IMAGE_NAME}"
