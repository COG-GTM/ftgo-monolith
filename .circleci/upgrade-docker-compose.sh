#! /bin/bash -e

set -euo pipefail

DOCKER_COMPOSE_VERSION=1.19.0
# SHA256 of docker-compose-Linux-x86_64 from
# https://github.com/docker/compose/releases/download/1.19.0/docker-compose-Linux-x86_64.sha256
DOCKER_COMPOSE_SHA256=78734996d716113f9f9716d0b5064166e9475835e5000fd01b0480d19e1f7372

if [ "$(uname -s)" != "Linux" ] || [ "$(uname -m)" != "x86_64" ]; then
  echo "Unsupported platform: $(uname -s)-$(uname -m); only Linux-x86_64 is pinned" >&2
  exit 1
fi

docker-compose version
docker version

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

curl -fsSL --proto '=https' --tlsv1.2 \
  "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-Linux-x86_64" \
  -o "$TMP_DIR/docker-compose"

echo "${DOCKER_COMPOSE_SHA256}  $TMP_DIR/docker-compose" | sha256sum -c -

chmod +x "$TMP_DIR/docker-compose"
sudo install -m 0755 -o root -g root "$TMP_DIR/docker-compose" /usr/local/bin/docker-compose
docker-compose version
