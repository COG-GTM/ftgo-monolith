#! /bin/bash -e

# docker-compose.yml uses the Compose Specification (no version key, depends_on condition
# service_completed_successfully), which needs the Docker Compose v2 CLI plugin.
COMPOSE_VERSION=v2.29.7

docker version
mkdir -p ~/.docker/cli-plugins
curl -fsSL https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-`uname -m` -o ~/.docker/cli-plugins/docker-compose
chmod +x ~/.docker/cli-plugins/docker-compose
docker compose version
