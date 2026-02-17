# FTGO Entity-to-Service Ownership Mapping

This document defines which microservice owns each domain entity extracted from the monolith. Ownership determines which service has write authority over the entity's data and schema.

## Ownership Rules

1. **Single owner**: Each entity has exactly one owning service
2. **Read replicas**: Other services may hold read-only projections via events
3. **Shared value objects**: Types in `ftgo-common-jpa-lib` (e.g., `Money`) have no owner — they are shared infrastructure

## Entity Ownership

| Entity | Owner Service | Database | Notes |
|--------|--------------|----------|-------|
| `Order` | order-service | ftgo_order_db | Full lifecycle: create, approve, revise, cancel |
| `OrderLineItem` | order-service | ftgo_order_db | Child of Order aggregate |
| `OrderLineItems` | order-service | ftgo_order_db | Value object within Order |
| `OrderRevision` | order-service | ftgo_order_db | Value object for order changes |
| `Consumer` | consumer-service | ftgo_consumer_db | Consumer registration and validation |
| `Restaurant` | restaurant-service | ftgo_restaurant_db | Restaurant profile and menus |
| `RestaurantMenu` | restaurant-service | ftgo_restaurant_db | Child of Restaurant aggregate |
| `MenuItem` | restaurant-service | ftgo_restaurant_db | Child of RestaurantMenu |
| `Courier` | courier-service | ftgo_courier_db | Courier availability and assignments |
| `Plan` | courier-service | ftgo_courier_db | Delivery plan for courier |
| `Action` | courier-service | ftgo_courier_db | Delivery action within a plan |
| `DeliveryInformation` | order-service | ftgo_order_db | Embedded value object in Order |
| `PaymentInformation` | order-service | ftgo_order_db | Embedded value object in Order |

## Repository Ownership

| Repository Interface | Owner Service | Notes |
|---------------------|--------------|-------|
| `OrderRepository` | order-service | Spring Data JPA repository |
| `ConsumerRepository` | consumer-service | Spring Data JPA repository |
| `RestaurantRepository` | restaurant-service | Spring Data JPA repository |
| `CourierRepository` | courier-service | Spring Data JPA repository |

## Shared Libraries

| Library | Contents | Used By |
|---------|----------|---------|
| `ftgo-common-jpa-lib` | `Money`, `FtgoJpaConfiguration` | All services |
| `ftgo-domain-lib` | All domain entities, enums, exceptions | All services (transitional) |
| `ftgo-common-lib` | Common utilities and base classes | All services |

## Migration Notes

- During migration, `ftgo-domain-lib` contains all entities in a single package. As services mature, each service should own its entities directly and `ftgo-domain-lib` will be deprecated.
- Cross-service references (e.g., Order references Consumer by ID) should use ID-based references, not JPA relationships.
- The `DomainConfiguration` class provides Spring component scanning for the domain package.
