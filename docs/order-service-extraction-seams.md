# ftgo-order-service: extraction seams

Inventory of every place `ftgo-order-service` reaches directly into another module's
internals instead of going through a published interface, grouped into the seams that
must be broken to extract it as an independently deployable service.

All line numbers refer to commit `4823d191` on `master`.

## Assumptions

- **"Interface"** means a published `*-api` module (DTOs / contracts) or an explicit
  port that order-service owns. Everything else is treated as another module's internals:
  - concrete service classes in another service's implementation module
    (e.g. `consumerservice.domain.ConsumerService`),
  - JPA entities, repositories and domain logic belonging to another bounded context,
    even though they physically live in the shared `ftgo-domain` module,
  - another context's database tables / foreign keys,
  - Spring configuration that boots another context's beans.
- `ftgo-common` (`Money`, `Address`, `PersonName`, `UnsupportedStateTransitionException`)
  and `common-swagger` are treated as an acceptable **shared kernel** (library, no state)
  and are not listed as seams.
- `ftgo-domain` is a grab-bag: it holds Order's own aggregate *and* Consumer, Restaurant
  and Courier aggregates. Order-owned classes there (`Order`, `OrderLineItem(s)`,
  `OrderRevision`, `OrderState`, `LineItemQuantityChange`, `DeliveryInformation`,
  `PaymentInformation`, `OrderMinimumNotMetException`, `OrderRepository`) are treated as
  "order internals in the wrong module" (Seam 6), not as foreign internals.
- Inbound dependencies (other modules reaching into order-service) are included in
  Seam 8, because they also block extraction.
- Scope was static analysis only (source, Gradle, Spring config, Flyway DDL); no runtime
  tracing. End-to-end tests only use `ftgo-order-service-api` DTOs and are not seams.

## Summary

| # | Seam | Target module | Coupling type | Severity |
|---|------|---------------|---------------|----------|
| 1 | Build-time dependency on implementation modules | consumer, domain | Gradle | High |
| 2 | Consumer validation via concrete `ConsumerService` | consumer-service | In-process call + shared txn | Medium |
| 3 | Restaurant lookup via `RestaurantRepository` / `Restaurant` entity | restaurant | Direct data access, entity graph | High |
| 4 | Courier selection & dispatch inside `OrderService` | courier | Direct data access, writes to foreign aggregate | **Highest** |
| 5 | Courier data in order read model / REST response | courier | Entity navigation, leaked domain type | High |
| 6 | Order's own model lives in shared `ftgo-domain` | domain | Module ownership | Medium |
| 7 | Shared Spring/JPA bootstrapping and single schema | domain, flyway | Config + DB FKs | High |
| 8 | Inbound references into order-service internals | application, courier | Reverse coupling | Medium |

---

## Seam 1 - Build dependencies on implementation modules

| Location | Detail |
|---|---|
| `ftgo-order-service/build.gradle:43` | `compile project(":ftgo-consumer-service")` - depends on the consumer **implementation** module, not just `ftgo-consumer-service-api` (line 40). This is what makes Seam 2 compile. |
| `ftgo-order-service/build.gradle:38` | `compile project(":ftgo-domain")` - pulls in every other context's entities and repositories (Seams 3-7). |
| `ftgo-order-service/build.gradle:41` | `compile project(":ftgo-restaurant-service-api")` - declared but no class from it is referenced by order-service; restaurant data is read via `ftgo-domain` instead. |

**To break:** depend only on `*-api` modules (or on nothing, if calls become remote);
remove `:ftgo-consumer-service` and `:ftgo-domain`.

## Seam 2 - Consumer validation (consumer-service)

