# ADR-0002: Domain Entity Ownership and Migration Plan

## Status

Proposed

## Date

2026-03-17

## Context

The `ftgo-domain` module contains 22 Java files with shared JPA entities and repositories that are currently used across all four microservices via direct project dependencies. In a proper microservices architecture, each service should own its domain entities. This ADR documents the entity-to-service ownership mapping and the migration plan for moving entities from the shared `ftgo-domain` library into service-owned modules.

### Current State

All domain entities live in a single shared module (`ftgo-domain`) under the package `net.chrisrichardson.ftgo.domain`. Services depend on this module directly, creating tight coupling between services through shared entities.

### Database Schema

All entities map to tables in a single `ftgo` MySQL database:

- `consumers` - Consumer entity
- `courier` - Courier entity
- `courier_actions` - Courier delivery actions (embedded in Courier via Plan)
- `orders` - Order entity
- `order_line_items` - Order line items (embedded in Order)
- `restaurants` - Restaurant entity
- `restaurant_menu_items` - Restaurant menu items (embedded in Restaurant)

Foreign key relationships exist between:
- `orders` -> `restaurants` (Order.restaurant)
- `orders` -> `courier` (Order.assignedCourier)
- `courier_actions` -> `orders` (Action.order)

### Decision Drivers

- Services should own their domain entities (Domain-Driven Design bounded contexts)
- Cross-service entity references must be replaced with DTOs/API contracts
- Migration must be incremental to avoid breaking changes
- Existing integration tests must continue to compile

## Decision

### 1. Entity-to-Service Ownership Mapping

#### Consumer Service

Owns the Consumer bounded context.

| Type | Class | Description |
|------|-------|-------------|
| Entity | `Consumer` | Consumer entity with PersonName |
| Repository | `ConsumerRepository` | CRUD + custom queries for consumers |

#### Restaurant Service

Owns the Restaurant bounded context.

| Type | Class | Description |
|------|-------|-------------|
| Entity | `Restaurant` | Restaurant entity with menu and address |
| Value Object | `RestaurantMenu` | Menu collection wrapper |
| Value Object | `MenuItem` | Individual menu item (id, name, price) |
| Repository | `RestaurantRepository` | CRUD for restaurants |

#### Order Service

Owns the Order bounded context. This is the largest bounded context.

| Type | Class | Description |
|------|-------|-------------|
| Entity | `Order` | Core order entity with state machine |
| Value Object | `OrderLineItem` | Line item with quantity, name, price |
| Value Object | `OrderLineItems` | Collection wrapper for line items |
| Value Object | `DeliveryInformation` | Delivery time and address |
| Value Object | `PaymentInformation` | Payment token |
| Enum | `OrderState` | Order lifecycle states |
| Domain Class | `OrderRevision` | Order revision tracking |
| Domain Class | `LineItemQuantityChange` | Tracks total changes during revision |
| Exception | `OrderMinimumNotMetException` | Order minimum validation |
| Repository | `OrderRepository` | CRUD + find by consumer |

#### Courier Service

Owns the Courier/Delivery bounded context.

| Type | Class | Description |
|------|-------|-------------|
| Entity | `Courier` | Courier entity with availability and plan |
| Value Object | `Plan` | Delivery plan with ordered actions |
| Value Object | `Action` | Individual pickup/dropoff action |
| Enum | `ActionType` | PICKUP, DROPOFF |
| Repository | `CourierRepository` | CRUD + find available couriers |

#### Shared Configuration

| Type | Class | Description |
|------|-------|-------------|
| Configuration | `DomainConfiguration` | Spring config (will be split per service) |

### 2. Cross-Entity Dependencies (Require DTOs)

The following entity relationships cross bounded context boundaries and must be replaced with DTOs or ID references:

| From Entity | To Entity | Relationship | Resolution |
|------------|-----------|--------------|------------|
| `Order.restaurant` | `Restaurant` | `@ManyToOne` | Replace with `restaurantId` (Long) + `RestaurantDTO` for read |
| `Order.assignedCourier` | `Courier` | `@ManyToOne` | Replace with `courierId` (Long) + `CourierDTO` for read |
| `Action.order` | `Order` | `@ManyToOne` | Replace with `orderId` (Long) + `OrderDTO` for read |

### 3. Cross-Service DTO/API Contracts

Each bounded context defines DTOs for cross-service communication:

