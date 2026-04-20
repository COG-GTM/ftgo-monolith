#! /bin/bash -e

for SN in ftgo-application ftgo-restaurant-service-standalone ; do

    ./gradlew :${SN?}:assemble
    docker-compose build ${SN?}
    docker-compose up -d --force-recreate ${SN?}
done

docker-compose logs -f $*
