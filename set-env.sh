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

# Token that restaurant/courier operators use for the order lifecycle endpoints.
# Generate a throwaway one for local runs unless the environment already provides it.
if [ -z "$FTGO_STAFF_API_TOKEN" ] ; then
    export FTGO_STAFF_API_TOKEN=`head -c 32 /dev/urandom | base64 | tr -d '=+/' `
    echo generated a local FTGO_STAFF_API_TOKEN
fi