# Cross-Service Dependencies

This document maps out which services need to call which other services,
based on the current direct method calls and repository access patterns
in the FTGO monolith codebase. These dependencies represent the integration
points that must become API calls when services are extracted into
separate microservices.

## Dependency Map

### Order Service

The Order Service has the most cross-service dependencies:

| Dependency | Current Implementation | Future API Contract |
|---|---|---|
| **Consumer Service** | `ConsumerService.validateOrderForConsumer(consumerId, orderTotal)` called during order creation | `POST /api/v1/consumers/validate-order` using `ValidateOrderForConsumerRequest` |
| **Restaurant Service** | `RestaurantRepository.findById(restaurantId)` called during order creation to look up menu items and validate the restaurant | `GET /api/v1/restaurants/{restaurantId}` using `GetRestaurantResponse` |
| **Courier Service** | `CourierRepository.findAllAvailable()` called during order acceptance to assign a courier for delivery scheduling | `GET /api/v1/couriers?available=true` (new endpoint needed in Phase 2) |

### Consumer Service

| Dependency | Current Implementation | Future API Contract |
|---|---|---|
| None | Consumer Service is self-contained | N/A |

### Restaurant Service

| Dependency | Current Implementation | Future API Contract |
|---|---|---|
| None | Restaurant Service is self-contained | N/A |

### Courier Service

| Dependency | Current Implementation | Future API Contract |
|---|---|---|
| None | Courier Service is self-contained | N/A |

## Dependency Direction Summary

```
Order Service --> Consumer Service   (validate consumer for order)
Order Service --> Restaurant Service (look up restaurant and menu)
Order Service --> Courier Service    (find available couriers, assign delivery)
```

## Direct Repository Access (Must Be Replaced)

These are cases where a service directly accesses another service's
repository. In a microservices architecture, these must become API calls:

1. **OrderService.createOrder()** - Uses `RestaurantRepository.findById()` directly
2. **OrderService.scheduleDelivery()** - Uses `CourierRepository.findAllAvailable()` directly
3. **OrderService.createOrder()** - Calls `ConsumerService.validateOrderForConsumer()` directly

## API Modules

Each service has a corresponding `-api` module containing DTOs:

| Service | API Module | DTOs |
|---|---|---|
| Order Service | `ftgo-order-service-api` | `CreateOrderRequest`, `CreateOrderResponse`, `GetOrderResponse`, `GetRestaurantResponse`, `ReviseOrderRequest`, `OrderAcceptance` |
| Consumer Service | `ftgo-consumer-service-api` | `CreateConsumerRequest`, `CreateConsumerResponse`, `GetConsumerResponse`, `ValidateOrderForConsumerRequest` |
| Restaurant Service | `ftgo-restaurant-service-api` | `CreateRestaurantRequest`, `CreateRestaurantResponse`, `GetRestaurantResponse`, `RestaurantMenuDTO`, `MenuItemDTO` |
| Courier Service | `ftgo-courier-service-api` | `CreateCourierRequest`, `CreateCourierResponse`, `GetCourierResponse`, `CourierAvailability` |

## Phase 2 Recommendations

1. Replace direct `ConsumerService` call with HTTP client using `ValidateOrderForConsumerRequest`
2. Replace direct `RestaurantRepository` access with HTTP client call to Restaurant Service API
3. Replace direct `CourierRepository` access with HTTP client call to Courier Service API
4. Add a new "find available couriers" endpoint to the Courier Service
5. Implement circuit breakers for all cross-service calls
6. Add retry logic and timeout configuration for inter-service communication
