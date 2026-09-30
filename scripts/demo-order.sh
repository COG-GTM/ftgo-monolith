#!/usr/bin/env bash
#
# Creates a consumer, a restaurant with one menu item, and an order through the REST API, then reads the order back.
# Usage: scripts/demo-order.sh (or `make demo-order`).

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

post() {
  local path=$1 body=$2 response
  printf '$ curl -fsS -H "Content-Type: application/json" -d %q %s\n' "$body" "$APP_URL$path" >&2
  response=$(curl -fsS --max-time 10 -H 'Content-Type: application/json' -d "$body" "$APP_URL$path")
  echo "$response" >&2
  echo "$response"
}

wait_healthy

step "Create a consumer"
CONSUMER_ID=$(post /consumers '{"name":{"firstName":"Ada","lastName":"Lovelace"}}' | jq -er '.consumerId')

step "Create a restaurant with one menu item"
RESTAURANT_ID=$(post /restaurants '{"name":"Demo Curry House",
  "address":{"street1":"1 High Street","city":"Oakland","state":"CA","zip":"94619"},
  "menu":{"menuItemDTOs":[{"id":"1","name":"Chicken Vindaloo","price":"12.34"}]}}' | jq -er '.id')

step "Place an order: 2 x Chicken Vindaloo"
ORDER_ID=$(post /orders "{\"consumerId\":$CONSUMER_ID,\"restaurantId\":$RESTAURANT_ID,
  \"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}" | jq -er '.orderId')

step "Read it back"
run curl -fsS "$APP_URL/orders/$ORDER_ID"
echo
run curl -fsS "$APP_URL/orders?consumerId=$CONSUMER_ID"
echo
