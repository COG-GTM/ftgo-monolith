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

# ADMIN user for the ftgo-application container and the end-to-end tests (see README, Security).
export FTGO_ADMIN_USERNAME=${FTGO_ADMIN_USERNAME:-admin}
if [ -z "$FTGO_ADMIN_PASSWORD" ] ; then
    export FTGO_ADMIN_PASSWORD=$(head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n')
fi
