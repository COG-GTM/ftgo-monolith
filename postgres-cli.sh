#! /bin/bash -e

docker run $* \
   --name postgresterm --rm \
   -e PGHOST=$DOCKER_HOST_IP -e PGPORT=5432 -e PGUSER=ftgouser -e PGPASSWORD=ftgopw -e PGDATABASE=ftgo \
   postgres:13-alpine \
   sh -c 'exec psql'
