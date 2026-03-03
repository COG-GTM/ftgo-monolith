# Per-Service Database Schema Migration Strategy

> **Jira:** EM-29 · **Phase:** 1 – Project Structure & Shared Libraries
> **Status:** Approved · **Last Updated:** 2026-03-03

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current State (Monolith)](#2-current-state-monolith)
3. [Target State (Database-per-Service)](#3-target-state-database-per-service)
4. [Per-Service Schema Design](#4-per-service-schema-design)
5. [Cross-Service Foreign Key Removal Plan](#5-cross-service-foreign-key-removal-plan)
6. [ID Generation Strategy](#6-id-generation-strategy)
7. [Per-Service Flyway Migration Structure](#7-per-service-flyway-migration-structure)
8. [Data Consistency Strategy](#8-data-consistency-strategy)
9. [Data Synchronization Approach](#9-data-synchronization-approach)
10. [Rollback Strategy](#10-rollback-strategy)
11. [Risk Matrix](#11-risk-matrix)
12. [References](#12-references)

---

## 1. Executive Summary

This document defines the strategy for migrating from a single shared MySQL database
(`ftgo`) to a **database-per-service** architecture, where each microservice owns and
manages its own database schema and migration history independently.

### Goals

| Goal | Description |
|------|-------------|
| **Service Autonomy** | Each service can evolve its schema independently without coordinating with other teams |
| **Deployment Independence** | Services can be deployed, scaled, and migrated independently |
| **Data Ownership** | Clear ownership boundaries — each table belongs to exactly one service |
| **Zero Downtime** | Migration executed with no application downtime using a phased approach |

### Scope

| Item | In Scope | Out of Scope |
|------|----------|-------------|
| Consumer Service schema | ✓ | |
| Courier Service schema | ✓ | |
| Order Service schema | ✓ | |
| Restaurant Service schema | ✓ | |
| Legacy ftgo-flyway module | Reference only | Modification |
| Event sourcing infrastructure | | ✓ (future phase) |
| CQRS read models | | ✓ (future phase) |

---

## 2. Current State (Monolith)

### Single Database Architecture

```
┌─────────────────────────────────────────────────────┐
│                  MySQL: ftgo database                │
│                                                     │
│  ┌──────────┐  ┌─────────┐  ┌───────────────────┐  │
│  │consumers │  │ courier  │  │  courier_actions   │  │
│  └──────────┘  └─────────┘  └───────────────────┘  │
│                                                     │
│  ┌──────────┐  ┌───────────────────┐                │
│  │  orders  │  │ order_line_items  │                │
│  └──────────┘  └───────────────────┘                │
│                                                     │
│  ┌──────────────┐  ┌───────────────────────────┐    │
│  │ restaurants   │  │  restaurant_menu_items    │    │
│  └──────────────┘  └───────────────────────────┘    │
│                                                     │
│  ┌────────────────────┐                             │
│  │ hibernate_sequence  │  (shared ID generator)     │
│  └────────────────────┘                             │
│                                                     │
│  ┌────────────────────────┐                         │
│  │ flyway_schema_history   │  (single history)      │
│  └────────────────────────┘                         │
└─────────────────────────────────────────────────────┘
```

### Current Flyway Configuration

- **Plugin:** Flyway 6.0.0 via `ftgo-flyway/build.gradle`
- **JDBC URL:** `jdbc:mysql://localhost/ftgo?useSSL=false`
- **User:** `root` / **Password:** `rootpassword`
- **Schema:** `ftgo`
- **Migration location:** `ftgo-flyway/src/main/resources/db/migration/`
- **Migration file:** `V1__create_ftgo_db.sql` (single migration, creates all 7 tables)

### Current Cross-Table Foreign Keys

| FK Constraint | Source Table.Column | Target Table.Column | Cross-Service? |
|--------------|--------------------|--------------------|----------------|
| `courier_actions_order_id` | `courier_actions.order_id` | `orders.id` | **Yes** (Courier → Order) |
| `courier_actions_courier_id` | `courier_actions.courier_id` | `courier.id` | No (same service) |
| `order_line_items_id` | `order_line_items.order_id` | `orders.id` | No (same service) |
| `orders_assigned_courier_id` | `orders.assigned_courier_id` | `courier.id` | **Yes** (Order → Courier) |
| `orders_restaurant_id` | `orders.restaurant_id` | `restaurants.id` | **Yes** (Order → Restaurant) |
| `restaurant_menu_items_restaurant_id` | `restaurant_menu_items.restaurant_id` | `restaurants.id` | No (same service) |

**Summary:** 3 cross-service FKs to remove; 3 intra-service FKs to retain.

---

## 3. Target State (Database-per-Service)

### Target Architecture

```
┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│  ftgo_consumer_db │  │  ftgo_courier_db  │  │   ftgo_order_db   │  │ftgo_restaurant_db │
│                   │  │                   │  │                   │  │                   │
│ ┌───────────────┐ │  │ ┌───────────────┐ │  │ ┌───────────────┐ │  │ ┌───────────────┐ │
│ │   consumers   │ │  │ │    courier     │ │  │ │    orders     │ │  │ │  restaurants   │ │
│ └───────────────┘ │  │ ├───────────────┤ │  │ ├───────────────┤ │  │ ├───────────────┤ │
│                   │  │ │courier_actions │ │  │ │order_line_items│ │  │ │rest_menu_items│ │
│ ┌───────────────┐ │  │ └───────────────┘ │  │ └───────────────┘ │  │ └───────────────┘ │
│ │flyway_schema_ │ │  │                   │  │                   │  │                   │
│ │   history     │ │  │ ┌───────────────┐ │  │ ┌───────────────┐ │  │ ┌───────────────┐ │
│ └───────────────┘ │  │ │flyway_schema_ │ │  │ │flyway_schema_ │ │  │ │flyway_schema_ │ │
│                   │  │ │   history     │ │  │ │   history     │ │  │ │   history     │ │
│                   │  │ └───────────────┘ │  │ └───────────────┘ │  │ └───────────────┘ │
└───────────────────┘  └───────────────────┘  └───────────────────┘  └───────────────────┘
```

### Database Naming Convention

| Service | Database Name | JDBC URL |
|---------|-------------|----------|
| Consumer Service | `ftgo_consumer_service` | `jdbc:mysql://localhost:3306/ftgo_consumer_service` |
| Courier Service | `ftgo_courier_service` | `jdbc:mysql://localhost:3306/ftgo_courier_service` |
| Order Service | `ftgo_order_service` | `jdbc:mysql://localhost:3306/ftgo_order_service` |
| Restaurant Service | `ftgo_restaurant_service` | `jdbc:mysql://localhost:3306/ftgo_restaurant_service` |

### Deployment Options

| Option | Description | Recommended For |
|--------|-------------|----------------|
| **Separate schemas, same MySQL instance** | Cheapest, simplest; schemas provide logical isolation | Development, Staging |
| **Separate MySQL instances** | Full resource isolation; independent scaling and failover | Production |

For the initial migration, we use **separate schemas on the same MySQL instance** and
upgrade to separate instances when service traffic justifies it.

---

## 4. Per-Service Schema Design

### 4.1 Consumer Service (`ftgo_consumer_service`)

**Tables:** `consumers`

```sql
CREATE TABLE consumers (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
```

**Migration file:** `services/ftgo-consumer-service/src/main/resources/db/migration/V1__create_consumer_service_schema.sql`

**Changes from monolith:**
- `id` changed from plain `BIGINT NOT NULL` (hibernate_sequence) to `AUTO_INCREMENT`
- Added `utf8mb4` charset for Unicode support
- No foreign keys (none existed in monolith)

---

### 4.2 Courier Service (`ftgo_courier_service`)

**Tables:** `courier`, `courier_actions`

```sql
CREATE TABLE courier (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    available  BIT,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    street1    VARCHAR(255),
    street2    VARCHAR(255),
    city       VARCHAR(255),
    state      VARCHAR(255),
    zip        VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE courier_actions (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    courier_id BIGINT NOT NULL,
    order_id   BIGINT,           -- cross-service ref (no FK)
    time       DATETIME,
    type       VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (courier_id) REFERENCES courier(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Migration file:** `services/ftgo-courier-service/src/main/resources/db/migration/V1__create_courier_service_schema.sql`

**Changes from monolith:**
- `courier.id` already used `AUTO_INCREMENT` (no change)
- `courier_actions`: Added `id` primary key column (was missing in monolith)
- `courier_actions.order_id`: FK to `orders` **removed** (cross-service)
- `courier_actions.courier_id`: FK to `courier` **retained** (same service)
- Added indexes on `courier_id`, `order_id`, and `available`

---

### 4.3 Order Service (`ftgo_order_service`)

**Tables:** `orders`, `order_line_items`

```sql
CREATE TABLE orders (
    id                       BIGINT NOT NULL AUTO_INCREMENT,
    accept_time              DATETIME,
    consumer_id              BIGINT,        -- cross-service ref (no FK)
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_zip     VARCHAR(255),
    delivery_time            DATETIME,
    order_state              VARCHAR(255),
    order_minimum            DECIMAL(19,2),
    payment_token            VARCHAR(255),
    picked_up_time           DATETIME,
    delivered_time           DATETIME,
    preparing_time           DATETIME,
    previous_ticket_state    INTEGER,
    ready_by                 DATETIME,
    ready_for_pickup_time    DATETIME,
    version                  BIGINT,
    assigned_courier_id      BIGINT,        -- cross-service ref (no FK)
    restaurant_id            BIGINT,        -- cross-service ref (no FK)
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE order_line_items (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    order_id     BIGINT NOT NULL,
    menu_item_id VARCHAR(255),
    name         VARCHAR(255),
    price        DECIMAL(19,2),
    quantity     INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Migration file:** `services/ftgo-order-service/src/main/resources/db/migration/V1__create_order_service_schema.sql`

**Changes from monolith:**
- `orders.assigned_courier_id`: FK to `courier` **removed** (cross-service)
- `orders.restaurant_id`: FK to `restaurants` **removed** (cross-service)
- `orders.consumer_id`: Was already a plain BIGINT (no change)
- `order_line_items`: Added `id` primary key column; FK to `orders` **retained**
- Added indexes on `consumer_id`, `restaurant_id`, `assigned_courier_id`, `order_state`

---

### 4.4 Restaurant Service (`ftgo_restaurant_service`)

**Tables:** `restaurants`, `restaurant_menu_items`

```sql
CREATE TABLE restaurants (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    street1 VARCHAR(255),
    street2 VARCHAR(255),
    city    VARCHAR(255),
    state   VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE restaurant_menu_items (
    restaurant_id BIGINT NOT NULL,
    id            VARCHAR(255),
    name          VARCHAR(255),
    price         DECIMAL(19,2),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Migration file:** `services/ftgo-restaurant-service/src/main/resources/db/migration/V1__create_restaurant_service_schema.sql`

**Changes from monolith:**
- `restaurants.id` already used `AUTO_INCREMENT` (no change)
- `restaurant_menu_items.restaurant_id`: FK **retained** (same service)
- Added index on `restaurant_id`

---

## 5. Cross-Service Foreign Key Removal Plan

### 5.1 Overview

Three cross-service foreign keys must be removed. Each is replaced with a plain
`BIGINT` column that stores the referenced entity's ID. Referential integrity is
enforced at the **application level** using API calls and eventual consistency patterns.

### 5.2 FK Removal Details

#### FK 1: `orders.assigned_courier_id → courier.id`

| Attribute | Value |
|-----------|-------|
| **Owning table** | `orders` (Order Service) |
| **Referenced table** | `courier` (Courier Service) |
| **Removal action** | Drop FK; keep column as plain `BIGINT` |
| **Application change** | Order Service calls Courier Service API to validate courier existence |
| **Consistency pattern** | Eventual consistency via domain events |
| **Risk** | Orphaned courier references if courier is deleted |
| **Mitigation** | Courier Service emits `CourierDeleted` event; Order Service handles reassignment |

#### FK 2: `orders.restaurant_id → restaurants.id`

| Attribute | Value |
|-----------|-------|
| **Owning table** | `orders` (Order Service) |
| **Referenced table** | `restaurants` (Restaurant Service) |
| **Removal action** | Drop FK; keep column as plain `BIGINT` |
| **Application change** | Order Service calls Restaurant Service API to validate restaurant at order creation |
| **Consistency pattern** | Validate-on-write; restaurant data cached locally |
| **Risk** | Orders referencing deactivated restaurants |
| **Mitigation** | Restaurant Service emits `RestaurantDeactivated` event; Order Service rejects new orders for deactivated restaurants |

#### FK 3: `courier_actions.order_id → orders.id`

| Attribute | Value |
|-----------|-------|
| **Owning table** | `courier_actions` (Courier Service) |
| **Referenced table** | `orders` (Order Service) |
| **Removal action** | Drop FK; keep column as plain `BIGINT` |
| **Application change** | Courier Service receives order ID via `OrderAssigned` domain event |
| **Consistency pattern** | Event-driven; order ID received from trusted source |
| **Risk** | Stale order references if order is cancelled after assignment |
| **Mitigation** | Order Service emits `OrderCancelled` event; Courier Service removes action from plan |

### 5.3 FK Removal Phasing (on the legacy `ftgo` database)

This phased approach applies to the **legacy monolith database** during the transition
period. The new per-service databases are created without cross-service FKs from the
start.

```
Phase A: Add application-level validation (API calls) alongside existing FKs
Phase B: Deploy domain event handlers for consistency
Phase C: Drop cross-service FKs from legacy database (ALTER TABLE ... DROP FOREIGN KEY)
Phase D: Verify data integrity with reconciliation scripts
Phase E: Decommission legacy database after all services migrated
```

---

## 6. ID Generation Strategy

### 6.1 Current State

The monolith uses a shared `hibernate_sequence` table for ID generation:

```sql
CREATE TABLE hibernate_sequence (next_val BIGINT) ENGINE=InnoDB;
INSERT INTO hibernate_sequence VALUES (1);
```

This is a **single global sequence** used by Hibernate's `TABLE` strategy across all
entities. It creates a bottleneck and prevents independent service operation.

### 6.2 Target Strategy: Per-Table AUTO_INCREMENT

| Aspect | Decision |
|--------|----------|
| **Strategy** | MySQL `AUTO_INCREMENT` per table |
| **JPA annotation** | `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| **Hibernate dialect** | `MySQL8Dialect` (supports IDENTITY generation) |
| **Sequence table** | `hibernate_sequence` is **not created** in per-service databases |

### 6.3 Migration Steps for ID Generation

1. **New per-service databases** use `AUTO_INCREMENT` from the start (V1 migrations)
2. **Data migration** preserves existing IDs from the monolith:
   - Export data with original IDs intact
   - Import with explicit ID values (temporarily disable `AUTO_INCREMENT`)
   - Reset `AUTO_INCREMENT` counters to `MAX(id) + 1` after import
3. **JPA entity change** (applied when extracting each service):
   ```java
   // Before (monolith)
   @GeneratedValue(strategy = GenerationType.TABLE)

   // After (per-service)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   ```

### 6.4 ID Uniqueness Across Services

Since each service has its own `AUTO_INCREMENT` counter, IDs are unique **within** a
service but not **across** services. This is acceptable because:

- Cross-service references always include the entity type (e.g., `consumer_id`,
  `restaurant_id`), so there is no ambiguity
- If globally unique IDs are needed in the future (e.g., for distributed tracing or
  event sourcing), consider migrating to **UUIDs** or **Snowflake IDs** in a later phase

---

## 7. Per-Service Flyway Migration Structure

### 7.1 Directory Layout

```
services/
├── ftgo-consumer-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_consumer_service_schema.sql
├── ftgo-courier-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_courier_service_schema.sql
├── ftgo-order-service/
│   └── src/main/resources/db/migration/
│       └── V1__create_order_service_schema.sql
└── ftgo-restaurant-service/
    └── src/main/resources/db/migration/
        └── V1__create_restaurant_service_schema.sql
```

### 7.2 Migration File Naming Convention

```
V{version}__{description}.sql
```

| Component | Convention | Example |
|-----------|-----------|---------|
| **Prefix** | `V` for versioned, `R` for repeatable | `V` |
| **Version** | Sequential integer (1, 2, 3...) | `1`, `2` |
| **Separator** | Double underscore `__` | `__` |
| **Description** | Snake_case, descriptive | `create_consumer_service_schema` |
| **Extension** | `.sql` | `.sql` |

**Examples of future migrations:**

```
V1__create_consumer_service_schema.sql      (initial schema)
V2__add_consumer_email_column.sql           (schema evolution)
V3__add_consumer_phone_index.sql            (performance optimization)
R__refresh_consumer_views.sql               (repeatable migration)
```

### 7.3 Flyway Configuration (per-service `application.yml`)

Each service will configure Flyway to point to its own database:

```yaml
# services/ftgo-consumer-service/src/main/resources/application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/ftgo_consumer_service
    username: ${DB_USER:ftgo_consumer}
    password: ${DB_PASSWORD}
```

### 7.4 Key Flyway Settings

| Setting | Value | Rationale |
|---------|-------|-----------|
| `baseline-on-migrate` | `false` | New databases; no baseline needed |
| `validate-on-migrate` | `true` | Ensure migration checksums match |
| `out-of-order` | `false` | Strict version ordering |
| `clean-disabled` | `true` (production) | Prevent accidental schema deletion |
| `table` | `flyway_schema_history` | Default; each DB has its own |

### 7.5 Relationship to Legacy `ftgo-flyway`

| Aspect | Legacy `ftgo-flyway` | Per-Service Migrations |
|--------|---------------------|----------------------|
| **Location** | `ftgo-flyway/src/main/resources/db/migration/` | `services/<svc>/src/main/resources/db/migration/` |
| **Database** | `ftgo` (shared) | `ftgo_<service>_service` (dedicated) |
| **Flyway version** | 6.0.0 (Gradle plugin) | Spring Boot managed (embedded) |
| **Execution** | Manual (`./gradlew flywayMigrate`) | Automatic on application startup |
| **Status** | Active (monolith) → Frozen → Decommissioned | New (active) |

The legacy `ftgo-flyway` module is **not modified**. It continues to manage the monolith
database during the transition period and is decommissioned after all services are fully
migrated.

---

## 8. Data Consistency Strategy

### 8.1 Consistency Model

Moving from a single database (strong consistency via FKs and transactions) to
database-per-service requires adopting **eventual consistency** for cross-service
operations.

### 8.2 Patterns

#### Pattern 1: Saga Pattern (for multi-service transactions)

Used when an operation spans multiple services and needs compensating transactions.

**Example: Create Order**
```
1. Order Service → Create order (PENDING)
2. Consumer Service → Validate consumer  ✓/✗
3. Restaurant Service → Validate restaurant & menu  ✓/✗
4. Order Service → Approve order (APPROVED)

Compensation (on failure):
- Order Service → Reject order (CANCELLED)
```

#### Pattern 2: Domain Events (for data propagation)

Used when one service needs to react to changes in another.

| Event | Publisher | Subscribers | Action |
|-------|-----------|------------|--------|
| `ConsumerCreated` | Consumer Service | — | (future subscribers) |
| `RestaurantCreated` | Restaurant Service | Order Service | Cache restaurant info |
| `RestaurantMenuUpdated` | Restaurant Service | Order Service | Update cached menu |
| `OrderCreated` | Order Service | Courier Service | Start delivery planning |
| `OrderCancelled` | Order Service | Courier Service | Remove from plan |
| `CourierAssigned` | Courier Service | Order Service | Update assigned_courier_id |
| `CourierDeleted` | Courier Service | Order Service | Clear courier assignment |

#### Pattern 3: API Calls with Validation (for synchronous checks)

Used for immediate validation during write operations.

```
Order Service creates an order:
  1. Call Consumer Service API → GET /consumers/{consumerId} → 200 OK?
  2. Call Restaurant Service API → GET /restaurants/{restaurantId} → 200 OK?
  3. If both valid → persist order
  4. If either invalid → reject with 400 Bad Request
```

### 8.3 Consistency Guarantees

| Operation | Consistency Level | Pattern | Latency |
|-----------|------------------|---------|---------|
| Create Order | Eventually consistent | Saga | ~100ms |
| Assign Courier | Eventually consistent | Domain Event | ~50ms |
| Validate Consumer | Strongly consistent | API Call | ~10ms |
| Validate Restaurant | Strongly consistent | API Call | ~10ms |
| Update Order Status | Locally consistent | Local transaction | ~5ms |

---

## 9. Data Synchronization Approach

### 9.1 Migration Phase: Dual-Write with Shadow Reads

During the transition period, data must be synchronized between the legacy monolith
database and the new per-service databases.

```
Phase 1: Shadow Write
  ┌──────────┐     writes     ┌──────────┐
  │ Monolith │───────────────→│  ftgo DB │ (primary)
  │   App    │                └──────────┘
  │          │     writes     ┌──────────┐
  │          │───────────────→│ Service  │ (shadow)
  └──────────┘                │   DBs    │
                              └──────────┘

Phase 2: Shadow Read (verify)
  ┌──────────┐     reads      ┌──────────┐
  │ Monolith │───────────────→│  ftgo DB │ (primary)
  │   App    │                └──────────┘
  │          │     reads      ┌──────────┐
  │          │- - - - - - - →│ Service  │ (compare)
  └──────────┘                │   DBs    │
                              └──────────┘

Phase 3: Cutover
  ┌──────────┐     reads/     ┌──────────┐
  │ Services │───────────────→│ Service  │ (primary)
  │          │     writes     │   DBs    │
  └──────────┘                └──────────┘
```

### 9.2 Data Migration Steps

For each service, the data migration follows this sequence:

1. **Create new database** and run V1 migration (schema only)
2. **Export data** from monolith `ftgo` database (service-owned tables only)
3. **Transform data** (if schema changes exist, e.g., column renames)
4. **Import data** into new service database with explicit IDs
5. **Reset AUTO_INCREMENT** to `MAX(id) + 1`
6. **Validate** row counts and checksums match
7. **Enable dual-write** (monolith writes to both databases)
8. **Verify consistency** via reconciliation job
9. **Switch reads** to new service database
10. **Disable writes** to monolith database for migrated tables
11. **Decommission** monolith tables after verification period

### 9.3 Data Export/Import Scripts

Example for Consumer Service:

```sql
-- Export from monolith
SELECT id, first_name, last_name
FROM ftgo.consumers
INTO OUTFILE '/tmp/consumers_export.csv'
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n';

-- Import to service DB
LOAD DATA INFILE '/tmp/consumers_export.csv'
INTO TABLE ftgo_consumer_service.consumers
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, first_name, last_name);

-- Reset AUTO_INCREMENT
SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_consumer_service.consumers;
SET @sql = CONCAT('ALTER TABLE ftgo_consumer_service.consumers AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### 9.4 Reconciliation

A scheduled reconciliation job compares data between the monolith and service databases:

```
Every 5 minutes during migration:
  1. Compare row counts: ftgo.consumers vs ftgo_consumer_service.consumers
  2. Compare checksums: MD5(GROUP_CONCAT(id, first_name, last_name))
  3. Alert if mismatch > threshold
  4. Log discrepancies for manual review
```

---

## 10. Rollback Strategy

### 10.1 Rollback Levels

| Level | Trigger | Action | Data Loss |
|-------|---------|--------|-----------|
| **L1: Migration rollback** | V1 migration fails | Flyway undo or manual DROP | None (empty DB) |
| **L2: Data import rollback** | Import validation fails | Truncate service DB, retry | None (monolith intact) |
| **L3: Dual-write rollback** | Consistency issues | Disable dual-write, revert to monolith only | None |
| **L4: Full rollback** | Critical production issue | Point all services back to monolith DB | Potential data written only to service DB |

### 10.2 Rollback Procedures

#### L1: Migration Rollback

```sql
-- If V1 migration fails on a new (empty) service database,
-- simply drop the database and recreate:
DROP DATABASE IF EXISTS ftgo_consumer_service;
CREATE DATABASE ftgo_consumer_service;
-- Fix migration file and re-run
```

#### L4: Full Rollback

```
1. Stop all microservices
2. Reconfigure to point to monolith ftgo database
3. Re-enable cross-service FKs (if dropped)
4. Reconcile any data written only to service DBs
5. Restart monolith application
6. Verify data integrity
```

### 10.3 Point of No Return

The **point of no return** is when the legacy `ftgo` database tables are dropped. Before
that point, rollback is straightforward. Recommendation:

- Keep the monolith database **read-only** for at least **2 weeks** after full cutover
- Keep **full backups** for at least **30 days** after decommissioning
- Automate rollback scripts and test them in staging

---

## 11. Risk Matrix

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Data loss during migration | Low | Critical | Backup before migration; validate checksums |
| Orphaned cross-service references | Medium | Medium | Reconciliation jobs; domain events for deletions |
| ID collision after migration | Low | High | Reset AUTO_INCREMENT to MAX(id)+1; validate |
| Inconsistent reads during dual-write | Medium | Low | Reconciliation; eventual consistency is expected |
| Flyway migration conflict between devs | Low | Low | Convention-based naming; PR review process |
| Service database connection exhaustion | Medium | Medium | Connection pooling (HikariCP); separate DB users |
| Legacy monolith and new service drift | Medium | High | Short dual-write window; automated reconciliation |

---

## 12. References

| Document | Location |
|----------|----------|
| Entity-Service Ownership Mapping | `docs/entity-service-ownership.md` |
| Legacy Flyway Migrations | `ftgo-flyway/src/main/resources/db/migration/V1__create_ftgo_db.sql` |
| ADR: Mono-Repo Structure | `docs/adr/0001-mono-repo-structure-and-naming-conventions.md` |
| Consumer Service V1 Migration | `services/ftgo-consumer-service/src/main/resources/db/migration/V1__create_consumer_service_schema.sql` |
| Courier Service V1 Migration | `services/ftgo-courier-service/src/main/resources/db/migration/V1__create_courier_service_schema.sql` |
| Order Service V1 Migration | `services/ftgo-order-service/src/main/resources/db/migration/V1__create_order_service_schema.sql` |
| Restaurant Service V1 Migration | `services/ftgo-restaurant-service/src/main/resources/db/migration/V1__create_restaurant_service_schema.sql` |
| Migration Runbook | `docs/database-migration-runbook.md` |
