# Transaction boundary audit: runtime evidence

Captured against the monolith at `4823d191` (`master`) running locally on Java 8 and MySQL 8, with the V1 and V2 Flyway migrations applied. The app was started with:

```
java -jar ftgo-application/build/libs/ftgo-application.jar \
  --logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG \
  --logging.level.org.springframework.transaction.interceptor=TRACE
```

**Local-only schema patch.** On a schema built only from the migrations, `POST /orders` fails: `DeliveryInformation` maps `Address.latitude/longitude` to columns `latitude`/`longitude`, but V2 adds `delivery_address_latitude`/`delivery_address_longitude` (see "Incidental findings" in `../../TRANSACTION_BOUNDARY_AUDIT.md`). To exercise the flows, the local database got an extra `ALTER TABLE orders ADD COLUMN latitude DOUBLE NULL, ADD COLUMN longitude DOUBLE NULL`. This repository does not include that change.

| File | What it shows |
|---|---|
| `scenarios.sh` / `scenarios-output.txt` | S1-S7 driver script and its HTTP/DB output |
| `s1-create-order.log` | `createOrder` is one JPA transaction; `ConsumerService.validateOrderForConsumer` logs "Participating in existing transaction" |
| `s2-create-order-unknown-consumer.log` | Unknown consumer rolls back and returns 500, with no order row |
| `s3-accept-no-courier.log` | `accept` rolls back on `NoCourierAvailableException`, so the order stays `APPROVED`. `api_request_log` is still committed in a separate transaction |
| `s4-accept-with-courier.log` | `update orders` and `insert into courier_actions` go out in one commit |
| `s5-accept-second-order.log` | A second assignment runs `delete from courier_actions where courier_id=?` and then re-inserts the whole plan |
| `s6-delivered.log` | `noteDelivered` updates only `orders`. The courier plan is left alone, so the workload still counts the delivered order |
| `s7-get-order.log` | `GET /orders/{id}` lazy-loads restaurant and courier plan through open-session-in-view, with no service transaction |
| `concurrent-accept.sh` / `concurrent-accept-output.txt` | 5 runs of 2 concurrent accepts onto one courier. No lost update showed up, which does not prove the code is safe (see B3) |