| Location | Detail |
|---|---|
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java:4` | imports `net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService` - a concrete class from consumer-service's `domain` package. |
| `OrderService.java:29, 36, 43` | field / constructor parameter / assignment of `ConsumerService`. |
| `OrderService.java:59` | `consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal())` - synchronous in-process call. |
| `OrderService.java:18, 48` + `ftgo-consumer-service/.../consumerservice/domain/ConsumerService.java:12, 18-21` | Both classes are `@Transactional`; the validation joins the order-creation transaction. Extraction turns this into a distributed step (needs a saga/compensation or pre-validation). |
| `ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderConfiguration.java:4, 27, 33` | bean wiring injects the concrete `ConsumerService`. |

**To break:** introduce an order-owned port, e.g. `ConsumerVerifier.verify(consumerId, Money)`,
with an in-process adapter now and an HTTP/messaging adapter after extraction. Contract DTOs
go into `ftgo-consumer-service-api` (currently web DTOs only).

## Seam 3 - Restaurant data (restaurant-service)

Order reads the restaurant aggregate straight from the restaurant tables and stores a
JPA reference to it, bypassing `RestaurantService.findById`
(`ftgo-restaurant-service/.../restaurantservice/domain/RestaurantService.java:31-33`).

| Location | Detail |
|---|---|
| `OrderService.java:25, 34, 41` | `RestaurantRepository` field / ctor / assignment. |
| `OrderService.java:51-52` | `restaurantRepository.findById(restaurantId)` - direct read of restaurant persistence. |
| `OrderService.java:72-76` | `restaurant.findMenuItem(...)`, `om.getName()`, `om.getPrice()` - walks restaurant menu internals (`ftgo-domain/.../Restaurant.java:65-67`, `MenuItem.java:54-64`). |
| `OrderService.java:57` | `new Order(consumerId, restaurant, orderLineItems)` - passes the foreign entity into the order aggregate. |
| `OrderService.java:119-126` | `order.getRestaurant().getAddress().getLatitude()/getLongitude()` for ETA. |
| `ftgo-domain/.../domain/Order.java:32-33` | `@ManyToOne(fetch = LAZY) private Restaurant restaurant;` - cross-aggregate JPA association. |
| `Order.java:61-63, 128-130` | constructor and `getRestaurant()` expose the foreign entity. |
| `ftgo-order-service/.../orderservice/web/OrderController.java:78` | `order.getRestaurant().getName()` - lazy navigation into restaurant in the read path. |
| `OrderConfiguration.java:24` | bean wiring injects `RestaurantRepository`. |
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:72, 108-109` | `orders.restaurant_id` + FK `orders_restaurant_id -> restaurants(id)`. |
| Tests: `ftgo-order-service/src/test/java/net/chrisrichardson/ftgo/orderservice/RestaurantMother.java:4-6, 19-24`; `OrderDetailsMother.java:36` | build `Restaurant`/`MenuItem`/`RestaurantMenu` entities directly. |

**To break:** replace `Restaurant restaurant` in `Order` with `long restaurantId` plus the
data order actually needs (restaurant name, pickup lat/lng) captured at order time; get menu
items/prices through a `RestaurantCatalog` port (in-process -> `RestaurantService` now, API or a
locally replicated restaurant read model fed by restaurant events later); drop the FK.

## Seam 4 - Courier selection and dispatch (courier-service) - hardest seam

`OrderService.accept` performs courier-domain work: it queries couriers, runs the
assignment algorithm, and **mutates the Courier aggregate** inside the order transaction.

| Location | Detail |
|---|---|
| `OrderService.java:30, 37, 44` | `CourierRepository` field / ctor / assignment. |
| `OrderService.java:102` | `courierRepository.findAllAvailable()` - direct read of courier persistence (`ftgo-domain/.../CourierRepository.java:10-11`); `CourierService` exposes no equivalent. |
| `OrderService.java:31, 38, 45, 103` | `CourierAssignmentStrategy.assignCourier(couriers, order)` - courier-selection logic invoked by order. |
| `OrderConfiguration.java:17-21` | order module instantiates `DistanceOptimizedCourierAssignmentStrategy` (comment `// TODO move to framework`). |
| `OrderService.java:105, 108` | `courier.addAction(Action.makePickup(order))` / `makeDropoff(...)` - **writes** to the Courier aggregate (`Courier.java:52-54` -> `Plan.java:13-15`). |
| `OrderService.java:113` | `courier.getId()`, `courier.getActiveDeliveryCount()` (`Courier.java:106-113`, iterates `Plan` internals). |
| `OrderService.java:119, 124` | `courier.hasLocation()`, `getCurrentLatitude()/getCurrentLongitude()` (`Courier.java:88-94, 115-117`). |
| `OrderService.java:123, 128` | static calls `DistanceOptimizedCourierAssignmentStrategy.haversineDistance(...)` / `estimateDeliveryMinutes(...)` (`DistanceOptimizedCourierAssignmentStrategy.java:81, 91`) - courier-routing internals used for the order ETA. |
| `OrderService.java:110` | `order.schedule(courier)` -> `Order.java:181-183` stores the Courier entity. |
| `OrderService.java:115` | `courier_assignments` metric emitted by order - belongs to courier. |
| `DistanceOptimizedCourierAssignmentStrategy.java:21, 78` | throws `NoCourierAvailableException` into the order `accept` call (handled in `ftgo-application/.../GlobalExceptionHandler.java:61-63`). |
| `ftgo-domain/.../CourierAssignmentStrategy.java:7`; `DistanceOptimizedCourierAssignmentStrategy.java:19, 26-28` | strategy signature takes `Order` and navigates `order.getRestaurant().getAddress()` - courier code depends on order and restaurant internals too. |

