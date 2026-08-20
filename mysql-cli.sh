#! /bin/bash -e

# Attach the client to the compose network and connect straight to the "mysql"
# service container, so this works regardless of how the host port is published
# (the DB now binds host loopback only) and on Docker Desktop (macOS/Windows),
# where a container's 127.0.0.1 is not the host. Compose v2 keeps the project
# dash (ftgo-monolith_default); v1 strips it (ftgomonolith_default) — detect
# either at runtime, falling back to the v2 name. Override MYSQL_CLI_NETWORK /
# MYSQL_CLI_ADDR for a custom compose project name or a non-compose DB.
MYSQL_CLI_NETWORK="${MYSQL_CLI_NETWORK:-$(docker network ls --format '{{.Name}}' | grep -E '^ftgo-?monolith_default$' | head -n1 || true)}"
MYSQL_CLI_NETWORK="${MYSQL_CLI_NETWORK:-ftgo-monolith_default}"

docker run $* \
   --name mysqlterm --rm --network "${MYSQL_CLI_NETWORK}" \
   -e MYSQL_PORT_3306_TCP_ADDR=${MYSQL_CLI_ADDR:-mysql} -e MYSQL_PORT_3306_TCP_PORT=3306 -e MYSQL_ENV_MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-rootpassword} \
   mysql:5.7.13  \
   sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" '
