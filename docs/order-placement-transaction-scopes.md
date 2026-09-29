# Order placement: transaction scopes and what becomes distributed

This note inventories every local (single-database, JPA) transaction that runs on the
order-placement path of the FTGO monolith, lists the tables each one touches, and
classifies what happens to it once Order, Consumer, Restaurant and Courier are split
into separately deployed services that each own their own database.

## Assumptions

1. **Target decomposition follows the existing Gradle modules.** Each `ftgo-*-service`
   module becomes one service owning its tables:

   | Service    | Owns tables                                   | Module                  |
   |------------|-----------------------------------------------|-------------------------|
   | Order      | `orders`, `order_line_items`                  | `ftgo-order-service`    |
   | Consumer   | `consumers`                                   | `ftgo-consumer-service` |
   | Restaurant | `restaurants`, `restaurant_menu_items`        | `ftgo-restaurant-service` |
   | Courier    | `courier`, `courier_actions`                  | `ftgo-courier-service`  |

   `api_request_log` is treated as per-service infrastructure (each service keeps its own).
   A separate Kitchen service (as in the reference FTGO microservices app) is **not** assumed
   for the main analysis, because ticket state currently lives on `Order`; its impact is
   called out separately where relevant.
2. **"Order-placement path"** means the flow from `POST /orders` through restaurant
   acceptance (`POST /orders/{id}/accept`), since acceptance is what triggers courier
   scheduling. The later lifecycle transitions (`preparing`, `ready`, `pickedup`,
   `delivered`), `cancel` and `revise` are covered briefly because they share the same
   aggregates.
3. **Database-per-service with no XA/2PC.** "Becomes distributed" means the unit of work
   can no longer be a single ACID transaction and must be replaced by a saga, an
   event-driven flow, a local replica, or accepted eventual consistency.
4. Spring Boot is 2.0.3 (`gradle.properties`), so `spring.jpa.open-in-view` defaults to
   `true` (it is not overridden in `ftgo-application/src/main/resources/application.properties`).
   Default propagation (`REQUIRED`) and isolation (MySQL InnoDB default,
   `REPEATABLE READ`) apply everywhere — nothing in the codebase overrides them.

## How transactions are demarcated today

| Class | Demarcation | Effect |
|---|---|---|
| `OrderService` | `@Transactional` on the class **and** on most methods | Every public method is transactional, including `accept` and `scheduleDelivery`, which have no method-level annotation. |
| `ConsumerService` | `@Transactional` on the class | `validateOrderForConsumer` **joins** the caller's transaction (`REQUIRED`). |
| `RestaurantService` | `@Transactional` on the class | Local only; not called on the placement path (`OrderService` uses `RestaurantRepository` directly). |
| `CourierService` | Method-level only | Local only; not called on the placement path (`OrderService` uses `CourierRepository` directly). |
| `ApiTrackingInterceptor` | None (repository default) | `apiRequestLogRepository.save` runs in its own short transaction in `afterCompletion`, after the business transaction has committed. |

Because `OrderService` reaches straight into the Restaurant, Consumer and Courier
repositories and entities, the service modules only look separate — at runtime they share
one `EntityManager`, one persistence context and one database transaction.

Cross-module coupling at the schema/ORM level that makes this possible:

- `Order.restaurant` — `@ManyToOne(fetch = LAZY)` → FK `orders_restaurant_id`
- `Order.assignedCourier` — `@ManyToOne` (eager) → FK `orders_assigned_courier_id`
- `Courier.plan.actions[].order` — `Action` embeddable with `@ManyToOne Order` → FK `courier_actions_order_id`
- `Order.consumerId` — plain `Long` (already decoupled; no FK)

## Transaction scope inventory

### T1 — `OrderService.createOrder` (`POST /orders`)  **→ becomes distributed**

Single `REQUIRED` transaction started at `OrderService.createOrder`:

| Step | Code | Tables | R/W | Owner after split |
|---|---|---|---|---|
| 1 | `restaurantRepository.findById` | `restaurants`, `restaurant_menu_items` | R | Restaurant |
| 2 | `makeOrderLineItems` (menu lookup, price snapshot) | — (in-memory on loaded restaurant) | — | Restaurant data |
| 3 | `consumerService.validateOrderForConsumer` (joins T1) | `consumers` | R | Consumer |
| 4 | *(TODO: charge a credit card)* | — | — | future Accounting/Payment |
| 5 | `orderRepository.save` | `orders`, `order_line_items` | W | Order |

Current guarantees that are lost after the split:

- **Atomic validate-then-create.** A missing restaurant, invalid menu item, or
  `ConsumerVerificationFailedException` rolls back the whole unit — no order row is ever
  visible in a half-validated state. The order is created directly in `APPROVED`.