**To break:** move `CourierAssignmentStrategy`, `DistanceOptimizedCourierAssignmentStrategy`,
ETA estimation and `Plan`/`Action` mutation into courier-service behind a command such as
`CourierDispatch.scheduleDelivery(orderId, pickupLatLng, readyBy) -> {courierId, eta}` (or an
`OrderAccepted` event -> `DeliveryScheduled` reply). Order keeps only `assignedCourierId` and
`estimatedDeliveryTime`. This also removes the cross-aggregate write from the order
transaction (currently one ACID txn; becomes a saga).

## Seam 5 - Courier data in the order read model / REST API

| Location | Detail |
|---|---|
| `OrderController.java:62-64` | `order.getAssignedCourier().actionsForDelivery(order)` - navigates Courier -> Plan -> Action (`Courier.java:76-78`, `Plan.java:25-27`). |
| `OrderController.java:69-70` | filters on courier `ActionType.DROPOFF` / `Action::getTime` to derive ETA. |
| `OrderController.java:79` | `order.getAssignedCourier().getId()`. |
| `ftgo-domain/.../Order.java:55-56, 185-187` | `@ManyToOne private Courier assignedCourier;` + getter. |
| `ftgo-order-service/.../orderservice/web/GetOrderResponse.java:4, 15, 30, 69-75` | response DTO exposes courier domain class `net.chrisrichardson.ftgo.domain.Action` (an `@Embeddable` with a `@ManyToOne Order` back-reference, `Action.java:16-17`) as part of the public REST contract. |
| `V1__create_ftgo_db.sql:71, 105-106` | `orders.assigned_courier_id` + FK `orders_assigned_courier_id -> courier(id)`. |

**To break:** store `assignedCourierId` and `estimatedDeliveryTime` on `Order` (populated by
Seam 4's reply/event); define a `CourierActionDTO` in `ftgo-order-service-api` (or
`ftgo-courier-service-api`) instead of returning `Action`; drop the FK.

## Seam 6 - Order's own model lives in shared `ftgo-domain`

| Location | Detail |
|---|---|
| `OrderService.java:5` | `import net.chrisrichardson.ftgo.domain.*;` - single wildcard import covering both order-owned and foreign classes. |
| `OrderController.java:3` | same wildcard import. |
| `OrderController.java:27, 30-32, 47, 53` | controller injects `OrderRepository` and queries it directly (`findById`, `findAllByConsumerId`), bypassing `OrderService`. |
| `ftgo-order-service/.../orderservice/domain/RevisedOrder.java:3-4` | `LineItemQuantityChange`, `Order` from `ftgo-domain`. |
| `ftgo-domain/src/main/java/net/chrisrichardson/ftgo/domain/` | `Order`, `OrderLineItem`, `OrderLineItems`, `OrderRevision`, `OrderState`, `LineItemQuantityChange`, `DeliveryInformation`, `PaymentInformation`, `OrderMinimumNotMetException`, `OrderRepository` all live here, next to Consumer/Courier/Restaurant. |
| Tests: `ftgo-order-service/src/test/java/.../web/OrderControllerTest.java:5, 26, 32`; `OrderDetailsMother.java:4, 20-39` | build/mocks `Order`, `OrderLineItem`, `OrderRepository` from `ftgo-domain`. |

**To break:** move these classes into `ftgo-order-service` (e.g. `orderservice.domain`), after
Seams 3-5 have removed their references to `Restaurant` and `Courier`. Route controller reads
through `OrderService` (or an order query service).

## Seam 7 - Shared Spring/JPA bootstrapping and shared schema

