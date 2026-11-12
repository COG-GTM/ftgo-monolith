#! /bin/bash -e

read -r -a compose_command <<< "${DOCKER_COMPOSE:-docker-compose}"
mysql_args=()
for arg in "$@"; do
  if [ "$arg" != "-i" ]; then
    mysql_args+=("$arg")
  fi
done

"${compose_command[@]}" exec -T mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$@"' sh "${mysql_args[@]}"
