# FTGO Monolith to Microservices Migration Guide

This document provides a comprehensive plan for decomposing the FTGO monolith into independent microservices using the **Strangler Fig Pattern**. It covers service boundaries, data ownership, communication patterns, migration sequencing, and database decomposition strategy.

---

## Table of Contents

1. [Service Boundaries and Responsibilities](#1-service-boundaries-and-responsibilities)
2. [Data Ownership Per Service](#2-data-ownership-per-service)
3. [Inter-Service Communication Patterns](#3-inter-service-communication-patterns)
4. [Migration Sequence (Strangler Fig Pattern)](#4-migration-sequence-strangler-fig-pattern)
5. [Database Decomposition Strategy](#5-database-decomposition-strategy)
6. [API Specs Reference](#6-api-specs-reference)

---

## 1. Service Boundaries and Responsibilities

The monolith is decomposed into four microservices aligned with domain-driven design (DDD) bounded contexts. Each service owns a single aggregate root and exposes a well-defined API.

### 1.1 Consumer Service

| Attribute | Details |
|---|---|
| **Aggregate Root** | `Consumer` |
| **Source Module** | `ftgo-consumer-service/`, `ftgo-consumer-service-api/` |
| **Responsibilities** | Consumer registration, profile management, order validation |
| **Key Classes** | `ConsumerService`, `ConsumerController`, `Consumer`, `ConsumerRepository` |

**Current monolith behavior**: The `ConsumerService.validateOrderForConsumer()` method is called directly (in-process) by `OrderService.createOrder()` to validate a consumer before creating an order.

**As a microservice**: Exposes a REST endpoint `POST /consumers/{consumerId}/validate-order` that the Order Service calls synchronously during order creation.

### 1.2 Restaurant Service

| Attribute | Details |
|---|---|
| **Aggregate Root** | `Restaurant` (with embedded `MenuItem` collection) |
| **Source Module** | `ftgo-restaurant-service/`, `ftgo-restaurant-service-api/` |
| **Responsibilities** | Restaurant registration, menu management, menu item validation |
| **Key Classes** | `RestaurantService`, `RestaurantController`, `Restaurant`, `RestaurantMenu`, `MenuItem`, `RestaurantRepository` |

**Current monolith behavior**: `OrderService.createOrder()` loads the `Restaurant` entity directly from the shared database to validate menu items and retrieve prices.

**As a microservice**: The Order Service either:
- Calls `POST /restaurants/{restaurantId}/menu/validate` synchronously, or
- Maintains a local read-only cache of restaurant menus, updated via `RestaurantMenuRevisedEvent` events

### 1.3 Order Service

| Attribute | Details |
|---|---|
| **Aggregate Root** | `Order` (with embedded `OrderLineItems`) |
| **Source Module** | `ftgo-order-service/`, `ftgo-order-service-api/` |
| **Responsibilities** | Order creation, state machine management (APPROVED → ACCEPTED → PREPARING → READY_FOR_PICKUP → PICKED_UP → DELIVERED), cancellation, revision, delivery scheduling coordination |
| **Key Classes** | `OrderService`, `OrderController`, `TicketController`, `Order`, `OrderLineItem`, `OrderState`, `OrderRepository` |

**Current monolith behavior**: The `OrderService` directly accesses `ConsumerService`, `RestaurantRepository`, `CourierRepository`, and `CourierAssignmentStrategy` within a single transaction.

**As a microservice**: Orchestrates order creation by making synchronous REST calls to Consumer Service and Restaurant Service for validation, and coordinates with Courier Service for delivery assignment when an order is accepted.

### 1.4 Courier Service

| Attribute | Details |
|---|---|
| **Aggregate Root** | `Courier` (with embedded `Plan` containing `Action` list) |
| **Source Module** | `ftgo-courier-service/`, `ftgo-courier-service-api/` |
| **Responsibilities** | Courier registration, availability management, real-time location tracking, delivery plan management, courier assignment strategy |
| **Key Classes** | `CourierService`, `CourierController`, `Courier`, `Plan`, `Action`, `CourierAssignmentStrategy`, `DistanceOptimizedCourierAssignmentStrategy`, `CourierRepository` |

**Current monolith behavior**: `OrderService.scheduleDelivery()` directly queries `CourierRepository.findAllAvailable()` and assigns a courier using the `CourierAssignmentStrategy`, then mutates the `Courier` entity's plan.

**As a microservice**: Exposes `GET /couriers/available` for the Order Service to find available couriers, and `POST /couriers/{courierId}/actions` for the Order Service to add delivery actions to a courier's plan.

---

## 2. Data Ownership Per Service

Each microservice owns its data exclusively. No other service may directly access another service's database.

### 2.1 Consumer Service Database

```
Table: consumers
├── id (PK, BIGINT AUTO_INCREMENT)
├── first_name (VARCHAR)
└── last_name (VARCHAR)
```

**Owned entities**: `Consumer`

### 2.2 Restaurant Service Database

```
Table: restaurants
├── id (PK, BIGINT AUTO_INCREMENT)
├── name (VARCHAR)
├── street1, street2, city, state, zip (VARCHAR) -- address
├── latitude, longitude (DOUBLE)               -- geolocation

Table: restaurant_menu_items
├── restaurant_id (FK → restaurants.id)
├── id (VARCHAR)           -- menu item ID
├── name (VARCHAR)
└── price (DECIMAL)        -- amount
```

**Owned entities**: `Restaurant`, `MenuItem`, `RestaurantMenu`

### 2.3 Order Service Database

```
Table: orders
├── id (PK, BIGINT AUTO_INCREMENT)
├── version (BIGINT)
├── order_state (VARCHAR)               -- enum: APPROVED, ACCEPTED, etc.
├── consumer_id (BIGINT)                -- reference (not FK) to Consumer Service
├── restaurant_id (BIGINT)              -- reference (not FK) to Restaurant Service
├── assigned_courier_id (BIGINT)        -- reference (not FK) to Courier Service
├── order_minimum (DECIMAL)
├── ready_by, accept_time, preparing_time, ready_for_pickup_time,
│   picked_up_time, delivered_time (DATETIME)
├── delivery_address_* (VARCHAR)        -- embedded delivery info
├── delivery_time (DATETIME)
└── order_line_items (embedded collection)
    ├── menu_item_id (VARCHAR)
    ├── name (VARCHAR)
    ├── price (DECIMAL)
    └── quantity (INT)
```

**Owned entities**: `Order`, `OrderLineItem`, `OrderLineItems`, `DeliveryInformation`, `PaymentInformation`

> **Important**: `consumer_id`, `restaurant_id`, and `assigned_courier_id` become **soft references** (not foreign keys) since those entities live in different databases. The Order Service stores denormalized copies of the restaurant name (for display) and courier info.

### 2.4 Courier Service Database

```
Table: courier
├── id (PK, BIGINT AUTO_INCREMENT)
├── first_name, last_name (VARCHAR)     -- name
├── street1, street2, city, state, zip (VARCHAR) -- address
├── latitude, longitude (DOUBLE)        -- address geolocation
├── available (BOOLEAN)
├── current_latitude, current_longitude (DOUBLE) -- live location
└── last_location_update (DATETIME)

Table: courier_actions (Plan)
├── courier_id (FK → courier.id)
├── type (VARCHAR)     -- PICKUP or DROPOFF
├── order_id (BIGINT)  -- reference (not FK) to Order Service
└── time (DATETIME)
```

**Owned entities**: `Courier`, `Plan`, `Action`

---

## 3. Inter-Service Communication Patterns

### 3.1 Synchronous REST (Request/Response)

Used for operations that require an immediate response to proceed:

| Caller | Callee | Endpoint | Purpose |
|---|---|---|---|
| Order Service | Consumer Service | `POST /consumers/{id}/validate-order` | Validate consumer can place order |
| Order Service | Restaurant Service | `POST /restaurants/{id}/menu/validate` | Validate menu items and get prices |
| Order Service | Courier Service | `GET /couriers/available` | Find available couriers for assignment |
| Order Service | Courier Service | `POST /couriers/{id}/actions` | Add pickup/dropoff actions to courier plan |
| Order Service | Courier Service | `DELETE /couriers/{id}/actions/{orderId}` | Remove delivery actions on cancellation |

**Resilience patterns to implement**:
- **Circuit breaker** (e.g., Resilience4j): Prevent cascading failures when downstream services are unavailable
- **Retry with exponential backoff**: Handle transient network failures
- **Timeout**: Prevent indefinite blocking on downstream calls
- **Fallback**: Degrade gracefully (e.g., if Consumer validation is unavailable, queue the order for async validation)

### 3.2 Asynchronous Events (Publish/Subscribe)

Used for eventual consistency and decoupling services:

| Publisher | Event | Subscribers | Purpose |
|---|---|---|---|
| Order Service | `OrderCreatedEvent` | Restaurant Service, Courier Service | Notify about new orders |
| Order Service | `OrderStateChangedEvent` | Consumer Service (notifications), Courier Service | Track order lifecycle |
| Order Service | `OrderCancelledEvent` | Restaurant Service, Courier Service | Cancel related tickets/deliveries |
| Consumer Service | `ConsumerCreatedEvent` | Order Service (cache) | Maintain consumer reference data |
| Restaurant Service | `RestaurantCreatedEvent` | Order Service (cache) | Cache restaurant data locally |
| Restaurant Service | `RestaurantMenuRevisedEvent` | Order Service (cache) | Update local menu cache |
| Courier Service | `CourierAvailabilityChangedEvent` | Order Service | Update courier pool awareness |
| Courier Service | `CourierLocationUpdatedEvent` | Order Service (tracking) | Real-time delivery tracking |
| Courier Service | `CourierDeliveryAssignedEvent` | Order Service | Confirm delivery assignment |

**Recommended message broker**: Apache Kafka or RabbitMQ

**Event envelope format**:
```json
{
  "eventId": "uuid",
  "eventType": "OrderCreatedEvent",
  "aggregateType": "Order",
  "aggregateId": 123,
  "timestamp": "2026-04-15T14:30:00Z",
  "payload": { ... }
}
```

### 3.3 Communication Pattern Decision Matrix

| Criteria | Use Sync REST | Use Async Events |
|---|---|---|
| Response needed to proceed? | Yes | No |
| Strong consistency required? | Yes | No (eventual consistency OK) |
| Caller needs confirmation? | Yes | No |
| Multiple subscribers? | No | Yes |
| High volume / real-time? | No | Yes (e.g., location updates) |

---

## 4. Migration Sequence (Strangler Fig Pattern)

The Strangler Fig Pattern incrementally extracts services from the monolith while keeping the system operational. Each phase introduces an API Gateway routing layer that directs traffic to either the monolith or the new microservice.

### Phase 0: Preparation (Week 1-2)

1. **Deploy an API Gateway** (e.g., Spring Cloud Gateway, Kong, or Envoy) in front of the monolith
2. **Route all traffic** through the gateway — initially 100% to the monolith
3. **Set up infrastructure**: Message broker (Kafka/RabbitMQ), service registry (Consul/Eureka), CI/CD pipelines
4. **Establish observability**: Distributed tracing (Jaeger/Zipkin), centralized logging (ELK), metrics (Prometheus/Grafana)
5. **Create shared libraries**: Common schemas (`Money`, `Address`, `PersonName`), HTTP client utilities, event publishing utilities

### Phase 1: Extract Consumer Service (Week 3-4)

**Why first**: Simplest domain with the fewest dependencies. Only two endpoints and minimal coupling.

1. Create a new Spring Boot application from `ftgo-consumer-service/` code
2. Set up a separate database with the `consumers` table
3. Migrate existing consumer data from the monolith database
4. Deploy the Consumer Service alongside the monolith
5. Update the API Gateway to route `/consumers/**` to the new service
6. Update the monolith's `OrderService` to call the Consumer Service REST API instead of the local `ConsumerService` bean (temporary adapter)
7. Verify end-to-end: consumer creation, retrieval, and order validation
8. Remove `ftgo-consumer-service/` code from the monolith

### Phase 2: Extract Restaurant Service (Week 5-6)

**Why second**: Self-contained domain. The Order Service reads restaurant data but doesn't mutate it.

1. Create a new Spring Boot application from `ftgo-restaurant-service/` code
2. Set up a separate database with `restaurants` and `restaurant_menu_items` tables
3. Migrate existing restaurant data
4. Implement the `RestaurantMenuRevisedEvent` publisher
5. Deploy and route `/restaurants/**` through the gateway
6. Update the monolith's `OrderService` to:
   - Call Restaurant Service REST API for menu validation
   - Subscribe to `RestaurantMenuRevisedEvent` for cache updates (or use sync calls)
7. Verify end-to-end: restaurant creation, menu management, order creation with menu validation
8. Remove `ftgo-restaurant-service/` code from the monolith

### Phase 3: Extract Courier Service (Week 7-9)

**Why third**: Required before Order Service extraction since the Order Service depends on courier assignment.

1. Create a new Spring Boot application from `ftgo-courier-service/` code
2. Move the `CourierAssignmentStrategy` and `DistanceOptimizedCourierAssignmentStrategy` to this service
3. Set up a separate database with `courier` and `courier_actions` tables
4. Implement the delivery assignment API (`GET /couriers/available`, `POST /couriers/{id}/actions`)
5. Migrate existing courier data
6. Deploy and route `/couriers/**` through the gateway
7. Update the monolith's `OrderService.scheduleDelivery()` to call the Courier Service REST API
8. Verify end-to-end: courier registration, availability, order acceptance with courier assignment
9. Remove `ftgo-courier-service/` code from the monolith

### Phase 4: Extract Order Service (Week 10-12)

**Why last**: Most complex service with the most dependencies. By this point, all dependencies are already microservices.

1. Create a new Spring Boot application from `ftgo-order-service/` code
2. Replace all direct repository/service calls with REST API calls to other services:
   - `ConsumerService.validateOrderForConsumer()` → `POST /consumers/{id}/validate-order`
   - `RestaurantRepository.findById()` → `GET /restaurants/{id}` + `POST /restaurants/{id}/menu/validate`
   - `CourierRepository.findAllAvailable()` → `GET /couriers/available`
   - Courier plan mutations → `POST /couriers/{id}/actions`
3. Implement the Saga pattern for `createOrder()` to handle distributed transaction:
   - Step 1: Validate consumer (Consumer Service)
   - Step 2: Validate menu items (Restaurant Service)
   - Step 3: Create order (local)
   - Compensating actions on failure: cancel order, release courier
4. Set up a separate database with `orders` and `order_line_items` tables
5. Migrate existing order data
6. Deploy and route `/orders/**` through the gateway
7. Decommission the monolith
8. Remove the shared `ftgo-domain/` module (all entities now live in their respective services)

### Phase 5: Decommission Monolith (Week 13-14)

1. Verify all traffic is routed to microservices (zero requests to monolith)
2. Keep the monolith running in shadow mode for 1-2 weeks
3. Decommission the monolith application and its database
4. Clean up gateway routes (remove monolith backend)

### Migration Timeline Summary

```
Week  1-2  : [Phase 0] Preparation — Gateway, infra, observability
Week  3-4  : [Phase 1] Consumer Service extraction
Week  5-6  : [Phase 2] Restaurant Service extraction
Week  7-9  : [Phase 3] Courier Service extraction
Week 10-12 : [Phase 4] Order Service extraction + Saga implementation
Week 13-14 : [Phase 5] Monolith decommission
```

---

## 5. Database Decomposition Strategy

### 5.1 Approach: Database-per-Service

Each microservice gets its own database instance (or schema). This ensures:
- **Loose coupling**: Services can evolve their schema independently
- **Independent scaling**: Each database can be scaled based on its service's needs
- **Technology freedom**: Services can choose different database technologies if needed

### 5.2 Migration Steps

#### Step 1: Identify Table Ownership

| Table | Owner Service | References From |
|---|---|---|
| `consumers` | Consumer Service | Order Service (`consumer_id`) |
| `restaurants` | Restaurant Service | Order Service (`restaurant_id`) |
| `restaurant_menu_items` | Restaurant Service | — |
| `orders` | Order Service | — |
| `order_line_items` (embedded) | Order Service | — |
| `courier` | Courier Service | Order Service (`assigned_courier_id`) |
| `courier_actions` | Courier Service | Order Service (via `order_id` reference) |

#### Step 2: Break Foreign Key Constraints

The monolith uses JPA relationships that span service boundaries:

| Current FK | Replacement |
|---|---|
| `orders.restaurant_id → restaurants.id` | Soft reference (BIGINT, no FK constraint) + denormalized restaurant name |
| `orders.assigned_courier_id → courier.id` | Soft reference (BIGINT, no FK constraint) |
| `courier_actions.order_id → orders.id` | Soft reference (BIGINT, no FK constraint) |

#### Step 3: Handle Cross-Service Queries

**Current**: `OrderController.makeGetOrderResponse()` joins `Order` → `Restaurant` (name) and `Order` → `Courier` (actions).

**After decomposition**:
- **Option A (API Composition)**: The API Gateway's `/orders/{id}/details` endpoint calls Order, Restaurant, and Courier services in parallel and combines the results
- **Option B (Denormalization)**: The Order Service stores denormalized copies of `restaurantName` and `courierName` at order creation/acceptance time, updated via events
- **Recommended**: Use Option B for the common read path (GetOrderResponse) and Option A for the detailed composite view

#### Step 4: Data Migration Procedure

For each extracted service:

1. **Create the new database** with the service's schema
2. **Dual-write**: Update the monolith to write to both the old and new database
3. **Backfill**: Copy existing data from the monolith database to the new database
4. **Verify**: Confirm data consistency between old and new databases
5. **Switch reads**: Route read traffic to the new database
6. **Switch writes**: Route write traffic to the new database only
7. **Remove dual-write**: Clean up the monolith's dual-write code
8. **Drop old tables**: Remove the migrated tables from the monolith database

#### Step 5: Handle Distributed Transactions

The monolith uses `@Transactional` annotations that span multiple aggregates. In the microservice world:

| Monolith Transaction | Microservice Approach |
|---|---|
| `createOrder()`: validate consumer + validate menu + save order | **Saga (Orchestration)**: Order Service orchestrates steps with compensating actions |
| `accept()`: update order state + assign courier + add actions | **Saga (Orchestration)**: Order Service calls Courier Service, compensates on failure |
| `cancel()`: cancel order + remove courier actions | **Event-driven**: Order Service publishes `OrderCancelledEvent`, Courier Service reacts |

**Saga for Order Creation**:
```
1. Order Service → Create Order (PENDING)
2. Order Service → Consumer Service: Validate Consumer
   ├── Success → Continue
   └── Failure → Reject Order (compensate: delete order)
3. Order Service → Restaurant Service: Validate Menu Items
   ├── Success → Continue
   └── Failure → Reject Order (compensate: delete order)
4. Order Service → Approve Order (APPROVED)
5. Publish OrderCreatedEvent
```

### 5.3 Data Consistency Guarantees

| Pattern | Consistency | Use Case |
|---|---|---|
| Synchronous REST | Strong (within timeout) | Order validation (consumer, menu items) |
| Saga with Orchestration | Eventual | Order creation, order acceptance |
| Event-driven (pub/sub) | Eventual | Menu updates, courier location, notifications |
| CQRS read model | Eventual | Composite order details view |

---

## 6. API Specs Reference

Complete OpenAPI 3.0 specifications are provided in the `api-specs/` directory:

| File | Description |
|---|---|
| [`api-specs/order-service.yaml`](api-specs/order-service.yaml) | Order Service API — order lifecycle, state transitions, async event contracts |
| [`api-specs/consumer-service.yaml`](api-specs/consumer-service.yaml) | Consumer Service API — registration, retrieval, order validation |
| [`api-specs/restaurant-service.yaml`](api-specs/restaurant-service.yaml) | Restaurant Service API — restaurant registration, menu management, validation |
| [`api-specs/courier-service.yaml`](api-specs/courier-service.yaml) | Courier Service API — courier management, availability, location, delivery planning |
| [`api-specs/api-gateway.yaml`](api-specs/api-gateway.yaml) | API Gateway — aggregated routing spec with API composition endpoint |

Each spec includes:
- Complete path definitions with request/response schemas
- HTTP methods and status codes derived from the existing controllers
- Schema definitions (`components/schemas`) mapped from domain models
- Authentication/security scheme definitions (JWT Bearer)
- Async event contracts for inter-service communication (in `components/schemas`)
