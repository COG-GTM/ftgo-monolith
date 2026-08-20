#! /bin/bash -e

# Create the ftgo-db-secret from environment variables so credentials are never
# hardcoded or committed. Required env vars (fail fast if unset):
: "${MYSQL_ROOT_PASSWORD:?set MYSQL_ROOT_PASSWORD}"
: "${MYSQL_USER:?set MYSQL_USER}"
: "${MYSQL_PASSWORD:?set MYSQL_PASSWORD}"

kubectl create secret generic ftgo-db-secret \
  --from-literal=root-password="$MYSQL_ROOT_PASSWORD" \
  --from-literal=username="$MYSQL_USER" \
  --from-literal=password="$MYSQL_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -
