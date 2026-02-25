# Per-Service Database Schema Migration Strategy

## Overview

This document defines the strategy for migrating from the FTGO monolith's single shared MySQL database (`ftgo`) to independent per-service databases, each with its own Flyway migration history and schema ownership.

## Current State (Monolith)

### Single Shared Database
- **Database**: `ftgo` on MySQL
- **Migration tool**: Flyway 6.0.0 via `ftgo-flyway/build.gradle`
- **Single migration**: `V1__create_ftgo_db.sql` creates all 7 tables
- **Shared ID generation**: Single `hibernate_sequence` table

### Table Ownership

| Table | Owning Service | Notes |
|-------|---------------|-------|
| `consumers` | Consumer Service | Standalone entity |
| `courier` | Courier Service | Has child table `courier_actions` |
| `courier_actions` | Courier Service | References orders (cross-service) |
| `orders` | Order Service | Central entity, many cross-service refs |
| `order_line_items` | Order Service | Child of `orders` |
| `restaurants` | Restaurant Service | Has child table `restaurant_menu_items` |
| `restaurant_menu_items` | Restaurant Service | Child of `restaurants` |
| `hibernate_sequence` | Shared | Replaced by per-service sequences |

### Cross-Service Foreign Keys (to be removed)

| FK Constraint | Source Table | Target Table | Action |
|--------------|-------------|-------------|--------|
| `orders_assigned_courier_id` | `orders.assigned_courier_id` | `courier.id` | Remove FK, keep column |
| `orders_restaurant_id` | `orders.restaurant_id` | `restaurants.id` | Remove FK, keep column |
| `courier_actions_order_id` | `courier_actions.order_id` | `orders.id` | Remove FK, keep column |

### Intra-Service Foreign Keys (retained)

| FK Constraint | Source Table | Target Table | Service |
|--------------|-------------|-------------|---------|
| `fk_order_line_items_order_id` | `order_line_items.order_id` | `orders.id` | Order Service |
| `fk_courier_actions_courier_id` | `courier_actions.courier_id` | `courier.id` | Courier Service |
| `fk_restaurant_menu_items_restaurant_id` | `restaurant_menu_items.restaurant_id` | `restaurants.id` | Restaurant Service |

## Target State (Microservices)

### Per-Service Databases

| Service | Database Name | Port | Tables |
|---------|-------------|------|--------|
| Consumer Service | `ftgo_consumer_service` | 3306 | `consumers`, `consumer_id_sequence` |
| Courier Service | `ftgo_courier_service` | 3306 | `courier`, `courier_actions`, `courier_id_sequence` |
| Order Service | `ftgo_order_service` | 3306 | `orders`, `order_line_items`, `order_id_sequence` |
| Restaurant Service | `ftgo_restaurant_service` | 3306 | `restaurants`, `restaurant_menu_items`, `restaurant_id_sequence` |

### Per-Service Flyway Configuration

Each service manages its own migrations independently:

```properties
# Spring Boot application.properties per service
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.table=flyway_schema_history
```

### Migration File Structure

```
services/
  ftgo-consumer-service/
    src/main/resources/db/migration/
      V1__create_consumer_service_schema.sql
  ftgo-courier-service/
    src/main/resources/db/migration/
      V1__create_courier_service_schema.sql
  ftgo-order-service/
    src/main/resources/db/migration/
      V1__create_order_service_schema.sql
  ftgo-restaurant-service/
    src/main/resources/db/migration/
      V1__create_restaurant_service_schema.sql
```

### Naming Conventions

- **Migration files**: `V{version}__{description}.sql`
  - Version: Integer, incrementing per service (V1, V2, V3...)
  - Description: Snake case, descriptive (e.g., `create_order_service_schema`)
- **Databases**: `ftgo_{service_name}` (e.g., `ftgo_order_service`)
- **Sequence tables**: `{entity}_id_sequence` (e.g., `order_id_sequence`)

## ID Generation Strategy

### Problem
The monolith uses a single shared `hibernate_sequence` table for ID generation across all entities. This creates a cross-service dependency that must be eliminated.

### Solution: Per-Service Sequence Tables

Each service gets its own sequence table with a high initial value (1000) to avoid conflicts with existing monolith data:

| Service | Sequence Table | Initial Value |
|---------|---------------|---------------|
| Consumer | `consumer_id_sequence` | 1000 |
| Courier | `courier_id_sequence` | 1000 |
| Order | `order_id_sequence` | 1000 |
| Restaurant | `restaurant_id_sequence` | 1000 |

### JPA Entity Configuration

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_id_gen")
    @TableGenerator(
        name = "order_id_gen",
        table = ServiceSequenceConstants.ORDER_SEQUENCE_TABLE,
        pkColumnName = "next_val",
        allocationSize = 50
    )
    private Long id;
}
```

### Future Migration Path

1. **Phase 1 (Current)**: Per-service TABLE sequence (compatible with existing BIGINT IDs)
2. **Phase 2**: Migrate new entities to IDENTITY (AUTO_INCREMENT) strategy
3. **Phase 3 (Optional)**: Consider UUIDs for new entities requiring cross-service uniqueness

## Cross-Service Reference Strategy

### Approach
Cross-service IDs are stored as plain BIGINT columns **without** foreign key constraints. Data consistency is maintained through domain events and eventual consistency patterns.

### Column Documentation
Each cross-service reference column includes a SQL comment indicating the source service:

```sql
consumer_id BIGINT COMMENT 'Reference to Consumer Service (no FK - cross-service)'
```

### Validation
- **Application-level**: Services validate cross-service IDs via API calls or cached lookups
- **Domain events**: Services publish events when entities are created/updated/deleted
- **Orphan detection**: Periodic reconciliation jobs detect and flag dangling references

## Shared JPA Library Enhancements

The `shared/ftgo-common-jpa` library has been enhanced with:

| Class | Package | Purpose |
|-------|---------|---------|
| `FlywayMigrationProperties` | `com.ftgo.common.jpa.migration` | Standard Flyway property constants and database naming conventions |
| `DatabaseMigrationValidator` | `com.ftgo.common.jpa.migration` | Utility to validate migration state (schema existence, migration count) |
| `IdGenerationStrategy` | `com.ftgo.common.jpa.id` | Enum documenting supported ID generation strategies |
| `ServiceSequenceConstants` | `com.ftgo.common.jpa.id` | Constants for per-service sequence table names and configuration |
