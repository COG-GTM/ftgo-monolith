# FTGO Monolith: Transaction Boundary Audit

This audit covers where the service layer depends on a shared local ACID transaction today, what stops being atomic once a service is extracted, and where a saga boundary should go in each case.

- Baseline: `master` @ `4823d191`
- Companion: `ORDER_SERVICE_EXTRACTION_PLAN.md` (branch `devin/1790695006-order-service-extraction-plan`) covers structural coupling in general. This document covers only **transactional** coupling.
- Runtime evidence: [`docs/transaction-boundary-audit/`](docs/transaction-boundary-audit/README.md). Each claim below is tagged **[code]** (read from source), **[runtime]** (observed in the local run, with the log file named), or **[inference]** (a design recommendation).

## Assumptions

1. **Target decomposition.** The target is the four bounded contexts already present as Gradle modules: Consumer, Restaurant, Order, and Courier/Delivery. Each gets its own database, with no distributed (XA/2PC) transactions. This follows the FTGO/Eventuate reference architecture this repo illustrates.
2. **Messaging.** Cross-service writes will go through a message broker with a transactional outbox (e.g. Eventuate Tram or Debezium). Delivery is assumed to be at-least-once, so every saga participant must be idempotent on `orderId`.
3. **Delivery ownership.** "Delivery" (courier assignment plus the courier plan) belongs to the Courier service. It does not belong to the Order service.
4. **Kitchen.** No Kitchen/Ticket service exists yet. `accept`, `notePreparing`, and `noteReadyForPickup` stay in Order for now, and saga shapes are written so a Kitchen service can later take those steps.
5. **Out of scope.** Payment is out of scope: it is a `TODO` at `OrderService.java:61`. It is included only as a placeholder step in the Create Order saga.
6. **Isolation level.** MySQL uses its InnoDB default, `REPEATABLE READ`. No `@Transactional(isolation=…)` or `@Lock` exists anywhere in `src/main`.

## 1. How transactions are demarcated today

| Fact | Evidence |
|---|---|
| All contexts run in one Spring Boot process, with one `DataSource` and one `JpaTransactionManager` | [code] `ftgo-application/.../FtgoApplicationMain.java:17-21`, `ftgo-application/src/main/resources/application.properties` (single `ftgo` schema) |
| `OrderService` is `@Transactional` at class level, so every public method, including the un-annotated `accept()`, runs in a transaction | [code] `ftgo-order-service/.../domain/OrderService.java:18`. [runtime] `s3-accept-no-courier.log`: `Creating new transaction with name [...OrderService.accept]` |
| `ConsumerService` and `RestaurantService` are class-level `@Transactional` with default `REQUIRED`, so they **join** the caller's transaction | [code] `ftgo-consumer-service/.../ConsumerService.java:12`, `ftgo-restaurant-service/.../RestaurantService.java:15`. [runtime] `s1-create-order.log`: `Participating in existing transaction` for `ConsumerService.validateOrderForConsumer` |
| `CourierService` is annotated per method. `findCourierById` has no transaction and relies on open-session-in-view for lazy loads | [code] `ftgo-courier-service/.../CourierService.java:18,26,41-43,45` |
| Open-session-in-view is on by default, so controllers read lazy associations **after** the service transaction has committed | [runtime] startup log: `spring.jpa.open-in-view is enabled by default`. `s7-get-order.log` |
| `scheduleDelivery()` is called by self-invocation from `accept()`, so any future `@Transactional(REQUIRES_NEW)` on it would be ignored by the proxy | [code] `OrderService.java:98,101` |
| Only `Order` has optimistic locking. `Courier` has no `@Version` | [code] `ftgo-domain/.../Order.java:24-25`. `Courier.java:11-33` has no version field |
| DB foreign keys run across context boundaries: action→order, action→courier, order→courier, order→restaurant | [code] `ftgo-flyway/.../V1__create_ftgo_db.sql:96-109` |
| API request tracking is written in its own transaction after the request, and failures are swallowed | [code] `ftgo-common/.../ApiTrackingInterceptor.java:60-77`. [runtime] `s3-accept-no-courier.log`: the business transaction rolls back, then `insert into api_request_log` commits separately |

### Transaction inventory (write paths)

