#! /bin/bash -e

. ./set-env.sh

./gradlew waitForMySql :ftgo-flyway:flywayMigrate

for SN in ftgo-application ; do

    ./gradlew :${SN?}:assemble
    docker-compose build ${SN?}
    docker-compose up -d --force-recreate ${SN?}
done

docker-compose logs -f $*
