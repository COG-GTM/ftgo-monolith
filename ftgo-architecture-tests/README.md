# ftgo-architecture-tests

ArchUnit rules that enforce the module boundaries of the bounded-context map. They run as part of
`./gradlew build` (`./gradlew :ftgo-architecture-tests:test` on its own) against the production
classes of every module on `ftgo-application`'s classpath.

## Context map (encoded in `BoundedContext` / `ContextMap`)

| Part | Packages | Owned classes in `ftgo-domain` | Published API |
|---|---|---|---|
| Consumer | `consumerservice..` | `Consumer`, `ConsumerRepository` | `consumerservice.api..` |
| Restaurant | `restaurantservice..` | `Restaurant`, `RestaurantRepository`, `RestaurantMenu`, `MenuItem` | `restaurantservice.events..` |
| Courier | `courierservice..` | `Courier`, `CourierRepository`, `Plan`, `Action`, `ActionType`, `CourierAssignmentStrategy`, `DistanceOptimizedCourierAssignmentStrategy`, `NoCourierAvailableException` | `courierservice.api..` |
| Order | `orderservice..` | `Order`, `OrderLineItem(s)`, `OrderRevision`, `OrderState`, `OrderRepository`, `DeliveryInformation`, `PaymentInformation`, `LineItemQuantityChange`, `OrderMinimumNotMetException` | `orderservice.api..` |
| Shared kernel | `ftgo.common..`, `common-swagger`, `DomainConfiguration` | | |
| Composition root | `net.chrisrichardson.ftgo` (ftgo-application) | | |

## Rules

| Rule | Status |
|---|---|
| Every class is placed on the context map | passes |
| Shared kernel does not depend on contexts or the composition root | passes |
| Nothing depends on the composition root | passes |
| Published APIs only depend on their own API and the shared kernel | passes |
| Service packages are free of cycles | passes |
| Contexts only reach other contexts through published APIs | passes with 27 excluded class pairs |
| `*service.domain` does not depend on the web layer | passes with 1 excluded class pair |
| Known violations are not stale | guards the exclusion lists |

Exclusions are `origin class -> target class` pairs in `ModuleBoundaryRulesTest`. A dependency from
a new class, or to a new target, fails the build. Removing a dependency without deleting its
exclusion also fails the build, so the lists can only shrink.

## Excluded violations

Cross-context (`contexts_only_reach_other_contexts_through_published_apis`):

| Origin | Target | Why |
|---|---|---|
| `orderservice.domain.OrderConfiguration` | `consumerservice.domain.ConsumerService` | order validates consumer in-process |
| `orderservice.domain.OrderService` | `consumerservice.domain.ConsumerService` | order validates consumer in-process |
| `orderservice.domain.OrderConfiguration` | `domain.RestaurantRepository` | order reads restaurant table |
| `orderservice.domain.OrderService` | `domain.RestaurantRepository` | order reads restaurant table |
| `orderservice.domain.OrderService` | `domain.Restaurant` | prices line items from restaurant entity |
| `orderservice.domain.OrderService` | `domain.MenuItem` | prices line items from restaurant entity |
| `orderservice.web.OrderController` | `domain.Restaurant` | response reads restaurant name |
| `domain.Order` | `domain.Restaurant` | `@ManyToOne Restaurant` |
| `orderservice.domain.OrderConfiguration` | `domain.CourierRepository` | order assigns couriers |
| `orderservice.domain.OrderConfiguration` | `domain.CourierAssignmentStrategy` | order assigns couriers |
| `orderservice.domain.OrderConfiguration` | `domain.DistanceOptimizedCourierAssignmentStrategy` | order assigns couriers |
| `orderservice.domain.OrderService` | `domain.CourierRepository` | order assigns couriers |
| `orderservice.domain.OrderService` | `domain.CourierAssignmentStrategy` | order assigns couriers |
| `orderservice.domain.OrderService` | `domain.DistanceOptimizedCourierAssignmentStrategy` | ETA via static courier helpers |
| `orderservice.domain.OrderService` | `domain.Courier` | writes courier plan |
| `orderservice.domain.OrderService` | `domain.Action` | writes courier plan |
| `orderservice.web.OrderController` | `domain.Courier` | response exposes courier |
| `orderservice.web.OrderController` | `domain.Action` | response exposes courier actions |
| `orderservice.web.OrderController` | `domain.ActionType` | response exposes courier actions |
| `orderservice.web.GetOrderResponse` | `domain.Action` | response exposes courier actions |
| `domain.Order` | `domain.Courier` | `@ManyToOne assignedCourier` |
| `domain.Action` | `domain.Order` | `@ManyToOne Order` |
| `domain.Plan` | `domain.Order` | looks up actions by `Order` |
| `domain.Courier` | `domain.Order` | cancels / looks up deliveries by `Order` |
| `domain.CourierAssignmentStrategy` | `domain.Order` | signature takes `Order` |
| `domain.DistanceOptimizedCourierAssignmentStrategy` | `domain.Order` | reads order delivery address |
| `domain.DistanceOptimizedCourierAssignmentStrategy` | `domain.Restaurant` | reads restaurant address |

Layering (`domain_layer_does_not_depend_on_web_layer`):

| Origin | Target | Why |
|---|---|---|
| `orderservice.domain.OrderService` | `orderservice.web.MenuItemIdAndQuantity` | `createOrder` takes the web request DTO |
