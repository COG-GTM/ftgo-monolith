# ftgo-order-service: cross-module seams blocking extraction

This document inventories every place where `ftgo-order-service` reaches directly
into another module's internals (concrete classes, JPA entities, repositories,
Spring configuration, database tables) instead of going through a published
interface, and lists the seams that would have to be broken to extract Order
into an independently deployable service.

All references are `path:line` relative to the repository root, against the
`master` branch at commit `4823d191`.

## Assumptions

- **"Module"** means a Gradle subproject in `settings.gradle`.
- **"Interface"** means either a `*-service-api` module (DTOs/contracts meant to be
  shared) or a Java interface/port *owned by the order module*. Today no module
  publishes a Java service interface; the `*-api` modules only hold web DTOs.
- **`ftgo-domain` is treated as other modules' internals.** It is a single shared
  module that holds the JPA entities and repositories for every bounded context
  (Consumer, Restaurant, Courier, Order). `Restaurant`, `Courier`, `Action`,
  `Plan`, `Consumer` and their repositories belong to the restaurant/courier/consumer
  contexts, even though they physically live in `ftgo-domain`.
- `Order`, `OrderLineItem(s)`, `OrderRevision`, `OrderState`, `OrderRepository`,
  `DeliveryInformation`, `PaymentInformation`, `LineItemQuantityChange`,
  `OrderMinimumNotMetException` are considered *Order-owned* but misplaced in
  `ftgo-domain`; they must move with the service (seam S6).
- Shared kernel types in `ftgo-common` (`Money`, `Address`, `PersonName`,
  `UnsupportedStateTransitionException`) and `common-swagger` are acceptable
  library dependencies and are **not** counted as seams.
- Both directions are covered: outbound (order -> others) and inbound
  (others -> order internals), plus test code and the shared schema, because all
  of them block a clean cut.
- This is a static read of the code; nothing was executed. The Gradle 4.10.2
  wrapper does not run on the JDK 17 installed on the analysis box, so no build
  was attempted (the change in this branch is documentation only).

## Summary of seams

| # | Seam | Other module | Kind | Severity |
|---|------|--------------|------|----------|
| S1 | Order -> Consumer: concrete `ConsumerService` call | `ftgo-consumer-service` | Build + in-process call + shared tx | High |
| S2 | Order -> Restaurant: `RestaurantRepository` + `Restaurant`/`MenuItem` entities | restaurant context in `ftgo-domain` | Repository + entity graph | High |
| S3 | Order -> Courier: `CourierRepository`, `Courier`, assignment strategy, mutating courier plan | courier context in `ftgo-domain` | Repository + cross-aggregate write | Critical |
| S4 | Courier types leaking into Order's web API (`Action`, `ActionType`) | courier context in `ftgo-domain` | Public contract | Medium |
| S5 | Shared Spring wiring (`DomainConfiguration`, implicit `ConsumerService` bean) | `ftgo-domain`, `ftgo-consumer-service` | Configuration | Medium |
| S6 | Order aggregate itself lives in shared `ftgo-domain` | `ftgo-domain` | Ownership | High |
| S7 | Shared database: FKs across context tables | `ftgo-flyway` | Schema | Critical |
| S8 | Inbound: `ftgo-application` depends on order internals | `ftgo-application` | Inbound build/code | Low-Medium |
| S9 | Order test fixtures built from other contexts' entities | `ftgo-domain` | Test code | Low |

---

## S1. Order -> Consumer service (concrete class, same transaction)

`ftgo-order-service` depends on the **implementation** module
`ftgo-consumer-service`, not only on `ftgo-consumer-service-api`
(which contains only `CreateConsumerRequest/Response` and no service contract).

