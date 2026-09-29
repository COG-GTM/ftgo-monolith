# Order Service Extraction Plan

Status: proposal
Scope: extract the Order bounded context (`ftgo-order-service`, `ftgo-order-service-api`, and the order
classes in `ftgo-domain`) from `ftgo-application` into an independently deployable service with its own
database, using a strangler-fig approach.

## 1. Current state (what couples Order to the rest of the monolith)

The Order code is already in its own Gradle modules, but it is not a separable unit yet. The coupling
points, all of which must be cut before the service can run alone:

| # | Coupling | Where | Kind |
|---|----------|-------|------|
| C1 | `Order.restaurant` is a JPA `@ManyToOne Restaurant` (FK `orders.restaurant_id -> restaurants.id`) | `ftgo-domain/.../Order.java` | object reference + FK |
| C2 | `Order.assignedCourier` is a JPA `@ManyToOne Courier` (FK `orders.assigned_courier_id -> courier.id`) | `Order.java` | object reference + FK |
| C3 | `Courier.plan.actions` (`courier_actions`) each hold `@ManyToOne Order` (FK `courier_actions.order_id -> orders.id`) | `Action.java`, `Plan.java` | reverse object reference + FK |
| C4 | `OrderService.createOrder` reads `Restaurant` + menu items from `RestaurantRepository` to price line items | `OrderService.java` | direct repository access |
| C5 | `OrderService.createOrder` calls `ConsumerService.validateOrderForConsumer` in-process (Gradle dependency on `:ftgo-consumer-service`) | `OrderService.java`, `ftgo-order-service/build.gradle` | in-process call |
| C6 | `OrderService.accept` -> `scheduleDelivery` loads all available couriers, runs `CourierAssignmentStrategy`, mutates the `Courier` aggregate and the `Order` **in one DB transaction** | `OrderService.java`, `OrderConfiguration.java` | cross-aggregate transaction |
| C7 | `OrderController.makeGetOrderResponse` walks `order.getAssignedCourier().actionsForDelivery(order)` to build `courierActions` / `estimatedDeliveryTime` and reads `order.getRestaurant().getName()` | `OrderController.java` | read-time join across contexts |
| C8 | Order aggregate classes (`Order`, `OrderLineItem(s)`, `OrderState`, `OrderRevision`, `LineItemQuantityChange`, `DeliveryInformation`, `PaymentInformation`, `OrderMinimumNotMetException`, `OrderRepository`) live in the shared `ftgo-domain` module next to Consumer/Courier/Restaurant | `ftgo-domain` | shared domain module |
| C9 | `GetOrderResponse` exposes the JPA-embeddable `net.chrisrichardson.ftgo.domain.Action` directly | `GetOrderResponse.java` | domain type leaks into API |
| C10 | `OrderNotFoundException` is mapped by the monolith's `GlobalExceptionHandler`; API tracking (`api_request_log`) is registered by `FtgoApplicationMain` | `ftgo-application` | cross-cutting config |

Non-coupling (safe to share): `ftgo-common` value types (`Money`, `Address`, `PersonName`) and the
`ftgo-common` tracking/logging utilities can be consumed as a library by both deployables.

## 2. Data ownership

### Tables that move to the Order service

| Table | Moves? | Notes |
|-------|--------|-------|
| `orders` | **Yes** — owned by Order service | Keeps its `auto_increment` id; `version` column continues to back optimistic locking. |
| `order_line_items` | **Yes** — owned by Order service | Already snapshots `name` and `price` at order time, so no runtime dependency on the menu once the order exists. |
| `api_request_log` | **Copy, not move** | Cross-cutting. Order service gets its own empty instance in its own schema; the monolith keeps its table. |
| `flyway_schema_history` | New per schema | Order service gets its own Flyway migration set. |

### Tables that stay in the monolith (owned by other contexts)

| Table | Owner | How Order service uses it after extraction |
|-------|-------|--------------------------------------------|
| `restaurants`, `restaurant_menu_items` | Restaurant | Sync API read at order creation (menu + pickup location). Restaurant name and pickup lat/lng are snapshotted onto `orders`. |
| `consumers`, `hibernate_sequence` | Consumer | Sync API call to validate the consumer at order creation. (`hibernate_sequence` only backs `Consumer` ids; Order uses `IDENTITY`.) |
| `courier`, `courier_actions` | Courier / Delivery | Delivery scheduling becomes an API owned by the courier side. `courier_actions.order_id` becomes a soft reference (no FK). |

