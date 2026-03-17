# Entity-to-Service Ownership Mapping

This document defines the ownership mapping of domain entities from `ftgo-domain` to their
target microservices. During the migration, the shared `ftgo-domain` library serves as a
transitional dependency. Each service will eventually own its entities directly.

## Ownership Matrix

### Consumer Service

| Entity/Class         | Type       | Target Module                     | Cross-Service DTO                |
|----------------------|------------|-----------------------------------|----------------------------------|
| `Consumer`           | Entity     | `services/consumer-service`       | `ConsumerDTO`                    |
| `ConsumerRepository` | Repository | `services/consumer-service`       | N/A (internal)                   |

**Database Tables:** `consumers`

### Restaurant Service

| Entity/Class         | Type        | Target Module                      | Cross-Service DTO                |
|----------------------|-------------|------------------------------------|----------------------------------|
| `Restaurant`         | Entity      | `services/restaurant-service`      | `RestaurantDTO`                  |
| `RestaurantMenu`     | Embeddable  | `services/restaurant-service`      | `RestaurantMenuDTO`              |
| `MenuItem`           | Embeddable  | `services/restaurant-service`      | `MenuItemDTO`                    |
| `RestaurantRepository` | Repository | `services/restaurant-service`     | N/A (internal)                   |

**Database Tables:** `restaurants`, `restaurant_menu_items`

### Order Service

| Entity/Class              | Type        | Target Module                  | Cross-Service DTO                |
|---------------------------|-------------|--------------------------------|----------------------------------|
| `Order`                   | Entity      | `services/order-service`       | `GetOrderResponse`               |
| `OrderLineItem`           | Embeddable  | `services/order-service`       | `OrderLineItemDTO`               |
| `OrderLineItems`          | Embeddable  | `services/order-service`       | N/A (internal aggregate)         |
| `OrderRepository`         | Repository  | `services/order-service`       | N/A (internal)                   |
| `OrderState`              | Enum        | `services/order-service`       | String in DTOs                   |
| `OrderRevision`           | Value Object| `services/order-service`       | `ReviseOrderRequest`             |
| `LineItemQuantityChange`  | Value Object| `services/order-service`       | N/A (internal)                   |
| `OrderMinimumNotMetException` | Exception | `services/order-service`    | N/A (internal)                   |
| `DeliveryInformation`     | Embeddable  | `services/order-service`       | N/A (internal)                   |
| `PaymentInformation`      | Embeddable  | `services/order-service`       | N/A (internal)                   |

**Database Tables:** `orders`, `order_line_items`

### Courier Service

| Entity/Class         | Type        | Target Module                    | Cross-Service DTO                |
|----------------------|-------------|----------------------------------|----------------------------------|
| `Courier`            | Entity      | `services/courier-service`       | `CourierDTO`                     |
| `Plan`               | Embeddable  | `services/courier-service`       | N/A (internal)                   |
| `Action`             | Embeddable  | `services/courier-service`       | N/A (internal)                   |
| `ActionType`         | Enum        | `services/courier-service`       | N/A (internal)                   |
| `CourierRepository`  | Repository  | `services/courier-service`       | N/A (internal)                   |

**Database Tables:** `courier`, `courier_actions`

### Shared Configuration

| Class                  | Type          | Current Location    | Migration Target                  |
|------------------------|---------------|---------------------|-----------------------------------|
| `DomainConfiguration`  | Configuration | `ftgo-domain`       | Split per service or removed      |

## Cross-Service Communication Contracts

Each service exposes API contracts via its `-api` module under `services/`:

| Service API Module            | Contracts                                                        |
|-------------------------------|------------------------------------------------------------------|
| `consumer-service-api`        | `CreateConsumerRequest`, `CreateConsumerResponse`, `ConsumerDTO`  |
| `restaurant-service-api`      | `CreateRestaurantRequest`, `RestaurantDTO`, `MenuItemDTO`, `RestaurantMenuDTO`, `RestaurantCreatedEvent` |
| `order-service-api`           | `CreateOrderRequest`, `CreateOrderResponse`, `GetOrderResponse`, `ReviseOrderRequest`, `OrderLineItemDTO`, `OrderStateChangeEvent` |
| `courier-service-api`         | `CreateCourierRequest`, `CreateCourierResponse`, `CourierDTO`, `CourierAvailability` |

## Key Design Decisions

1. **DTOs use primitive IDs** (long) for cross-service references instead of entity objects
2. **OrderState** is transmitted as a String in DTOs to avoid coupling to the enum
3. **Events** use dedicated event DTOs (e.g., `OrderStateChangeEvent`, `RestaurantCreatedEvent`) for async communication
4. **Repositories** are always internal to the owning service and never exposed via API modules
