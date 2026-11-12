#! /bin/bash -e

if kubectl get secret ftgo-db-secret >/dev/null 2>&1; then
  if [ -n "$(kubectl get secret ftgo-db-secret -o jsonpath='{.data.root-password}')" ] &&
      [ -n "$(kubectl get secret ftgo-db-secret -o jsonpath='{.data.username}')" ] &&
      [ -n "$(kubectl get secret ftgo-db-secret -o jsonpath='{.data.password}')" ]; then
    echo "Secret ftgo-db-secret already exists, leaving it unchanged"
    exit 0
  fi

  echo "Secret ftgo-db-secret predates the externalized-credentials change. Its values were committed to git and must be treated as compromised. Run 'kubectl delete secret ftgo-db-secret', rerun this script, and rotate the MySQL accounts (ALTER USER) on the existing volume." >&2
  exit 1
fi

MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-$(openssl rand -hex 24)}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-$(openssl rand -hex 24)}

umask 077
f=$(mktemp)
trap 'rm -f "$f"' EXIT
printf 'root-password=%s\nusername=%s\npassword=%s\n' \
  "$MYSQL_ROOT_PASSWORD" mysqluser "$MYSQL_PASSWORD" > "$f"

kubectl create secret generic ftgo-db-secret --from-env-file="$f"
