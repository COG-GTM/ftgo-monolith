# Entity-to-Service Ownership Mapping

This document maps each JPA entity and repository from the monolith's `ftgo-domain` module
to the microservice that will own it after decomposition.

## Overview

During the monolith-to-microservices migration, each domain entity is assigned to exactly one
owning service. The owning service is the single source of truth for that entity's data and
lifecycle. Other services that need this data will access it via APIs or events, not by sharing
the database directly.

## Entity Ownership

| Entity | Package | Owning Service | Description |
|--------|---------|----------------|-------------|
| `Order` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Core order aggregate root with state machine |
| `OrderLineItem` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Embeddable line item within an order |
| `OrderLineItems` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Embeddable collection wrapper for line items |
| `OrderState` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Order lifecycle state enum |
| `OrderRevision` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Value object for order modification requests |
| `LineItemQuantityChange` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Value object tracking quantity change deltas |
| `OrderMinimumNotMetException` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Domain exception for minimum order validation |
| `DeliveryInformation` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Embeddable delivery address and time |
| `PaymentInformation` | `net.chrisrichardson.ftgo.domain` | **Order Service** | Embeddable payment details |
| `Consumer` | `net.chrisrichardson.ftgo.domain` | **Consumer Service** | Consumer aggregate root |
| `Restaurant` | `net.chrisrichardson.ftgo.domain` | **Restaurant Service** | Restaurant aggregate root with menu |
| `MenuItem` | `net.chrisrichardson.ftgo.domain` | **Restaurant Service** | Embeddable menu item with price |
| `RestaurantMenu` | `net.chrisrichardson.ftgo.domain` | **Restaurant Service** | Embeddable menu containing menu items |
| `Courier` | `net.chrisrichardson.ftgo.domain` | **Courier Service** | Courier aggregate root with availability |
| `Plan` | `net.chrisrichardson.ftgo.domain` | **Courier Service** | Courier delivery plan (list of actions) |
| `Action` | `net.chrisrichardson.ftgo.domain` | **Courier Service** | Embeddable pickup/dropoff action |
| `ActionType` | `net.chrisrichardson.ftgo.domain` | **Courier Service** | Enum: PICKUP, DROPOFF |

## Repository Ownership

| Repository | Owning Service | Entity |
|------------|----------------|--------|
| `OrderRepository` | **Order Service** | `Order` |
| `ConsumerRepository` | **Consumer Service** | `Consumer` |
| `RestaurantRepository` | **Restaurant Service** | `Restaurant` |
| `CourierRepository` | **Courier Service** | `Courier` |

## Configuration

| Class | Description | Migration Notes |
|-------|-------------|-----------------|
| `DomainConfiguration` | Spring Boot auto-configuration with `@EntityScan`, `@EnableJpaRepositories`, `@Import(CommonConfiguration.class)` | Each service will define its own configuration importing only the entities it owns |

## Cross-Service Dependencies

The following cross-entity references exist in the monolith and will need to be
resolved during service extraction:

| Source Entity | Target Entity | Relationship | Resolution Strategy |
|---------------|---------------|-------------|---------------------|
| `Order` | `Restaurant` | `@ManyToOne` | Replace with `restaurantId` (Long) + API lookup |
| `Order` | `Courier` | `@ManyToOne` | Replace with `courierId` (Long) + API lookup |
| `Action` | `Order` | `@ManyToOne` | Replace with `orderId` (Long) + API lookup |

## API/DTO Modules for Cross-Service Communication

Each service has a corresponding API module under `shared/` containing DTOs
for cross-service communication:

| API Module | Service | DTOs |
|------------|---------|------|
| `shared/ftgo-order-service-api` | Order Service | `CreateOrderRequest`, `CreateOrderResponse`, `OrderAcceptance`, `ReviseOrderRequest`, `OrderDetails`, `OrderLineItemDTO` |
| `shared/ftgo-consumer-service-api` | Consumer Service | `CreateConsumerRequest`, `CreateConsumerResponse` |
| `shared/ftgo-restaurant-service-api` | Restaurant Service | `CreateRestaurantRequest`, `MenuItemDTO`, `RestaurantMenuDTO` |
| `shared/ftgo-courier-service-api` | Courier Service | `CourierAvailability`, `CreateCourierRequest`, `CreateCourierResponse` |

## Migration Strategy

1. **Phase 1 (Current):** Extract entities into `shared/ftgo-domain` as a shared library.
   All services depend on this library during the transition period.

2. **Phase 2:** Each service copies its owned entities into its own domain package,
   replacing cross-entity JPA references with ID-based references and API calls.

3. **Phase 3:** Remove the shared `ftgo-domain` library once all services own
   their entities independently.