| Location | What |
|---|---|
| `ftgo-order-service/build.gradle:43` | `compile project(":ftgo-consumer-service")` - build-time dependency on another service's implementation module. |
| `ftgo-order-service/build.gradle:40` | `compile project(":ftgo-consumer-service-api")` - declared but unused by order code; the real coupling bypasses it. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:4` | Imports `net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService` (consumer's internal `domain` package). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:27,33` | Injects the concrete `ConsumerService` bean into `OrderService`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:4` | Imports `consumerservice.domain.ConsumerService`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:29,36,43` | Field/constructor typed to the concrete class. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:59` | `consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal())` - synchronous in-process call executed **inside `OrderService`'s `@Transactional` boundary** (`OrderService.java:18,48`). |
| `ftgo-consumer-service/src/main/java/net/chrisrichardson/ftgo/consumerservice/domain/ConsumerService.java:12-21` | Callee is itself `@Transactional` and loads the `Consumer` entity via `ConsumerRepository`; order and consumer reads share one DB transaction. |

**To break:** define an order-owned port, e.g.
`interface ConsumerValidation { void validateOrderForConsumer(long consumerId, Money total); }`
in the order module; add an in-process adapter that delegates to `ConsumerService`
while still in the monolith, then swap for an HTTP/messaging adapter. Remove the
`:ftgo-consumer-service` dependency. Decide on the failure semantics
(`ConsumerNotFoundException`, `ConsumerVerificationFailedException`) as part of
the contract, and accept that the check no longer shares a transaction
(saga/compensation, or validate-before-save only).

## S2. Order -> Restaurant context (repository + entity graph)

Order reads restaurant data straight from the restaurant context's repository
and holds a JPA reference to the `Restaurant` entity.

| Location | What |
|---|---|
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:24,31` | Injects `RestaurantRepository` (from `ftgo-domain`) into `OrderService`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:25,34,41` | `RestaurantRepository` field/constructor. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:51-52` | `restaurantRepository.findById(restaurantId)` - direct read of another context's table. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:57` | `new Order(consumerId, restaurant, ...)` - passes the `Restaurant` entity into the Order aggregate. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:72-75` | `restaurant.findMenuItem(...)`, `MenuItem.getName()/getPrice()` - navigates restaurant internals to price line items. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:119-126` | `order.getRestaurant().getAddress().getLatitude()/getLongitude()` - reads restaurant location for ETA. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:78` | `order.getRestaurant().getName()` - lazy-loads the restaurant entity to build the response. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java:32-33` | `@ManyToOne(fetch = LAZY) private Restaurant restaurant;` - object reference across aggregates/contexts. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java:61,128-130` | Constructor takes and getter exposes `Restaurant`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/DistanceOptimizedCourierAssignmentStrategy.java:26-28` | Courier strategy (called by order) also dereferences `order.getRestaurant().getAddress()`. |

Mitigating fact: `OrderLineItem` already snapshots `menuItemId`, `name`, `price`
(`OrderService.java:75`), so the Order does not need live menu data after creation.

**To break:** replace `Restaurant restaurant` in `Order` with `long restaurantId`
plus a denormalized `restaurantName` (and pickup `Address` if ETA stays in Order).
Introduce an order-owned port such as
`RestaurantCatalog.findRestaurantForOrdering(id) -> RestaurantSnapshot{id, name, address, menu}`.
Longer term, keep a local replica of restaurant menus fed by restaurant events
(the pattern the original FTGO microservices version uses).
`ftgo-restaurant-service-api` (`build.gradle:41`) is declared but not used by
order code today and is the natural home for the DTO.

## S3. Order -> Courier context (repository, strategy, cross-aggregate writes)

The most entangled seam. `OrderService.accept` selects and **mutates** a
`Courier` aggregate in the same transaction as the Order.

| Location | What |
|---|---|
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:17-21` | Order module instantiates courier-context logic: `new DistanceOptimizedCourierAssignmentStrategy()` (with a `// TODO move to framework`). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:28-29,34-35` | Injects `CourierRepository` and `CourierAssignmentStrategy` into `OrderService`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:30-31,37-38,44-45` | Courier repository/strategy fields and constructor params. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:98` | `accept()` calls `scheduleDelivery()` synchronously. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:102` | `courierRepository.findAllAvailable()` - queries courier table directly. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:103` | `courierAssignmentStrategy.assignCourier(couriers, order)` - courier-selection policy executed in order's process. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:105,108` | `courier.addAction(Action.makePickup/makeDropoff(order, ...))` - **writes into another aggregate's state** (`Courier.plan`). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:110` | `order.schedule(courier)` - stores a `Courier` entity reference on the Order. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:113` | Reads `courier.getActiveDeliveryCount()` (derived from courier plan internals). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:119-128` | ETA uses `courier.hasLocation()`, `getCurrentLatitude/Longitude()` and static helpers `DistanceOptimizedCourierAssignmentStrategy.haversineDistance/estimateDeliveryMinutes` (concrete class, not the interface). |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java:55-56` | `@ManyToOne private Courier assignedCourier;` |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java:181-187` | `schedule(Courier)` / `getAssignedCourier()` expose the courier entity. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Action.java:16-17` | Reverse direction: courier-owned `Action` has `@ManyToOne private Order order;` |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Action.java:28-38` | `actionFor(Order)`, `makePickup(Order)`, `makeDropoff(Order, ...)` take the Order entity. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Plan.java:135,143` | `removeDelivery(Order)`, `actionsForDelivery(Order)`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Courier.java:56,76` | `cancelDelivery(Order)`, `actionsForDelivery(Order)`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/CourierAssignmentStrategy.java:7` | Strategy interface signature is `assignCourier(List<Courier>, Order)` - couples courier policy to the Order entity. |

Note: `Order.cancel()` (`Order.java:82-90`) does not call `Courier.cancelDelivery`,
so cancelling an accepted order would leave courier actions behind - a latent
bug that will surface once these are separate services and should be handled
explicitly by the new contract.

