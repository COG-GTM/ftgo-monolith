# Per-Service Database Schema Migration Strategy

## Overview

This document defines the strategy for transitioning from the current single shared MySQL database
(`ftgo`) to independent per-service databases, each with its own Flyway migration history. This is
a core requirement of the monolith-to-microservices decomposition.

### Current State (Monolith)

- **Single database**: `ftgo` on MySQL
- **Single Flyway migration set**: `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql`
- **Flyway version**: 6.0.0 (configured in `ftgo-flyway/build.gradle`)
- **7 tables** in a single schema with cross-table foreign keys
- **Shared `hibernate_sequence`** table for ID generation across all entities

### Target State (Microservices)

Each service owns its own database with independent schema and migration history:

| Service             | Database Name              | Tables                                  |
|---------------------|----------------------------|-----------------------------------------|
| Consumer Service    | `ftgo_consumer_service`    | `consumers`, `consumer_sequence`        |
| Order Service       | `ftgo_order_service`       | `orders`, `order_line_items`, `order_sequence` |
| Courier Service     | `ftgo_courier_service`     | `courier`, `courier_actions`, `courier_sequence` |
| Restaurant Service  | `ftgo_restaurant_service`  | `restaurants`, `restaurant_menu_items`, `restaurant_sequence` |

---

## 1. Per-Service Database Schema Design

### 1.1 Consumer Service (`ftgo_consumer_service`)

**Tables:**
- `consumers` - Consumer aggregate root (id, first_name, last_name, created_at, updated_at)
- `consumer_sequence` - Per-service ID generation

**Changes from monolith:**
- `id` changed from manual assignment to `AUTO_INCREMENT`
- Added `created_at` / `updated_at` audit columns
- Added `NOT NULL` constraints on `first_name`, `last_name`
- UTF-8mb4 charset for full Unicode support

### 1.2 Order Service (`ftgo_order_service`)

**Tables:**
- `orders` - Order aggregate root with all delivery/ticket state fields
- `order_line_items` - Embedded line items within Order aggregate
- `order_sequence` - Per-service ID generation

**Changes from monolith:**
- Removed FK `orders.assigned_courier_id -> courier(id)` (cross-service)
- Removed FK `orders.restaurant_id -> restaurants(id)` (cross-service)
- Retained `consumer_id`, `restaurant_id`, `assigned_courier_id` as plain BIGINT columns
  with SQL comments documenting their cross-service nature
- Added `AUTO_INCREMENT` primary key on `order_line_items`
- Added `NOT NULL` on `consumer_id`, `restaurant_id`
- Added indexes on cross-service reference columns for query performance
- Added `created_at` / `updated_at` audit columns

### 1.3 Courier Service (`ftgo_courier_service`)

**Tables:**
- `courier` - Courier aggregate root with availability and address
- `courier_actions` - Delivery plan actions (part of Courier aggregate)
- `courier_sequence` - Per-service ID generation

**Changes from monolith:**
- Removed FK `courier_actions.order_id -> orders(id)` (cross-service)
- Retained `order_id` as plain BIGINT with SQL comment
- Added `AUTO_INCREMENT` primary key on `courier_actions`
- Added `NOT NULL` on `first_name`, `last_name`
- Added `created_at` / `updated_at` audit columns

### 1.4 Restaurant Service (`ftgo_restaurant_service`)

**Tables:**
- `restaurants` - Restaurant aggregate root
- `restaurant_menu_items` - Menu items (part of Restaurant aggregate)
- `restaurant_sequence` - Per-service ID generation

**Changes from monolith:**
- `restaurant_menu_items` retains original column structure (`id VARCHAR(255)` as business
  menu-item identifier) to preserve JPA `MenuItem.id` mapping compatibility. No surrogate PK
  is added since this is an embedded collection table (`@Embeddable`)
- Added `NOT NULL` on `restaurants.name`
- Added `created_at` / `updated_at` audit columns on `restaurants`

---

## 2. Cross-Service Foreign Key Removal Plan

### 2.1 Foreign Keys Being Removed

The monolith's `V1__create_ftgo_db.sql` defines the following cross-service foreign keys that
**must be removed** in the microservices architecture:

| FK Constraint                      | Source Table       | Target Table  | Resolution                          |
|------------------------------------|--------------------|---------------|-------------------------------------|
| `courier_actions_order_id`         | `courier_actions`  | `orders`      | Retain `order_id` as BIGINT, no FK  |
| `orders_assigned_courier_id`       | `orders`           | `courier`     | Retain `assigned_courier_id` as BIGINT, no FK |
| `orders_restaurant_id`             | `orders`           | `restaurants` | Retain `restaurant_id` as BIGINT, no FK |

