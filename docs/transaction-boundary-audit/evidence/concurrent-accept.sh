#!/bin/bash
# Two orders accepted concurrently onto the same (only available) courier.
B=http://localhost:8080; J='-s -H Content-Type:application/json'
db() { sudo mysql ftgo -N -e "$1"; }
CID=$(db "select max(id) from consumers"); RID=$(db "select max(id) from restaurants")
READY=$(date -u -d '+1 hour' +%Y-%m-%dT%H:%M:%S)
for run in 1 2 3 4 5; do
  db "update courier set available=0"
  K=$(curl $J -X POST $B/couriers -d '{"name":{"firstName":"R","lastName":"'$run'"},"address":{"street1":"x","city":"Oakland","state":"CA","zip":"94611","latitude":37.81,"longitude":-122.21}}' | sed -E 's/.*"id":([0-9]+).*/\1/')
  curl $J -o /dev/null -X POST $B/couriers/$K/availability -d '{"available":true}'
  A=$(curl $J -X POST $B/orders -d "{\"consumerId\":$CID,\"restaurantId\":$RID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}" | sed -E 's/.*"orderId":([0-9]+).*/\1/')
  C=$(curl $J -X POST $B/orders -d "{\"consumerId\":$CID,\"restaurantId\":$RID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}" | sed -E 's/.*"orderId":([0-9]+).*/\1/')
  curl $J -o /dev/null -w "accept $A http=%{http_code}\n" -X POST $B/orders/$A/accept -d "{\"readyBy\":\"$READY\"}" &
  curl $J -o /dev/null -w "accept $C http=%{http_code}\n" -X POST $B/orders/$C/accept -d "{\"readyBy\":\"$READY\"}" &
  wait
  echo "run $run courier=$K orders assigned to it: $(db "select group_concat(id) from orders where assigned_courier_id=$K")  courier_actions rows: $(db "select count(*) from courier_actions where courier_id=$K") orders_with_actions: $(db "select group_concat(distinct order_id) from courier_actions where courier_id=$K")"
done
