#!/bin/bash -e

echo "Waiting for MySQL to start..."

MAX_RETRIES=60
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if mysql -h ${DOCKER_HOST_IP:-localhost} -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1" > /dev/null 2>&1; then
    echo "MySQL is ready!"
    exit 0
  fi
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "Waiting for MySQL... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo "MySQL did not start in time!"
exit 1