**To break:** move courier selection behind a courier-owned API:
`DeliveryScheduling.scheduleDelivery(orderId, pickupAddress, deliveryAddress, readyBy) -> {courierId, pickupTime, dropoffEta}`.
In `Order`, replace `Courier assignedCourier` with `Long assignedCourierId` and
store the returned ETA. In courier context, change `Action.order` to
`long orderId` and `Plan/Courier/CourierAssignmentStrategy` methods to take ids
or a small value object. Replace the synchronous in-transaction write with a
command/event (`OrderAccepted` -> courier assigns -> `DeliveryScheduled`), plus
a compensating `cancelDelivery(orderId)` on order cancellation.

## S4. Courier internals leaking into Order's public web contract

| Location | What |
|---|---|
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/GetOrderResponse.java:4,15,30,69-75` | Response DTO exposes `List<net.chrisrichardson.ftgo.domain.Action>` (a courier-context JPA `@Embeddable`). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:3` | Wildcard import `net.chrisrichardson.ftgo.domain.*`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:62-73` | `order.getAssignedCourier().actionsForDelivery(order)`, filters by `ActionType.DROPOFF` - controller navigates courier plan internals to compute ETA. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:79` | `order.getAssignedCourier().getId()`. |

`GetOrderResponse` also lives in `ftgo-order-service` rather than
`ftgo-order-service-api`, so consumers of the order API cannot depend on it
without depending on the implementation.

**To break:** introduce an order-owned `DeliveryActionDTO{type, time}` (or just
`pickupTime`/`estimatedDeliveryTime`) in `ftgo-order-service-api`, and populate
it from data stored on the Order (S3) instead of walking the courier's plan.

## S5. Shared Spring wiring / implicit bean dependencies

| Location | What |
|---|---|
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:15` | `@Import(DomainConfiguration.class)`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/DomainConfiguration.java:11-17` | `@ComponentScan @EntityScan @EnableJpaRepositories` over the whole `ftgo.domain` package - order's context gets every entity and repository (Consumer, Courier, Restaurant) whether it should or not. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:27` | Requires a `ConsumerService` bean, but neither `OrderConfiguration` nor `OrderServiceConfiguration` (`ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/main/OrderServiceConfiguration.java:16`) imports `ConsumerConfiguration`. It only resolves because `ftgo-application` also imports `ConsumerServiceConfiguration` (`ftgo-application/src/main/java/net/chrisrichardson/ftgo/FtgoApplicationMain.java:17`), which pulls in `ConsumerConfiguration` via `ftgo-consumer-service/src/main/java/net/chrisrichardson/ftgo/consumerservice/web/ConsumerWebConfiguration.java:10`. `OrderServiceConfiguration` cannot boot on its own today. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderServiceWithRepositoriesConfiguration.java:9` | `@EnableJpaRepositories` on `orderservice.domain`, which contains no repositories - the real `OrderRepository` comes from `ftgo-domain`. |

**To break:** give the order module its own `@EntityScan`/`@EnableJpaRepositories`
scoped to its own entity package (after S6), drop `@Import(DomainConfiguration)`,
and have `OrderConfiguration` declare the S1/S2/S3 ports as explicit beans
(in-process adapters now, remote adapters later). A standalone
`OrderServiceConfiguration` boot test is a good guard.

## S6. Order aggregate is owned by the shared `ftgo-domain` module

The order module's core model is not in the order module.

| Location | What |
|---|---|
| `ftgo-order-service/build.gradle:38` | `compile project(":ftgo-domain")`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/Order.java:14-18` | `Order` entity (table `orders`). |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/OrderRepository.java:7` | `OrderRepository`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/OrderLineItems.java:14-15` | `order_line_items` collection. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/OrderRevision.java:8-9` | `OrderRevision` (uses `DeliveryInformation`). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:5` | `import net.chrisrichardson.ftgo.domain.*;` (wildcard over all contexts). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/RevisedOrder.java:3-4` | `LineItemQuantityChange`, `Order` from `ftgo-domain`. |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:27,32,47,53` | Controller uses `OrderRepository` directly (bypasses `OrderService` for reads). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/web/OrderController.java:98` | Builds `ftgo.domain.OrderRevision` directly in the web layer. |

**To break:** move `Order`, `OrderLineItem(s)`, `OrderState`, `OrderRevision`,
`OrderRepository`, `DeliveryInformation`, `PaymentInformation`,
`LineItemQuantityChange`, `OrderMinimumNotMetException` into
`ftgo-order-service` (e.g. `net.chrisrichardson.ftgo.orderservice.domain`).
This is only possible after S2/S3 remove the `Restaurant`/`Courier`
references from `Order` and the `Order` references from `Action`/`Plan`/`Courier`,
otherwise the move creates a cycle between order and `ftgo-domain`.

