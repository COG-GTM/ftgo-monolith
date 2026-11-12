if [ -f ./.env ] ; then
    _mysql_root_password_before_dotenv=${MYSQL_ROOT_PASSWORD-}
    _mysql_password_before_dotenv=${MYSQL_PASSWORD-}
    set -a
    . ./.env
    set +a
    if [ -n "$_mysql_root_password_before_dotenv" ]; then
        export MYSQL_ROOT_PASSWORD="$_mysql_root_password_before_dotenv"
    fi
    if [ -n "$_mysql_password_before_dotenv" ]; then
        export MYSQL_PASSWORD="$_mysql_password_before_dotenv"
    fi
    unset _mysql_root_password_before_dotenv _mysql_password_before_dotenv
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