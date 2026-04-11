#!/bin/bash -e

KEEP_RUNNING=
DATABASE_SERVICES="mysql"

if [ -z "$DOCKER_COMPOSE" ] ; then
    DOCKER_COMPOSE=docker-compose
fi

while [ ! -z "$*" ] ; do
  case $1 in
    "--keep-running" )
      KEEP_RUNNING=yes
      ;;
    "--help" )
      echo ./build-and-test-all.sh --keep-running
      exit 0
      ;;
  esac
  shift
done

echo KEEP_RUNNING=$KEEP_RUNNING

# Step 1: Compile test classes
echo "=== Compiling test classes ==="
./gradlew testClasses

# Step 2: Start database with Docker Compose
echo "=== Starting database ==="
${DOCKER_COMPOSE?} down --remove-orphans -v
${DOCKER_COMPOSE?} up -d --build ${DATABASE_SERVICES?}

# Step 3: Wait for MySQL to be ready
echo "=== Waiting for MySQL ==="
./wait-for-mysql.sh

# Step 4: Run Flyway migrations
echo "=== Running Flyway migrations ==="
./gradlew flywayMigrate

# Step 5: Run unit tests
echo "=== Running unit tests ==="
./gradlew test

# Step 6: Run integration tests
echo "=== Running integration tests ==="
./gradlew integrationTest

# Step 7: Start the service for end-to-end tests
echo "=== Starting Courier Service for E2E tests ==="
./gradlew bootRun &
SERVICE_PID=$!

echo "=== Waiting for service to start ==="
sleep 15

for i in $(seq 1 30); do
  if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "Service is up!"
    break
  fi
  echo "Waiting for service... ($i)"
  sleep 2
done

# Step 8: Run end-to-end tests
echo "=== Running end-to-end tests ==="
./gradlew endToEndTest || E2E_RESULT=$?

# Cleanup
kill $SERVICE_PID 2>/dev/null || true
wait $SERVICE_PID 2>/dev/null || true

if [ -z "$KEEP_RUNNING" ] ; then
  ${DOCKER_COMPOSE?} down --remove-orphans -v
fi

if [ -n "$E2E_RESULT" ]; then
  echo "End-to-end tests failed!"
  exit $E2E_RESULT
fi

echo "=== All tests passed! ==="
