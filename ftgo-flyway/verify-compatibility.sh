#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

containers=()
cleanup() {
  for container in "${containers[@]}"; do
    docker rm -f -v "$container" > /dev/null
  done
}
trap cleanup EXIT

docker build -t ftgo-mysql-compat ./mysql
for history in fresh legacy; do
  container="ftgo-flyway-${history}-$$"
  containers+=("$container")
  docker run -d --name "$container" -p 127.0.0.1::3306 \
    -e MYSQL_ROOT_PASSWORD=rootpassword -e MYSQL_ROOT_HOST=% \
    -e MYSQL_USER=mysqluser -e MYSQL_PASSWORD=mysqlpw ftgo-mysql-compat > /dev/null
  port=$(docker port "$container" 3306/tcp | cut -d: -f2)
  url="jdbc:mysql://127.0.0.1:${port}/ftgo?useSSL=false&allowPublicKeyRetrieval=true"
  ready=false
  for attempt in {1..90}; do
    if docker exec "$container" mysql -h127.0.0.1 -uroot -prootpassword -e 'USE ftgo; SELECT 1' > /dev/null 2>&1; then
      ready=true
      break
    fi
    sleep 2
  done
  if [[ "$ready" != true ]]; then docker logs "$container"; exit 1; fi
  if [[ "$history" == legacy ]]; then
    ./gradlew :ftgo-flyway:legacyFlywayMigrate "-Dflyway.url=$url"
    migration_count=$(docker exec "$container" mysql -uroot -prootpassword -N \
      -e 'SELECT COUNT(*) FROM ftgo.flyway_schema_history WHERE success=1 AND version IN (1,2)')
    [[ "$migration_count" == 2 ]]
    legacy_checksums=$(docker exec "$container" mysql -uroot -prootpassword -N \
      -e 'SELECT version, checksum FROM ftgo.flyway_schema_history ORDER BY installed_rank')
  fi
  ./gradlew :ftgo-flyway:flywayMigrate :ftgo-flyway:flywayValidate "-Dflyway.url=$url"
  docker exec "$container" mysql -uroot -prootpassword -e \
    'SELECT version, checksum, success FROM ftgo.flyway_schema_history ORDER BY installed_rank'
  if [[ "$history" == legacy ]]; then
    current_checksums=$(docker exec "$container" mysql -uroot -prootpassword -N \
      -e 'SELECT version, checksum FROM ftgo.flyway_schema_history ORDER BY installed_rank')
    [[ "$current_checksums" == "$legacy_checksums" ]]
  fi
  echo "PASS: $history database migrated and validated with Flyway 10"
done
