# FTGO monolith: table ownership and cross-context write seams

Goal: for every table in the `ftgo` schema, record which module / bounded context
reads and writes it, and flag every table written by more than one bounded context.
Those tables are the hard seams that must be split (or given a single owner) before
a context can be extracted into its own service with its own database.

Baseline: `master` @ `4823d191`, schema = `ftgo-flyway` V1 + V2.

## Method

1. **Static analysis** of every JPA entity/embeddable/element collection in
   `ftgo-domain` and `ftgo-common`, every Spring Data repository, and every service /
   controller that injects a repository. There is no native SQL, `JdbcTemplate`,
   `EntityManager` usage or `@Modifying` query anywhere in `src/main`, so JPA mappings
   plus repository/aggregate call sites are the complete set of writers.
2. **Runtime confirmation.** Ran `ftgo-application` against MySQL 5.7 with the Flyway
   schema and `org.hibernate.SQL=DEBUG` (already on in `application.properties`), drove
   every write endpoint once with [`trace-sql-writes.sh`](trace-sql-writes.sh), and
   grouped the resulting `insert` / `update` / `delete` statements by table. Raw output:
   [`trace-output.txt`](trace-output.txt).

## Assumptions

- **Bounded contexts** follow the FTGO domain model (Richardson, *Microservices
  Patterns*, ch. 2 and 13), not just Gradle modules:
  | Context | Code today |
  |---|---|
  | Consumer | `ftgo-consumer-service` |
  | Restaurant | `ftgo-restaurant-service` |
  | Order | `ftgo-order-service`: `POST /orders`, `/revise`, `/cancel`, `GET /orders` |
  | Kitchen (tickets) | `ftgo-order-service`: `/accept`, `/preparing`, `/ready` (the empty `TicketController` shows these were meant to live apart) |
  | Delivery / Courier | `ftgo-courier-service` + `OrderService.scheduleDelivery`, `/pickedup`, `/delivered` |
  | Platform (cross-cutting) | `ftgo-common` API tracking (`ApiTrackingInterceptor`) |
  Kitchen and Delivery have no module of their own; they are carved out of
  `ftgo-order-service` because that is where extraction would draw the line. Where
  the module view and the context view differ, both are shown.
- `ftgo-domain` is a shared-kernel module that holds **all** entities and
  repositories; "module X writes table T" means X's code causes the write, regardless
  of which module the entity class lives in.
- "Write" = any `INSERT`, `UPDATE` or `DELETE`, including those Hibernate issues on
  flush because an aggregate was dirtied (not only explicit `repository.save`).
- Test code, `ftgo-end-to-end-tests` and Flyway itself are out of scope as writers.

## 1. Module / context -> tables

W = writes, R = reads (via repository, JPA association, or in-process service call).

| Table | Consumer | Restaurant | Order | Kitchen | Delivery / Courier | Platform (api tracking) |
|---|---|---|---|---|---|---|
| `consumers` | **W** | | R (`ConsumerService.validateOrderForConsumer`) | | | |
| `hibernate_sequence` | **W** (id gen for `Consumer`) | | | | | |
| `restaurants` | **W** | | R (`RestaurantRepository`, `Order.restaurant` @ManyToOne) | | | |
| `restaurant_menu_items` | **W** | | R (`Restaurant.findMenuItem`) | | | |
| `orders` | | | **W** | **W** | **W** (`assigned_courier_id`, `picked_up_time`, `delivered_time`) | |
| `order_line_items` | | | **W** | | | |
| `courier` | | | R (`CourierRepository.findAllAvailable`, `Order.assignedCourier`) | | **W** (courier module) | |
| `courier_actions` | | | R (`GET /orders` ETA) | | **W** (from `OrderService.scheduleDelivery`), R (`/workload`) | |
| `api_request_log` | **W** | **W** | **W** | **W** | **W** | **W** (owner), R (`/api/tracking/**`) |

Same data by Gradle module (what code physically issues the write):