#### Consumer API DTOs (`services/consumer-service-api`)

| DTO | Fields | Used By |
|-----|--------|---------|
| `ConsumerDTO` | id, firstName, lastName | Order Service (order validation) |
| `ValidateConsumerRequest` | consumerId, orderTotal | Order Service |
| `ValidateConsumerResponse` | valid, reason | Order Service |

#### Restaurant API DTOs (`services/restaurant-service-api`)

| DTO | Fields | Used By |
|-----|--------|---------|
| `RestaurantDTO` | id, name, address | Order Service (order display) |
| `MenuItemDTO` | id, name, price | Order Service (order creation) |
| `RestaurantMenuDTO` | restaurantId, menuItems | Order Service |

#### Order API DTOs (`services/order-service-api`)

| DTO | Fields | Used By |
|-----|--------|---------|
| `OrderDTO` | id, state, consumerId, restaurantId, lineItems, total | Courier Service, Consumer Service |
| `OrderLineItemDTO` | menuItemId, name, price, quantity | Restaurant Service |
| `OrderStateDTO` | state (String enum) | All services |
| `CreateOrderRequest` | consumerId, restaurantId, lineItems, deliveryAddress, deliveryTime | API Gateway |
| `CreateOrderResponse` | orderId | API Gateway |
| `GetOrderResponse` | order details + state | API Gateway |

#### Courier API DTOs (`services/courier-service-api`)

| DTO | Fields | Used By |
|-----|--------|---------|
| `CourierDTO` | id, name, available | Order Service (delivery scheduling) |
| `DeliveryActionDTO` | type, orderId, time | Order Service |
| `CourierAvailabilityDTO` | courierId, available | Dispatch Service |

### 4. Migration Phases

#### Phase A: Extract as Shared Library (Current - EM-31)

- Extract `ftgo-domain` and `ftgo-common-jpa` as versioned shared libraries
- All services continue to depend on the shared domain library
- No entity ownership changes yet
- Document ownership mapping (this ADR)

#### Phase B: Create Service-Owned Entity Copies

For each service:
1. Copy owned entities from `ftgo-domain` into the service's `domain` package
2. Replace cross-boundary entity references with ID fields (Long)
3. Create DTOs in the service's `-api` module for cross-service communication
4. Update the service's `build.gradle` to remove `ftgo-domain` dependency
5. Add REST client calls or event handlers for cross-service data needs

**Order of migration** (least to most coupled):
1. Consumer Service (fewest dependencies)
2. Restaurant Service (only referenced by Order)
3. Courier Service (references Order via Action)
4. Order Service (most complex, references Restaurant and Courier)

#### Phase C: Database Decomposition

1. Each service gets its own database schema/instance
2. Foreign keys between service-owned tables are removed
3. Data consistency is maintained via:
   - Saga pattern for distributed transactions
   - Event-driven synchronization for read models
   - API calls for real-time cross-service queries

#### Phase D: Remove Shared Domain Library

1. Once all services own their entities, deprecate `ftgo-domain`
2. Remove the shared library from the build
3. Each service is fully independent

## Consequences

### Positive

- **Clear ownership**: Each service team knows exactly which entities they maintain
- **Incremental migration**: Services can be migrated one at a time
- **Backward compatible**: Shared library remains available during transition
- **Well-defined contracts**: DTOs establish clear API boundaries between services
- **Database independence**: Path to per-service databases is documented

### Negative

- **Code duplication**: Entity classes will temporarily exist in both shared library and service modules
- **DTO mapping overhead**: Services must map between entities and DTOs
- **Complexity**: Cross-service queries require API calls instead of JPA joins

### Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| Inconsistent entity copies across services | Automated comparison tests during migration |
| Breaking changes during entity moves | Feature flags to switch between shared and local entities |
| Data inconsistency after DB split | Saga pattern + eventual consistency patterns |
| Performance degradation from API calls | Caching, read replicas, CQRS patterns |

## References

- [Microservices Patterns - Chapter 2: Decomposition](https://microservices.io/book)
- [Domain-Driven Design - Bounded Contexts](https://martinfowler.com/bliki/BoundedContext.html)
- [Database per Service pattern](https://microservices.io/patterns/data/database-per-service.html)
- [Saga pattern](https://microservices.io/patterns/data/saga.html)
- ADR-0001: Microservices Repository Structure
