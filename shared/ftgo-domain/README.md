# ftgo-domain — Shared Domain Library

Standalone versioned library extracted from the FTGO monolith.  
Contains all JPA entities, repositories, value objects, and domain configuration shared across FTGO microservices during the migration period.

## Coordinates

```
group:    com.ftgo
artifact: ftgo-domain
version:  1.0.0-SNAPSHOT
```

## Quick Start

**Declare the dependency** in any microservice `build.gradle`:

```groovy
dependencies {
    implementation 'com.ftgo:ftgo-domain:1.0.0-SNAPSHOT'
}
```

## Entity-to-Service Ownership Mapping

The following table documents which microservice will ultimately own each entity once the shared domain is decomposed into service-owned modules.

### Consumer Service (`ftgo-consumer-service`)

| Entity / Class | Type | Description |
|---------------|------|-------------|
| `Consumer` | `@Entity` | Consumer with PersonName, order validation |
| `ConsumerRepository` | Repository | CRUD + Spring Data for Consumer |

### Restaurant Service (`ftgo-restaurant-service`)

| Entity / Class | Type | Description |
|---------------|------|-------------|
| `Restaurant` | `@Entity` | Restaurant with name, address, menu items |
| `RestaurantMenu` | `@Embeddable` | Menu containing list of MenuItems |
| `MenuItem` | `@Embeddable` | Menu item with id, name, price |
| `RestaurantRepository` | Repository | CRUD for Restaurant |

### Order Service (`ftgo-order-service`)

| Entity / Class | Type | Description |
|---------------|------|-------------|
| `Order` | `@Entity` | Core order with state machine, delivery info, line items |
| `OrderLineItem` | `@Embeddable` | Order line item value object |
| `OrderLineItems` | `@Embeddable` | Collection wrapper for OrderLineItem |
| `OrderState` | Enum | Order lifecycle states (APPROVED → DELIVERED) |
| `OrderRevision` | Value Object | Order revision with delivery info and quantity changes |
| `LineItemQuantityChange` | Value Object | Tracks order total changes during revision |
| `OrderMinimumNotMetException` | Exception | Domain exception for minimum order validation |
| `DeliveryInformation` | `@Embeddable` | Delivery time and address |
| `PaymentInformation` | Value Object | Payment token holder |
| `OrderRepository` | Repository | CRUD + findAllByConsumerId |

### Courier Service (`ftgo-courier-service`)

| Entity / Class | Type | Description |
|---------------|------|-------------|
| `Courier` | `@Entity` | Courier with availability and delivery plan |
| `Plan` | Value Object | Courier's delivery plan (list of Actions) |
| `Action` | `@Embeddable` | Pickup/dropoff action for an order |
| `ActionType` | Enum | PICKUP, DROPOFF |
| `CourierRepository` | Repository | CRUD + findAllAvailable |

### Shared Configuration

| Class | Description |
|-------|-------------|
| `DomainConfiguration` | Spring `@Configuration` enabling auto-config, entity scan, JPA repositories, and importing CommonConfiguration + CommonJpaConfiguration |

## Cross-Service DTO/API Contracts

When services need to communicate across bounded contexts, they should use DTOs from the service API modules rather than sharing JPA entities directly. The following contracts are defined for cross-service communication:

### Order Service API (`shared/ftgo-order-service-api`)
- `OrderDetails` — Order summary for read-only cross-service queries
- `OrderState` — Order lifecycle state (shared enum)
- `CreateOrderRequest` / `CreateOrderResponse` — Order creation contract
- `OrderLineItemDto` — Line item representation without JPA annotations

### Consumer Service API (`shared/ftgo-consumer-service-api`)
- `ConsumerDetails` — Consumer summary for cross-service queries
- `ValidateConsumerRequest` / `ValidateConsumerResponse` — Consumer validation contract

### Restaurant Service API (`shared/ftgo-restaurant-service-api`)
- `RestaurantDetails` — Restaurant summary for cross-service queries
- `MenuItemDto` — Menu item representation without JPA annotations
- `ValidateOrderByRestaurantRequest` — Restaurant order validation contract

### Courier Service API (`shared/ftgo-courier-service-api`)
- `CourierDetails` — Courier summary for cross-service queries
- `CourierAvailability` — Availability status DTO
- `DeliveryActionDto` — Delivery action representation without JPA annotations

