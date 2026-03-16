# Database-Per-Service Migration Strategy

## Overview

This document describes the strategy for migrating from the FTGO monolith's single
shared MySQL database to independent per-service database schemas. This is a
foundational step in the microservices extraction — each service must own its data
exclusively to achieve loose coupling and independent deployability.

## Current State (Monolith)

The monolith uses a single `ftgo` MySQL database managed by one Flyway migration
set (`ftgo-flyway/`). All tables live in the same schema:

| Table                  | Owner (future)       | Description                        |
|------------------------|----------------------|------------------------------------|
| `consumers`            | Consumer Service     | Customer profiles                  |
| `orders`               | Order Service        | Order records                      |
| `order_line_items`     | Order Service        | Line items within an order         |
| `restaurants`          | Restaurant Service   | Restaurant profiles                |
| `restaurant_menu_items`| Restaurant Service   | Menu items per restaurant          |
| `courier`              | Courier Service      | Courier profiles and availability  |
| `courier_actions`      | Courier Service      | Actions/events per courier         |
| `hibernate_sequence`   | (shared)             | Single shared ID sequence          |

### Cross-Table Foreign Keys (Monolith)

The monolith enforces the following cross-service FK constraints:

| FK Constraint                              | From Table         | To Table      | Cross-Service? |
|--------------------------------------------|--------------------|---------------|----------------|
| `courier_actions_order_id`                 | `courier_actions`  | `orders`      | Yes            |
| `courier_actions_courier_id`               | `courier_actions`  | `courier`     | No (same svc)  |
| `order_line_items_id`                      | `order_line_items` | `orders`      | No (same svc)  |
| `orders_assigned_courier_id`               | `orders`           | `courier`     | Yes            |
| `orders_restaurant_id`                     | `orders`           | `restaurants` | Yes            |
| `restaurant_menu_items_restaurant_id`      | `restaurant_menu_items` | `restaurants` | No (same svc) |

## Target State (Microservices)

Each service gets its own database schema and Flyway migration set:

```
services/
  order-service/src/main/resources/db/migration/
    V1__create_order_service_schema.sql
  consumer-service/src/main/resources/db/migration/
    V1__create_consumer_service_schema.sql
  restaurant-service/src/main/resources/db/migration/
    V1__create_restaurant_service_schema.sql
  courier-service/src/main/resources/db/migration/
    V1__create_courier_service_schema.sql
```

### Per-Service Database Configuration

Each service will connect to its own database:

| Service              | Database Name              | Port  |
|----------------------|----------------------------|-------|
| Order Service        | `ftgo_order_service`       | 8081  |
| Consumer Service     | `ftgo_consumer_service`    | 8082  |
| Restaurant Service   | `ftgo_restaurant_service`  | 8083  |
| Courier Service      | `ftgo_courier_service`     | 8084  |

## Key Design Decisions

### 1. Cross-Service Foreign Keys Removed

All foreign key constraints that span service boundaries have been removed.
Cross-service references are retained as plain `BIGINT` columns with indexes
for query performance, but referential integrity is enforced at the application
level through eventual consistency patterns.

**Removed FK constraints:**

| Original FK                    | Table → Referenced Table       | Replacement                                    |
|--------------------------------|--------------------------------|------------------------------------------------|
| `orders_assigned_courier_id`   | `orders` → `courier`          | Soft reference + index; validated via API call  |
| `orders_restaurant_id`         | `orders` → `restaurants`      | Soft reference + index; validated via API call  |
| `courier_actions_order_id`     | `courier_actions` → `orders`  | Soft reference + index; validated via API call  |

**Retained FK constraints (intra-service):**

| FK                                        | Table → Referenced Table             | Reason                     |
|-------------------------------------------|--------------------------------------|----------------------------|
| `fk_order_line_items_order_id`            | `order_line_items` → `orders`        | Same service (Order)       |
| `fk_courier_actions_courier_id`           | `courier_actions` → `courier`        | Same service (Courier)     |
| `fk_restaurant_menu_items_restaurant_id`  | `restaurant_menu_items` → `restaurants` | Same service (Restaurant) |

### 2. Per-Service ID Generation

The monolith uses a single `hibernate_sequence` table shared across all services.
In the microservices architecture, each service has its own sequence table:

| Service              | Sequence Table                  |
|----------------------|---------------------------------|
| Order Service        | `order_service_sequence`        |
| Consumer Service     | `consumer_service_sequence`     |
| Restaurant Service   | `restaurant_service_sequence`   |
| Courier Service      | `courier_service_sequence`      |

**Why per-service sequences?**
- Eliminates cross-service coordination for ID generation
- Each service can be deployed and scaled independently
- No single point of contention for ID allocation
- Tables with `AUTO_INCREMENT` primary keys (orders, courier, restaurants)
  continue to use MySQL's native auto-increment; the sequence table is for
  entities that use Hibernate's `TABLE` generation strategy (e.g., consumers)

**Future consideration:** Services may migrate to UUIDs or distributed ID
generation (e.g., Snowflake IDs) for globally unique identifiers across services.
This is deferred to a later phase.

### 3. Flyway Configuration

Each service configures Flyway independently via `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

Each service's Flyway migration history (`flyway_schema_history`) is stored in
its own database, providing complete isolation.

## Migration Phases

### Phase 1: Schema Definition (This Task)
- Define per-service Flyway migration scripts
- Document table ownership and FK removal decisions
- Establish per-service ID generation strategy

### Phase 2: Data Migration (Future)
- Deploy per-service databases alongside the monolith database
- Run ETL scripts to copy data from the monolith to per-service databases
- Validate data integrity post-migration

### Phase 3: Dual-Write Period (Future)
- Services write to both old and new databases during transition
- Read from new per-service databases
- Monitor for data consistency issues

### Phase 4: Cutover (Future)
- Switch all reads/writes to per-service databases
- Decommission monolith database connections
- Remove dual-write logic

### Phase 5: Cleanup (Future)
- Archive monolith `ftgo` database
- Remove migration shims and compatibility layers

## Monolith Migration Compatibility

The existing `ftgo-flyway/` migration set is **not modified**. It continues to
manage the monolith's shared database during the transition period. The per-service
migrations are designed to create equivalent schemas in isolated databases,
allowing a gradual cutover.

## References

- [Microservices Patterns - Database per Service](https://microservices.io/patterns/data/database-per-service.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- Monolith schema: `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql`