- **Referential integrity** of `orders.restaurant_id` is enforced by an FK into the
  Restaurant service's table.
- Menu reads and the consumer check see one consistent snapshot.

Classification: writes are local to Order, but the transaction **validates against data
owned by two other services**, so the invariant "an order exists only for a valid
consumer and a valid restaurant menu" spans three databases. Once the credit-card TODO is
implemented it gains a remote *write* (payment authorization), which makes it a true
multi-service write transaction.

Recommended replacement — **Create Order saga** (orchestrated by Order):

1. Order: validate line items against a **local read-only replica** of restaurant menus
   (kept in sync from `RestaurantCreated` / `MenuRevised` events), snapshot prices (already
   done), insert order in a new `APPROVAL_PENDING` state.
2. Consumer: `verifyConsumer(consumerId, orderTotal)` → on failure, Order moves to
   `REJECTED` (compensation).
3. *(future)* Accounting: `authorize(consumerId, orderTotal)` → on failure, reject order.
4. Order: `approve` → `APPROVED`.

Required model changes: add `APPROVAL_PENDING` and `REJECTED` to `OrderState`;
replace `Order.restaurant` (`@ManyToOne`) with `restaurantId` + denormalised
`restaurantName`; drop FK `orders_restaurant_id`; publish events via a transactional
outbox so that the order insert and its event are atomic.

### T2 — `ConsumerService.validateOrderForConsumer`  **→ becomes a remote step of T1**

Not an independent transaction today — it joins T1 via `REQUIRED`. After the split it is
a synchronous call or a saga participant command on the Consumer service. Note that
`Consumer.validateOrderByConsumer` is currently a no-op, so the only real check today is
existence (`ConsumerNotFoundException`).

### T3 — `OrderService.accept` → `scheduleDelivery` (`POST /orders/{id}/accept`)  **→ becomes distributed (multi-service writes)**

One transaction via the class-level `@Transactional`; `scheduleDelivery` is a
self-invocation so it runs in the same transaction regardless of its own annotations.

| Step | Code | Tables | R/W | Owner after split |
|---|---|---|---|---|
| 1 | `tryToFindOrder` | `orders`, `order_line_items`; eager `courier` via `assignedCourier` | R | Order |
| 2 | `order.acceptTicket(readyBy)` | `orders` (`order_state`, `accept_time`, `ready_by`, `version`) | W | Order (Kitchen if split) |
| 3 | `courierRepository.findAllAvailable()` | `courier`, `courier_actions` | R | Courier |
| 4 | `assignCourier` / `estimateDeliveryTime` (reads restaurant lat/lng via lazy `order.getRestaurant()`) | `restaurants` | R | Restaurant |
| 5 | `courier.addAction(pickup)`, `courier.addAction(dropoff)` | `courier_actions` (unordered `@ElementCollection`; once initialised, Hibernate rewrites all rows for that courier on change) | W | Courier |
| 6 | `order.schedule(courier)` | `orders.assigned_courier_id` | W | Order |

This is the **only transaction on the path that writes to two services' tables**
(`orders` and `courier_actions`) and the one most clearly requiring redesign. Current
guarantees that are lost:

- **Acceptance and courier assignment are all-or-nothing.** If no courier is available
  (`NoCourierAvailableException`), the restaurant's acceptance is rolled back too — the
  restaurant sees an error even though accepting the ticket is a restaurant-side fact.
- Order ↔ Courier linkage is bidirectional and FK-enforced (`orders_assigned_courier_id`,
  `courier_actions_order_id`) and written in the same commit, so the two sides can never
  disagree.
- Courier selection reads courier load and location from the same snapshot it writes to.

Recommended replacement — **event-driven choreography** (or a small saga) instead of one
transaction:

1. Order (or Kitchen): accept locally → `ACCEPTED`, publish `TicketAccepted{orderId,
   restaurantId, readyBy, pickupAddress}` via outbox.
2. Courier/Delivery: consume event, pick courier from its own data, write `courier_actions`
   keyed by `orderId` (no FK), publish `CourierAssigned{orderId, courierId, eta}` — or
   `CourierAssignmentFailed` for retry/escalation instead of rejecting the acceptance.
3. Order: consume `CourierAssigned`, store `assignedCourierId` + ETA as plain columns.

Consequences to plan for: the order is visibly `ACCEPTED` with no courier for a short
window; courier assignment must be idempotent per `orderId` (at-least-once delivery);
restaurant coordinates must be carried in the event or replicated into Courier. The
`Order.assignedCourier` and `Action.order` `@ManyToOne` mappings and both FKs must go.