| Entry point | Transaction owner | Tables written in the one transaction | Contexts touched | Breaks on extraction? |
|---|---|---|---|---|
| `POST /orders` → `OrderService.createOrder` (`OrderService.java:48-70`) | Order | `orders`, `order_line_items` | Order (write); Restaurant and Consumer (reads inside the transaction) | **Yes: B1** |
| `POST /orders/{id}/accept` → `OrderService.accept` + `scheduleDelivery` (`OrderService.java:95-116`) | Order | `orders`, `courier_actions` | Order + Courier (both write) | **Yes: B2, B3** |
| `POST /orders/{id}/pickedup`, `/delivered` (`OrderService.java:155-165`) | Order | `orders` only | Order (Courier plan *should* change but doesn't) | **Yes: B4** (gap already exists) |
| `POST /orders/{id}/cancel`, `/revise` (`OrderService.java:79-93`) | Order | `orders`, `order_line_items` | Order (write); response reads Courier | Write: no. Read: **B5** |
| `POST /orders/{id}/preparing`, `/ready` (`OrderService.java:143-153`) | Order | `orders` | Order | No (single aggregate) |
| `POST /consumers` (`ConsumerService.java:23-26`) | Consumer | `consumers` | Consumer | No |
| `POST /restaurants` (`RestaurantService.java:21-25`) | Restaurant | `restaurants`, `restaurant_menu_items` | Restaurant | No (a new event is needed for B1's replica) |
| `POST /couriers`, `/availability`, `/location` (`CourierService.java:18-31,45-50`) | Courier | `courier` | Courier | No (availability feeds B2/B3) |
| API tracking (`ApiTrackingInterceptor.java:73-77`) | none (separate transaction) | `api_request_log` | cross-cutting | No: **B7** |

## 2. Where extraction breaks atomicity

### B1. Create Order: order insert + restaurant menu read + consumer validation

**Today [code].** One transaction, `OrderService.java:48-70`:
- It reads the `Restaurant` aggregate and its menu (`OrderService.java:51-52`, `Restaurant.java:25-28,65-67`).
- It prices the line items from that menu (`OrderService.java:55,72-76`).
- It calls `ConsumerService.validateOrderForConsumer` in-process, and that call **joins the same transaction** (`OrderService.java:59`, `ConsumerService.java:18-21`).
- It then inserts `orders` and `order_line_items` (`OrderService.java:63`).
- The order row stores a `restaurant_id` FK (`Order.java:32-33`, `V1:108-109`).
- The order is created directly in `APPROVED` (`OrderState` has no pending state).

A validation exception rolls the whole thing back. [runtime] `s2-create-order-unknown-consumer.log` shows a rollback and an HTTP 500, and `scenarios-output.txt` shows zero rows for consumer 999999.

**What breaks.** Once Consumer and Restaurant own their own databases:
- The menu read, the validation, and the insert happen in three different transactions.
- An order can be persisted and "approved" after the consumer was rejected (or while it is unreachable).
- An order can be priced from a menu that was revised between the read and the insert.
- `orders.restaurant_id` can no longer be FK-enforced.

Today `Consumer.validateOrderByConsumer` is a no-op (`Consumer.java:30-32`), so the check is structurally present but has no rules yet. Any rule added later (credit limit, blocked consumer) would lose its all-or-nothing guarantee.

**Suggested saga boundary: Create Order saga (orchestrated by Order) [inference].**

| # | Participant | Local transaction | Compensation |
|---|---|---|---|
| 1 | Order | Insert `Order` in new state `APPROVAL_PENDING`. Price it from a **local read-replica** of the restaurant menu, kept current by `RestaurantCreated`/`RestaurantMenuRevised` events. Write `OrderCreated` to the outbox | `rejectOrder()` → `REJECTED` |
| 2 | Consumer | `ValidateOrderForConsumer(orderId, consumerId, total)` → reply `ConsumerVerified` / `ConsumerVerificationFailed` | none (read-only) |
| 3 | *(future)* Accounting | `AuthorizeCard` (replaces the TODO at `OrderService.java:61`) | **pivot**: past this point, the saga only moves forward |
| 4 | Order | `approveOrder()` → `APPROVED`. Increment `approved_orders` here, not in step 1 (`OrderService.java:65`) | none |

- The saga starts at the `POST /orders` handler and ends when the order leaves `APPROVAL_PENDING`.
- The API has to become asynchronous: return `orderId` plus the pending state.
- `cancel()`/`revise()` must reject `APPROVAL_PENDING`. Today they accept only `APPROVED` (`Order.java:82-90,92-100`), so this holds as long as the new state is kept distinct.
- The menu snapshot is taken by step 1 and never re-read, which removes the menu read-then-write race.

### B2. Accept Order + courier assignment: two aggregates, two future services, one commit

**Today [code].** `accept()` (`OrderService.java:95-99`) does all of the following in one transaction:
- `Order.acceptTicket` → `ACCEPTED` (`Order.java:136-146`).
- Reads all available couriers (`OrderService.java:102`, `CourierRepository.java:10-11`) and picks one (`OrderService.java:103`, `DistanceOptimizedCourierAssignmentStrategy.java:19-22`).
- **Mutates the `Courier` aggregate** by appending PICKUP and DROPOFF actions to its plan (`OrderService.java:105,108`, `Courier.java:52-54`, `Plan.java:10-15`).
- Sets `Order.assignedCourier` (`OrderService.java:110`, `Order.java:55-56,181-183`).

The action rows hold an FK back to the order (`Action.java:16-17`, `V1:25-31,96-100`), and the order holds an FK to the courier (`V1:105-106`).

[runtime] The transaction behaves all-or-nothing:
- `s3-accept-no-courier.log`: with no available courier, `NoCourierAvailableException` → `Initiating transaction rollback` → HTTP 503 (`GlobalExceptionHandler.java:61-69`). `scenarios-output.txt` shows the order still `APPROVED` with a null `accept_time`/`ready_by`, so the acceptance was undone.
- `s4-accept-with-courier.log`: `update orders …` and 2× `insert into courier_actions …` go out in a single commit.

**What breaks.** Once Courier owns `courier`/`courier_actions`:
- `Order` can be committed `ACCEPTED` while no courier plan was written (if Courier is down or has no capacity).
- A courier plan can reference an order whose acceptance later failed or was rolled back.
- The "503 and nothing happened" contract is lost, as are the FKs `orders.assigned_courier_id` and `courier_actions.order_id`.

The accept response also depends on ETA logic that reads the courier's live location and the restaurant's coordinates (`OrderService.java:107,118-136`), which will then be in two other services.

**Suggested saga boundary: Schedule Delivery saga, triggered by `OrderAccepted` [inference].** Keep order acceptance as the Order service's own local transaction and treat courier assignment as a follow-on saga rather than part of the accept command.

| # | Participant | Local transaction | Compensation / failure path |
|---|---|---|---|
| 1 | Order | `acceptTicket(readyBy)` → `ACCEPTED`, `deliveryState = PENDING`. Outbox `OrderAccepted{orderId, restaurantId, readyBy, pickup/dropoff address}` | `revertAcceptance()` → `APPROVED` (only if the business prefers "the order can't be accepted without a courier") |
| 2 | Courier | `ScheduleDelivery(orderId, …)`: select a courier and append PICKUP/DROPOFF to its plan (see B3). Reply `DeliveryScheduled{courierId, eta}` / `DeliveryScheduleFailed` | on failure: nothing to undo locally |
| 3 | Order | `noteDeliveryScheduled(courierId, eta)`: store `assignedCourierId` as an **opaque id** plus the ETA snapshot | none |

- The saga starts on `OrderAccepted` and ends on `DeliveryScheduled` or on a terminal failure.
- The recommended failure policy is to **retry or queue** in Courier: the order stays `ACCEPTED`/`deliveryState=PENDING`, and ops get alerted. Compensating back to `APPROVED` is the alternative, and it reproduces today's 503 semantics asynchronously. Which one to use is a product decision; record it.
- `ScheduleDelivery` must be idempotent on `orderId`. If the plan already contains actions for `orderId`, reply with the existing assignment.

### B3. Courier capacity invariant: a read-then-write without a lock (keep it in one local transaction)

**Today [code].**
- The assignment strategy skips couriers with `getActiveDeliveryCount() >= MAX_ACTIVE_DELIVERIES` (`DistanceOptimizedCourierAssignmentStrategy.java:16,43-45`, `Courier.java:106-113`) and then appends to the plan.
- `Courier` has no `@Version` (`Courier.java:11-33`).
- The plan is an `@ElementCollection` bag, so Hibernate rewrites it wholesale.

[runtime] `s5-accept-second-order.log` shows the second assignment running `delete from courier_actions where courier_id=?` and then re-inserting all 4 actions. Two concurrent accepts that read the same plan can therefore overwrite each other's actions, or both pass the capacity check. The shared transaction does **not** protect against this today (REPEATABLE READ, no row lock). In 5 concurrent runs in `concurrent-accept-output.txt` no lost update showed up, because the race window is small locally. The risk comes from the code structure and was not reproduced.

**What breaks.** Extraction does not make this worse on its own. It moves the check and the write into a different service, and each saga redelivery becomes another concurrent writer.

**Suggested boundary: none. Keep it inside Courier's local transaction [inference].**
- Step 2 of B2 must select the courier, check capacity, and append the actions in **one** Courier-local transaction.
- Protect that transaction with `@Version` on `Courier`, or `SELECT … FOR UPDATE` on the chosen courier row.
- Model the plan as its own table keyed by `(courier_id, order_id, type)` with a unique constraint rather than a bag. The unique key also makes step 2 idempotent.
- Do not split "pick courier" and "reserve courier" into separate saga steps.

### B4. Order lifecycle vs courier plan: already non-atomic today, and extraction makes it explicit

**Today [code].**
- `notePickedUp`/`noteDelivered` change only `Order` (`OrderService.java:155-165`, `Order.java:170-179,189-198`).
- The courier plan is never updated. `Courier.cancelDelivery`/`Plan.removeDelivery` exist (`Courier.java:56-58`, `Plan.java:17-19`) but have no callers in `src/main`.

[runtime] `s6-delivered.log` shows only `update orders set delivered_time=…, order_state=…`. After order 1 was `DELIVERED`, `GET /couriers/1/workload` still reported `activeDeliveries: 2` and `courier_actions` still held order 1's rows (`scenarios-output.txt`). This inflates the load used by B3's capacity check, so a courier eventually hits `MAX_ACTIVE_DELIVERIES = 5` and stops receiving orders.

**What breaks.** This state change isn't atomic today, so extraction doesn't break it. The problem is that the obvious port ("Order calls Courier to remove the plan entries") would add a distributed write with no compensation. Also, the pickup and delivery facts come from the courier, so the **direction** is backwards today: Order is the system of record for events that happen at the Courier.

**Suggested saga boundary: Delivery lifecycle as a choreography owned by Courier [inference].**

| # | Participant | Local transaction | Event |
|---|---|---|---|
| 1 | Courier | `notePickedUp(orderId)`: mark the PICKUP action done | `DeliveryPickedUp{orderId, courierId, time}` |
| 2 | Order | `notePickedUp()` → `PICKED_UP` (idempotent: ignore if already ≥ `PICKED_UP`) | none |
| 3 | Courier | `noteDelivered(orderId)`: complete the DROPOFF and remove the delivery from the active plan | `DeliveryCompleted{orderId, courierId, time}` |
| 4 | Order | `noteDelivered()` → `DELIVERED` | none |

- No compensations are needed because these are facts. Order's handler must tolerate out-of-order events (e.g. `DeliveryCompleted` arriving before `DeliveryPickedUp`).
- Until extraction, keep the existing `/orders/{id}/pickedup|delivered` endpoints as adapters that emit the same events.
- Fixing the plan leak inside the monolith now (call `courier.cancelDelivery(order)` in `noteDelivered`) would make the monolith's behaviour match the target semantics before extraction.

### B5. Read-after-commit joins through open-session-in-view: `GET /orders`, `cancel`, `revise` responses

**Today [code].**
- `OrderController.makeGetOrderResponse` traverses `order.getRestaurant().getName()` and `order.getAssignedCourier().actionsForDelivery(order)` (`OrderController.java:61-83`).
- It does this from `getOrder`/`getOrders`, which call `OrderRepository` directly with no service transaction (`OrderController.java:45-59`).
- It also runs **after** `cancel`/`revise` commit (`OrderController.java:88-89,98-99`).

[runtime] `s7-get-order.log` shows lazy selects on `restaurants` and `courier_actions` running outside any `OrderService` transaction, via OSIV. The response is a consistent snapshot only because everything shares one database.

**What breaks.** No write atomicity is lost. Read consistency across Order, Restaurant, and Courier is lost, and the endpoint stops working as soon as those tables move.

**Suggested boundary: no saga. Use a projection [inference].**
- Order keeps denormalised `restaurantName` (captured in B1 step 1) plus `assignedCourierId` and `eta` (captured in B2 step 3), so `GetOrderResponse` is served from Order's own tables.
- The detailed courier action list, if still needed, becomes an API-composition call to Courier.
- Before extraction, set `spring.jpa.open-in-view=false` to flush out every hidden cross-context lazy load.

### B6. Cross-context foreign keys: referential integrity enforced by the shared schema

**Today [code].** `V1__create_ftgo_db.sql:96-109` enforces:
- `courier_actions.order_id → orders`
- `courier_actions.courier_id → courier`
- `orders.assigned_courier_id → courier`
- `orders.restaurant_id → restaurants`

Any write in B1/B2 that references a missing row fails and rolls back at commit.

**What breaks.** Each FK that crosses a service boundary disappears. The guarantee that "you cannot reference a non-existent restaurant/courier/order" moves out of the database into the sagas.

**Suggested boundary [inference].** Keep in-service FKs:
- `order_line_items → orders`
- `restaurant_menu_items → restaurants`

Replace cross-service FKs with opaque IDs, validated in the saga steps that introduce them:
- B1 step 1 validates `restaurantId` against the local replica.
- B2 step 2 validates `orderId`.
- B2 step 3 validates `courierId`.

Drop each FK in the same migration that moves its table.

### B7. API request tracking: already decoupled (no action)

**Today.**
- [code] The tracking write happens in `afterCompletion` with a swallowed exception (`ApiTrackingInterceptor.java:60-77`).
- [runtime] `s3-accept-no-courier.log` shows the business transaction rolled back and `api_request_log` committed in a new transaction.

**Boundary.** No atomicity dependency. Each extracted service keeps its own interceptor and its own `api_request_log` table, or ships the logs to a central sink. Correlation IDs (`ApiTrackingInterceptor.java:31-37`) should be forwarded as a message header so saga steps can be traced.

## 3. Summary

| ID | Atomic unit today | Breaks when extracting | Boundary | Pivot / key compensation |
|---|---|---|---|---|
| B1 | `createOrder` (`OrderService.java:48-70`) | Consumer, Restaurant | **Create Order saga** (Order orchestrates): `APPROVAL_PENDING` → validate → *(pay)* → `APPROVED` | Pivot: card authorisation. Compensate: `rejectOrder` |
| B2 | `accept` + `scheduleDelivery` (`OrderService.java:95-116`) | Courier | **Schedule Delivery saga** triggered by `OrderAccepted` | Retry in Courier (or compensate `revertAcceptance`) |
| B3 | capacity check + plan append (`DistanceOptimized…:43-45`, `OrderService.java:105-108`) | Courier | **No split**: one Courier local transaction + `@Version`/row lock + unique key | n/a |
| B4 | `notePickedUp`/`noteDelivered` (`OrderService.java:155-165`) | Courier | **Delivery lifecycle choreography**, owned by Courier | none (facts). Idempotent handlers |
| B5 | OSIV response assembly (`OrderController.java:61-83`) | Restaurant, Courier | **Projection / API composition** | n/a |
| B6 | cross-context FKs (`V1:96-109`) | all | IDs validated in B1/B2 steps | n/a |
| B7 | tracking (`ApiTrackingInterceptor.java:60-77`) | none | none | n/a |

## 4. Incidental findings (not transactional, but found during verification)

1. **`POST /orders` fails on a migration-built schema.** `DeliveryInformation.java:15-23` overrides only street/city/state/zip, so Hibernate writes `Address.latitude/longitude` to `orders.latitude/longitude`. V2 creates `delivery_address_latitude/longitude` instead (`V2__add_courier_optimization_and_api_tracking.sql:17-18`). Result: `Unknown column 'latitude' in 'field list'` → 500. Fix: add the two `@AttributeOverride`s (or rename the columns). It was worked around locally for this audit only.
2. **`ConsumerNotFoundException` returns 500, not 4xx.** No handler exists in `GlobalExceptionHandler.java`, so it falls through to the catch-all at `:81-90` ([runtime] S2).
3. **The courier plan is never pruned** (B4). It inflates `activeDeliveries` and eventually excludes couriers from assignment.

## 5. Suggested next steps (in order)

1. **Pre-extraction hardening inside the monolith:**
   - Fix finding 1.
   - Add `@Version` to `Courier`.
   - Prune the plan on delivery.
   - Set `spring.jpa.open-in-view=false` and fix the resulting lazy-load failures.

   These are low-risk and make the monolith behave like the target.
2. **Introduce the Order state machine changes** needed by B1/B2: `APPROVAL_PENDING`, `REJECTED`, `deliveryState`. Store `restaurantName`, `assignedCourierId`, and `eta` as snapshots on `Order`.
3. **Add an in-process outbox table**, and publish `OrderCreated`/`OrderAccepted`/`RestaurantMenuRevised` from the existing local transactions. The sagas can then run in-process first, with synchronous handlers, before any network hop.
4. **Extract Courier/Delivery first.** B2-B4 are the largest atomicity dependency, and Courier has the fewest inbound dependencies. Then move Consumer validation behind B1.
5. **Add integration tests** asserting each saga's failure path. Examples: no courier → order remains `ACCEPTED/PENDING` (or reverts), consumer rejected → `REJECTED`, duplicate `ScheduleDelivery` → no duplicate actions.
