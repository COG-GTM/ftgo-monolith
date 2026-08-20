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
# Credentials the caller already exported win over the seeded defaults.
_pre_mysql_root_password=$MYSQL_ROOT_PASSWORD
_pre_mysql_password=$MYSQL_PASSWORD
if [ ! -f .env ] && [ -f .env.example ] ; then
    echo "No .env found; creating one from .env.example (edit it to change credentials)."
    cp .env.example .env
fi
if [ -f .env ] ; then
    set -a
    . ./.env
    set +a
fi
[ -n "$_pre_mysql_root_password" ] && export MYSQL_ROOT_PASSWORD=$_pre_mysql_root_password
[ -n "$_pre_mysql_password" ] && export MYSQL_PASSWORD=$_pre_mysql_password
export SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD:-mysqlpw}
# Connector/J 8.x ships only the 'cj' driver class; WaitForMySql defaults to the
# legacy com.mysql.jdbc.Driver, so set it explicitly for the host waitForMySql task.
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=${SPRING_DATASOURCE_DRIVER_CLASS_NAME:-com.mysql.cj.jdbc.Driver}
