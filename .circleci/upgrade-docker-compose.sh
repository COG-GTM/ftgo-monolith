#! /bin/bash -e

docker-compose version
docker version

DOCKER_COMPOSE_VERSION=1.19.0
# SHA-256 of docker-compose-Linux-x86_64 for the pinned version above.
DOCKER_COMPOSE_SHA256=78734996d716113f9f9716d0b5064166e9475835e5000fd01b0480d19e1f7372

curl -fL "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o ~/docker-compose
echo "${DOCKER_COMPOSE_SHA256}  ${HOME}/docker-compose" | sha256sum -c -
chmod +x ~/docker-compose
sudo mv ~/docker-compose /usr/local/bin/docker-compose
