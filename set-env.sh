if [ -f ./.env ] ; then
    set -a
    . ./.env
    set +a
fi

if [ -z "${MYSQL_ROOT_PASSWORD:-}" ] || [ -z "${MYSQL_PASSWORD:-}" ] ; then
    echo "MYSQL_ROOT_PASSWORD and MYSQL_PASSWORD must be set; run ./create-local-env.sh or export them"
    return 1 2>/dev/null || exit 1
fi

if [ -z "${DOCKER_HOST_IP:-}" ] ; then
    if [ -z "${DOCKER_HOST:-}" ] ; then
      export DOCKER_HOST_IP=localhost
    else
      echo using ${DOCKER_HOST?}
      XX=${DOCKER_HOST%\:*}
      export DOCKER_HOST_IP=${XX#tcp\:\/\/}
    fi
fi

echo DOCKER_HOST_IP is $DOCKER_HOST_IP
export COMPOSE_HTTP_TIMEOUT=240