## Migration Plan

### Phase 1: Extract Shared Libraries (Current — EM-31)
- [x] Extract `ftgo-common` as versioned library (EM-32)
- [x] Extract `ftgo-common-jpa` as versioned library with ORM mappings
- [x] Extract `ftgo-domain` as versioned library with all entities and repositories
- [x] Document entity-to-service ownership mapping
- [x] Define cross-service DTO/API contracts

### Phase 2: Service-Owned Entity Migration
Each service gradually takes ownership of its entities:

1. **Copy entities** into service-owned modules (e.g., `services/ftgo-order-service/src/.../domain/`)
2. **Update package** from `com.ftgo.domain` to `com.ftgo.order.domain` (service-specific)
3. **Remove cross-entity references** (e.g., `Order.restaurant` → `Order.restaurantId`)
4. **Replace direct entity sharing** with API/DTO contracts from service API modules
5. **Add service-owned Flyway migrations** for schema changes

### Phase 3: Database Decomposition
Each service gets its own database schema:

1. **Split shared `ftgo` database** into per-service schemas
2. **Remove foreign keys** across service boundaries
3. **Implement eventual consistency** via events/sagas for cross-service data
4. **Deprecate `ftgo-domain`** shared library once all services own their entities

### Key Considerations
- **Transitional Period**: During migration, services depend on `ftgo-domain` for shared entities
- **Breaking Changes**: Coordinate entity changes across all consuming services
- **Data Integrity**: Foreign keys (orders→restaurants, courier_actions→orders) must be replaced with eventual consistency patterns
- **Testing**: Integration tests must verify both shared library and service-owned entity compatibility

## Dependencies

This library depends on:
- `ftgo-common` (1.0.0-SNAPSHOT) — shared value objects (Money, Address, PersonName)
- `ftgo-common-jpa` (1.0.0-SNAPSHOT) — JPA ORM mappings for common value objects
- Spring Boot Starter Data JPA — JPA/Hibernate support
- Apache Commons Lang — builder utilities

## Building

```bash
# Compile + run tests
./gradlew :shared:ftgo-domain:build

# Publish to local Maven repo (~/.m2)
./gradlew :shared:ftgo-domain:publishToMavenLocal

# Publish to project-local repo (build/repo)
./gradlew :shared:ftgo-domain:publish
```

## Versioning

This library follows [Semantic Versioning](https://semver.org/):

- **MAJOR** — breaking API changes (entity removals, renamed fields)
- **MINOR** — new entities/methods, backward-compatible
- **PATCH** — backward-compatible bug fixes

Current version is managed in `build.gradle` (`version = '1.0.0-SNAPSHOT'`) and mirrored in `gradle.properties`.

## Package Structure

```
com.ftgo.domain
├── entities/
│   ├── Order.java
│   ├── Consumer.java
│   ├── Restaurant.java
│   └── Courier.java
├── value-objects/
│   ├── OrderLineItem.java
│   ├── OrderLineItems.java
│   ├── MenuItem.java
│   ├── RestaurantMenu.java
│   ├── DeliveryInformation.java
│   ├── PaymentInformation.java
│   ├── Plan.java
│   ├── Action.java
│   ├── OrderRevision.java
│   └── LineItemQuantityChange.java
├── enums/
│   ├── OrderState.java
│   └── ActionType.java
├── exceptions/
│   └── OrderMinimumNotMetException.java
├── repositories/
│   ├── OrderRepository.java
│   ├── ConsumerRepository.java
│   ├── RestaurantRepository.java
│   └── CourierRepository.java
└── config/
    └── DomainConfiguration.java
```

## Database Schema

All entities map to tables in the shared `ftgo` MySQL database:

| Table | Entity | Owner Service |
|-------|--------|--------------|
| `consumers` | Consumer | Consumer Service |
| `restaurants` | Restaurant | Restaurant Service |
| `restaurant_menu_items` | MenuItem (collection) | Restaurant Service |
| `orders` | Order | Order Service |
| `order_line_items` | OrderLineItem (collection) | Order Service |
| `courier` | Courier | Courier Service |
| `courier_actions` | Action (collection) | Courier Service |

### Foreign Keys (to be decomposed)
- `orders.restaurant_id` → `restaurants.id`
- `orders.assigned_courier_id` → `courier.id`
- `courier_actions.order_id` → `orders.id`
