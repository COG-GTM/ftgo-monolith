#!/bin/bash
B=http://localhost:8080
LOG=/home/ubuntu/app.log
OUT=/home/ubuntu/audit-evidence
J='-s -H Content-Type:application/json'
mark() { wc -l < $LOG; }
slice() { sed -n "$(( $1 + 1 )),\$p" $LOG | grep -E "JpaTransactionManager|TransactionInterceptor|org.hibernate.SQL" | sed -E 's/^.{24}//' > $OUT/$2.log; }
db() { sudo mysql ftgo -N -e "$1"; }

CID=$(curl $J -X POST $B/consumers -d '{"name":{"firstName":"Ann","lastName":"Lee"}}' | sed -E 's/.*"consumerId":([0-9]+).*/\1/')
RID=$(curl $J -X POST $B/restaurants -d '{"name":"Ajanta","address":{"street1":"1 High St","city":"Oakland","state":"CA","zip":"94619","latitude":37.8,"longitude":-122.2},"menu":{"menuItemDTOs":[{"id":"1","name":"Chicken Vindaloo","price":"12.34"}]}}' | sed -E 's/.*"id":([0-9]+).*/\1/')
echo "consumer=$CID restaurant=$RID"

echo "== S1 createOrder (happy path)"
M=$(mark); R=$(curl $J -X POST $B/orders -d "{\"consumerId\":$CID,\"restaurantId\":$RID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}"); echo "$R"; slice $M s1-create-order
O1=$(echo $R | sed -E 's/.*"orderId":([0-9]+).*/\1/')

echo "== S2 createOrder with unknown consumer"
M=$(mark); curl $J -o /dev/null -w "http=%{http_code}\n" -X POST $B/orders -d "{\"consumerId\":999999,\"restaurantId\":$RID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}"; slice $M s2-create-order-unknown-consumer
echo "orders for consumer 999999: $(db "select count(*) from orders where consumer_id=999999")"

echo "== S3 accept with no available courier"
READY=$(date -u -d '+1 hour' +%Y-%m-%dT%H:%M:%S)
M=$(mark); curl $J -w "\nhttp=%{http_code}\n" -X POST $B/orders/$O1/accept -d "{\"readyBy\":\"$READY\"}"; slice $M s3-accept-no-courier
echo "order $O1 after failed accept: $(db "select order_state, accept_time, ready_by, assigned_courier_id from orders where id=$O1")"

echo "== S4 create courier + available, accept again"
KID=$(curl $J -X POST $B/couriers -d '{"name":{"firstName":"Cy","lastName":"Doe"},"address":{"street1":"2 Low St","city":"Oakland","state":"CA","zip":"94611","latitude":37.81,"longitude":-122.21}}' | sed -E 's/.*"id":([0-9]+).*/\1/')
curl $J -X POST $B/couriers/$KID/availability -d '{"available":true}' -o /dev/null
M=$(mark); curl $J -w "http=%{http_code}\n" -X POST $B/orders/$O1/accept -d "{\"readyBy\":\"$READY\"}"; slice $M s4-accept-with-courier
echo "order $O1: $(db "select order_state, assigned_courier_id from orders where id=$O1")"
echo "courier_actions: "; db "select courier_id, order_id, type, time from courier_actions where courier_id=$KID"

echo "== S5 second order accepted onto same courier"
O2=$(curl $J -X POST $B/orders -d "{\"consumerId\":$CID,\"restaurantId\":$RID,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}" | sed -E 's/.*"orderId":([0-9]+).*/\1/')
M=$(mark); curl $J -w "http=%{http_code}\n" -X POST $B/orders/$O2/accept -d "{\"readyBy\":\"$READY\"}"; slice $M s5-accept-second-order

echo "== S6 drive O1 to DELIVERED, check courier workload"
for s in preparing ready pickedup; do curl $J -o /dev/null -w "$s http=%{http_code}\n" -X POST $B/orders/$O1/$s; done
M=$(mark); curl $J -o /dev/null -w "delivered http=%{http_code}\n" -X POST $B/orders/$O1/delivered; slice $M s6-delivered
echo "order $O1: $(db "select order_state from orders where id=$O1")"
echo "courier_actions for delivered order $O1: $(db "select count(*) from courier_actions where order_id=$O1")"
echo "workload: $(curl -s $B/couriers/$KID/workload)"

echo "== S7 GET /orders/$O1 (read path, OSIV)"
M=$(mark); curl -s $B/orders/$O1; echo; slice $M s7-get-order
