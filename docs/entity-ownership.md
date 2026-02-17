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

| Library | Gradle Path | Version | Contents | Used By |
|---------|-------------|---------|----------|---------|
| `ftgo-common-jpa` | `:libs:ftgo-common-jpa` | 1.0.0 | `Money` (JPA embeddable), `FtgoJpaConfiguration`, `orm.xml` | All services |
| `ftgo-domain` | `:libs:ftgo-domain` | 1.0.0 | All domain entities, enums, repositories, exceptions | All services (transitional) |
| `ftgo-common-lib` | `:libs:ftgo-common-lib` | 1.0.0-SNAPSHOT | Common utilities, error handling, `Money` (serialization) | All services |
| `ftgo-common-jpa-lib` | `:libs:ftgo-common-jpa-lib` | 1.0.0 | `Money`, `FtgoJpaConfiguration` (legacy duplicate) | Deprecated in favor of `:libs:ftgo-common-jpa` |
| `ftgo-domain-lib` | `:libs:ftgo-domain-lib` | 1.0.0 | Domain entities (legacy duplicate) | Deprecated in favor of `:libs:ftgo-domain` |

## API/DTO Modules

| Module | Gradle Path | Version | Contents | Used By |
|--------|-------------|---------|----------|---------|
| `ftgo-order-service-api` | `:apis:ftgo-order-service-api` | 1.0.0 | `CreateOrderRequest`, `CreateOrderResponse`, `OrderDetails`, DTOs | consumer-service, restaurant-service |
| `ftgo-consumer-service-api` | `:apis:ftgo-consumer-service-api` | 1.0.0 | `CreateConsumerRequest`, `CreateConsumerResponse` | order-service |
| `ftgo-restaurant-service-api` | `:apis:ftgo-restaurant-service-api` | 1.0.0 | `CreateRestaurantRequest`, `MenuItemDTO`, `RestaurantMenuDTO` | order-service |
| `ftgo-courier-service-api` | `:apis:ftgo-courier-service-api` | 1.0.0 | `CreateCourierRequest`, `CreateCourierResponse`, `CourierAvailability` | order-service |

## Migration Notes

- During migration, `ftgo-domain` (`:libs:ftgo-domain`) contains all entities in a single package. As services mature, each service should own its entities directly and `:libs:ftgo-domain` will be deprecated.
- Cross-service references (e.g., Order references Consumer by ID) should use ID-based references, not JPA relationships.
- The `DomainConfiguration` class provides Spring component scanning for the domain package.
- API/DTO modules under `apis/` define the cross-service communication contracts. Services should depend on these API modules instead of directly depending on other services' domain models.
- The versioned libraries under `libs/` (`:libs:ftgo-common-jpa`, `:libs:ftgo-domain`) are the canonical versions. The legacy `-lib` variants will be removed once all consumers migrate.
