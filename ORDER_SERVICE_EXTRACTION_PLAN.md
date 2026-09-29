# Order Service Extraction Plan — FTGO Monolith

Plan for pulling the Order Service out of `ftgo-application` into its own deployable, with its own data, while the monolith keeps running. It is based on the code as of `master` (`4823d191`).

Sections: [1. Current state](#1-current-state) · [2. Target boundary & data ownership](#2-target-boundary--data-ownership) · [3. API surface](#3-api-surface) · [4. Migration order](#4-migration-order) · [5. Running both sides during the transition](#5-running-both-sides-during-the-transition) · [6. Risks & open questions](#6-risks--open-questions) · [Appendix: assumptions](#appendix-assumptions)

---

## 1. Current state

### 1.1 Build and runtime topology

- One Spring Boot process (`FtgoApplicationMain`) imports `OrderServiceConfiguration`, `ConsumerServiceConfiguration`, `RestaurantServiceConfiguration`, `CourierWebConfiguration`, and `ApiTrackingConfiguration`. It exposes port 8080 (8081 in `docker-compose.yml`).
- One MySQL schema, `ftgo`, managed by Flyway (`ftgo-flyway`, V1 + V2). `mysql/schema.sql` only creates the database and grants.
- **Every** JPA entity (`Order`, `Restaurant`, `Courier`, `Consumer`) lives in the shared `ftgo-domain` module, together with every repository and the `CourierAssignmentStrategy`.
- `ftgo-order-service` already exists as a Gradle module with its own `main/OrderServiceConfiguration`. It depends on `ftgo-domain` **and on the `ftgo-consumer-service` implementation module**, not just the API module.

### 1.2 Coupling that blocks extraction

| # | Coupling | Where | Kind |
|---|----------|-------|------|
| C1 | `Order.restaurant` is a `@ManyToOne Restaurant` (FK `orders_restaurant_id`) | `Order.java` | JPA object reference + DB FK |
| C2 | `Order.assignedCourier` is a `@ManyToOne Courier` (FK `orders_assigned_courier_id`) | `Order.java` | JPA object reference + DB FK |
| C3 | `Courier.plan.actions` → `courier_actions.order_id` is a `@ManyToOne Order` (FK `courier_actions_order_id`) | `Action.java`, `Plan.java` | Reverse reference from Courier into Order |
| C4 | `OrderService.createOrder` reads `RestaurantRepository` for the menu (name and price per menu item) | `OrderService.java` | Cross-aggregate read in the same transaction |
| C5 | `OrderService.createOrder` calls `ConsumerService.validateOrderForConsumer` in-process | `OrderService.java` | Synchronous in-process call |
| C6 | `OrderService.accept → scheduleDelivery` loads all available couriers, runs the assignment strategy, **mutates `Courier.plan`**, and sets `order.assignedCourier`, all in one DB transaction | `OrderService.java` | Cross-aggregate write in one transaction |
| C7 | `estimateDeliveryTime` reads restaurant lat/lng and courier location | `OrderService.java` | Cross-aggregate read |
| C8 | `GetOrderResponse` includes `restaurantName` (lazy load via C1), plus `courierActions` and `estimatedDeliveryTime` (via C2/C3) | `OrderController.makeGetOrderResponse` | Read-side join across three aggregates |
| C9 | `GlobalExceptionHandler` (in `ftgo-application`) maps `OrderNotFoundException`, `UnsupportedStateTransitionException`, and others to HTTP codes | `ftgo-application` | Error contract lives outside the service |
| C10 | `ApiTrackingInterceptor` writes every request to the shared `api_request_log` table | `ftgo-common` | Shared cross-cutting table |

`Order.consumerId` is already a plain `Long` with no FK, so the Consumer side is effectively decoupled at the data level.

---

## 2. Target boundary & data ownership

### 2.1 Responsibilities of the extracted Order Service

The Order Service owns the **order aggregate and its lifecycle state machine**: `APPROVED → ACCEPTED → PREPARING → READY_FOR_PICKUP → PICKED_UP → DELIVERED`, plus `CANCELLED`. This includes the kitchen- and delivery-driven transitions (`accept`, `preparing`, `ready`, `pickedup`, `delivered`), because today they are just state changes on `Order`. Splitting those out into Kitchen and Delivery services is a later extraction (see §6) and not part of this plan.

The Order Service does **not** own:

- Couriers, courier plans, or courier assignment. These stay in the monolith, which becomes the "Delivery" side (C6).
- Restaurants and menus. The Order Service keeps a read-only replica (C4, C7).
- Consumers. The Order Service calls the Consumer API to validate (C5).

### 2.2 Tables

| Table | Today | After extraction | Notes |
|-------|-------|------------------|-------|
| `orders` | shared `ftgo` schema | **moves → `ftgo_order`**, owned by Order Service | Drop FKs `orders_restaurant_id` and `orders_assigned_courier_id`. `restaurant_id` and `assigned_courier_id` stay as plain IDs. Add `restaurant_name` (snapshot at order time) and `estimated_delivery_time`. Keep `version` for optimistic locking; this matters during the dual-run window. Keep the `AUTO_INCREMENT` sequence. |
| `order_line_items` | shared | **moves → `ftgo_order`** | Already carries a name and price snapshot per line. No change beyond the move. |
| `order_restaurant_replica` *(new)* | — | **new in `ftgo_order`** | `id, name, street1..zip, latitude, longitude, version`. Filled from Restaurant events plus a one-time backfill. |
| `order_restaurant_menu_item_replica` *(new)* | — | **new in `ftgo_order`** | `restaurant_id, menu_item_id, name, price`. Replaces the menu lookup that used `RestaurantRepository`. |
| `order_outbox` *(new)* | — | **new in `ftgo_order`** | Transactional outbox for Order events (see §3.3). |
| `order_processed_messages` *(new)* | — | **new in `ftgo_order`** | `(consumer_id, message_id)` table for idempotent event handling. |
| `courier_actions` | shared | **stays in monolith** | Drop FK `courier_actions_order_id`. `order_id` becomes an opaque ID. |
| `courier`, `restaurants`, `restaurant_menu_items`, `consumers`, `hibernate_sequence` | shared | stay in monolith | `hibernate_sequence` is used only by `Consumer` (`@GeneratedValue` AUTO). `Order` uses `IDENTITY`. |
| `api_request_log` | shared | each process writes its own copy (`ftgo_order.api_request_log` for the Order Service) | The table definition moves with `ftgo-common`. It is not part of the order domain. |
| monolith `order_outbox` *(new, temporary)* | — | **new in `ftgo`**, removed in Phase 5 | Used in Phases 1–3, while the monolith is still the writer. |

Unused or suspect columns to handle during the move: `previous_ticket_state` (never mapped) and `payment_token` (`PaymentInformation` has no `@Embeddable`). Carry both over as-is, and drop them only in a separate, explicit migration.

### 2.3 Code moves

- Move `Order`, `OrderLineItem(s)`, `OrderState`, `OrderRevision`, `LineItemQuantityChange`, `OrderMinimumNotMetException`, `DeliveryInformation`, `PaymentInformation`, and `OrderRepository` out of `ftgo-domain` into `ftgo-order-service` under a new package, `...orderservice.domain`.
- Change `Action.order` (`@ManyToOne Order`) to `Long orderId`. `Plan.actionsForDelivery(Order)` becomes `actionsForDelivery(long orderId)`.
- Change the signature of `CourierAssignmentStrategy.assignCourier(List<Courier>, Order)` to take a small value object, `DeliveryRequest(orderId, pickupLat, pickupLng, readyBy)`, and keep the strategy in the monolith.
- Make `ftgo-order-service/build.gradle` depend on `ftgo-consumer-service-api` and `ftgo-restaurant-service-api` only, and remove `ftgo-consumer-service`.

---

## 3. API surface

### 3.1 Public REST API (served by the Order Service)

This is the **same contract as today**, so clients (the UI, `AbstractEndToEndTests`) do not change and the gateway can route by path.

| Method | Path | Request | Response | Errors |
|--------|------|---------|----------|--------|
| POST | `/orders` | `CreateOrderRequest {consumerId, restaurantId, lineItems[{menuItemId, quantity}]}` | `CreateOrderResponse {orderId}` | 404 restaurant/consumer, 400 invalid menu item |
| GET | `/orders/{orderId}` | — | `GetOrderResponse` | 404 |
| GET | `/orders?consumerId=` | — | `GetOrderResponse[]` | — |
| POST | `/orders/{orderId}/cancel` | `{}` | `GetOrderResponse` | 404, 409 bad transition |
| POST | `/orders/{orderId}/revise` | `ReviseOrderRequest {revisedLineItemQuantities}` | `GetOrderResponse` | 404, 409 |
| POST | `/orders/{orderId}/accept` | `OrderAcceptance {readyBy}` | 200 | 404, 409, 503 no courier available |
| POST | `/orders/{orderId}/preparing` | — | 200 | 404, 409 |
| POST | `/orders/{orderId}/ready` | — | 200 | 404, 409 |
| POST | `/orders/{orderId}/pickedup` | — | 200 | 404, 409 |
| POST | `/orders/{orderId}/delivered` | — | 200 | 404, 409 |

`GetOrderResponse` keeps `orderId, state, orderTotal, restaurantName, assignedCourier, courierActions[{type,time}], estimatedDeliveryTime`. In the extracted service:

- `restaurantName` comes from the `orders.restaurant_name` snapshot.
- `assignedCourier` and `estimatedDeliveryTime` come from columns written when the delivery is scheduled.
- `courierActions` comes from the Delivery query API (§3.2), called on read. It is best-effort: if Delivery is unavailable, the field is `null` rather than returning a 5xx. A follow-up can store the actions locally and drop that read call.

Error bodies use the existing `ErrorResponse` shape. The order-specific handlers move from `GlobalExceptionHandler` into an `@ControllerAdvice` inside `ftgo-order-service`.

Operational endpoints: `/actuator/health`, `/actuator/prometheus` (keep the metric names `placed_orders`, `approved_orders`, and `courier_assignments`, tagged `service=ftgo-order-service`), and Swagger via `common-swagger`.

### 3.2 APIs the Order Service consumes

| Dependency | Call | Exists today? | Change needed |
|------------|------|---------------|---------------|
| Consumer | `GET /consumers/{id}` (existence), later `POST /consumers/{id}/order-validations {orderTotal}` | GET yes | Add the validation endpoint so `Consumer.validateOrderByConsumer` logic stays with Consumer. |
| Restaurant | Events `RestaurantCreated` / `RestaurantMenuRevised`, plus `GET /restaurants/{id}` returning address and menu for backfill and cache misses | GET returns only `{id, name}` | Extend `GetRestaurantResponse` with address and menu. Publish events from `RestaurantService.create`. |
| Delivery (monolith) | `POST /internal/deliveries {orderId, restaurantId, pickupLat, pickupLng, readyBy}` → `{courierId, estimatedDeliveryTime}`. `GET /internal/deliveries/{orderId}/actions` → `[{type,time}]` | No | New internal endpoints in `ftgo-courier-service`. They wrap today's `scheduleDelivery` / `estimateDeliveryTime` logic. `NoCourierAvailableException` maps to 503. |

`accept` stays **synchronous** in step 1. The Order Service calls `POST /internal/deliveries`. On success, it transitions to `ACCEPTED` and stores `assigned_courier_id` and `estimated_delivery_time` in one local transaction. This keeps today's behavior, where `accept` fails with an error if no courier is free. If the Delivery call succeeds but the local commit fails, the courier holds an orphan plan entry. A reconciliation job cleans these up by removing plan entries whose order is not `ACCEPTED+`. Moving to an async saga (`OrderAccepted` → `DeliveryScheduled`) is listed as a follow-up.

### 3.3 Events published by the Order Service

Events are published from the `order_outbox` table to a broker, one topic per aggregate: `net.chrisrichardson.ftgo.orderservice.Order`. The payloads extend the existing `ftgo-order-service-api` `events` package, where `OrderDetails` and `OrderLineItemDTO` already exist.

| Event | Payload | Consumers |
|-------|---------|-----------|
| `OrderCreated` | `orderId, OrderDetails` | Monolith read replica (Phases 3–4); future Kitchen/Accounting |
| `OrderRevised` | `orderId, revisedLineItemQuantities, newOrderTotal` | same |
| `OrderCancelled` | `orderId` | same |
| `OrderStateChanged` | `orderId, fromState, toState, timestamp, assignedCourierId?, estimatedDeliveryTime?` | Monolith (UI dashboards, courier workload); future Delivery |

Event rules: every event carries `eventId` (a UUID) and `orderVersion` (= `orders.version`), so consumers can deduplicate and ignore stale events.

---

## 4. Migration order

Each phase can be deployed on its own, and each has a rollback that does not lose data. The phases follow the strangler / "decouple inside the monolith first" approach: every step except Phase 3 is invisible to clients.

### Phase 0 — Safety net *(monolith only)*
1. Make `FtgoApplicationTest` / `AbstractEndToEndTests` run green against the real MySQL database. These become the contract test suite that is run against **both** the monolith and the new service.
2. Add `OrderControllerTest` coverage for all mutating endpoints, following `UNIT_TEST_PLAN.md` Phase 1–3.
3. Fix or pin current behavior that would otherwise look like a regression later:
   - `Order.revise` applies the revision twice, and its minimum check is inverted. It effectively never triggers because `orderMinimum = Integer.MAX_VALUE`.
   - `DeliveryInformation` embeds `Address`, which now has `latitude` and `longitude` but no column overrides. It likely maps to non-existent `orders.latitude/longitude` columns instead of `delivery_address_latitude/longitude`. Verify this.

   Decide explicitly whether to keep each of these as-is. The default is to keep the behavior and document it.

**Rollback:** n/a.

### Phase 1 — Decouple inside the monolith *(no topology change)*
1. **Remove the JPA object references.** Change `Order.restaurant` to `Long restaurantId` plus `String restaurantName`. Change `Order.assignedCourier` to `Long assignedCourierId`. Change `Action.order` to `Long orderId`. The column names stay the same, so this does not change the schema.
2. **Introduce ports in `OrderService`**, with in-process adapters for now:
   - `RestaurantMenuPort.findMenu(restaurantId)`
   - `ConsumerPort.validateOrder(consumerId, total)`
   - `DeliveryPort.scheduleDelivery(DeliveryRequest)` → `{courierId, eta}`
   - `DeliveryPort.actionsFor(orderId)`

   Move the `scheduleDelivery` / `estimateDeliveryTime` body into `CourierService`, behind `DeliveryPort`.
3. Flyway **V3**:
   - Drop the FKs `orders_restaurant_id`, `orders_assigned_courier_id`, and `courier_actions_order_id`.
   - Add `orders.restaurant_name` and `orders.estimated_delivery_time`, backfilled from `restaurants.name` and from the `courier_actions` DROPOFF time.
4. Move the order classes out of `ftgo-domain` into `ftgo-order-service` (§2.3). Fix the `build.gradle` dependency on `ftgo-consumer-service`.

**Exit criteria:** the E2E suite is green, and `ftgo-order-service` compiles without depending on any other `*-service` implementation module.

**Rollback:** a normal code revert. V3 only drops FKs and adds nullable columns.

### Phase 2 — Introduce messaging and the restaurant replica *(monolith only)*
1. Add a broker. Assumption: Kafka (see Appendix), added to `docker-compose.yml`, plus an outbox relay (polling publisher, or Debezium CDC on the binlog).
2. `RestaurantService` publishes `RestaurantCreated` / `RestaurantMenuRevised` through a monolith outbox.
3. Create the `order_restaurant_replica` / `order_restaurant_menu_item_replica` tables. Initially they live in the `ftgo` schema, prefixed `order_`, so they can move together with `orders` later. Backfill them from `restaurants` / `restaurant_menu_items`. Switch the `RestaurantMenuPort` adapter to read from the replica.
4. `OrderService` writes `OrderCreated` / `OrderRevised` / `OrderCancelled` / `OrderStateChanged` to the outbox in the same transaction as the order change. Nothing consumes these yet; this step exercises the pipeline.

**Rollback:** switch the `RestaurantMenuPort` adapter back to `RestaurantRepository` with a config flag.

### Phase 3 — Stand up the Order Service; shared tables, single writer
1. Deploy `ftgo-order-service` as its own Spring Boot app, using the existing `OrderServiceConfiguration`, a new `Dockerfile`, and a k8s manifest in `ftgo-order-service/src/deployment/kubernetes/`. It connects to the **same** `ftgo` schema with a dedicated MySQL user, `ftgo_order_svc`, that is granted only on `orders`, `order_line_items`, `order_restaurant_*`, `order_outbox`, and `order_processed_messages`. The grants enforce the ownership boundary before the tables physically move.
2. HTTP adapters in the Order Service: `ConsumerPort` → Consumer REST, `DeliveryPort` → `/internal/deliveries` on the monolith. The restaurant replica is consumed from events.
3. Put an edge router in front of both processes (§5.1). Cut over **reads first, then writes**. Each step is a per-route flag:
   1. Mirror `GET /orders/**` to the Order Service (shadow). Compare the responses and alert on any difference.
   2. Serve `GET /orders/**` from the Order Service.
   3. Serve the lifecycle transitions (`/preparing`, `/ready`, `/pickedup`, `/delivered`) from the Order Service.
   4. Serve `/cancel` and `/revise`.
   5. Serve `POST /orders` and `/accept`. These are last, because they carry the most cross-service calls.
4. After the final flip, disable the monolith's `OrderController` with `ftgo.orders.enabled=false`, but keep the code.

**Rollback:** flip the route back. Both processes read and write the same rows, and `orders.version` optimistic locking protects against concurrent writes during the flip. No data sync is needed.

### Phase 4 — Physically split the data
1. Create the `ftgo_order` schema. For a first cut it can live on the same MySQL instance; moving it to a separate instance or DB later changes only the connection URL.
2. Move the tables. The Order Service is the only writer and the monolith has no grants on these tables, so the move can be an online copy:
   1. Create the tables in `ftgo_order`.
   2. Copy the data with `pt-online-schema-change` or `gh-ost`, or use a short maintenance window: freeze writes on the `/orders` routes (return 503 with `Retry-After`), `INSERT … SELECT`, set `AUTO_INCREMENT` to `MAX(id)+1`, repoint the datasource, and unfreeze. Order volume in this app makes a window of a few minutes realistic; confirm this with prod numbers.
3. Drop the grants for `ftgo_order_svc` on `ftgo`.
4. The monolith consumes `OrderCreated` / `OrderStateChanged` for anything it still needs about orders (for example UI dashboards, courier workload, `courier_actions` cleanup on cancellation). It no longer reads the `orders` table.

**Rollback:** until the old tables are dropped, keep them in `ftgo` and write-only-by-nobody (renamed `orders_frozen_YYYYMMDD`). To roll back, copy back the rows with `id > frozen_max_id`, then repoint.

### Phase 5 — Decommission
1. Delete `OrderController`, `OrderService`, and the order entities from the monolith. Remove `OrderServiceConfiguration` from `FtgoApplicationMain` and `FtgoApplicationTest`.
2. Drop the frozen `orders` / `order_line_items` copies and the monolith `order_outbox`, in Flyway V-next. Flyway migrations for `ftgo_order` move into `ftgo-order-service/src/main/resources/db/migration`.
3. Point `AbstractEndToEndTests.orderBaseUrl` at the gateway. The E2E suite is now the cross-service contract test.

---

## 5. Running both sides during the transition

### 5.1 Routing (strangler facade)
- Add an edge router, either Spring Cloud Gateway as a new module or nginx, as the single public entry point. It takes over port 8081 in `docker-compose.yml` and the Service in k8s.
- Default route: everything goes to the monolith. Order routes are controlled by per-route flags, in the order given in §4 Phase 3.3. Flags live in config (or a flag service), so a rollback is a config change, not a deploy.
- Shadow mode: the gateway mirrors requests to the Order Service and discards the responses. Only `GET` requests are mirrored, never mutating calls. A small diff job logs mismatches with the correlation ID.
- Correlation: the gateway sets `X-Correlation-ID`. Both processes already put it in MDC and `api_request_log` through `ApiTrackingInterceptor`, so a request can be followed across both sides.

### 5.2 Data consistency across the dual-run window

| Phase | Writer of `orders` | Readers | Sync mechanism |
|-------|--------------------|---------|----------------|
| 0–2 | monolith | monolith | n/a |
| 3 (shadow / partial flip) | monolith **or** Order Service, per route | both | **Shared tables**: no replication. Optimistic locking (`version`) handles overlap. |
| 4+ | Order Service only | Order Service; monolith through events/API | Outbox → broker → monolith consumers (idempotent via `order_processed_messages` / `orderVersion`) |

Key rule: **exactly one logical writer per row at any time.** Phase 3 relies on the router for this, and Phase 4 on DB grants. This plan never uses dual-write from application code.

### 5.3 Cross-service calls during overlap
- The Order Service calls the monolith (`/consumers`, `/internal/deliveries`) through the **internal** service URL, not the gateway, so it cannot loop back to itself.
- The monolith's in-process `DeliveryPort` adapter and the HTTP `/internal/deliveries` endpoint call the same `CourierService` method, so courier assignment behaves the same whichever side handled `accept`.
- Timeouts, retries, and circuit breaking use Resilience4j on the Order Service side. `POST /internal/deliveries` must be idempotent by `orderId`: a repeated call returns the existing assignment.

### 5.4 Local and CI setup
- `docker-compose.yml`: add `kafka` / `zookeeper` (Phase 2), `ftgo-order-service` (Phase 3), and `gateway` (Phase 3). Environment variables already hint at Eventuate/Kafka (`EVENTUATELOCAL_KAFKA_BOOTSTRAP_SERVERS`) but no such services are defined yet.
- CI: run the E2E suite twice per build, once with all routes pointed at the monolith and once with all order routes pointed at the Order Service, until Phase 5.
- Observability: dashboards per route that compare the monolith and the Order Service on p50/p99 latency, 4xx/5xx rate, and the `placed_orders` / `courier_assignments` counters. Cut over only when the Order Service is at or better than the monolith for 24–48h on each step.

---

## 6. Risks & open questions

| Risk | Mitigation |
|------|------------|
| `accept` becomes a distributed operation: the courier is assigned in the monolith and the order state in the Order Service | Idempotent `/internal/deliveries` keyed by `orderId`, plus a reconciliation job for orphan plan entries. Later, move to an async saga. |
| Restaurant replica lags behind, so an order is placed against a stale menu or price | Menu changes are rare. `reviseMenu` is not even implemented. Fall back to `GET /restaurants/{id}` when the replica has no row. Line items already snapshot the price. |
| `GetOrderResponse.courierActions` now needs a network call | Best-effort, with a short timeout. Later, store `DeliveryScheduled` data locally. |
| Latent mapping bugs (§4 Phase 0.3) look like extraction regressions | Pin them in tests before moving any code. |
| Kitchen and Delivery transitions live on the Order Service in this plan | Accepted for now. The next extractions (Kitchen: accept/preparing/ready; Delivery: pickedup/delivered) will turn these endpoints into event consumers. |

Open questions for the team:
1. Is a broker (Kafka) acceptable to add, or should Phase 2 use a DB-polling feed (`GET /orders/events?after=`) instead?
2. What are the prod order volume and acceptable write-freeze duration for the Phase 4 copy?
3. Should `api_request_log` stay per-service, or move to a central log pipeline instead of a table?

---

## Appendix: assumptions

- **Scope:** "Order Service" = the current `ftgo-order-service` module (`OrderController`, `OrderService`) plus the `Order` aggregate from `ftgo-domain`. The empty `TicketController` is ignored.
- **Messaging:** Kafka with a transactional outbox is assumed as the target, based on the Eventuate/Kafka environment variables already in `docker-compose.yml`. The plan does not depend on Kafka specifically.
- **Database:** stays MySQL. Phase 4 initially uses a separate schema on the same instance. A separate instance is an operational follow-up.
- **Public API:** stays backward-compatible (same paths and payloads), so the UI and the E2E tests do not change. The bundled UI (`static/js/app.js`) currently runs against an in-browser mock API, so it is not a live client.
- **Environment:** there is no production traffic data in the repo, so durations and freeze windows are estimates.
