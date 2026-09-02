if [ -z "$DOCKER_HOST_IP" ] ; then
    if [ -z "$DOCKER_HOST" ] ; then
      export DOCKER_HOST_IP=`hostname`
    else
      echo using ${DOCKER_HOST?}
      XX=${DOCKER_HOST%\:*}
      export DOCKER_HOST_IP=${XX#tcp\:\/\/}
    fi
fi

echo DOCKER_HOST_IP is $DOCKER_HOST_IP
export COMPOSE_HTTP_TIMEOUT=240

# Per-role API keys for privileged order lifecycle endpoints; generated per run unless supplied.
export FTGO_RESTAURANT_API_KEY=${FTGO_RESTAURANT_API_KEY:-$(openssl rand -hex 32)}
export FTGO_COURIER_API_KEY=${FTGO_COURIER_API_KEY:-$(openssl rand -hex 32)}
export FTGO_OPERATOR_API_KEY=${FTGO_OPERATOR_API_KEY:-$(openssl rand -hex 32)}