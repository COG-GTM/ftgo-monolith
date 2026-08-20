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

# Load DB credentials from a local, gitignored .env so both docker-compose and the
# host-side Gradle tasks (waitForMySql, flywayMigrate) use the same values. On first
# run, seed .env from the committed example so out-of-the-box runs keep working.
if [ ! -f .env ] && [ -f .env.example ] ; then
    echo "No .env found; creating one from .env.example (edit it to change credentials)."
    cp .env.example .env
fi
if [ -f .env ] ; then
    set -a
    . ./.env
    set +a
fi
export SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD:-mysqlpw}
