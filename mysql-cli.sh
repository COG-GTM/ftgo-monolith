#! /bin/bash -e

# Use host networking so the client reaches the DB published on the host loopback
# (mysql/docker-compose bind ${MYSQL_HOST:-127.0.0.1}:3306); a container's own
# 127.0.0.1 would otherwise not reach the host.
docker run $* \
   --name mysqlterm --rm --network host \
   -e MYSQL_PORT_3306_TCP_ADDR=${MYSQL_HOST:-127.0.0.1} -e MYSQL_PORT_3306_TCP_PORT=3306 -e MYSQL_ENV_MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-rootpassword} \
   mysql:5.7.13  \
   sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" '
