
# Host DNS name doesn't resolve in Docker alpine images

export DOCKER_HOST_IP=$(hostname -I | sed -e 's/ .*//g')
export TERM=dumb
export FTGO_OPERATOR_PASSWORD="${FTGO_OPERATOR_PASSWORD:-$(openssl rand -hex 24)}"
