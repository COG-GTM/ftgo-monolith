
# Host DNS name doesn't resolve in Docker alpine images

export DOCKER_HOST_IP=$(hostname -I | sed -e 's/ .*//g')
export TERM=dumb
export FTGO_CONSUMER_TOKEN_SECRET="${FTGO_CONSUMER_TOKEN_SECRET:-$(openssl rand -hex 32)}"

