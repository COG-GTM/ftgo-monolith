#! /bin/bash -e

# Create the ftgo-db-secret from environment variables so credentials are never
# hardcoded or committed. The username is fixed to 'mysqluser' (matches the grant
# in mysql/schema.sql); only the passwords are secrets. Required env vars:
: "${MYSQL_ROOT_PASSWORD:?set MYSQL_ROOT_PASSWORD}"
: "${MYSQL_PASSWORD:?set MYSQL_PASSWORD}"

kubectl create secret generic ftgo-db-secret \
  --from-literal=root-password="$MYSQL_ROOT_PASSWORD" \
  --from-literal=username="mysqluser" \
  --from-literal=password="$MYSQL_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -
