#! /bin/bash -e

if [ -e .env ]; then
  echo ".env already exists, leaving it"
  exit 0
fi

umask 077
printf 'MYSQL_ROOT_PASSWORD=%s\nMYSQL_USER=mysqluser\nMYSQL_PASSWORD=%s\n' \
  "$(openssl rand -hex 24)" \
  "$(openssl rand -hex 24)" > .env
