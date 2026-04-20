# FTGO Microservices Migration Roadmap

## Overview

This document outlines the step-by-step plan for migrating the FTGO monolith to a microservices architecture using the **Strangler Fig pattern**. The migration is designed to be gradual, reversible, and safe for production systems.

## Migration Phases

### Phase 0: Infrastructure Foundation (Current Phase)

**Duration**: 2-3 weeks
**Status**: In Progress

#### Objectives
- Set up event-driven communication infrastructure (Kafka + Outbox pattern)
- Deploy service discovery (Eureka) and API Gateway (Spring Cloud Gateway)
- Implement security layer (JWT authentication)
- Add distributed tracing (Sleuth + Zipkin) and enhanced observability
- Implement resilience patterns (circuit breakers, retries)

#### Deliverables
- [x] Event infrastructure module with Outbox pattern (`ftgo-event-infrastructure`)
- [x] Eureka service discovery server (`ftgo-service-discovery`)
- [x] Spring Cloud Gateway with route configuration (`ftgo-api-gateway`)
- [x] JWT security module (`ftgo-security`)
- [x] Observability module with Sleuth/Zipkin integration (`ftgo-observability`)
- [x] Resilience module with Resilience4j (`ftgo-resilience`)
- [x] Flyway migration for outbox table
- [x] Updated docker-compose with Kafka, Zookeeper, Zipkin, Eureka
- [x] Architecture design document
- [x] Migration roadmap (this document)

#### Success Criteria
- All infrastructure modules compile and integrate with existing monolith
- Monolith registers with Eureka and is accessible via API Gateway
- Events can be published to outbox and relayed to Kafka
- JWT authentication works end-to-end through gateway
- Distributed traces visible in Zipkin
- Circuit breakers can be configured per service call

#### Rollback Plan
- Infrastructure modules are additive; disable by removing imports
- API Gateway can be bypassed; clients can connect directly to monolith
- Event publishing is transactional; disable outbox relay to stop event flow

---

### Phase 1: Extract Consumer Service

**Duration**: 2-3 weeks
**Dependencies**: Phase 0 complete

#### Rationale
Consumer Service has the **lowest coupling** to other services:
- No dependencies on other domain services
- Only 3 methods: `create`, `findById`, `validateOrderForConsumer`
- Owns only the `consumers` table
- Other services depend on it, but it depends on nothing

#### Steps

1. **Create standalone Consumer Service application**
   - New Spring Boot application with its own `main` class
   - Own database connection to `consumers` table
   - Register with Eureka as `consumer-service`

2. **Implement Consumer Events**
   - `ConsumerCreated` event published when consumer is created
   - `ConsumerValidated` / `ConsumerValidationFailed` events for order validation

3. **Create Anti-Corruption Layer in Order Service**
   - Replace direct `ConsumerService.validateOrderForConsumer()` call
   - Implement `ConsumerServiceProxy` that uses REST + circuit breaker
   - Add event listener for consumer validation responses (Saga step)

4. **Update API Gateway Routes**
   - Route `/api/consumers/**` to new Consumer microservice
   - Keep monolith as fallback during transition

5. **Database Migration**
   - Consumer Service gets its own database/schema
   - Remove `consumers` table from monolith's Flyway migrations
   - Run data migration script to copy existing consumers

6. **Parallel Running & Verification**
   - Run both monolith consumer endpoints and new service
   - Compare responses using shadow traffic
   - Gradually shift traffic from monolith to microservice

#### Success Criteria
- Consumer Service runs independently with its own database
- Order creation works via async consumer validation (Saga)
- No data loss during migration
- Response times within 10% of monolith performance

#### Rollback Plan
- Revert API Gateway routes to monolith
- Re-enable direct ConsumerService call in OrderService
- Consumer data remains in monolith database as backup

---

### Phase 2: Extract Restaurant Service

**Duration**: 2-3 weeks
**Dependencies**: Phase 1 complete

#### Rationale
Restaurant Service has **moderate coupling**:
- Owns `restaurants` and `restaurant_menu_items` tables
- Order Service reads restaurant data for order creation
- No dependencies on other services

#### Steps

1. **Create standalone Restaurant Service application**
   - Own database with `restaurants` and `restaurant_menu_items` tables
   - Register with Eureka as `restaurant-service`

2. **Implement Restaurant Events**
   - `RestaurantCreated` event
   - `RestaurantMenuRevised` event (for Order Service to update cached menus)

3. **Create Anti-Corruption Layer in Order Service**
   - Replace direct `RestaurantRepository` access
   - Implement `RestaurantServiceProxy` with REST client + circuit breaker
   - Cache restaurant/menu data locally in Order Service
   - Listen for `RestaurantMenuRevised` events to invalidate cache

4. **Remove Foreign Key: `orders.restaurant_id`**
   - Replace JPA `@ManyToOne` relationship with plain `Long restaurantId`
   - Store restaurant name denormalized in order for display

5. **Update API Gateway Routes**
   - Route `/api/restaurants/**` to new Restaurant microservice

6. **Database Migration**
   - Restaurant Service gets its own database
   - Migrate restaurant data
   - Update Order entity to store `restaurantId` as plain field

#### Success Criteria
- Restaurant Service runs independently
- Order creation works with REST-based restaurant lookup
- Menu data cached and refreshed via events
- Foreign key removed without data integrity issues

#### Rollback Plan
- Revert API Gateway routes
- Restore `@ManyToOne` relationship in Order entity
- Restaurant data remains in monolith database

---

### Phase 3: Extract Delivery Service (from Courier Service)

