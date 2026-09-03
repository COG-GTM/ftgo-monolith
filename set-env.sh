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

# Local development database credentials. Override these in your shell for any non-local deployment.
export MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-rootpassword}
export MYSQL_USER=${MYSQL_USER:-mysqluser}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-mysqlpw}