| Location | Detail |
|---|---|
| `OrderConfiguration.java:15` | `@Import(DomainConfiguration.class)`. |
| `ftgo-domain/.../DomainConfiguration.java:11-16` | `@EnableAutoConfiguration @ComponentScan @EntityScan @EnableJpaRepositories` over the whole `net.chrisrichardson.ftgo.domain` package - order-service's context boots Consumer, Courier and Restaurant entities and repositories. |
| `ftgo-order-service/.../orderservice/main/OrderServiceConfiguration.java:13-16` | `@EnableAutoConfiguration @ComponentScan @EntityScan` - relies on the shared domain being on the classpath. |
| `ftgo-order-service/.../orderservice/domain/OrderServiceWithRepositoriesConfiguration.java:9-11` | `@EnableJpaRepositories` in `orderservice.domain` (finds nothing there; repos actually come from `DomainConfiguration`). |
| `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql:1` | single `ftgo` schema for all contexts, one Flyway module. |
| `V1__create_ftgo_db.sql:96-97` | FK `courier_actions.order_id -> orders(id)` (courier table referencing order table - inbound). |
| `V1__create_ftgo_db.sql:105-109` | FKs from `orders` to `courier` and `restaurants` (also listed in Seams 3/5). |
| `ftgo-application/src/main/resources/application.properties:6` | one datasource `jdbc:mysql://.../ftgo` shared by every module. |

**To break:** give order-service its own `@EntityScan`/`@EnableJpaRepositories` limited to
its package, its own schema (`orders`, `order_line_items`) and migrations; drop the three
cross-context FKs (`orders_restaurant_id`, `orders_assigned_courier_id`,
`courier_actions_order_id`) and replace with ID columns + eventual consistency.

## Seam 8 - Inbound references into order-service internals

| Location | Detail |
|---|---|
| `ftgo-domain/.../Action.java:16-17` | `@ManyToOne private Order order;` - courier's embeddable holds an Order entity. |
| `ftgo-domain/.../Plan.java:17-19, 25-27`; `Courier.java:56-58, 76-78` | `removeDelivery(Order)`, `actionsForDelivery(Order)`, `cancelDelivery(Order)` take the Order entity. |
| `ftgo-application/src/main/java/net/chrisrichardson/ftgo/GlobalExceptionHandler.java:7-8, 24-40` | imports and handles `orderservice.domain.OrderNotFoundException` / `RestaurantNotFoundException`. |
| `ftgo-application/src/main/java/net/chrisrichardson/ftgo/FtgoApplicationMain.java:6, 18` | imports `orderservice.main.OrderServiceConfiguration`. |
| `ftgo-application/src/test/java/net/chrisrichardson/ftgo/FtgoApplicationTest.java:5, 24` | same. |
| `ftgo-application/build.gradle:5` | `compile project(":ftgo-order-service")`. |

**To break:** courier `Action` should reference `orderId` (Long) only; move order exception
handling into order-service's own `@ControllerAdvice`; remove order-service from the
monolith's composition root once it is deployed separately (or route through a gateway).

---

## Minor findings (not cross-module, but worth fixing during extraction)

- `OrderService.java:6, 50, 72` - the domain service depends on the web DTO
  `orderservice.web.MenuItemIdAndQuantity` (layering inversion inside the module).
- `ftgo-order-service/.../orderservice/web/TicketController.java:3, 9-19` - empty controller with
  an unused import of `OrderAcceptance`.
- `ftgo-order-service-api/.../api/events/OrderDetails.java`, `OrderLineItemDTO.java` - event
  DTOs exist but nothing publishes them; they are the natural starting point for the
  `OrderCreated`/`OrderAccepted` events needed by Seams 3-5.

## Suggested order of work

1. **Seam 1 + 2** (smallest, mechanical): introduce `ConsumerVerifier` port; drop the
   `:ftgo-consumer-service` dependency.
2. **Seam 4 + 5** (largest, highest-risk): move courier dispatch into courier-service behind a
   `CourierDispatch` port; store `assignedCourierId` / `estimatedDeliveryTime` on `Order`.
3. **Seam 3**: `RestaurantCatalog` port; replace the `Restaurant` association with
   `restaurantId` + snapshot fields.
4. **Seam 6 + 8**: move order classes out of `ftgo-domain`; courier `Action` uses `orderId`;
   move exception handling.
5. **Seam 7**: split JPA config, schema and migrations; drop cross-context FKs.
