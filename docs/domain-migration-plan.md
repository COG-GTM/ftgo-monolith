# Domain Entity Migration Plan

## Overview

This document outlines the plan for migrating domain entities from the shared `ftgo-domain`
library into service-owned modules. The current `shared-libraries/ftgo-domain` serves as a
versioned transitional library (v1.0.0) while services are incrementally decoupled.

## Current State

- **ftgo-domain** (v1.0.0): Published as `net.chrisrichardson.ftgo:ftgo-domain-lib`
- **ftgo-common-jpa** (v1.0.0): Published as `net.chrisrichardson.ftgo:ftgo-common-jpa-lib`
- All services depend on both libraries via project dependencies
- Cross-service DTOs defined in `services/*-api/` modules

## Migration Phases

### Phase 1: Extract as Versioned Libraries (Current - Complete)

- [x] Extract `ftgo-common-jpa` as versioned shared library
- [x] Extract `ftgo-domain` as versioned shared library
- [x] Define cross-service DTO/API contracts
- [x] Document entity-to-service ownership mapping

### Phase 2: Create Service-Owned Entity Copies

For each service, copy the owned entities from `ftgo-domain` into the service module:

1. **Consumer Service**
   - Copy `Consumer.java` and `ConsumerRepository.java`
   - Update package to `net.chrisrichardson.ftgo.consumerservice.domain`
   - Adjust imports and JPA configuration

2. **Restaurant Service**
   - Copy `Restaurant.java`, `RestaurantMenu.java`, `MenuItem.java`, `RestaurantRepository.java`
   - Update package to `net.chrisrichardson.ftgo.restaurantservice.domain`
   - Adjust imports and JPA configuration

3. **Order Service**
   - Copy `Order.java`, `OrderLineItem.java`, `OrderLineItems.java`, `OrderRepository.java`
   - Copy `OrderState.java`, `OrderRevision.java`, `LineItemQuantityChange.java`
   - Copy `DeliveryInformation.java`, `PaymentInformation.java`, `OrderMinimumNotMetException.java`
   - Update package to `net.chrisrichardson.ftgo.orderservice.domain`
   - Adjust imports and JPA configuration

4. **Courier Service**
   - Copy `Courier.java`, `Plan.java`, `Action.java`, `ActionType.java`, `CourierRepository.java`
   - Update package to `net.chrisrichardson.ftgo.courierservice.domain`
   - Adjust imports and JPA configuration

### Phase 3: Replace Cross-Entity References with IDs

Entities currently reference other entities directly (e.g., `Order.restaurant` is a
`@ManyToOne` to `Restaurant`). These must be replaced with ID-based references:

| Current Reference              | Replacement                       | Service Impact           |
|--------------------------------|-----------------------------------|--------------------------|
| `Order.restaurant` (ManyToOne) | `Order.restaurantId` (Long)       | Order Service            |
| `Order.assignedCourier` (ManyToOne) | `Order.assignedCourierId` (Long) | Order Service        |
| `Action.order` (ManyToOne)     | `Action.orderId` (Long)           | Courier Service          |

### Phase 4: Database Schema Decomposition

Split the shared `ftgo` database into per-service schemas:

| Service            | Tables                                    | New Schema                |
|--------------------|-------------------------------------------|---------------------------|
| Consumer Service   | `consumers`                               | `ftgo_consumer`           |
| Restaurant Service | `restaurants`, `restaurant_menu_items`    | `ftgo_restaurant`         |
| Order Service      | `orders`, `order_line_items`              | `ftgo_order`              |
| Courier Service    | `courier`, `courier_actions`              | `ftgo_courier`            |

**Foreign Key Removal:**
- `orders.restaurant_id` FK to `restaurants` — replace with eventual consistency
- `orders.assigned_courier_id` FK to `courier` — replace with eventual consistency
- `courier_actions.order_id` FK to `orders` — replace with eventual consistency

### Phase 5: Remove ftgo-domain Dependency

Once all services own their entities and cross-entity references are replaced with IDs:

1. Remove `ftgo-domain` from each service's `build.gradle`
2. Remove `DomainConfiguration` import from service configurations
3. Update integration tests to use service-owned entities
4. Deprecate and eventually remove `shared-libraries/ftgo-domain`

## Versioning Strategy

| Library              | Artifact ID              | Current Version | Bump Policy                          |
|----------------------|--------------------------|-----------------|--------------------------------------|
| ftgo-common          | ftgo-common-lib          | 1.0.0           | Minor for additions, Major for breaks|
| ftgo-common-jpa      | ftgo-common-jpa-lib      | 1.0.0           | Minor for additions, Major for breaks|
| ftgo-domain          | ftgo-domain-lib          | 1.0.0           | Minor for additions, Major for breaks|

Libraries follow semantic versioning:
- **Patch** (1.0.x): Bug fixes, no API changes
- **Minor** (1.x.0): New entities/methods, backward compatible
- **Major** (x.0.0): Breaking changes (entity removal, package changes)

## Risks and Mitigations

| Risk                                    | Mitigation                                           |
|-----------------------------------------|------------------------------------------------------|
| Dual entity definitions during migration| Version shared library, pin services to specific versions |
| Foreign key removal breaks data integrity | Implement saga patterns for cross-service consistency |
| Shared database during transition       | Use schema-level isolation before full DB split       |
| Test coverage gaps                      | Maintain integration tests against shared library     |