### Schema changes (in order)

These are expressed as Flyway migrations in the existing `ftgo-flyway` module while the DB is still shared,
and are then replayed as the baseline of the new Order schema.

- **V3 (additive, monolith still works unchanged)**
  - `orders.restaurant_name VARCHAR(255)` — backfill from `restaurants.name`.
  - `orders.pickup_latitude DOUBLE`, `orders.pickup_longitude DOUBLE` — backfill from `restaurants.latitude/longitude`.
  - `orders.estimated_delivery_time DATETIME` — backfill from the `DROPOFF` row in `courier_actions` for that order.
- **V4 (drop cross-context FKs, after code no longer relies on them)**
  - `ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id;`
  - `ALTER TABLE orders DROP FOREIGN KEY orders_assigned_courier_id;`
  - `ALTER TABLE courier_actions DROP FOREIGN KEY courier_actions_order_id;`
  - Keep `order_line_items_id` (both tables move together).
- Column names `restaurant_id`, `assigned_courier_id` and `courier_actions.order_id` do **not** change:
  replacing `@ManyToOne Restaurant restaurant` with `Long restaurantId` (and likewise for courier/order) maps
  to the same column names under Spring's default naming strategy, so no data rewrite is needed.

## 3. API surface

### 3.1 Public API exposed by the Order service (unchanged contract)