**Duration**: 3-4 weeks
**Dependencies**: Phase 2 complete

#### Rationale
Delivery/Courier Service has **moderate coupling**:
- Owns `courier` and `courier_actions` tables
- Order Service directly accesses `CourierRepository` for delivery scheduling
- Courier assignment currently happens synchronously in Order Service

#### Steps

1. **Create standalone Delivery Service application**
   - Own database with `courier` and `courier_actions` tables
   - Register with Eureka as `delivery-service`

2. **Implement Delivery Events**
   - `CourierCreated`, `CourierAvailabilityUpdated` events
   - `DeliveryScheduled`, `DeliveryPickedUp`, `DeliveryCompleted` events

3. **Decouple Delivery Scheduling from Order Service**
   - Move `scheduleDelivery()` logic to Delivery Service
   - Order Service publishes `OrderConfirmed` event
   - Delivery Service listens and schedules delivery asynchronously
   - Delivery Service publishes `CourierAssigned` event back

4. **Remove Foreign Keys**
   - Remove `orders.assigned_courier_id` FK
   - Remove `courier_actions.order_id` FK
   - Store as plain ID fields

5. **Update API Gateway Routes**
   - Route `/api/couriers/**` to new Delivery microservice

6. **Database Migration**
   - Delivery Service gets its own database
   - Migrate courier and actions data

#### Success Criteria
- Delivery scheduling works asynchronously via events
- Courier assignment reflected in Order via events
- No foreign key constraints between services
- Delivery tracking works independently

#### Rollback Plan
- Revert to synchronous delivery scheduling
- Restore foreign key relationships
- Revert API Gateway routes

---

### Phase 4: Extract Order Service

**Duration**: 4-6 weeks
**Dependencies**: Phases 1-3 complete

#### Rationale
Order Service is extracted **last** because it has the **most dependencies**:
- Depends on Consumer, Restaurant, and Delivery services
- Most complex business logic (order creation saga)
- Highest risk due to being the core business flow

#### Steps

1. **Create standalone Order Service application**
   - Own database with `orders` and `order_line_items` tables
   - Register with Eureka as `order-service`

2. **Implement Order Creation Saga**
   - Orchestrator-based saga for order creation
   - Steps: Create Order -> Validate Consumer -> Confirm Restaurant -> Schedule Delivery
   - Compensating transactions for each step

3. **Implement All Order Events**
   - `OrderCreated`, `OrderApproved`, `OrderCancelled`, `OrderRevised`
   - `OrderPreparing`, `OrderReadyForPickup`, `OrderPickedUp`, `OrderDelivered`

4. **Replace All Direct Dependencies**
   - All cross-service calls go through REST + circuit breaker
   - All state changes communicated via events

5. **Full API Gateway Migration**
   - All routes point to individual microservices
   - Remove monolith fallback route

6. **Decommission Monolith**
   - Verify all functionality works via microservices
   - Keep monolith in standby for 2-4 weeks
   - Gradually decommission

#### Success Criteria
- Complete order lifecycle works via microservices
- Saga handles all failure scenarios with compensation
- Performance within acceptable bounds
- All monitoring and alerting in place

#### Rollback Plan
- Re-enable monolith as primary
- API Gateway routes back to monolith
- Event replay for data consistency

---

### Phase 5: Post-Migration Optimization

**Duration**: Ongoing
**Dependencies**: Phase 4 complete

#### Activities
1. **Performance Optimization**
   - Tune circuit breaker thresholds based on production data
   - Optimize Kafka consumer group configurations
   - Add caching layers where needed

2. **Operational Excellence**
   - Set up alerting for saga failures
   - Create runbooks for common failure scenarios
   - Implement chaos engineering tests

3. **Advanced Patterns**
   - Implement CQRS for complex queries that span services
   - Add event sourcing where beneficial
   - Consider service mesh (Istio) for advanced traffic management

4. **Infrastructure Evolution**
   - Containerize with Kubernetes
   - Implement CI/CD per service
   - Add centralized configuration (Spring Cloud Config)

---

## Risk Matrix

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Data inconsistency during migration | Medium | High | Dual-write with reconciliation, event replay |
| Performance degradation | Medium | Medium | Circuit breakers, caching, performance testing |
| Saga failure handling | High | High | Comprehensive compensation logic, dead letter queues |
| Team learning curve | Medium | Medium | Training, pair programming, incremental approach |
| Increased operational complexity | High | Medium | Observability stack, runbooks, automation |

## Timeline Summary

| Phase | Duration | Cumulative |
|-------|----------|-----------|
| Phase 0: Infrastructure | 2-3 weeks | 2-3 weeks |
| Phase 1: Consumer Service | 2-3 weeks | 4-6 weeks |
| Phase 2: Restaurant Service | 2-3 weeks | 6-9 weeks |
| Phase 3: Delivery Service | 3-4 weeks | 9-13 weeks |
| Phase 4: Order Service | 4-6 weeks | 13-19 weeks |
| Phase 5: Optimization | Ongoing | - |

**Total estimated migration time: 13-19 weeks (3-5 months)**

## Key Principles

1. **Always maintain a working system** - No big bang migrations
2. **Strangler Fig pattern** - New routes through gateway, old routes still work
3. **Database per service** - Each service owns its data exclusively
4. **Event-driven communication** - Async where possible, sync only for queries
5. **Reversibility** - Every phase has a documented rollback plan
6. **Observability first** - Monitor everything before, during, and after migration
7. **Incremental value delivery** - Each phase delivers independently useful improvements
