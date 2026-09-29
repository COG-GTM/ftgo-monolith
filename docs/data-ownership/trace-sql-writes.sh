#!/usr/bin/env bash
# Drives each REST endpoint once against a running ftgo-application and prints
# the DML (insert/update/delete) statements Hibernate issued for that request.
# Usage: APP_LOG_CMD="docker logs ftgo-app" ./trace-sql-writes.sh [base-url]
set -euo pipefail
BASE=${1:-http://localhost:8080}
LOG_CMD=${APP_LOG_CMD:-"docker logs ftgo-app"}

log_lines() { $LOG_CMD 2>&1 | wc -l; }

call() {
  local label=$1 method=$2 path=$3 body=${4:-}
  local start; start=$(log_lines)
  if [ -n "$body" ]; then
    RESP=$(curl -s -X "$method" -H 'Content-Type: application/json' -d "$body" "$BASE$path")
  else
    RESP=$(curl -s -X "$method" "$BASE$path")
  fi
  sleep 1
  echo "### $label  ($method $path)"
  $LOG_CMD 2>&1 | tail -n +$((start + 1)) \
    | grep -E 'org.hibernate.SQL' \
    | sed -E 's/.*org.hibernate.SQL[^:]*: //' \
    | grep -Ei '^(insert|update|delete)' \
    | sed -E 's/^(insert into|update|delete from) ([a-z_]+).*/\1 \2/I' \
    | sort | uniq -c | sed 's/^/    /' || true
  echo
}

ADDR='{"street1":"1 Main St","city":"Oakland","state":"CA","zip":"94611","latitude":37.8,"longitude":-122.2}'

call "consumer: create" POST /consumers '{"name":{"firstName":"Jane","lastName":"Doe"}}'
CONSUMER_ID=$(echo "$RESP" | sed -E 's/.*"consumerId":([0-9]+).*/\1/')

call "restaurant: create" POST /restaurants "{\"name\":\"Ajanta\",\"address\":$ADDR,\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Curry\",\"price\":\"12.34\"}]}}"
RESTAURANT_ID=$(echo "$RESP" | sed -E 's/.*"id":([0-9]+).*/\1/')

call "courier: create" POST /couriers "{\"name\":{\"firstName\":\"Carl\",\"lastName\":\"Courier\"},\"address\":$ADDR}"
COURIER_ID=$(echo "$RESP" | sed -E 's/.*"id":([0-9]+).*/\1/')

call "courier: availability" POST /couriers/$COURIER_ID/availability '{"available":true}'
call "courier: location" POST /couriers/$COURIER_ID/location '{"latitude":37.81,"longitude":-122.21}'

ORDER_BODY="{\"consumerId\":$CONSUMER_ID,\"restaurantId\":$RESTAURANT_ID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}"
call "order: create" POST /orders "$ORDER_BODY"
ORDER_ID=$(echo "$RESP" | sed -E 's/.*"orderId":([0-9]+).*/\1/')

call "order: revise" POST /orders/$ORDER_ID/revise '{"revisedLineItemQuantities":{"1":3}}'
READY_BY=$(date -u -d '+1 hour' +%Y-%m-%dT%H:%M:%S)
call "kitchen: accept (schedules courier)" POST /orders/$ORDER_ID/accept "{\"readyBy\":\"$READY_BY\"}"
call "kitchen: preparing" POST /orders/$ORDER_ID/preparing
call "kitchen: ready" POST /orders/$ORDER_ID/ready
call "delivery: picked up" POST /orders/$ORDER_ID/pickedup
call "delivery: delivered" POST /orders/$ORDER_ID/delivered

call "order: create (to cancel)" POST /orders "$ORDER_BODY"
ORDER2_ID=$(echo "$RESP" | sed -E 's/.*"orderId":([0-9]+).*/\1/')
call "order: cancel" POST /orders/$ORDER2_ID/cancel

call "order: get (read-only)" GET /orders/$ORDER_ID
call "courier: workload (read-only)" GET /couriers/$COURIER_ID/workload
