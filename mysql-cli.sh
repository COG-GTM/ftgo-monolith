#! /bin/bash -e

docker run $* \
   --name mysqlterm --rm \
   -e MYSQL_PORT_3306_TCP_ADDR=$DOCKER_HOST_IP -e MYSQL_PORT_3306_TCP_PORT=3306 -e MYSQL_ENV_MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-rootpassword} \
   mysql:8.0@sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b  \
   sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" '
