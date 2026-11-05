#! /bin/bash -e

host=${DOCKER_HOST_IP:-localhost}
if [ "$host" = localhost ] || [ "$host" = 127.0.0.1 ] ; then
    host=host.docker.internal
fi

docker run $* \
   --name mysqlterm --rm \
   --add-host host.docker.internal:host-gateway \
   -e MYSQL_PORT_3306_TCP_ADDR=$host -e MYSQL_PORT_3306_TCP_PORT=3306 -e MYSQL_ENV_MYSQL_ROOT_PASSWORD=rootpassword \
   mysql:8.4  \
   sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" '