### 2.2 Intra-Service Foreign Keys Retained

These FKs reference tables within the same service and are **kept**:

| FK Constraint                      | Source Table            | Target Table   | Service            |
|------------------------------------|-------------------------|----------------|--------------------|
| `fk_order_line_items_order`        | `order_line_items`      | `orders`       | Order Service      |
| `fk_courier_actions_courier`       | `courier_actions`       | `courier`      | Courier Service    |
| `fk_menu_items_restaurant`         | `restaurant_menu_items` | `restaurants`  | Restaurant Service |

### 2.3 Data Consistency Without Foreign Keys

Cross-service references are enforced through:

1. **Application-level validation**: Services validate referenced IDs via synchronous API calls
   before persisting (e.g., Order Service calls Consumer Service to verify `consumer_id` exists
   before creating an order).

2. **Eventual consistency via domain events**: When an entity is deleted or modified in the
   owning service, a domain event is published (e.g., `ConsumerDeleted`). Subscribing services
   react by updating or soft-deleting related records.

3. **Soft deletes**: Cross-referenced entities use soft deletes (logical deletion) rather than
   hard deletes to prevent dangling references. A future migration (V2) can add `deleted_at`
   columns as needed.

4. **Orphan detection jobs**: Periodic background jobs detect and report orphaned references
   (e.g., orders referencing non-existent restaurants) for manual or automated remediation.

---

## 3. Per-Service Flyway Migration Structure

### 3.1 Directory Layout

Each microservice maintains its own Flyway migration directory following the standard convention:

```
services/
  ftgo-consumer-service/
    src/main/resources/db/migration/
      V1__create_consumer_service_schema.sql
  ftgo-order-service/
    src/main/resources/db/migration/
      V1__create_order_service_schema.sql
  ftgo-courier-service/
    src/main/resources/db/migration/
      V1__create_courier_service_schema.sql
  ftgo-restaurant-service/
    src/main/resources/db/migration/
      V1__create_restaurant_service_schema.sql
```

### 3.2 Naming Conventions

Migration files follow the Flyway naming convention:

```
V{version}__{description}.sql
```

- **Version**: Integer, monotonically increasing per service (V1, V2, V3...)
- **Separator**: Double underscore (`__`)
- **Description**: Snake_case, descriptive of changes
- **Each service's version history is independent** (all start at V1)

Examples:
```
V1__create_consumer_service_schema.sql    (initial schema)
V2__add_consumer_email_column.sql         (future evolution)
V3__create_consumer_preferences_table.sql (future evolution)
```

### 3.3 Flyway Configuration (Per-Service `application.yml`)

Each service configures Flyway to target its own database:

```yaml
# Example: Consumer Service application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/ftgo_consumer_service?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USER:ftgo_consumer}
    password: ${DB_PASSWORD:}
```

### 3.4 Flyway Best Practices

1. **Immutable migrations**: Never modify a migration after it has been applied. Create a new
   versioned migration for any changes.
2. **Backward compatibility**: Each migration should be backward-compatible with the previous
   application version to support rolling deployments.
3. **No DDL + DML in same migration**: Separate schema changes (DDL) from data changes (DML)
   into different migration files for safer rollback.
4. **Test migrations**: All migrations are tested in CI via an embedded H2 or Testcontainers
   MySQL instance.

---

## 4. ID Generation Strategy

### 4.1 Replacing `hibernate_sequence`

The monolith uses a single shared `hibernate_sequence` table for ID generation across all
entities. This must be replaced with per-service ID generation to avoid cross-service coupling.

### 4.2 Chosen Strategy: AUTO_INCREMENT + Per-Service Sequence Tables

**Primary approach**: MySQL `AUTO_INCREMENT` on primary key columns.

- Simple, performant, and well-supported by Spring Data JPA / Hibernate
- Each service's database has its own auto-increment counter
- No risk of ID collision since each service has its own database

**Fallback for non-PK sequences**: Per-service sequence tables (`<service>_sequence`) are
provided for cases where Hibernate's `TABLE` strategy or custom ID generation is needed
(e.g., batch inserts, pre-allocated ID ranges).

### 4.3 JPA Entity Configuration

```java
@Entity
@Table(name = "consumers")
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Uses AUTO_INCREMENT
    private Long id;
    // ...
}
```

### 4.4 Future Consideration: Snowflake IDs

If globally unique IDs across services become necessary (e.g., for distributed tracing or
event sourcing), consider migrating to Snowflake-style IDs:

- 64-bit IDs encoding: timestamp (41 bits) + service ID (5 bits) + sequence (12 bits)
- Globally unique without coordination
- Sortable by creation time
- Can be introduced as a V2 migration when needed

