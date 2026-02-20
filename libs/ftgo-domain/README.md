# ftgo-domain

Shared domain model library containing domain entities, DDD base types, and domain event infrastructure for FTGO microservices.

## Package

`com.ftgo.domain`

## Contents

### Domain Event Infrastructure

| Class | Description |
|-------|-------------|
| `DomainEvent` | Marker interface for domain events with a default `occurredOn()` timestamp. |
| `DomainEventPublisher` | Thread-local publisher for dispatching domain events to registered handlers. |
| `ResultWithEvents<T>` | Generic wrapper pairing an aggregate result with the domain events it produced. |

### Domain Entities

| Class | Description |
|-------|-------------|
| `Order` | JPA entity representing a customer order with lifecycle state transitions (approved → accepted → preparing → ready → picked up → delivered). |
| `Restaurant` | JPA entity representing a restaurant with menu items. |
| `Consumer` | JPA entity representing a consumer who places orders. |
| `Courier` | JPA entity representing a delivery courier with availability and delivery plan. |

### Value Objects / Embeddables

| Class | Description |
|-------|-------------|
| `MenuItem` | Embeddable value object for a restaurant menu item (id, name, price). |
| `RestaurantMenu` | Embeddable collection wrapper for a restaurant's menu items. |
| `OrderLineItem` | Embeddable value object for a single line item in an order. |
| `OrderLineItems` | Embeddable collection wrapper with order total and revision logic. |
| `DeliveryInformation` | Embeddable value object for delivery address and time. |
| `PaymentInformation` | Value object for payment token. |
| `Action` | Embeddable representing a pickup or dropoff action in a courier's plan. |
| `Plan` | Courier's delivery plan containing ordered actions. |
| `OrderRevision` | Encapsulates a requested revision to an order's line items or delivery info. |
| `LineItemQuantityChange` | Result of computing the monetary delta for a quantity change. |

### Enums

| Enum | Description |
|------|-------------|
| `OrderState` | Order lifecycle states: APPROVED, ACCEPTED, PREPARING, READY_FOR_PICKUP, PICKED_UP, DELIVERED, CANCELLED. |
| `ActionType` | Courier action types: PICKUP, DROPOFF. |

### Exceptions

| Class | Description |
|-------|-------------|
| `OrderMinimumNotMetException` | Thrown when an order revision results in a total below the minimum. |

### Repositories

| Interface | Description |
|-----------|-------------|
| `OrderRepository` | Spring Data repository for `Order` entities. |
| `RestaurantRepository` | Spring Data repository for `Restaurant` entities. |
| `ConsumerRepository` | Spring Data repository for `Consumer` entities. |
| `CourierRepository` | Spring Data repository for `Courier` entities with `findAllAvailable()`. |

### Configuration

| Class | Description |
|-------|-------------|
| `DomainConfiguration` | Spring configuration importing `CommonConfiguration`, enabling auto-configuration, entity scan, and JPA repositories. |

## Usage

Add as a project dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-domain')
}
```

## Build

```bash
./gradlew :libs:ftgo-domain:build
```

## Dependencies

- `ftgo-common` (api)
- `ftgo-common-jpa` (api)
- Spring Boot Starter Data JPA 3.2.5
- Jakarta Persistence API 3.1
- Hibernate ORM 6.4.x
- JUnit 5 / AssertJ / Mockito (test)