The external contract stays byte-compatible with today so the UI (`ftgo-application/src/main/resources/static`)
and `ftgo-end-to-end-tests` keep working through the router without changes.

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/orders` | `CreateOrderRequest {consumerId, restaurantId, lineItems[{menuItemId, quantity}]}` | `CreateOrderResponse {orderId}` |
| GET | `/orders/{orderId}` | — | `GetOrderResponse {orderId, state, orderTotal, restaurantName, assignedCourier, courierActions[{type,time}], estimatedDeliveryTime}` / 404 |
| GET | `/orders?consumerId={id}` | — | `List<GetOrderResponse>` |
| POST | `/orders/{orderId}/cancel` | — | `GetOrderResponse` / 404 |
| POST | `/orders/{orderId}/revise` | `ReviseOrderRequest {revisedLineItemQuantities}` | `GetOrderResponse` / 404 |
| POST | `/orders/{orderId}/accept` | `OrderAcceptance {readyBy}` | 200 |
| POST | `/orders/{orderId}/preparing` | — | 200 |
| POST | `/orders/{orderId}/ready` | — | 200 |
| POST | `/orders/{orderId}/pickedup` | — | 200 |
| POST | `/orders/{orderId}/delivered` | — | 200 |
| GET | `/actuator/health`, `/actuator/prometheus` | — | ops |

Contract clean-up done as part of extraction (JSON shape unchanged):
- Move `GetOrderResponse` from `ftgo-order-service` into `ftgo-order-service-api` so the router,
  e2e tests and any future client share one DTO.
- Replace the domain `Action` in `GetOrderResponse.courierActions` with `CourierActionDTO {type, time}`
  (C9). Jackson output is identical because `Action` only exposes `getType()`/`getTime()`.
- Error bodies: port the `OrderNotFoundException` handler from `GlobalExceptionHandler` into the
  service so 404/409 payloads match (C10).

### 3.2 APIs the Order service consumes (provided by the monolith)

| Purpose | Endpoint | Exists today? | Notes |
|---------|----------|---------------|-------|
| Validate consumer (C5) | `GET /consumers/{id}` (phase 1), then `POST /internal/consumers/{id}/order-validations {orderTotal}` | GET exists | `Consumer.validateOrderByConsumer` is a no-op today, so existence check is behaviourally equivalent. Add the POST when real rules appear. |
| Price line items (C4) | `GET /internal/restaurants/{id}/menu` -> `{id, name, address{lat,lng}, menuItems[{id,name,price}]}` | **New** | Current `GET /restaurants/{id}` returns only `{id,name}`. Add a new internal endpoint rather than widening the public one. |
| Schedule delivery (C6) | `POST /internal/deliveries {orderId, pickup{lat,lng}, readyBy}` -> `{courierId, pickupEta, estimatedDeliveryTime}` | **New** | Idempotent on `orderId` (repeat returns existing assignment). Moves `scheduleDelivery` + `estimateDeliveryTime` + `CourierAssignmentStrategy` into `CourierService`. 409 when `NoCourierAvailableException`. |
| Compensate delivery | `DELETE /internal/deliveries/{orderId}` | **New** | Wraps existing `Courier.cancelDelivery(order)` (reworked to take `orderId`). |
| Courier actions for read model (C7) | `GET /internal/deliveries/{orderId}` -> `{courierId, actions[{type,time}]}` | **New** | Used to populate `courierActions` in `GetOrderResponse`; on timeout, return `courierActions: null` (the field is already nullable) and keep `estimatedDeliveryTime` from the local column. |

`/internal/**` paths are not routed from the edge; they are reachable only on the service network.

### 3.3 Events (deferred, not required for cut-over)

`ftgo-order-service-api` already has an `api.events` package (`OrderDetails`, `OrderLineItemDTO`). Once
the synchronous split is stable, publish `OrderCreated`, `OrderAccepted`, `OrderCancelled`,
`OrderDelivered` via a transactional outbox table in the Order schema. This is a follow-up; the plan below
deliberately uses synchronous calls first because every existing interaction is synchronous and the
call graph is small (3 downstream endpoints).

## 4. Migration order

Each step is independently shippable and leaves the system working. Steps 1–3 happen inside the
monolith (no new deployable); this is where most of the risk is retired.

### Phase 1 — Build seams inside the monolith

1. **Move the Order aggregate out of `ftgo-domain` (C8).** Move `Order`, `OrderLineItem`, `OrderLineItems`,
   `OrderState`, `OrderRevision`, `LineItemQuantityChange`, `DeliveryInformation`, `PaymentInformation`,
   `OrderMinimumNotMetException`, `OrderRepository` into `ftgo-order-service` (`...orderservice.domain`).
   Pure package move; `ftgo-domain` no longer knows about orders except via ids.
2. **Replace object references with ids (C1, C2, C3).**
   - `Order.restaurant` -> `Long restaurantId` + `String restaurantName` + pickup lat/lng snapshot (Flyway V3).
   - `Order.assignedCourier` -> `Long assignedCourierId` + `LocalDateTime estimatedDeliveryTime`.
   - `Action.order` -> `Long orderId`; `Plan.removeDelivery/actionsForDelivery` take `long orderId`.
3. **Introduce ports in the Order service with in-process adapters (C4, C5, C6).**
   ```java
   interface ConsumerVerifier   { void verify(long consumerId, Money orderTotal); }
   interface RestaurantCatalog  { RestaurantMenuSnapshot getMenu(long restaurantId); }
   interface DeliveryScheduler  { DeliveryAssignment schedule(long orderId, Location pickup, LocalDateTime readyBy);
                                  void cancel(long orderId);
                                  Optional<DeliveryPlan> find(long orderId); }
   ```
   In-process adapters delegate to `ConsumerService`, `RestaurantService`, `CourierService`. Move
   `scheduleDelivery`/`estimateDeliveryTime` and the `CourierAssignmentStrategy` bean from
   `OrderService`/`OrderConfiguration` into the courier module. Remove the Gradle dependency
   `ftgo-order-service -> :ftgo-consumer-service`.
4. **Break the cross-aggregate transaction (C6).** `OrderService.accept` becomes:
   1. load order, validate `APPROVED -> ACCEPTED` transition (no write yet);
   2. `deliveryScheduler.schedule(orderId, pickup, readyBy)` — idempotent on `orderId`;
   3. apply `acceptTicket(readyBy)`, set `assignedCourierId`/`estimatedDeliveryTime`, commit;
   4. if step 3 fails (e.g. optimistic lock), call `deliveryScheduler.cancel(orderId)`.
   Behaviour matches today: if no courier is available the order stays `APPROVED` and the caller gets an
   error. `cancel` is only legal in `APPROVED` (before scheduling), so cancellation never needs to touch
   the courier side.
5. **Serve reads from Order-owned data (C7).** `makeGetOrderResponse` uses `restaurantName`,
   `assignedCourierId`, `estimatedDeliveryTime` from `orders` and `DeliveryScheduler.find` for
   `courierActions`.
6. **Flyway V4: drop the three cross-context FKs.**
7. **Guard the boundary.** Add an ArchUnit (or simple Gradle dependency) check that
   `net.chrisrichardson.ftgo.orderservice..` does not depend on `consumerservice..`, `courierservice..`,
   `restaurantservice..` or the Courier/Restaurant/Consumer entities.

Exit criteria: `./build-and-test-all.sh` and the existing e2e tests (`shouldCreateReviseAndCancelOrder`,
`shouldDeliverOrder`) pass against the monolith with no behaviour change.

### Phase 2 — Stand up the Order service as its own process (shared database)

8. Add `ftgo-order-service` `main` (`OrderServiceMain` with `@Import(OrderServiceConfiguration)`),
   `FtgoServicePlugin`, Dockerfile, `application.properties` (`spring.application.name=ftgo-order-service`),
   and register the `ApiTrackingConfiguration` + error handler.
9. Implement HTTP adapters for the three ports (RestTemplate/WebClient with timeouts, retries only on the
   idempotent `schedule` and GETs, correlation-id header propagation). Select adapter by property
   `ftgo.order.integration=in-process|http`.
10. Add the new `/internal/**` endpoints to the monolith (§3.2).
11. Deploy the Order service **dark**, pointed at the same `ftgo` schema, with a DB user that only has
    DML on `orders` and `order_line_items`. It receives no user traffic yet.

### Phase 3 — Shift traffic (still shared database)

12. Put a router in front of both processes (nginx container in `docker-compose.yml`, an Ingress rule in
    `deployment/kubernetes`). `/orders/**` routes to the monolith by default.
13. **Shadow reads:** mirror `GET /orders/**` to the Order service, diff responses offline.
14. **Canary:** route a percentage of all `/orders/**` traffic (reads and writes) to the Order service,
    ramp 1% -> 10% -> 50% -> 100%. Safe because both processes write the same tables with the same
    optimistic-lock `version` column and the same `auto_increment` ids.
15. At 100% and after a soak period, disable the monolith's `OrderController` (property flag) so there is a
    single writer.

### Phase 4 — Split the database

16. Create schema `ftgo_order` (same MySQL instance initially) with the Order Flyway baseline
    (`orders`, `order_line_items`, `api_request_log`).
17. Copy data. Recommended for this data volume: a short write freeze on `/orders/**` writes (router
    returns 503 for POSTs), `INSERT ... SELECT` into `ftgo_order`, verify row counts + checksum per table,
    set `AUTO_INCREMENT` on `ftgo_order.orders` to `MAX(id)+1000`, switch the Order service datasource,
    lift the freeze. Zero-downtime alternative if needed: CDC (Debezium/binlog) from `ftgo.orders*` into
    `ftgo_order` then flip.
18. Revoke the Order service's grants on `ftgo`; revoke the monolith's grants on `orders`/`order_line_items`.

### Phase 5 — Decommission

19. Remove `:ftgo-order-service` from `ftgo-application/build.gradle` and `OrderServiceConfiguration` from
    `FtgoApplicationMain`. Keep `:ftgo-order-service-api` only where DTOs are needed (e2e tests).
20. After a retention window, drop `orders`/`order_line_items` from the `ftgo` schema.
21. Later: move the service DB to its own MySQL instance; add the outbox/events (§3.3).

## 5. Running both sides during the transition

**Topology during phases 2–4**

```
             +---------+       /orders/**  (flag: monolith | shadow | canary:N | service)
  UI/e2e --> | router  | ------------------------------------------+
             +---------+                                           |
                  | everything else                                v
                  v                                         +--------------+
           +--------------+   /internal/consumers|restaurants|deliveries   |
           | ftgo-app     | <-------------------------------| order-service|
           | (monolith)   |                                 +--------------+
           +--------------+                                        |
                  |                                                |
                  v                    phase 2-3: same schema       v
           +-------------------------------------------------------------+
           | MySQL  ftgo (consumers, restaurants, courier*, orders*)     |
           |        ftgo_order (orders, order_line_items)  <- phase 4    |
           +-------------------------------------------------------------+
```

**Rules**

- **Traffic cut-over happens before the data split.** While both processes share one schema, rollback is a
  router flag flip with no data reconciliation. Only enter Phase 4 after the Order service has served 100%
  of traffic for a full soak period.
- **One code path per request, one writer per table.** No dual-writes. In the canary phase both processes
  may write `orders`, but always to the same rows/schema, protected by `@Version`. After step 15 only the
  Order service writes.
- **Monolith never reads `orders` directly after Phase 1.** Courier data only carries `orderId`. This is
  what makes the Phase 4 split mechanical.
- **Idempotency.** `POST /internal/deliveries` is keyed on `orderId`; a retried or double-routed `accept`
  cannot assign two couriers.
- **Configuration flags** (Spring properties / env vars):
  - router: `ORDERS_ROUTE=monolith|shadow|canary:<pct>|service`
  - Order service: `ftgo.order.integration=in-process|http`
  - monolith: `ftgo.order.controller.enabled=true|false`
- **Observability.** Both sides already tag Micrometer metrics with `spring.application.name`
  (`OrderConfiguration.meterRegistryCustomizer`). Compare `placed_orders`, `courier_assignments`,
  error rates and p95 latency by `service` tag during canary. Propagate the correlation id used by
  `ApiTrackingInterceptor` on outbound internal calls so a request can be followed across both
  `api_request_log` tables.
- **Testing in CI.** Run `ftgo-end-to-end-tests` twice: against the monolith (`ORDERS_ROUTE=monolith`) and
  against the router with `ORDERS_ROUTE=service`. Add consumer-driven contract tests (e.g. Spring Cloud
  Contract / Pact) for the three `/internal/**` endpoints so the monolith cannot break the Order service
  silently.
- **Rollback per phase.** Phase 1: revert commits. Phase 2: service is dark, nothing to roll back. Phase 3:
  flip `ORDERS_ROUTE=monolith` and re-enable monolith controller. Phase 4: before revoking grants on
  `ftgo`, switch datasource back and copy delta rows written to `ftgo_order` since cut-over (bounded by
  `id > cutover_max_id` and `version`); after grants are revoked, roll forward only.

## 6. Risks and open issues

- **Delivery scheduling semantics change from one ACID transaction to call + compensate (C6).** Mitigated by
  idempotent schedule + compensation; there is a small window where a courier plan holds an action for an
  order that failed to commit — the compensation call and a periodic reconciler (courier actions whose
  `orderId` is not `ACCEPTED+` in the Order service) close it.
- **Menu pricing becomes a network call on order creation.** Adds latency and a failure mode to
  `POST /orders`. Acceptable for now; a replicated restaurant-menu read model fed by Restaurant events is
  the follow-up if it becomes a problem.
- **Pre-existing behaviour to preserve, not fix, during extraction** (fix separately so the migration
  diff stays behaviour-neutral):
  - `Order.revise` applies the revision twice and compares against `orderMinimum` (default
    `Integer.MAX_VALUE`) with an inverted-looking condition.
  - `delivery_address_latitude/longitude` (V2) are not mapped on `DeliveryInformation`.
  - `TicketController` is an empty placeholder; the kitchen lifecycle endpoints (`accept`, `preparing`,
    `ready`) stay on `/orders/**` for compatibility. A future Kitchen/Ticket service would take them.

## 7. Assumptions

- Only the Order context is extracted now; Consumer, Restaurant and Courier stay in the monolith and are
  the "other side" of every integration.
- Existing public URLs and JSON shapes must not change (UI and e2e tests are the compatibility contract).
- MySQL stays the database; the Order service starts on the same instance in a separate schema.
- Synchronous REST is acceptable for inter-service calls initially; no message broker is currently in
  the stack (the Kafka/Zookeeper env vars in `docker-compose.yml` have no corresponding services).
- Data volume is small enough for a brief write freeze during the Phase 4 copy; CDC is the fallback.
- Delivery/courier assignment logic belongs to the courier side, not to Order.

## 8. Rough effort

| Phase | Effort (agent sessions) | External waits |
|-------|-------------------------|----------------|
| 1 — seams in monolith | 1–2 | code review |
| 2 — deployable + HTTP adapters + internal APIs | 1 | image registry / env provisioning |
| 3 — router, shadow, canary | 1 | soak time, ops sign-off |
| 4 — DB split | <1 | maintenance window / DBA grants |
| 5 — decommission | <1 | retention window |
