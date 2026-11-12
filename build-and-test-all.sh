#! /bin/bash -e

KEEP_RUNNING=
ASSEMBLE_ONLY=
DATABASE_SERVICES="mysql"

if [ -z "$DOCKER_COMPOSE" ] ; then
    DOCKER_COMPOSE=docker-compose
fi

while [ ! -z "$*" ] ; do
  case $1 in
    "--keep-running" )
      KEEP_RUNNING=yes
      ;;
    "--assemble-only" )
      ASSEMBLE_ONLY=yes
      ;;
    "--help" )
      echo ./build-and-test-all.sh --keep-running --assemble-only
      exit 0
      ;;
  esac
  shift
done

echo KEEP_RUNNING=$KEEP_RUNNING

. ./set-env.sh

if [ -z "$FTGO_OPERATOR_PASSWORD_HASH" ] ; then
  export FTGO_OPERATOR_USERNAME=${FTGO_OPERATOR_USERNAME:-operator}
  export FTGO_OPERATOR_PASSWORD=${FTGO_OPERATOR_PASSWORD:-$(head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n')}
  FTGO_OPERATOR_PASSWORD_HASH=$(docker run --rm httpd:2.4-alpine htpasswd -nbBC 10 "" "$FTGO_OPERATOR_PASSWORD" | tr -d ':\n' | sed 's/^\$2y/\$2a/')
  if ! [[ "$FTGO_OPERATOR_PASSWORD_HASH" =~ ^\$2a\$[0-9]{2}\$[./0-9A-Za-z]{53}$ ]] ; then
    echo "Failed to generate a BCrypt hash for the operator password" >&2
    exit 1
  fi
  export FTGO_OPERATOR_PASSWORD_HASH
fi

# TODO Temporarily

./gradlew testClasses

${DOCKER_COMPOSE?} down --remove-orphans -v
${DOCKER_COMPOSE?} up -d --build ${DATABASE_SERVICES?}

./gradlew waitForMySql

echo mysql is started

./gradlew :ftgo-flyway:flywayMigrate

if [ -z "$ASSEMBLE_ONLY" ] ; then

  ./gradlew -x :ftgo-end-to-end-tests:test $* build

  ${DOCKER_COMPOSE?} build

  ./gradlew $* integrationTest

  ${DOCKER_COMPOSE?} up -d
else

  ./gradlew $* assemble

  ${DOCKER_COMPOSE?} up -d --build ${DATABASE_SERVICES?}

  ./gradlew waitForMySql

  echo mysql is started

  ${DOCKER_COMPOSE?} up -d --build
fi

./wait-for-services.sh

./run-end-to-end-tests.sh

if [ -z "$KEEP_RUNNING" ] ; then
  ${DOCKER_COMPOSE?} down --remove-orphans -v
fi
