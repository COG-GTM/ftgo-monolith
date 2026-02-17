# FTGO Microservices Architecture Design

## 1. Current Architecture Analysis

### 1.1 Monolith Structure

The FTGO application is a modular monolith built with Spring Boot 2.0.3, deployed as a single JAR with all four business domains:

```
ftgo-application (Main Entry Point)
├── ftgo-order-service      (Order lifecycle management)
├── ftgo-consumer-service   (Consumer profile and validation)
├── ftgo-restaurant-service (Restaurant and menu management)
├── ftgo-courier-service    (Delivery scheduling and courier management)
├── ftgo-domain             (Shared JPA entities)
├── ftgo-common             (Shared value objects: Money, Address, PersonName)
└── ftgo-common-jpa         (JPA utilities)
```

### 1.2 Coupling Issues

| Issue | Location | Impact |
|-------|----------|--------|
| Direct method call | `OrderService` calls `ConsumerService.validateOrderForConsumer()` | Tight coupling between Order and Consumer domains |
| Shared repository | `OrderService` uses `RestaurantRepository` directly | Order domain depends on Restaurant data access |
| Shared repository | `OrderService` uses `CourierRepository` directly | Order domain depends on Courier data access |
| Compile-time dependency | `ftgo-order-service/build.gradle` depends on `ftgo-consumer-service` | Cannot deploy independently |
| Shared database | All services share MySQL `ftgo` database | Single point of failure, schema coupling |
| Foreign key constraints | `orders.restaurant_id` -> `restaurants.id`, `orders.assigned_courier_id` -> `courier.id` | Cross-domain data integrity enforced at DB level |
| Shared entity model | All entities in `ftgo-domain` module | Changes to one domain entity affect all services |

### 1.3 Current Strengths

- Modular configuration (`@Configuration` per service)
- Clean repository interfaces (`CrudRepository`)
- Value objects pattern (`@Embeddable` Money, Address)
- Micrometer metrics with Prometheus
- Spring Boot Actuator health checks
- Flyway database migrations

## 2. Target Architecture

### 2.1 Service Decomposition

```
                    ┌──────────────────┐
                    │   API Gateway    │
                    │  (Spring Cloud   │
                    │    Gateway)      │
                    └────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼──┐  ┌───────▼────┐  ┌──────▼───────┐  ┌──────────────┐
    │   Order    │  │  Consumer  │  │  Restaurant  │  │   Delivery   │
    │  Service   │  │  Service   │  │   Service    │  │   Service    │
    └─────┬──────┘  └─────┬──────┘  └──────┬───────┘  └──────┬───────┘
          │               │                │                  │
    ┌─────▼──────┐  ┌─────▼──────┐  ┌──────▼───────┐  ┌──────▼───────┐
    │  Order DB  │  │ Consumer DB│  │Restaurant DB │  │ Delivery DB  │
    │  orders    │  │ consumers  │  │ restaurants  │  │   courier    │
    │ line_items │  │            │  │  menu_items  │  │courier_action│
    └────────────┘  └────────────┘  └──────────────┘  └──────────────┘
          │               │                │                  │
          └───────────────┴────────┬───────┴──────────────────┘
                                   │
                          ┌────────▼────────┐
                          │  Apache Kafka   │
                          │  (Event Bus)    │
                          └─────────────────┘
```

### 2.2 Database Ownership

| Service | Tables Owned | Current Foreign Keys to Remove |
|---------|-------------|-------------------------------|
| Order Service | `orders`, `order_line_items` | `orders.restaurant_id` (replace with ID field), `orders.assigned_courier_id` (replace with ID field) |
| Consumer Service | `consumers` | None |
| Restaurant Service | `restaurants`, `restaurant_menu_items` | None |
| Delivery Service | `courier`, `courier_actions` | `courier_actions.order_id` (replace with ID field) |

### 2.3 Communication Patterns

#### Synchronous (Query Path)
- API Gateway routes requests to individual services
- Services use REST clients with circuit breakers for cross-service queries
- Used for: fetching restaurant details for order display, consumer profile lookup

#### Asynchronous (Command Path - Saga)
- Order creation uses the Saga pattern via Kafka events
- Each step is a compensatable transaction
- Used for: order creation flow, order cancellation, delivery scheduling

## 3. Event-Driven Communication Design

### 3.1 Domain Events

```
OrderCreated          -> Consumer Service (validate), Restaurant Service (confirm)
ConsumerValidated     -> Order Service (proceed with order)
ConsumerValidationFailed -> Order Service (reject order)
OrderConfirmed        -> Delivery Service (schedule delivery)
OrderCancelled        -> Delivery Service (cancel delivery)
CourierAssigned       -> Order Service (update order with courier)
DeliveryScheduled     -> Order Service (update delivery info)
RestaurantMenuRevised -> Order Service (update cached menu data)
```

### 3.2 Outbox Pattern

To ensure reliable event publishing with exactly-once semantics:

1. Service writes domain event to `outbox_events` table in the same transaction as the business operation
2. A background relay process polls the outbox table and publishes events to Kafka
3. After successful publishing, the relay marks events as published

```sql
CREATE TABLE outbox_events (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        TEXT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMP NULL,
    published      BOOLEAN DEFAULT FALSE
);
```

### 3.3 Kafka Topic Design

| Topic | Producer | Consumers | Key |
|-------|----------|-----------|-----|
| `ftgo.order.events` | Order Service | Consumer, Restaurant, Delivery | `orderId` |
| `ftgo.consumer.events` | Consumer Service | Order Service | `consumerId` |
| `ftgo.restaurant.events` | Restaurant Service | Order Service | `restaurantId` |
| `ftgo.delivery.events` | Delivery Service | Order Service | `courierId` |

