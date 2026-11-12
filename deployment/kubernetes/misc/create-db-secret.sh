#! /bin/bash -e

if kubectl get secret ftgo-db-secret >/dev/null 2>&1; then
  echo "Secret ftgo-db-secret already exists, leaving it unchanged"
  exit 0
fi

MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-$(openssl rand -hex 24)}
MYSQL_USER=${MYSQL_USER:-mysqluser}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-$(openssl rand -hex 24)}

umask 077
f=$(mktemp)
trap 'rm -f "$f"' EXIT
printf 'root-password=%s\nusername=%s\npassword=%s\n' \
  "$MYSQL_ROOT_PASSWORD" "$MYSQL_USER" "$MYSQL_PASSWORD" > "$f"

kubectl create secret generic ftgo-db-secret --from-env-file="$f"