Pre-existing concurrency issue (not caused by the split, but amplified by it): `Courier`
has no `@Version`, and assignment reads available couriers without locking, so two
concurrent `accept` calls can pick the same courier and the second bag rewrite of
`courier_actions` can overwrite the first. In the Courier service this should be guarded
with optimistic locking on `Courier` or a per-courier serialised consumer.

### T4 — `OrderService.cancel` / `reviseOrder`  **→ local writes, cross-service reads**

Both write only `orders` / `order_line_items`, so the transaction itself stays local.
However:

- `OrderController.cancel` / `revise` build the response with `makeGetOrderResponse`,
  which navigates `order.getRestaurant().getName()` and
  `order.getAssignedCourier().actionsForDelivery(order)`. This relies on
  open-session-in-view and becomes an **API composition** across Order, Restaurant and
  Courier after the split (or is served from the denormalised fields above).
- `cancel` is only allowed in `APPROVED`, i.e. before a courier is assigned, so no courier
  compensation is needed today. If cancellation is ever allowed after acceptance, it must
  call `Courier.cancelDelivery` in the Courier service — a **Cancel Order saga**.
- If Kitchen becomes its own service (or Accounting is added), `revise` and `cancel`
  each become sagas (ticket revise/cancel, payment adjust/refund), as in the reference
  FTGO microservices design.

### T5 — `notePreparing`, `noteReadyForPickup`, `notePickedUp`, `noteDelivered`  **→ stay local (under assumption 1)**

Each updates only `orders`. They remain single-service transactions as long as Order owns
ticket and delivery state. If Kitchen is split, `preparing`/`ready` move to Kitchen and
Order learns about them via events; `pickedup`/`delivered` naturally belong to
Courier/Delivery and should likewise be event-propagated. Observation: nothing on this
path removes the courier's `courier_actions` on delivery, so `Courier.getActiveDeliveryCount`
counts historical pickups. After the split, a `OrderDelivered` event consumed by Courier is
the natural place to prune the plan.

### T6 — Supporting transactions (not on the hot path)

| Transaction | Tables | After split |
|---|---|---|
| `ConsumerService.create` | `consumers`, `hibernate_sequence` | Local to Consumer. |
| `RestaurantService.create` | `restaurants`, `restaurant_menu_items` | Local, **but must publish `RestaurantCreated` atomically (outbox)** so Order's menu replica used in T1 stays correct. |
| `CourierService.createCourier` / `updateAvailability` / `updateLocation` | `courier` | Local to Courier. |
| `ApiTrackingInterceptor.afterCompletion` save | `api_request_log` | Already a separate transaction after commit; becomes per-service. Correlation IDs (`X-Correlation-ID`) should be propagated on inter-service calls/events so a saga can be traced end to end. |

### Open-session-in-view reads (non-transactional, but cross-service)

`GET /orders/{id}` and `GET /orders?consumerId=` run outside any `@Transactional` method
and lazily load `Restaurant` and `Courier` through OSIV. They are not distributed
*transactions*, but they are cross-service *queries* and need either API composition or a
CQRS order-history view once the services are split.

## Summary

| # | Scope | Services touched (R / W) | After split |
|---|---|---|---|
| T1 | `createOrder` | Restaurant (R), Consumer (R), Order (W) | **Distributed** → Create Order saga + restaurant menu replica |
| T2 | `validateOrderForConsumer` (joins T1) | Consumer (R) | Remote saga step of T1 |
| T3 | `accept` + `scheduleDelivery` | Order (R/W), Courier (R/W), Restaurant (R) | **Distributed, multi-service writes** → event-driven courier assignment |
| T4 | `cancel`, `reviseOrder` | Order (W); response reads Restaurant, Courier | Local tx; cross-service read. Saga if Kitchen/Accounting split or post-acceptance cancel allowed |
| T5 | lifecycle `note*` | Order (W) | Local (Kitchen/Delivery split would turn them into events) |
| T6 | create/update in other services, API log | single service each | Local; `RestaurantService.create` needs outbox |

## Suggested next steps

1. Characterisation tests that pin current rollback behaviour of T1 and T3 (e.g. consumer
   not found ⇒ no `orders` row; no courier available ⇒ order stays `APPROVED`) so the
   refactor can be checked against them.
2. Decouple the ORM first, still inside the monolith: replace `Order.restaurant`,
   `Order.assignedCourier` and `Action.order` with ID columns, and stop `OrderService`
   injecting `CourierRepository` / `RestaurantRepository`. This removes the shared
   persistence context without yet introducing a network.
3. Split T3 into "accept" and "assign courier" local transactions connected by an
   in-process domain event, then move the listener to the Courier service.
4. Introduce `APPROVAL_PENDING`/`REJECTED` and the Create Order saga before adding payment.