### 3.4 Order Creation Saga

```
Step 1: Order Service   -> Create Order (PENDING) + publish OrderCreated
Step 2: Consumer Service -> Validate consumer + publish ConsumerValidated/Failed
Step 3: Order Service   -> If validated, confirm order + publish OrderConfirmed
                        -> If failed, reject order + publish OrderRejected
Step 4: Delivery Service -> Schedule delivery + publish DeliveryScheduled
Step 5: Order Service   -> Update with courier assignment, set APPROVED
```

## 4. Service Discovery and API Gateway

### 4.1 Service Discovery (Eureka)

- Eureka Server runs as standalone Spring Boot application
- Each microservice registers as Eureka client
- During migration: monolith registers all service endpoints
- After migration: each microservice registers independently

### 4.2 API Gateway (Spring Cloud Gateway)

Route Configuration Strategy:
```
/api/orders/**     -> order-service
/api/consumers/**  -> consumer-service
/api/restaurants/**-> restaurant-service
/api/couriers/**   -> delivery-service
/actuator/**       -> each service individually
```

Gateway Features:
- JWT token validation at gateway level
- Rate limiting per client
- CORS configuration
- Request/response logging
- Circuit breaker per route
- Load balancing via Eureka

### 4.3 Migration Routing (Strangler Fig)

During migration, the gateway routes to both monolith and new microservices:
```
Phase 1: All routes -> Monolith
Phase 2: /api/consumers/** -> Consumer Microservice, rest -> Monolith
Phase 3: /api/restaurants/** -> Restaurant Microservice, rest -> Monolith
Phase 4: /api/couriers/** -> Delivery Microservice, rest -> Monolith
Phase 5: /api/orders/** -> Order Microservice (last due to most dependencies)
```

## 5. Security Architecture

### 5.1 Authentication Flow

```
Client -> API Gateway -> JWT Validation -> Service
                │
        ┌───────▼──────┐
        │  JWT Token    │
        │  Validation   │
        │  (Gateway)    │
        └──────────────┘
```

### 5.2 JWT Token Structure

```json
{
  "sub": "user-id",
  "iss": "ftgo-auth",
  "exp": 1234567890,
  "roles": ["CONSUMER", "ADMIN"],
  "consumerId": 12345
}
```

### 5.3 Security Layers

1. **API Gateway**: Validates JWT, extracts user context, adds headers
2. **Service Level**: Verifies service-to-service tokens, authorizes operations
3. **Inter-Service**: Services use internal JWT tokens for service-to-service calls

### 5.4 Role-Based Access Control

| Role | Permissions |
|------|------------|
| CONSUMER | Create/view own orders, view restaurants |
| RESTAURANT_OWNER | Manage own restaurant, accept/prepare orders |
| COURIER | View/accept deliveries, update delivery status |
| ADMIN | Full access to all operations |

## 6. Observability Strategy

### 6.1 Distributed Tracing (Sleuth + Zipkin)

- Spring Cloud Sleuth auto-instruments HTTP requests, Kafka messages
- Trace IDs propagated via HTTP headers (`X-B3-TraceId`, `X-B3-SpanId`)
- Zipkin collects and visualizes traces
- Sampling rate: 100% in dev/staging, 10% in production

### 6.2 Metrics (Micrometer + Prometheus)

Existing metrics enhanced with:
- Service-level RED metrics (Rate, Errors, Duration)
- Circuit breaker state metrics
- Kafka consumer lag metrics
- JVM and connection pool metrics
- Custom business metrics (orders per minute, delivery time)

### 6.3 Centralized Logging

- Structured JSON logging format
- Correlation IDs in all log entries (trace ID, span ID)
- Log aggregation via ELK stack or similar
- Standard log fields: timestamp, service, traceId, spanId, level, message

## 7. Resilience Patterns

### 7.1 Circuit Breaker (Resilience4j)

Configuration per service call:
```
failureRateThreshold: 50%
waitDurationInOpenState: 60s
slidingWindowSize: 10
minimumNumberOfCalls: 5
```

### 7.2 Retry Strategy

```
maxAttempts: 3
waitDuration: 500ms
retryOn: IOException, TimeoutException
ignoreExceptions: BusinessException
```

### 7.3 Timeout Strategy

| Call Type | Timeout |
|-----------|---------|
| HTTP inter-service | 3 seconds |
| Database query | 5 seconds |
| Kafka publish | 10 seconds |
| Saga step | 30 seconds |

### 7.4 Bulkhead Pattern

- Thread pool isolation for cross-service calls
- Separate thread pools per downstream service
- Prevents cascade failures

### 7.5 Fallback Mechanisms

| Service Call | Fallback |
|-------------|----------|
| Consumer validation | Cache last known validation result |
| Restaurant menu fetch | Return cached menu |
| Courier assignment | Queue for retry |
| Order status query | Return last known state |

## 8. Infrastructure Components Summary

| Component | Technology | Module |
|-----------|-----------|--------|
| Message Broker | Apache Kafka | `ftgo-event-infrastructure` |
| Service Discovery | Netflix Eureka | `ftgo-service-discovery` |
| API Gateway | Spring Cloud Gateway | `ftgo-api-gateway` |
| Authentication | Spring Security + JWT | `ftgo-security` |
| Distributed Tracing | Sleuth + Zipkin | `ftgo-observability` |
| Metrics | Micrometer + Prometheus | Already exists, enhanced |
| Circuit Breaker | Resilience4j | `ftgo-resilience` |
| Database Migration | Flyway | Already exists, enhanced |
| Configuration | Spring Cloud Config | Future phase |
