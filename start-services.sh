#! /bin/bash -e

docker compose up -d --build mysql

./wait-for-mysql.sh
./gradlew :ftgo-flyway:flywayMigrate

docker compose up -d --build

echo -n waiting for the services to start...

./wait-for-services.sh
