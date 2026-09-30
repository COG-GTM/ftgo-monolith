#! /bin/bash -e

: "${FTGO_ADMIN_PASSWORD:?export FTGO_ADMIN_PASSWORD before starting the application (see README, Security)}"

docker-compose up -d --build mysql

./wait-for-mysql.sh

docker-compose up -d --build

echo -n waiting for the services to start...

./wait-for-services.sh