---

## 5. Data Synchronization Approach

### 5.1 Event-Driven Architecture (Primary)

Cross-service data is synchronized using **domain events** published to a message broker
(e.g., Apache Kafka, RabbitMQ, or Eventuate). This is the primary mechanism for maintaining
data consistency without shared databases.

**Event flow examples:**

```
Restaurant Created:
  Restaurant Service --[RestaurantCreated]--> Order Service (caches restaurant name/minimum)

Consumer Created:
  Consumer Service --[ConsumerCreated]--> Order Service (caches consumer info for validation)

Order Accepted:
  Order Service --[OrderAccepted]--> Courier Service (assigns courier to order)

Courier Location Updated:
  Courier Service --[CourierLocationUpdated]--> Order Service (updates delivery ETA)
```

### 5.2 Event Schema

Events follow a standard envelope:

```json
{
  "eventId": "uuid",
  "eventType": "RestaurantCreated",
  "aggregateType": "Restaurant",
  "aggregateId": 42,
  "timestamp": "2026-03-04T12:00:00Z",
  "payload": {
    "name": "Ajanta",
    "address": { "street1": "123 Main St", "city": "Oakland" }
  }
}
```

### 5.3 Local Caching of Cross-Service Data

Services that need data owned by another service maintain **read-only local caches** populated
by consuming domain events:

| Consuming Service | Cached Data                  | Source Service      | Source Event           |
|-------------------|------------------------------|---------------------|------------------------|
| Order Service     | Restaurant name, minimum     | Restaurant Service  | `RestaurantCreated/Updated` |
| Order Service     | Consumer name                | Consumer Service    | `ConsumerCreated/Updated`   |
| Order Service     | Courier assignment status    | Courier Service     | `CourierAssigned`            |
| Courier Service   | Order delivery address       | Order Service       | `OrderAccepted`              |

These caches are stored in service-local tables (introduced in future V2+ migrations) and are
eventually consistent. The source service remains the single source of truth.

### 5.4 Synchronous API Calls (Secondary)

For real-time validation where eventual consistency is insufficient:

- **Order creation**: Order Service calls Consumer Service API to validate consumer exists
- **Order creation**: Order Service calls Restaurant Service API to validate restaurant and
  fetch current menu/prices
- **Courier assignment**: Order Service calls Courier Service API to check courier availability

API calls use the shared API modules (`shared/ftgo-*-service-api/`) for DTOs and contracts.

### 5.5 Saga Pattern for Distributed Transactions

Multi-service operations that require atomicity use the **Saga pattern** (choreography or
orchestration):

**Example: Create Order Saga**
1. Order Service creates order in `APPROVAL_PENDING` state
2. Consumer Service validates consumer (event/API)
3. Restaurant Service validates menu items and pricing (event/API)
4. Order Service transitions to `APPROVED` or rolls back to `REJECTED`

Each step is a local transaction within a single service database. Compensating transactions
handle failures (e.g., if restaurant validation fails, Order Service marks the order as rejected).

---

## 6. Rollback Strategy

### 6.1 Migration Rollback (Flyway)

- **Flyway Community Edition** does not support automatic undo migrations. Rollbacks must be
  handled manually by writing a new forward migration that reverses the changes.
- **Convention**: For each significant migration `V{N}__change.sql`, prepare a corresponding
  rollback script `rollback/R{N}__undo_change.sql` stored alongside but not auto-executed.
- **Flyway Pro/Enterprise**: If upgraded, use `U{N}__undo_change.sql` undo migrations.

### 6.2 Schema Rollback Procedure

1. Identify the failed migration version
2. Apply the corresponding rollback script manually or via Flyway repair
3. Run `flyway repair` to fix the schema history table
4. Redeploy the previous application version

### 6.3 Data Rollback

- **Before migration**: Take a full database backup of the monolith `ftgo` database
- **During migration**: Each service migration step is idempotent and can be rerun
- **After migration**: If rollback is needed, restore from backup and redeploy monolith

### 6.4 Blue-Green Database Migration

For zero-downtime migration from monolith to per-service databases:

1. **Phase A**: Create per-service databases and run V1 migrations (empty schemas)
2. **Phase B**: Deploy dual-write mode - monolith writes to both old and new databases
3. **Phase C**: Backfill historical data from monolith to per-service databases
4. **Phase D**: Switch reads to per-service databases (verify data consistency)
5. **Phase E**: Disable writes to monolith database
6. **Phase F**: Decommission monolith database (after verification period)

See [Migration Runbook](database-migration-runbook.md) for detailed step-by-step procedures.