| Module | Writes | Reads (other contexts' tables) |
|---|---|---|
| `ftgo-consumer-service` | `consumers`, `hibernate_sequence` | - |
| `ftgo-restaurant-service` | `restaurants`, `restaurant_menu_items` | - |
| `ftgo-courier-service` | `courier` | `courier_actions` (workload) |
| `ftgo-order-service` | `orders`, `order_line_items`, **`courier_actions`** | `consumers`, `restaurants`, `restaurant_menu_items`, `courier` |
| `ftgo-common` (interceptor, on every request of every module) | `api_request_log` | - |

## 2. Evidence: DML per endpoint (runtime trace)

`api_request_log` insert omitted from each row (it is present on every request).

| Endpoint | Context | DML observed |
|---|---|---|
| `POST /consumers` | Consumer | `insert consumers`, `update hibernate_sequence` |
| `POST /restaurants` | Restaurant | `insert restaurants`, `insert restaurant_menu_items` |
| `POST /couriers` | Courier | `insert courier` |
| `POST /couriers/{id}/availability` | Courier | `update courier set available` |
| `POST /couriers/{id}/location` | Courier | `update courier set current_latitude, current_longitude, last_location_update` |
| `POST /orders` | Order | `insert orders`, `insert order_line_items` |
| `POST /orders/{id}/revise` | Order | `update orders set version`, `delete`+`insert order_line_items` |
| `POST /orders/{id}/cancel` | Order | `update orders set order_state, version` |
| `POST /orders/{id}/accept` | Kitchen + Delivery | `update orders set accept_time, ready_by, order_state, assigned_courier_id, version`; **`delete from courier_actions where courier_id=?`** + re-insert **all** of that courier's actions |
| `POST /orders/{id}/preparing` | Kitchen | `update orders set preparing_time, order_state, version` |
| `POST /orders/{id}/ready` | Kitchen | `update orders set ready_for_pickup_time, order_state, version` |
| `POST /orders/{id}/pickedup` | Delivery | `update orders set picked_up_time, order_state, version` |
| `POST /orders/{id}/delivered` | Delivery | `update orders set delivered_time, order_state, version` |
| `GET /orders/{id}`, `GET /couriers/{id}/workload` | - | none besides `api_request_log` |

The `delete ... where courier_id=?` only appears once the courier already has actions
(second `accept` for the same courier): `Plan.actions` is an unordered
`@ElementCollection List` (a Hibernate "bag"), so any change rewrites the whole set.

## 3. Flagged tables (written by more than one bounded context)

### 3.1 `orders` - Order + Kitchen + Delivery (hardest seam)

- One row, one aggregate (`Order`), one `@Version`, three contexts:
  - Order: `consumer_id`, `restaurant_id`, `order_minimum`, `payment_token`, delivery address, `order_state` (APPROVED/CANCELLED), `version`
  - Kitchen: `accept_time`, `ready_by`, `preparing_time`, `ready_for_pickup_time`, `order_state` (ACCEPTED/PREPARING/READY_FOR_PICKUP); `previous_ticket_state` column exists but is unmapped
  - Delivery: `assigned_courier_id`, `picked_up_time`, `delivered_time`, `order_state` (PICKED_UP/DELIVERED)
- `order_state` is a single column whose values are owned by three different contexts,
  and all three bump the same optimistic-lock `version`, so a kitchen update and a
  delivery update on the same order conflict today.
- Extraction implication: split into `orders` (Order), `tickets` (Kitchen, keyed by
  order id) and `deliveries` (Delivery, keyed by order id) with their own state
  machines; replace direct `Order` mutation with commands/events
  (`TicketAccepted`, `TicketReadyForPickup`, `DeliveryPickedUp`, ...). Nothing
  else can be extracted from `ftgo-order-service` cleanly until this is done.

### 3.2 `courier_actions` - owned by Courier, written by Order/Kitchen flow

- Physically only one module writes it, but it is written **across** a context
  boundary: `Plan`/`Action` are part of the `Courier` aggregate, yet the only writer is
  `OrderService.scheduleDelivery` (triggered by the kitchen's `/accept`). The courier
  module only reads it (`getActiveDeliveryCount` on `/workload`).
  `Courier.cancelDelivery` exists but is never called, so cancelled orders keep their
  courier actions.
- Each write deletes and re-inserts the courier's entire plan (observed). `Courier` has
  no `@Version`, so two concurrent `accept`s for the same courier can lose actions
  (hypothesis from the mapping; not reproduced).
- `courier_actions.order_id` has an FK to `orders` and `Action.order` is a
  `@ManyToOne Order`, so the Courier aggregate holds a hard reference into the Order
  context.
- Extraction implication: Delivery must own courier assignment. Order/Kitchen should
  emit `TicketAccepted(orderId, restaurantId, readyBy)`; Delivery picks the courier,
  writes the plan, and publishes `CourierAssigned(orderId, courierId, eta)`.
  Replace the FK/`@ManyToOne` with a plain `order_id` value.

### 3.3 `api_request_log` - every context (low business coupling)

- `ApiTrackingInterceptor` (in `ftgo-common`, registered on `/**`) inserts a row for
  every request handled by every module, inside the shared datasource.
- Extraction implication: easy but mandatory - each extracted service would otherwise
  need write access to the monolith DB. Move to structured logs / a log pipeline, or a
  per-service table, and retire the shared table.

### Single-writer tables (not flagged, but note read coupling)

| Table | Sole writer | Cross-context readers / coupling to remove at extraction |
|---|---|---|
| `consumers` | Consumer | Order calls `ConsumerService` in-process; `orders.consumer_id` is already a plain id (no FK) |
| `hibernate_sequence` | Consumer | Global generator: any future `@GeneratedValue` (AUTO) entity in any context would share it. Switch `Consumer` to `IDENTITY` or a per-service sequence |
| `restaurants`, `restaurant_menu_items` | Restaurant | Order reads them directly via `RestaurantRepository` and `@ManyToOne Restaurant` + FK `orders_restaurant_id`; needs a local menu replica or API call |
| `courier` | Courier | Order reads it via `CourierRepository` + `@ManyToOne Courier` + FK `orders_assigned_courier_id` (goes away with 3.2) |
| `order_line_items` | Order | - |

### Cross-context foreign keys to drop

`orders.restaurant_id -> restaurants`, `orders.assigned_courier_id -> courier`,
`courier_actions.order_id -> orders`.

## 4. Incidental defect found while tracing

**Sev 1: `POST /orders` returns 500 for every request on `master` against the Flyway schema.**
`Address` gained `latitude`/`longitude` in `420f9057`, but `DeliveryInformation`
only overrides street/city/state/zip, so Hibernate inserts into `orders.latitude` /
`orders.longitude`, which don't exist (V2 added `delivery_address_latitude` /
`delivery_address_longitude`). Observed:
`SQLSyntaxErrorException: Unknown column 'latitude' in 'field list'`. Core flow broken
for all users; it would be Sev 0 if `420f9057` has shipped to production (not verified).
Fix: add `@AttributeOverride`s for `latitude` -> `delivery_address_latitude` and
`longitude` -> `delivery_address_longitude`. Not fixed in this branch (analysis-only);
the trace was run with those two columns temporarily added to the local DB.

## 5. Suggested extraction order

1. Consumer and Restaurant (single-writer; only read coupling from Order).
2. Retire `api_request_log` shared writes (3.3) - prerequisite for any extracted service.
3. Move courier assignment into Delivery and cut `courier_actions`/`courier` coupling (3.2), then extract Courier/Delivery.
4. Split `orders` into order / ticket / delivery state (3.1), then extract Kitchen.

## Reproducing

```bash
# MySQL 5.7 with schema from ftgo-flyway (V1, V2); build needs JDK 8 (Gradle 4.10), then:
./gradlew :ftgo-application:assemble
java -jar ftgo-application/build/libs/ftgo-application.jar > app.log 2>&1 &
APP_LOG_CMD="cat app.log" docs/data-ownership/trace-sql-writes.sh http://localhost:8080
```
