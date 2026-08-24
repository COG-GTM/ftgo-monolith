#! /bin/bash

set -e

: "${FTGO_DB_USERNAME:?set FTGO_DB_USERNAME}"
: "${FTGO_DB_PASSWORD:?set FTGO_DB_PASSWORD}"
: "${FTGO_DB_ROOT_PASSWORD:?set FTGO_DB_ROOT_PASSWORD}"

kubectl create secret generic ftgo-db-secret \
  --from-literal=username="${FTGO_DB_USERNAME}" \
  --from-literal=password="${FTGO_DB_PASSWORD}" \
  --from-literal=root-password="${FTGO_DB_ROOT_PASSWORD}"
