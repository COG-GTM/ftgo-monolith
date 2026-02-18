# FTGO Monolith-to-Microservices Migration Plan

> **Technical Design Document**
> Version 1.0 | February 2026

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current State Analysis](#2-current-state-analysis)
3. [Target Architecture](#3-target-architecture)
4. [Service Topology Design](#4-service-topology-design)
5. [Communication Patterns](#5-communication-patterns)
6. [Security Strategy](#6-security-strategy)
7. [Data Strategy](#7-data-strategy)
8. [Infrastructure Requirements](#8-infrastructure-requirements)
9. [Observability Enhancements](#9-observability-enhancements)
10. [Phased Migration Plan](#10-phased-migration-plan)
11. [Risk Mitigation](#11-risk-mitigation)
12. [Appendices](#12-appendices)

---

## 1. Executive Summary

This document defines a phased migration plan to decompose the FTGO modular monolith into four independent microservices: **Consumer Service**, **Restaurant Service**, **Delivery Service**, and **Order Service**. The migration uses the **Strangler Fig** pattern to allow safe coexistence of the monolith and new services during transition, with zero-downtime deployments at each phase.

### Key Principles

- **Incremental extraction** - one service at a time, lowest coupling first
- **Backward compatibility** - the monolith continues to work at every phase boundary
- **Database-per-service** - each microservice owns its data exclusively
- **Event-driven decoupling** - replace synchronous cross-domain calls with asynchronous events and synchronous REST where latency requires it
- **Infrastructure-first** - stand up shared platform capabilities (gateway, broker, observability) before extracting any service

### Migration Order

| Phase | Duration (est.) | Deliverable |
|-------|----------------|-------------|
| 0 - Foundation | 4 weeks | Shared infrastructure (API Gateway, Kafka, Config Server, Observability) |
| 1 - Consumer Service | 3 weeks | Independent Consumer microservice + database |
| 2 - Restaurant Service | 3 weeks | Independent Restaurant microservice + database |
| 3 - Delivery Service | 4 weeks | Independent Delivery microservice + database |
| 4 - Order Service | 5 weeks | Independent Order microservice + database; monolith decommissioned |
| 5 - Hardening | 3 weeks | Performance tuning, chaos testing, runbook finalization |

**Total estimated duration: 22 weeks**

---

## 2. Current State Analysis

### 2.1 Application Structure

The FTGO application is a Spring Boot 2.0.3 monolith (Java 8) deployed as a single JAR. It is composed of Gradle sub-modules that map to logical business domains but are compiled and deployed together.

```
ftgo-monolith/
  ftgo-application/          # Main entry point - imports all service configs
  ftgo-common/               # Shared value objects (Money, Address, PersonName)
  ftgo-common-jpa/           # Shared JPA configuration
  ftgo-domain/               # Shared JPA entities and repository interfaces
  ftgo-order-service/        # Order domain logic + controller
  ftgo-order-service-api/    # Order API DTOs
  ftgo-consumer-service/     # Consumer domain logic + controller
  ftgo-consumer-service-api/ # Consumer API DTOs
  ftgo-restaurant-service/   # Restaurant domain logic + controller
  ftgo-restaurant-service-api/ # Restaurant API DTOs
  ftgo-courier-service/      # Courier domain logic + controller
  ftgo-courier-service-api/  # Courier API DTOs
  ftgo-flyway/               # Database migrations
  common-swagger/            # Shared Swagger/OpenAPI config
```

**Entry point** (`FtgoApplicationMain.java`):
```java
@Import({ConsumerServiceConfiguration.class,
        OrderServiceConfiguration.class,
        RestaurantServiceConfiguration.class})
public class FtgoApplicationMain { ... }
```

### 2.2 Dependency Graph

```
                     ┌──────────────────────┐
                     │   ftgo-application    │
                     │   (main entry point)  │
                     └──────┬───┬───┬───┬───┘
                            │   │   │   │
              ┌─────────────┘   │   │   └─────────────┐
              ▼                 ▼   ▼                 ▼
     ┌────────────────┐ ┌───────────────┐  ┌──────────────────┐
     │ consumer-service│ │ order-service │  │restaurant-service│
     └───────┬────────┘ └──┬────┬───┬───┘  └───────┬──────────┘
             │              │    │   │              │
             │              │    │   └──────────────┤
             │              │    │                  │
             ▼              ▼    ▼                  ▼
     ┌───────────────────────────────────────────────────────┐
     │                    ftgo-domain                        │
     │  (Order, Consumer, Restaurant, Courier entities)      │
     │  (OrderRepository, ConsumerRepository, etc.)          │
     └───────────────────────┬───────────────────────────────┘
                             │
                             ▼
     ┌───────────────────────────────────────────────────────┐
     │                    ftgo-common                        │
     │  (Money, Address, PersonName value objects)           │
     └───────────────────────────────────────────────────────┘
```

### 2.3 Critical Coupling Points

#### Compile-Time Dependencies

| Source | Target | Type | Location |
|--------|--------|------|----------|
| `ftgo-order-service` | `ftgo-consumer-service` | Gradle `compile project` | `ftgo-order-service/build.gradle:43` |
| `OrderService` | `ConsumerService` | Direct class import | `OrderService.java:4` |
| `OrderService` | `RestaurantRepository` | Direct repository access | `OrderService.java:26` |
| `OrderService` | `CourierRepository` | Direct repository access | `OrderService.java:31` |
| All services | `ftgo-domain` | Shared entity module | Every service `build.gradle` |

#### Synchronous Cross-Domain Calls

**1. Consumer validation during order creation** (`OrderService.java:57`):
```java
consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());
```
OrderService directly invokes ConsumerService's `validateOrderForConsumer()` as an in-process method call within the same transaction.

**2. Delivery scheduling** (`OrderService.java:99-110`):
```java
List<Courier> couriers = courierRepository.findAllAvailable();
Courier courier = couriers.get(random.nextInt(couriers.size()));
courier.addAction(Action.makePickup(order));
courier.addAction(Action.makeDropoff(order, readyBy.plusMinutes(30)));
order.schedule(courier);
```
OrderService directly queries CourierRepository and mutates Courier entities, crossing the Delivery bounded context.

#### Database Foreign Key Constraints

| Constraint | Parent Table | Child Column | Referenced Table |
|-----------|-------------|-------------|-----------------|
| `orders_restaurant_id` | `orders` | `restaurant_id` | `restaurants` |
| `orders_assigned_courier_id` | `orders` | `assigned_courier_id` | `courier` |
| `courier_actions_order_id` | `courier_actions` | `order_id` | `orders` |
| `courier_actions_courier_id` | `courier_actions` | `courier_id` | `courier` |
| `order_line_items_id` | `order_line_items` | `order_id` | `orders` |
| `restaurant_menu_items_restaurant_id` | `restaurant_menu_items` | `restaurant_id` | `restaurants` |

### 2.4 Identified Gaps

| Gap | Impact | Resolution Phase |
|-----|--------|-----------------|
| No security (no Spring Security, OAuth2, JWT) | No authN/authZ | Phase 0 |
| No distributed tracing (no Sleuth/Zipkin) | Cannot trace cross-service requests | Phase 0 |
| No inter-service communication patterns | Services can't call each other remotely | Phase 0 |
| No API gateway | No single entry point for clients | Phase 0 |
| No service discovery | Services can't locate each other | Phase 0 |
| No centralized configuration | Config scattered across properties files | Phase 0 |
| No circuit breakers | No fault tolerance | Phase 0 |
| No feature flags | Can't safely toggle behavior during migration | Phase 0 |

---

## 3. Target Architecture

### 3.1 High-Level Architecture Diagram

```
                            ┌────────────────┐
                            │   API Clients   │
                            └───────┬────────┘
                                    │
                            ┌───────▼────────┐
                            │  API Gateway   │
                            │ (Spring Cloud  │
                            │  Gateway)      │
                            └──┬──┬──┬──┬───┘
                               │  │  │  │
          ┌────────────────────┘  │  │  └────────────────────┐
          │            ┌──────────┘  └──────────┐            │
          ▼            ▼                        ▼            ▼
  ┌───────────┐ ┌─────────────┐       ┌──────────────┐ ┌──────────┐
  │ Consumer  │ │   Order     │       │  Restaurant  │ │ Delivery │
  │ Service   │ │   Service   │       │  Service     │ │ Service  │
  │ :8081     │ │   :8082     │       │  :8083       │ │ :8084    │
  └─────┬─────┘ └──────┬──────┘       └──────┬───────┘ └────┬─────┘
        │               │                     │              │
        ▼               ▼                     ▼              ▼
  ┌───────────┐ ┌─────────────┐       ┌──────────────┐ ┌──────────┐
  │ consumer  │ │   order     │       │  restaurant  │ │ delivery │
  │ _db       │ │   _db       │       │  _db         │ │ _db      │
  │ (MySQL)   │ │   (MySQL)   │       │  (MySQL)     │ │ (MySQL)  │
  └───────────┘ └─────────────┘       └──────────────┘ └──────────┘
        │               │                     │              │
        └───────────────┴──────────┬──────────┴──────────────┘
                                   │
                            ┌──────▼───────┐
                            │    Kafka     │
                            │  (Event Bus) │
                            └──────────────┘
```

### 3.2 Service Responsibility Matrix

| Service | Owns Entities | Owns Tables | API Endpoints | Key Responsibilities |
|---------|--------------|-------------|---------------|---------------------|
| **Consumer Service** | `Consumer` | `consumers` | `POST /consumers`, `GET /consumers/{id}` | Consumer registration, consumer validation |
| **Restaurant Service** | `Restaurant`, `MenuItem` | `restaurants`, `restaurant_menu_items` | `POST /restaurants`, `GET /restaurants/{id}` | Restaurant registration, menu management |
| **Delivery Service** | `Courier`, `Action`, `Plan` | `courier`, `courier_actions` | `POST /couriers`, `POST /couriers/{id}/availability`, `GET /couriers/{id}`, `POST /deliveries/schedule` | Courier management, delivery scheduling, courier assignment |
| **Order Service** | `Order`, `OrderLineItem` | `orders`, `order_line_items` | `POST /orders`, `GET /orders/{id}`, `POST /orders/{id}/cancel`, etc. | Order lifecycle, state transitions |

---

## 4. Service Topology Design

### 4.1 Consumer Service

**Bounded Context:** Consumer Management

**Owned Entities (migrated from `ftgo-domain`):**
- `Consumer` entity (with `PersonName` embedded)
- `ConsumerRepository` interface

**Owned Database Tables:**
- `consumers`

**Exposed API:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/consumers` | Register a new consumer |
| GET | `/consumers/{consumerId}` | Get consumer details |
| POST | `/consumers/{consumerId}/validate-order` | Validate consumer can place an order of a given total |

**Published Events (Kafka topic: `consumer-events`):**
| Event | Trigger | Payload |
|-------|---------|---------|
| `ConsumerCreated` | New consumer registered | `{ consumerId, name }` |
| `ConsumerValidated` | Order validation succeeded | `{ consumerId, orderId }` |
| `ConsumerValidationFailed` | Order validation failed | `{ consumerId, orderId, reason }` |

**Dependencies:** None (leaf service)

### 4.2 Restaurant Service

**Bounded Context:** Restaurant Management

**Owned Entities (migrated from `ftgo-domain`):**
- `Restaurant` entity (with `Address` embedded, `MenuItem` collection)
- `RestaurantMenu` (value object)
- `MenuItem` (embeddable)
- `RestaurantRepository` interface

**Owned Database Tables:**
- `restaurants`
- `restaurant_menu_items`

**Exposed API:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/restaurants` | Register a new restaurant |
| GET | `/restaurants/{restaurantId}` | Get restaurant details including menu |

**Published Events (Kafka topic: `restaurant-events`):**
| Event | Trigger | Payload |
|-------|---------|---------|
| `RestaurantCreated` | New restaurant registered | `{ restaurantId, name, menuItems }` |
| `RestaurantMenuRevised` | Menu updated | `{ restaurantId, menuItems }` |

**Dependencies:** None (leaf service)

### 4.3 Delivery Service

**Bounded Context:** Delivery / Courier Management

**Owned Entities (migrated from `ftgo-domain`):**
- `Courier` entity (with `PersonName`, `Address`, `Plan` embedded)
- `Action` (embeddable)
- `Plan` (embeddable)
- `CourierRepository` interface

**Owned Database Tables:**
- `courier`
- `courier_actions`

**Exposed API:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/couriers` | Register a new courier |
| GET | `/couriers/{courierId}` | Get courier details |
| POST | `/couriers/{courierId}/availability` | Update courier availability |
| POST | `/deliveries/schedule` | Schedule a delivery for an order |
| GET | `/deliveries/order/{orderId}` | Get delivery info for an order |

**Published Events (Kafka topic: `delivery-events`):**
| Event | Trigger | Payload |
|-------|---------|---------|
| `DeliveryScheduled` | Courier assigned to order | `{ orderId, courierId, pickupTime, dropoffTime }` |
| `CourierCreated` | New courier registered | `{ courierId, name }` |

**Consumed Events:**
| Event | Source | Action |
|-------|--------|--------|
| `OrderAccepted` | Order Service | Trigger delivery scheduling |

**Dependencies:** Consumes events from Order Service

### 4.4 Order Service

**Bounded Context:** Order Management

**Owned Entities (migrated from `ftgo-domain`):**
- `Order` entity (local copy, no JPA relations to `Restaurant` or `Courier`)
- `OrderLineItem` (embeddable)
- `OrderLineItems` (embeddable)
- `DeliveryInformation` (embeddable)
- `PaymentInformation` (embeddable)
- `OrderState` (enum)
- `OrderRepository` interface

**Owned Database Tables:**
- `orders` (with `restaurant_id` and `assigned_courier_id` as plain `BIGINT` columns, no FK)
- `order_line_items`

**Exposed API:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/orders` | Create a new order |
| GET | `/orders/{orderId}` | Get order details |
| GET | `/orders?consumerId={id}` | Get orders for a consumer |
| POST | `/orders/{orderId}/cancel` | Cancel an order |
| POST | `/orders/{orderId}/revise` | Revise an order |
| POST | `/orders/{orderId}/accept` | Accept an order ticket |
| POST | `/orders/{orderId}/preparing` | Mark order as preparing |
| POST | `/orders/{orderId}/ready` | Mark order as ready for pickup |
| POST | `/orders/{orderId}/pickedup` | Mark order as picked up |
| POST | `/orders/{orderId}/delivered` | Mark order as delivered |

**Published Events (Kafka topic: `order-events`):**
| Event | Trigger | Payload |
|-------|---------|---------|
| `OrderCreated` | New order placed | `{ orderId, consumerId, restaurantId, lineItems, orderTotal }` |
| `OrderAccepted` | Restaurant accepts ticket | `{ orderId, readyBy }` |
| `OrderCancelled` | Order cancelled | `{ orderId }` |
| `OrderStateChanged` | Any state transition | `{ orderId, previousState, newState }` |

**Consumed Events:**
| Event | Source | Action |
|-------|--------|--------|
| `ConsumerValidated` | Consumer Service | Continue order creation saga |
| `ConsumerValidationFailed` | Consumer Service | Reject order |
| `DeliveryScheduled` | Delivery Service | Record courier assignment |
| `RestaurantCreated` | Restaurant Service | Cache restaurant data locally |

**Dependencies:** Orchestrates saga with Consumer Service and Delivery Service

---

## 5. Communication Patterns

### 5.1 Overview of Patterns

| Pattern | Use Case | Technology |
|---------|----------|------------|
| **Synchronous REST** | Client-facing API calls, real-time queries | Spring WebClient (non-blocking) via API Gateway |
| **Asynchronous Events** | Cross-service state changes, eventual consistency | Apache Kafka |
| **Saga (Choreography)** | Distributed transactions (order creation) | Kafka events + local state machines |
| **CQRS** | Order detail queries needing data from multiple services | Local read-model caches |

### 5.2 Replacing `OrderService.createOrder()` - The Create Order Saga

**Current monolith flow:**
```
OrderService.createOrder(consumerId, restaurantId, lineItems)
  → restaurantRepository.findById(restaurantId)          // direct DB query
  → consumerService.validateOrderForConsumer(...)         // synchronous in-process call
  → orderRepository.save(order)                           // single transaction
```

**Target microservices flow (Choreography-Based Saga):**

```
┌──────────┐     ┌───────────────┐     ┌──────────────┐     ┌──────────┐
│  Client  │     │ Order Service │     │Consumer Svc  │     │Restaurant│
└────┬─────┘     └──────┬────────┘     └──────┬───────┘     └────┬─────┘
     │ POST /orders     │                     │                  │
     │─────────────────>│                     │                  │
     │                  │                     │                  │
     │                  │ 1. Validate restaurant locally         │
     │                  │    (cached menu data)                  │
     │                  │                     │                  │
     │                  │ 2. Create Order     │                  │
     │                  │    (state=PENDING)  │                  │
     │                  │                     │                  │
     │  201 Created     │                     │                  │
     │<─────────────────│                     │                  │
     │                  │                     │                  │
     │                  │ 3. Publish          │                  │
     │                  │ OrderCreated ──────>│                  │
     │                  │   (Kafka)           │                  │
     │                  │                     │                  │
     │                  │               4. Validate consumer     │
     │                  │                     │                  │
     │                  │ 5. ConsumerValidated│                  │
     │                  │<────────────────────│                  │
     │                  │   (Kafka)           │                  │
     │                  │                     │                  │
     │                  │ 6. Transition to    │                  │
     │                  │    state=APPROVED   │                  │
     │                  │                     │                  │
```

**Saga state machine (in Order Service):**
```
PENDING → (ConsumerValidated) → APPROVED
PENDING → (ConsumerValidationFailed) → REJECTED
```

**Compensating action:** If consumer validation fails, the order is moved to `REJECTED` state. No compensating actions are needed for Consumer or Restaurant since they have not made any state changes.

### 5.3 Replacing `OrderService.scheduleDelivery()` - Event-Driven Delivery

**Current monolith flow:**
```
OrderService.accept(orderId, readyBy)
  → order.acceptTicket(readyBy)
  → scheduleDelivery(order, readyBy)
      → courierRepository.findAllAvailable()    // direct DB query across bounded context
      → courier.addAction(Action.makePickup())  // direct entity mutation
      → courier.addAction(Action.makeDropoff()) // direct entity mutation
      → order.schedule(courier)                 // FK relationship
```

**Target microservices flow (Event-Driven):**

```
┌──────────────┐          ┌───────────────┐          ┌──────────────┐
│Order Service │          │     Kafka     │          │Delivery Svc  │
└──────┬───────┘          └──────┬────────┘          └──────┬───────┘
       │                         │                          │
       │ 1. accept(orderId)      │                          │
       │    order.acceptTicket() │                          │
       │                         │                          │
       │ 2. Publish              │                          │
       │ OrderAccepted ─────────>│                          │
       │ {orderId, readyBy}      │                          │
       │                         │ 3. Consume OrderAccepted │
       │                         │─────────────────────────>│
       │                         │                          │
       │                         │          4. Find available courier
       │                         │             courier.addAction(PICKUP)
       │                         │             courier.addAction(DROPOFF)
       │                         │                          │
       │                         │ 5. Publish               │
       │                         │<─────────────────────────│
       │                         │ DeliveryScheduled        │
       │                         │ {orderId, courierId}     │
       │                         │                          │
       │ 6. Consume              │                          │
       │<────────────────────────│                          │
       │ DeliveryScheduled       │                          │
       │                         │                          │
       │ 7. order.assignedCourierId = courierId             │
       │    (store ID only, no FK)                          │
```

### 5.4 Synchronous Fallback via REST

For queries that need real-time data from another service (e.g., `GET /orders/{id}` needs the restaurant name), the Order Service will:

1. **Primary approach:** Maintain a local read-model cache of restaurant data, populated by consuming `RestaurantCreated` / `RestaurantMenuRevised` events.
2. **Fallback:** If cache miss, make a synchronous REST call to `GET /restaurants/{restaurantId}` with circuit breaker protection (Resilience4j).

```java
@Service
public class RestaurantDataService {
    private final RestaurantLocalCacheRepository cache;
    private final WebClient restaurantClient;
    private final CircuitBreaker circuitBreaker;

    public RestaurantInfo getRestaurant(long restaurantId) {
        return cache.findById(restaurantId)
            .orElseGet(() -> circuitBreaker.executeSupplier(
                () -> restaurantClient.get()
                    .uri("/restaurants/{id}", restaurantId)
                    .retrieve()
                    .bodyToMono(RestaurantInfo.class)
                    .block()
            ));
    }
}
```

### 5.5 Kafka Topic Design

| Topic | Partitions | Key | Producers | Consumers |
|-------|-----------|-----|-----------|-----------|
| `consumer-events` | 6 | `consumerId` | Consumer Service | Order Service |
| `restaurant-events` | 6 | `restaurantId` | Restaurant Service | Order Service |
| `order-events` | 12 | `orderId` | Order Service | Delivery Service |
| `delivery-events` | 6 | `orderId` | Delivery Service | Order Service |

Partition keys ensure ordering per entity. Consumer groups enable independent scaling.

---

## 6. Security Strategy

### 6.1 Authentication Architecture

```
┌──────────┐      ┌─────────────┐      ┌─────────────────┐
│  Client  │─────>│ API Gateway │─────>│ Microservices   │
│          │ JWT  │ (validates  │ JWT  │ (extract claims,│
│          │      │  token)     │      │  authorize)     │
└──────────┘      └──────┬──────┘      └─────────────────┘
                         │
                  ┌──────▼──────┐
                  │   OAuth2    │
                  │   Auth      │
                  │   Server    │
                  │ (Keycloak)  │
                  └─────────────┘
```

### 6.2 Implementation Details

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Identity Provider | Keycloak (or Auth0) | Issue and manage JWT tokens |
| Token Format | JWT (RS256) | Stateless authentication |
| API Gateway | Spring Cloud Gateway + Spring Security | Token validation, rate limiting |
| Service-to-Service | JWT propagation + service accounts | Internal auth for REST calls |
| Event Security | Kafka SASL/SSL | Encrypted and authenticated event streams |

### 6.3 Authorization Model

```yaml
roles:
  CUSTOMER:
    - POST /orders
    - GET /orders (own orders only, filtered by consumerId from JWT)
    - GET /consumers/{self}
  RESTAURANT_OWNER:
    - POST /restaurants
    - GET /restaurants/{own}
    - POST /orders/{orderId}/accept (own restaurant orders)
    - POST /orders/{orderId}/preparing
    - POST /orders/{orderId}/ready
  COURIER:
    - POST /couriers/{self}/availability
    - POST /orders/{orderId}/pickedup
    - POST /orders/{orderId}/delivered
  ADMIN:
    - All endpoints
```

### 6.4 Spring Security Configuration (per service)

Each microservice includes `spring-boot-starter-security` and `spring-boot-starter-oauth2-resource-server`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### 6.5 Service-to-Service Authentication

Internal REST calls (e.g., Order Service querying Restaurant Service on cache miss) use a dedicated service account JWT:

```java
@Bean
public WebClient restaurantServiceClient(
        @Value("${services.restaurant.url}") String baseUrl,
        OAuth2AuthorizedClientManager clientManager) {
    var oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
    oauth2.setDefaultClientRegistrationId("service-account");
    return WebClient.builder()
        .baseUrl(baseUrl)
        .apply(oauth2.oauth2Configuration())
        .build();
}
```

---

## 7. Data Strategy

### 7.1 Database-per-Service Target State

```
┌─────────────────────────────────────────────────────────────┐
│                     Current State                           │
│                                                             │
│  ┌─────────────────── ftgo (single MySQL DB) ────────────┐  │
│  │ consumers │ orders │ order_line_items │ restaurants │   │  │
│  │ restaurant_menu_items │ courier │ courier_actions │     │  │
│  │                                                       │  │
│  │ FK: orders.restaurant_id → restaurants.id             │  │
│  │ FK: orders.assigned_courier_id → courier.id           │  │
│  │ FK: courier_actions.order_id → orders.id              │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

                            ▼▼▼

┌─────────────────────────────────────────────────────────────┐
│                     Target State                            │
│                                                             │
│  ┌──────────┐  ┌──────────────┐  ┌────────────────┐        │
│  │consumer_db│  │  order_db   │  │ restaurant_db  │        │
│  │          │  │              │  │                │        │
│  │consumers │  │orders        │  │restaurants     │        │
│  │          │  │order_line_   │  │restaurant_menu_│        │
│  │          │  │  items       │  │  items         │        │
│  │          │  │              │  │                │        │
│  │          │  │restaurant_   │  │                │        │
│  │          │  │  cache(*)    │  │                │        │
│  └──────────┘  └──────────────┘  └────────────────┘        │
│                                                             │
│  ┌──────────────┐                                           │
│  │ delivery_db  │  (*) restaurant_cache is a local          │
│  │              │      read-model table in order_db         │
│  │courier       │      populated via Kafka events           │
│  │courier_actions│                                          │
│  └──────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 Foreign Key Elimination Plan

| FK Constraint | Migration Strategy |
|--------------|-------------------|
| `orders.restaurant_id → restaurants.id` | Keep `restaurant_id` as a plain `BIGINT` column (no FK). Order Service maintains a local `restaurant_cache` table populated from `restaurant-events` Kafka topic. Referential integrity is maintained at the application level. |
| `orders.assigned_courier_id → courier.id` | Keep `assigned_courier_id` as a plain `BIGINT` column (no FK). Populated when Order Service consumes `DeliveryScheduled` event. |
| `courier_actions.order_id → orders.id` | Keep `order_id` as a plain `BIGINT` column (no FK) in Delivery Service's `courier_actions` table. The `order_id` is provided in the `OrderAccepted` event payload. |
| `courier_actions.courier_id → courier.id` | Remains as FK within Delivery Service's own database (same bounded context). |
| `order_line_items.order_id → orders.id` | Remains as FK within Order Service's own database (same bounded context). |
| `restaurant_menu_items.restaurant_id → restaurants.id` | Remains as FK within Restaurant Service's own database (same bounded context). |

### 7.3 Data Migration Approach

For each service extraction, follow this sequence:

```
Step 1: Create new database schema
  └─ Run Flyway migration to create tables in the new DB

Step 2: Set up dual-write (monolith writes to both old and new DB)
  └─ Using Spring's AbstractRoutingDataSource or Change Data Capture (Debezium)

Step 3: Backfill historical data
  └─ One-time batch migration script from old DB → new DB

Step 4: Verify data consistency
  └─ Run reconciliation queries comparing old and new databases

Step 5: Switch reads to new database
  └─ Feature flag: route reads to new DB, writes still dual

Step 6: Switch writes to new service only
  └─ Feature flag: disable writes to old DB tables

Step 7: Drop FK constraints from old DB
  └─ ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id (etc.)

Step 8: Drop old tables (after migration stabilizes)
  └─ Deferred until full monolith decommission
```

### 7.4 Flyway Migration Strategy per Service

Each microservice gets its own Flyway migration directory:

```
consumer-service/src/main/resources/db/migration/
  V1__create_consumer_db.sql

restaurant-service/src/main/resources/db/migration/
  V1__create_restaurant_db.sql

delivery-service/src/main/resources/db/migration/
  V1__create_delivery_db.sql

order-service/src/main/resources/db/migration/
  V1__create_order_db.sql
  V2__add_restaurant_cache_table.sql
```

**Example: `V1__create_order_db.sql`**
```sql
CREATE TABLE orders (
  id                       BIGINT NOT NULL AUTO_INCREMENT,
  accept_time              DATETIME,
  consumer_id              BIGINT,
  delivery_address_city    VARCHAR(255),
  delivery_address_state   VARCHAR(255),
  delivery_address_street1 VARCHAR(255),
  delivery_address_street2 VARCHAR(255),
  delivery_address_zip     VARCHAR(255),
  delivery_time            DATETIME,
  order_state              VARCHAR(255),
  order_minimum            DECIMAL(19, 2),
  payment_token            VARCHAR(255),
  picked_up_time           DATETIME,
  delivered_time           DATETIME,
  preparing_time           DATETIME,
  previous_ticket_state    INTEGER,
  ready_by                 DATETIME,
  ready_for_pickup_time    DATETIME,
  version                  BIGINT,
  assigned_courier_id      BIGINT,       -- No FK, just a reference ID
  restaurant_id            BIGINT,       -- No FK, just a reference ID
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE order_line_items (
  order_id     BIGINT NOT NULL,
  menu_item_id VARCHAR(255),
  name         VARCHAR(255),
  price        DECIMAL(19, 2),
  quantity     INTEGER NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE = InnoDB;
```

### 7.5 Shared Domain Module (`ftgo-domain`) Decomposition

The shared `ftgo-domain` module contains entities from all four bounded contexts. It must be decomposed:

| Entity | Target Service | Migration Action |
|--------|---------------|-----------------|
| `Consumer` | Consumer Service | Move to `consumer-service/src/.../domain/` |
| `ConsumerRepository` | Consumer Service | Move to `consumer-service/src/.../domain/` |
| `Restaurant`, `RestaurantMenu`, `MenuItem` | Restaurant Service | Move to `restaurant-service/src/.../domain/` |
| `RestaurantRepository` | Restaurant Service | Move to `restaurant-service/src/.../domain/` |
| `Courier`, `Plan`, `Action`, `ActionType` | Delivery Service | Move to `delivery-service/src/.../domain/` |
| `CourierRepository` | Delivery Service | Move to `delivery-service/src/.../domain/` |
| `Order`, `OrderLineItem`, `OrderLineItems`, `OrderState` | Order Service | Move to `order-service/src/.../domain/` |
| `OrderRepository` | Order Service | Move to `order-service/src/.../domain/` |
| `DeliveryInformation`, `PaymentInformation` | Order Service | Move to `order-service/src/.../domain/` |
| `LineItemQuantityChange`, `OrderRevision`, `OrderMinimumNotMetException` | Order Service | Move to `order-service/src/.../domain/` |

**Shared value objects** (`ftgo-common` module: `Money`, `Address`, `PersonName`) are published as a shared library artifact (`ftgo-common-lib`) to a Maven/Gradle repository. All services depend on this library.

### 7.6 Order Entity Refactoring

The current `Order` entity has JPA `@ManyToOne` relationships to `Restaurant` and `Courier`:

```java
// CURRENT (monolith)
@ManyToOne(fetch = FetchType.LAZY)
private Restaurant restaurant;

@ManyToOne
private Courier assignedCourier;
```

**After migration**, these become plain ID references:

```java
// TARGET (microservice)
private Long restaurantId;
private String restaurantName;   // Denormalized from restaurant-events

private Long assignedCourierId;  // Populated from DeliveryScheduled event
```

---

## 8. Infrastructure Requirements

### 8.1 Required Components

| Component | Technology | Purpose | Deployment |
|-----------|-----------|---------|------------|
| **API Gateway** | Spring Cloud Gateway | Routing, rate limiting, auth | Kubernetes Deployment |
| **Message Broker** | Apache Kafka + Zookeeper (or KRaft) | Async event streaming | Kubernetes StatefulSet |
| **Service Discovery** | Kubernetes DNS (built-in) | Service-to-service resolution | Native K8s |
| **Config Server** | Spring Cloud Config (Git-backed) or Kubernetes ConfigMaps | Centralized configuration | Kubernetes Deployment |
| **Identity Provider** | Keycloak | OAuth2 / JWT token issuance | Kubernetes StatefulSet |
| **Schema Registry** | Confluent Schema Registry (Avro/JSON Schema) | Event schema evolution | Kubernetes Deployment |
| **Feature Flags** | Unleash or LaunchDarkly | Safe migration toggles | SaaS or self-hosted |

### 8.2 Kubernetes Namespace Layout

```
ftgo-platform/
  ├── namespace: ftgo-infra
  │   ├── kafka (StatefulSet, 3 brokers)
  │   ├── zookeeper (StatefulSet, 3 nodes)
  │   ├── schema-registry (Deployment)
  │   ├── keycloak (StatefulSet)
  │   ├── spring-cloud-config (Deployment)
  │   └── prometheus + grafana (StatefulSet)
  │
  ├── namespace: ftgo-services
  │   ├── api-gateway (Deployment, HPA)
  │   ├── consumer-service (Deployment, HPA)
  │   ├── restaurant-service (Deployment, HPA)
  │   ├── delivery-service (Deployment, HPA)
  │   └── order-service (Deployment, HPA)
  │
  ├── namespace: ftgo-data
  │   ├── consumer-db (StatefulSet, MySQL)
  │   ├── restaurant-db (StatefulSet, MySQL)
  │   ├── delivery-db (StatefulSet, MySQL)
  │   ├── order-db (StatefulSet, MySQL)
  │   └── ftgo-db (StatefulSet, MySQL) ← legacy, decommissioned in Phase 4
  │
  └── namespace: ftgo-monitoring
      ├── jaeger (or Zipkin) (Deployment)
      ├── elasticsearch (StatefulSet)
      ├── fluentd/fluent-bit (DaemonSet)
      └── kibana (Deployment)
```

### 8.3 API Gateway Route Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        # During migration: route to monolith by default
        - id: monolith-fallback
          uri: http://ftgo-monolith:8080
          predicates:
            - Path=/**
          order: 9999  # lowest priority

        # Phase 1: Consumer routes go to new service
        - id: consumer-service
          uri: http://consumer-service:8081
          predicates:
            - Path=/consumers/**
          filters:
            - CircuitBreaker=name=consumerCB,fallbackUri=forward:/fallback/consumer
          order: 1

        # Phase 2: Restaurant routes
        - id: restaurant-service
          uri: http://restaurant-service:8083
          predicates:
            - Path=/restaurants/**
          order: 2

        # Phase 3: Delivery routes
        - id: delivery-service
          uri: http://delivery-service:8084
          predicates:
            - Path=/couriers/**,/deliveries/**
          order: 3

        # Phase 4: Order routes
        - id: order-service
          uri: http://order-service:8082
          predicates:
            - Path=/orders/**
          order: 4
```

This configuration demonstrates the Strangler Fig pattern: the gateway defaults to the monolith but progressively routes traffic to new microservices as they are extracted.

### 8.4 Resilience Patterns

| Pattern | Technology | Configuration |
|---------|-----------|--------------|
| **Circuit Breaker** | Resilience4j | `failureRateThreshold=50`, `waitDurationInOpenState=10s`, `slidingWindowSize=10` |
| **Retry** | Resilience4j | `maxAttempts=3`, `waitDuration=500ms`, exponential backoff |
| **Timeout** | Resilience4j / WebClient | `connectionTimeout=2s`, `readTimeout=5s` |
| **Bulkhead** | Resilience4j | `maxConcurrentCalls=25` per downstream service |
| **Rate Limiting** | Spring Cloud Gateway | `replenishRate=10`, `burstCapacity=20` per user |

---

## 9. Observability Enhancements

### 9.1 Current State

- Micrometer with Prometheus registry (Order Service only)
- SLF4J with Logback (unstructured text logs)
- No distributed tracing

### 9.2 Target Observability Stack

```
┌──────────────────────────────────────────────────────────┐
│                    Observability Stack                    │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │   Metrics    │  │   Logging    │  │   Tracing     │  │
│  │              │  │              │  │               │  │
│  │ Micrometer   │  │ Logback →    │  │ Micrometer    │  │
│  │ → Prometheus │  │ JSON format  │  │ Tracing →     │  │
│  │ → Grafana    │  │ → Fluent Bit │  │ OpenTelemetry │  │
│  │              │  │ → ELK Stack  │  │ → Jaeger      │  │
│  └──────────────┘  └──────────────┘  └───────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐    │
│  │              Shared Libraries                     │    │
│  │                                                   │    │
│  │  ftgo-observability-lib:                          │    │
│  │  - Micrometer auto-configuration                  │    │
│  │  - JSON structured logging (logback-spring.xml)   │    │
│  │  - OpenTelemetry auto-instrumentation             │    │
│  │  - Standard health check endpoints               │    │
│  │  - Common Grafana dashboard templates             │    │
│  └──────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

### 9.3 Implementation Details

**Distributed Tracing (all services):**
```gradle
dependencies {
    implementation 'io.micrometer:micrometer-tracing-bridge-otel'
    implementation 'io.opentelemetry:opentelemetry-exporter-otlp'
}
```
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://jaeger-collector:4318/v1/traces
```

**Structured JSON Logging (all services):**
```xml
<!-- logback-spring.xml -->
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>spanId</includeMdcKeyName>
    <includeMdcKeyName>serviceName</includeMdcKeyName>
  </encoder>
</appender>
```

**Metrics (all services):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,info
  metrics:
    tags:
      service: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

### 9.4 Key Dashboards & Alerts

| Dashboard | Metrics | Alert Threshold |
|-----------|---------|----------------|
| Service Health | `http_server_requests_seconds`, `jvm_memory_used` | P99 latency > 2s |
| Order Pipeline | `placed_orders`, `approved_orders`, saga completion time | Saga timeout > 30s |
| Kafka Lag | `kafka_consumer_lag` | Consumer lag > 1000 |
| Error Rate | `http_server_requests` filtered by `status=5xx` | Error rate > 5% |
| Circuit Breaker | `resilience4j_circuitbreaker_state` | Circuit open for > 60s |

---

## 10. Phased Migration Plan

### Phase 0: Foundation Infrastructure (Weeks 1-4)

**Goal:** Deploy all shared infrastructure without changing the monolith's functionality.

#### Week 1-2: Core Infrastructure
| Task | Details |
|------|---------|
| Deploy Kafka cluster | 3-broker StatefulSet in `ftgo-infra` namespace |
| Deploy Zookeeper | 3-node StatefulSet (or use KRaft mode) |
| Deploy Schema Registry | For Avro/JSON Schema event validation |
| Create Kafka topics | `consumer-events`, `restaurant-events`, `order-events`, `delivery-events` |
| Deploy Keycloak | With FTGO realm, client apps, roles defined in Section 6.3 |

#### Week 2-3: Platform Services
| Task | Details |
|------|---------|
| Deploy API Gateway | Spring Cloud Gateway with monolith-only routing initially |
| Deploy Spring Cloud Config Server | Git-backed, with profiles per service |
| Set up feature flag system | Unleash with toggles: `consumer-service.extracted`, `restaurant-service.extracted`, etc. |
| Publish `ftgo-common-lib` | Extract `ftgo-common` as a versioned Maven artifact |
| Publish `ftgo-observability-lib` | Shared observability auto-configuration library |

#### Week 3-4: Observability
| Task | Details |
|------|---------|
| Deploy Jaeger | For distributed tracing collection |
| Deploy ELK Stack | Elasticsearch + Fluent Bit + Kibana for centralized logging |
| Deploy Prometheus + Grafana | With service dashboards from Section 9.4 |
| Add tracing to monolith | Add Micrometer Tracing + OTLP exporter to existing monolith |
| Add structured logging to monolith | Replace text logback with JSON encoder |
| Verify gateway routes to monolith | Smoke test: all existing APIs work through the gateway |

**Exit Criteria:**
- [ ] All infrastructure components healthy in Kubernetes
- [ ] API Gateway routes all traffic to monolith successfully
- [ ] Distributed traces visible in Jaeger for monolith requests
- [ ] Structured logs flowing to ELK
- [ ] Feature flags operational (all flags OFF)

---

### Phase 1: Extract Consumer Service (Weeks 5-7)

**Rationale:** Consumer Service has the **lowest coupling** of any domain. It has no outgoing dependencies on other services. It is only depended upon by OrderService (via `consumerService.validateOrderForConsumer()`), and this dependency can be replaced with a Kafka event.

#### Step 1.1: Create Consumer Service Application (Week 5)

**New project structure:**
```
consumer-service/
  src/main/java/net/chrisrichardson/ftgo/consumerservice/
    ConsumerServiceApplication.java      # @SpringBootApplication
    config/
      SecurityConfig.java               # OAuth2 resource server
      KafkaProducerConfig.java          # Event publishing config
    domain/
      Consumer.java                     # Moved from ftgo-domain
      ConsumerRepository.java           # Moved from ftgo-domain
      ConsumerService.java              # Moved from ftgo-consumer-service
    web/
      ConsumerController.java           # Moved from ftgo-consumer-service
      CreateConsumerRequest.java
      CreateConsumerResponse.java
      GetConsumerResponse.java
    events/
      ConsumerEventPublisher.java       # Publishes to consumer-events topic
      ConsumerValidated.java
      ConsumerValidationFailed.java
      ConsumerCreated.java
  src/main/resources/
    application.yml                     # Points to consumer_db
    db/migration/V1__create_consumer_db.sql
  build.gradle                          # Depends on ftgo-common-lib, spring-kafka
```

**Key changes to `ConsumerService`:**
```java
@Service
@Transactional
public class ConsumerService {
    private final ConsumerRepository consumerRepository;
    private final ConsumerEventPublisher eventPublisher;

    public void validateOrderForConsumer(long consumerId, long orderId, Money orderTotal) {
        try {
            Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(ConsumerNotFoundException::new);
            consumer.validateOrderByConsumer(orderTotal);
            eventPublisher.publish(new ConsumerValidated(consumerId, orderId));
        } catch (Exception e) {
            eventPublisher.publish(new ConsumerValidationFailed(consumerId, orderId, e.getMessage()));
        }
    }
}
```

**New endpoint for event-driven validation:**
Consumer Service subscribes to `order-events` topic and listens for `OrderCreated`:
```java
@KafkaListener(topics = "order-events", groupId = "consumer-service")
public void handleOrderCreated(OrderCreated event) {
    consumerService.validateOrderForConsumer(
        event.getConsumerId(), event.getOrderId(), event.getOrderTotal());
}
```

#### Step 1.2: Deploy and Dual-Run (Week 6)

| Task | Details |
|------|---------|
| Deploy `consumer_db` MySQL instance | With Flyway migration `V1__create_consumer_db.sql` |
| Backfill consumer data | Migrate all rows from `ftgo.consumers` → `consumer_db.consumers` |
| Deploy Consumer Service | Kubernetes Deployment with HPA |
| Configure API Gateway route | Add `/consumers/**` route to Consumer Service (feature-flagged) |
| Run parallel validation | Both monolith and new service process consumer requests; compare results |

#### Step 1.3: Cut Over (Week 7)

| Task | Details |
|------|---------|
| Enable feature flag `consumer-service.extracted` | Gateway routes `/consumers/**` to new Consumer Service |
| Update monolith `OrderService` | Replace direct `consumerService.validateOrderForConsumer()` call with publishing `OrderCreated` event to Kafka and waiting for `ConsumerValidated`/`ConsumerValidationFailed` response event |
| Add new `OrderState.PENDING` | Orders start in `PENDING` state, transition to `APPROVED` upon `ConsumerValidated` |
| Remove `compile project(":ftgo-consumer-service")` | From `ftgo-order-service/build.gradle` |
| Remove `ConsumerService` import | From `OrderService.java` and `OrderConfiguration.java` |
| Update `FtgoApplicationMain` | Remove `ConsumerServiceConfiguration.class` from `@Import` |
| Verify: Create consumer via gateway | Should hit new Consumer Service |
| Verify: Create order → consumer validation | Should happen via Kafka events |

**Monolith changes at end of Phase 1:**
```java
// FtgoApplicationMain.java - ConsumerServiceConfiguration REMOVED
@Import({OrderServiceConfiguration.class,
        RestaurantServiceConfiguration.class})
public class FtgoApplicationMain { ... }
```

```java
// OrderService.java - consumer validation via events
public Order createOrder(long consumerId, long restaurantId, List<MenuItemIdAndQuantity> lineItems) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId)
        .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);
    Order order = new Order(consumerId, restaurant, orderLineItems);
    order.setPending();  // New PENDING state
    orderRepository.save(order);
    orderEventPublisher.publish(new OrderCreated(order.getId(), consumerId, order.getOrderTotal()));
    return order;
}

@KafkaListener(topics = "consumer-events", groupId = "order-service")
public void handleConsumerValidated(ConsumerValidated event) {
    Order order = orderRepository.findById(event.getOrderId()).orElseThrow();
    order.approve();
    meterRegistry.ifPresent(mr -> mr.counter("approved_orders").increment());
}
```

**Exit Criteria:**
- [ ] Consumer Service fully operational as independent microservice
- [ ] Consumer validation happens asynchronously via Kafka
- [ ] No compile-time dependency from order-service to consumer-service
- [ ] Monolith no longer handles `/consumers/**` traffic
- [ ] All existing end-to-end tests pass (adapted for async flow)

---

### Phase 2: Extract Restaurant Service (Weeks 8-10)

**Rationale:** Restaurant Service has no outgoing dependencies. OrderService depends on `RestaurantRepository` to look up menu items during order creation, but this can be replaced with a local cache.

#### Step 2.1: Create Restaurant Service Application (Week 8)

**New project structure:**
```
restaurant-service/
  src/main/java/net/chrisrichardson/ftgo/restaurantservice/
    RestaurantServiceApplication.java
    config/
      SecurityConfig.java
      KafkaProducerConfig.java
    domain/
      Restaurant.java              # Moved from ftgo-domain
      RestaurantMenu.java          # Moved from ftgo-domain
      MenuItem.java                # Moved from ftgo-domain
      RestaurantRepository.java    # Moved from ftgo-domain
      RestaurantService.java       # Moved from ftgo-restaurant-service
    web/
      RestaurantController.java    # Moved from ftgo-restaurant-service
    events/
      RestaurantEventPublisher.java
      RestaurantCreated.java
      RestaurantMenuRevised.java
  src/main/resources/
    application.yml
    db/migration/V1__create_restaurant_db.sql
```

#### Step 2.2: Deploy and Data Migration (Week 9)

| Task | Details |
|------|---------|
| Deploy `restaurant_db` MySQL instance | Flyway migration creates `restaurants` + `restaurant_menu_items` |
| Backfill restaurant data | Migrate from `ftgo.restaurants` + `ftgo.restaurant_menu_items` |
| Deploy Restaurant Service | With Kafka producer for `restaurant-events` |
| Add `restaurant_cache` table to monolith order schema | For local read-model |
| Set up event consumer in monolith | Consume `RestaurantCreated` events to populate `restaurant_cache` |

#### Step 2.3: Cut Over (Week 10)

| Task | Details |
|------|---------|
| Enable feature flag `restaurant-service.extracted` | Gateway routes `/restaurants/**` to new service |
| Update monolith `OrderService` | Replace `restaurantRepository.findById()` with local `restaurant_cache` lookup |
| Drop FK `orders_restaurant_id` | `ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id` |
| Remove `RestaurantServiceConfiguration` from monolith | From `FtgoApplicationMain.java` |
| Remove `RestaurantRepository` import from `OrderService` | Use `RestaurantCacheRepository` instead |

**Order entity change:**
```java
// Remove @ManyToOne relationship
// BEFORE:
@ManyToOne(fetch = FetchType.LAZY)
private Restaurant restaurant;

// AFTER:
private Long restaurantId;
private String restaurantName;  // Denormalized
```

**Exit Criteria:**
- [ ] Restaurant Service fully operational as independent microservice
- [ ] Order creation uses local restaurant cache
- [ ] No FK between `orders` and `restaurants` tables
- [ ] `/restaurants/**` served by new Restaurant Service
- [ ] Monolith `@Import` reduced to `OrderServiceConfiguration` only

---

### Phase 3: Extract Delivery Service (Weeks 11-14)

**Rationale:** Delivery Service (Courier domain) requires the most careful extraction because `OrderService.scheduleDelivery()` directly manipulates courier entities. This cross-boundary logic must be moved entirely into the Delivery Service.

#### Step 3.1: Create Delivery Service Application (Weeks 11-12)

**New project structure:**
```
delivery-service/
  src/main/java/net/chrisrichardson/ftgo/deliveryservice/
    DeliveryServiceApplication.java
    config/
      SecurityConfig.java
      KafkaConfig.java
    domain/
      Courier.java                  # Moved from ftgo-domain
      CourierRepository.java        # Moved from ftgo-domain
      Action.java                   # Moved from ftgo-domain
      ActionType.java               # Moved from ftgo-domain
      Plan.java                     # Moved from ftgo-domain
      CourierService.java           # Moved from ftgo-courier-service
      DeliverySchedulingService.java  # NEW - contains scheduling logic
    web/
      CourierController.java        # Moved from ftgo-courier-service
      DeliveryController.java       # NEW - scheduling API
    events/
      DeliveryEventPublisher.java
      OrderEventConsumer.java       # Consumes order-events
      DeliveryScheduled.java
```

**Key new class - `DeliverySchedulingService`:**
```java
@Service
@Transactional
public class DeliverySchedulingService {
    private final CourierRepository courierRepository;
    private final DeliveryEventPublisher eventPublisher;
    private final Random random = new Random();

    public void scheduleDelivery(long orderId, LocalDateTime readyBy) {
        List<Courier> couriers = courierRepository.findAllAvailable();
        if (couriers.isEmpty()) {
            throw new NoCouriersAvailableException();
        }
        Courier courier = couriers.get(random.nextInt(couriers.size()));
        courier.addAction(new Action(ActionType.PICKUP, orderId, null));
        courier.addAction(new Action(ActionType.DROPOFF, orderId, readyBy.plusMinutes(30)));

        eventPublisher.publish(new DeliveryScheduled(orderId, courier.getId(),
            readyBy, readyBy.plusMinutes(30)));
    }
}
```

**Note:** The `Action` entity is refactored to store `orderId` (long) instead of a JPA `@ManyToOne Order` reference, since Order is now in a different bounded context.

#### Step 3.2: Event-Driven Integration (Week 12)

**Kafka consumer in Delivery Service:**
```java
@KafkaListener(topics = "order-events", groupId = "delivery-service")
public void handleOrderAccepted(OrderAccepted event) {
    deliverySchedulingService.scheduleDelivery(event.getOrderId(), event.getReadyBy());
}
```

**Kafka consumer in Order Service (monolith):**
```java
@KafkaListener(topics = "delivery-events", groupId = "order-service")
public void handleDeliveryScheduled(DeliveryScheduled event) {
    Order order = orderRepository.findById(event.getOrderId()).orElseThrow();
    order.setAssignedCourierId(event.getCourierId());
}
```

#### Step 3.3: Data Migration and Cut Over (Weeks 13-14)

| Task | Details |
|------|---------|
| Deploy `delivery_db` MySQL instance | Flyway migration creates `courier` + `courier_actions` |
| Backfill courier data | Migrate from `ftgo.courier` + `ftgo.courier_actions` |
| Deploy Delivery Service | With Kafka consumers and producers |
| Enable feature flag `delivery-service.extracted` | Gateway routes `/couriers/**` and `/deliveries/**` |
| Remove `scheduleDelivery()` from monolith `OrderService` | Replace with event publishing |
| Drop FK `orders_assigned_courier_id` | `ALTER TABLE orders DROP FOREIGN KEY orders_assigned_courier_id` |
| Drop FK `courier_actions_order_id` | `ALTER TABLE courier_actions DROP FOREIGN KEY courier_actions_order_id` |
| Remove `CourierRepository` from monolith `OrderService` | No more direct courier access |

**Monolith OrderService after Phase 3:**
```java
public void accept(long orderId, LocalDateTime readyBy) {
    Order order = tryToFindOrder(orderId);
    order.acceptTicket(readyBy);
    orderEventPublisher.publish(new OrderAccepted(orderId, readyBy));
    // Delivery scheduling now handled by Delivery Service via event
}
```

**Exit Criteria:**
- [ ] Delivery Service fully operational as independent microservice
- [ ] `scheduleDelivery()` logic moved to Delivery Service
- [ ] OrderService publishes `OrderAccepted` event instead of directly manipulating couriers
- [ ] No FK between `orders`/`courier_actions` and `courier` tables
- [ ] `/couriers/**` served by new Delivery Service

---

### Phase 4: Extract Order Service & Decommission Monolith (Weeks 15-19)

**Rationale:** Order Service is extracted last because it is the most complex service with the most dependencies. By this point, all its outgoing dependencies (Consumer, Restaurant, Courier) have been replaced with Kafka events and local caches.

#### Step 4.1: Create Order Service Application (Weeks 15-16)

**New project structure:**
```
order-service/
  src/main/java/net/chrisrichardson/ftgo/orderservice/
    OrderServiceApplication.java
    config/
      SecurityConfig.java
      KafkaConfig.java
      ResilienceConfig.java
    domain/
      Order.java                   # Refactored: no JPA relations to Restaurant/Courier
      OrderLineItem.java           # Moved from ftgo-domain
      OrderLineItems.java          # Moved from ftgo-domain
      OrderState.java              # Moved from ftgo-domain (+ PENDING, REJECTED states)
      OrderRepository.java         # Moved from ftgo-domain
      OrderRevision.java           # Moved from ftgo-domain
      DeliveryInformation.java     # Moved from ftgo-domain
      PaymentInformation.java      # Moved from ftgo-domain
      OrderService.java            # Refactored: event-driven
    web/
      OrderController.java         # Moved from ftgo-order-service
    events/
      OrderEventPublisher.java
      ConsumerEventConsumer.java
      DeliveryEventConsumer.java
      RestaurantEventConsumer.java
    cache/
      RestaurantCacheRepository.java   # Local read-model
      RestaurantCacheEntry.java
  src/main/resources/
    application.yml
    db/migration/
      V1__create_order_db.sql
      V2__add_restaurant_cache.sql
```

#### Step 4.2: Data Migration (Week 17)

| Task | Details |
|------|---------|
| Deploy `order_db` MySQL instance | Flyway creates `orders`, `order_line_items`, `restaurant_cache` |
| Backfill order data | Migrate from `ftgo.orders` + `ftgo.order_line_items` |
| Backfill restaurant cache | Populate from current `restaurant_db` or replay `restaurant-events` |
| Verify data consistency | Reconciliation between old and new databases |

#### Step 4.3: Cut Over and Monolith Decommission (Weeks 18-19)

| Task | Details |
|------|---------|
| Enable feature flag `order-service.extracted` | Gateway routes `/orders/**` to new Order Service |
| Verify all order lifecycle flows | Create, accept, prepare, pickup, deliver, cancel |
| Verify saga flow | Create order → consumer validation → approval |
| Verify delivery scheduling | Accept order → delivery scheduled → courier assigned |
| Run full end-to-end test suite | Against new microservices architecture |
| Disable monolith in gateway | Remove monolith-fallback route |
| Scale down monolith deployment | `kubectl scale deployment ftgo-monolith --replicas=0` |
| Keep monolith DB in read-only mode | For 2 weeks as safety net |
| Decommission monolith | Delete deployment, remove old code |

**Exit Criteria:**
- [ ] All four microservices operational and serving production traffic
- [ ] Monolith receives zero traffic
- [ ] All end-to-end tests pass against microservices
- [ ] Monolith scaled to 0 replicas
- [ ] Old `ftgo` database in read-only mode (safety net)

---

### Phase 5: Hardening (Weeks 20-22)

| Task | Details |
|------|---------|
| Performance testing | Load test each service independently; identify bottlenecks |
| Chaos engineering | Use Chaos Monkey / Litmus to test failure scenarios |
| Tune Kafka consumer groups | Adjust partition counts and consumer group sizing |
| Tune circuit breaker thresholds | Based on production latency data |
| Finalize Grafana dashboards | Production-ready alerting rules |
| Create runbooks | For each service: deployment, rollback, incident response |
| Decommission old `ftgo` database | Drop tables, release storage |
| Security audit | Penetration testing of API Gateway + OAuth2 flow |
| Documentation | API documentation (SpringDoc OpenAPI), architecture decision records |

---

## 11. Risk Mitigation

### 11.1 Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Data inconsistency during dual-write | Medium | High | Use Debezium CDC instead of application-level dual-write; run reconciliation jobs |
| Kafka message loss | Low | High | Configure `acks=all`, `min.insync.replicas=2`; enable idempotent producer |
| Saga timeout (consumer validation) | Medium | Medium | Set 30s saga timeout; order auto-rejected on timeout; dead letter queue for failed events |
| Performance degradation from network hops | Medium | Medium | Local caches for read-heavy data; async where possible; connection pooling |
| Team unfamiliarity with event-driven patterns | High | Medium | Conduct workshops on Kafka, saga patterns; pair programming during first extraction |
| Partial migration state (service half-extracted) | Medium | High | Feature flags enable instant rollback to monolith routing; no destructive DB changes until verified |

### 11.2 Rollback Strategy

Each phase is designed to be independently rollable:

1. **Feature flags** control API Gateway routing. Disabling a flag instantly routes traffic back to the monolith.
2. **Old database tables are preserved** until the subsequent phase is complete. Data can be reconciled.
3. **Kafka consumers use consumer groups** with committed offsets. Replay is possible if needed.
4. **Monolith code changes are additive** (event publishers added, not removed) until the final cut-over.

### 11.3 Backward Compatibility Guarantees

- API contracts remain identical during migration (same paths, same request/response DTOs)
- The only user-visible change is the addition of `PENDING` order state (orders briefly await async consumer validation)
- All existing API clients continue to work without changes

---

## 12. Appendices

### Appendix A: Technology Versions

| Technology | Version | Notes |
|-----------|---------|-------|
| Java | 17+ | Upgrade from Java 8 as part of Phase 0 |
| Spring Boot | 3.2.x | Upgrade from 2.0.3 (required for Spring Cloud 2023.x) |
| Spring Cloud | 2023.0.x | Gateway, Config, Circuit Breaker |
| Apache Kafka | 3.6+ | With KRaft mode (no Zookeeper) or Zookeeper 3.8 |
| Keycloak | 23.x | Identity Provider |
| Resilience4j | 2.x | Circuit breaker, retry, bulkhead |
| Micrometer | 1.12+ | Metrics + Tracing bridge |
| OpenTelemetry | 1.32+ | Distributed tracing |
| Flyway | 9.x | Database migration per service |
| MySQL | 8.0 | Per-service databases |
| Kubernetes | 1.28+ | Orchestration |

### Appendix B: Kafka Event Schema Examples

**OrderCreated (Avro)**
```json
{
  "type": "record",
  "name": "OrderCreated",
  "namespace": "net.chrisrichardson.ftgo.orderservice.events",
  "fields": [
    {"name": "orderId", "type": "long"},
    {"name": "consumerId", "type": "long"},
    {"name": "restaurantId", "type": "long"},
    {"name": "orderTotal", "type": {"type": "bytes", "logicalType": "decimal", "precision": 19, "scale": 2}},
    {"name": "lineItems", "type": {"type": "array", "items": {
      "type": "record", "name": "LineItem", "fields": [
        {"name": "menuItemId", "type": "string"},
        {"name": "name", "type": "string"},
        {"name": "price", "type": {"type": "bytes", "logicalType": "decimal", "precision": 19, "scale": 2}},
        {"name": "quantity", "type": "int"}
      ]
    }}},
    {"name": "timestamp", "type": {"type": "long", "logicalType": "timestamp-millis"}}
  ]
}
```

**DeliveryScheduled (Avro)**
```json
{
  "type": "record",
  "name": "DeliveryScheduled",
  "namespace": "net.chrisrichardson.ftgo.deliveryservice.events",
  "fields": [
    {"name": "orderId", "type": "long"},
    {"name": "courierId", "type": "long"},
    {"name": "pickupTime", "type": ["null", {"type": "long", "logicalType": "timestamp-millis"}]},
    {"name": "dropoffTime", "type": {"type": "long", "logicalType": "timestamp-millis"}},
    {"name": "timestamp", "type": {"type": "long", "logicalType": "timestamp-millis"}}
  ]
}
```

### Appendix C: Migration Checklist Template (Per Service)

```
[ ] New service project scaffolded with Spring Boot 3.x
[ ] Entities moved from ftgo-domain to service's domain package
[ ] Repository interfaces moved to service's domain package
[ ] Flyway migration script created for new database
[ ] New MySQL database instance deployed
[ ] Historical data backfilled from monolith DB
[ ] Data reconciliation passed
[ ] Kafka producer configured and publishing events
[ ] Kafka consumer configured (if applicable)
[ ] Security configured (OAuth2 resource server)
[ ] Observability configured (tracing, logging, metrics)
[ ] Health check endpoint operational
[ ] API Gateway route configured (feature-flagged)
[ ] Service deployed to Kubernetes
[ ] Integration tests passing
[ ] Feature flag enabled (traffic routed to new service)
[ ] Monolith dependencies removed (build.gradle, imports, configuration)
[ ] Cross-service FK constraints dropped
[ ] End-to-end tests passing
[ ] Performance baseline established
[ ] Runbook documented
```

### Appendix D: Dependency Removal Sequence

```
Phase 1 (Consumer):
  ftgo-order-service/build.gradle:
    REMOVE: compile project(":ftgo-consumer-service")
  OrderService.java:
    REMOVE: import ConsumerService
    REMOVE: private ConsumerService consumerService
    REMOVE: consumerService.validateOrderForConsumer(...)
    ADD:    orderEventPublisher.publish(new OrderCreated(...))
  OrderConfiguration.java:
    REMOVE: ConsumerService parameter from orderService() bean
  FtgoApplicationMain.java:
    REMOVE: ConsumerServiceConfiguration.class from @Import

Phase 2 (Restaurant):
  OrderService.java:
    REMOVE: import RestaurantRepository
    REMOVE: private RestaurantRepository restaurantRepository
    REPLACE: restaurantRepository.findById() → restaurantCacheRepository.findById()
  Order.java:
    REMOVE: @ManyToOne Restaurant restaurant
    ADD:    private Long restaurantId; private String restaurantName;
  FtgoApplicationMain.java:
    REMOVE: RestaurantServiceConfiguration.class from @Import

Phase 3 (Delivery):
  OrderService.java:
    REMOVE: import CourierRepository
    REMOVE: private CourierRepository courierRepository
    REMOVE: scheduleDelivery() method entirely
    ADD:    orderEventPublisher.publish(new OrderAccepted(...)) in accept()
  Order.java:
    REMOVE: @ManyToOne Courier assignedCourier
    ADD:    private Long assignedCourierId

Phase 4 (Order):
  ftgo-application module: DECOMMISSIONED
  ftgo-domain module: DECOMMISSIONED (entities distributed to services)
```