Intra-module note (not a cross-module seam, but will bite during the move):
`OrderService` (domain layer) imports the web DTO
`orderservice.web.MenuItemIdAndQuantity`
(`ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:6`).

## S7. Shared database schema with cross-context foreign keys

| Location | What |
|---|---|
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:108-109` | `orders.restaurant_id` FK -> `restaurants(id)`. |
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:105-106` | `orders.assigned_courier_id` FK -> `courier(id)`. |
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:96-97` | `courier_actions.order_id` FK -> `orders(id)` (courier table depends on order table). |
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:50-74` | `orders` table defined in the single shared migration set alongside all other contexts. |
| `ftgo-flyway/src/main/resources/db/migration/V2__add_courier_optimization_and_api_tracking.sql:13-18` | One migration mixes restaurant, courier and order column changes. |

**To break:** drop the three cross-context FKs (keep plain id columns), move
`orders` and `order_line_items` DDL into an order-owned migration set/schema,
and have courier keep `order_id` as an opaque id. Do this before the physical
DB split so the JPA changes in S2/S3 can be validated against the existing DB.

## S8. Inbound: other modules reaching into order internals

| Location | What |
|---|---|
| `ftgo-application/build.gradle:5` | `compile project(":ftgo-order-service")` (expected for the monolith assembly). |
| `ftgo-application/src/main/java/net/chrisrichardson/ftgo/FtgoApplicationMain.java:6,18` | Imports `orderservice.main.OrderServiceConfiguration` (expected). |
| `ftgo-application/src/main/java/net/chrisrichardson/ftgo/GlobalExceptionHandler.java:7-8,24-40` | Handles `orderservice.domain.OrderNotFoundException` and `RestaurantNotFoundException` - app-level code depends on order's internal exception types. |
| `ftgo-application/src/main/java/net/chrisrichardson/ftgo/GlobalExceptionHandler.java:6,61-69` | Handles `ftgo.domain.NoCourierAvailableException`, which is thrown from the courier strategy invoked inside order's `accept` flow (S3). |
| `ftgo-application/src/test/java/net/chrisrichardson/ftgo/FtgoApplicationTest.java:5,24` | End-to-end test imports `OrderServiceConfiguration`. |

No other *service* module (`ftgo-consumer-service`, `ftgo-restaurant-service`,
`ftgo-courier-service`) imports `net.chrisrichardson.ftgo.orderservice.*`;
the end-to-end tests use only `ftgo-order-service-api`
(`ftgo-end-to-end-tests-common/build.gradle:4`). The inbound coupling from the
courier side is via the shared `Order` entity (S3: `Action.java:16-17`,
`Plan.java:135,143`, `Courier.java:56,76`) and the FK in S7.

**To break:** move the order-specific exception handlers into an order-owned
`@ControllerAdvice` in `ftgo-order-service` (or map to `ErrorResponse` inside
`OrderController`); keep only generic handlers in `ftgo-application`.

## S9. Test code coupled to other contexts' entities

| Location | What |
|---|---|
| `ftgo-order-service/src/test/java/net/chrisrichardson/ftgo/orderservice/RestaurantMother.java:4-6,19-24` | Builds `Restaurant`, `RestaurantMenu`, `MenuItem` entities. |
| `ftgo-order-service/src/test/java/net/chrisrichardson/ftgo/orderservice/OrderDetailsMother.java:4,36` | `new Order(CONSUMER_ID, new Restaurant(...), ...)`. |
| `ftgo-order-service/src/test/java/net/chrisrichardson/ftgo/orderservice/web/OrderControllerTest.java:5` | Mocks `ftgo.domain.OrderRepository`. |

**To break:** update fixtures alongside S2/S6 (restaurant snapshot/DTO instead
of the entity).

---

## Suggested extraction order

1. **Ports first, no behaviour change (S1, S2, S3 in-process).** Introduce
   `ConsumerValidation`, `RestaurantCatalog`, `DeliveryScheduling` interfaces in
   the order module with in-process adapters over the existing
   services/repositories. Remove `:ftgo-consumer-service` from
   `ftgo-order-service/build.gradle:43`.
2. **Replace entity references with ids (S2, S3, S4).** `Order.restaurant` ->
   `restaurantId` + snapshot; `Order.assignedCourier` -> `assignedCourierId` +
   stored ETA; `Action.order` -> `orderId`; fix `GetOrderResponse`.
3. **Move Order-owned types out of `ftgo-domain` (S6) and scope Spring config (S5).**
4. **Own the exceptions (S8) and fixtures (S9).**
5. **Split the schema (S7)**, then swap in-process adapters for remote ones and
   turn the `accept` flow into a saga/event exchange with the courier context.
