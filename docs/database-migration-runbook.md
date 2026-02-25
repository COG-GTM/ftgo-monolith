# Database Migration Runbook

## Purpose

Step-by-step guide for transitioning from the shared FTGO monolith database to per-service databases.

## Prerequisites

- [ ] MySQL server accessible with root/admin privileges
- [ ] All services built and tested against monolith database
- [ ] Backup of the current `ftgo` database taken
- [ ] Downtime window scheduled (or blue-green deployment ready)
- [ ] Monitoring dashboards configured for all services

## Phase 1: Preparation

### 1.1 Create Per-Service Databases

Execute on the MySQL server:

```sql
-- Create per-service databases
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service;
CREATE DATABASE IF NOT EXISTS ftgo_courier_service;
CREATE DATABASE IF NOT EXISTS ftgo_order_service;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service;

-- Create service user with per-database privileges
CREATE USER IF NOT EXISTS 'ftgo_user'@'%' IDENTIFIED BY 'ftgo_password';

GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'ftgo_user'@'%';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'ftgo_user'@'%';
GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'ftgo_user'@'%';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'ftgo_user'@'%';

FLUSH PRIVILEGES;
```

### 1.2 Verify Flyway Migration Files

Confirm each service has its V1 migration:

```bash
ls -la services/ftgo-consumer-service/src/main/resources/db/migration/
ls -la services/ftgo-courier-service/src/main/resources/db/migration/
ls -la services/ftgo-order-service/src/main/resources/db/migration/
ls -la services/ftgo-restaurant-service/src/main/resources/db/migration/
```

### 1.3 Backup Monolith Database

```bash
mysqldump -u root -p --single-transaction --routines --triggers ftgo > ftgo_backup_$(date +%Y%m%d_%H%M%S).sql
```

## Phase 2: Schema Migration

### 2.1 Run Flyway Migrations (Schema Only)

Start each service or run Flyway directly to create schemas:

```bash
# Option A: Start services (Flyway runs on startup)
# Each service's spring.flyway.enabled=true will trigger migration

# Option B: Run Flyway CLI per service
flyway -url=jdbc:mysql://localhost/ftgo_consumer_service -user=ftgo_user -password=ftgo_password -locations=filesystem:services/ftgo-consumer-service/src/main/resources/db/migration migrate

flyway -url=jdbc:mysql://localhost/ftgo_courier_service -user=ftgo_user -password=ftgo_password -locations=filesystem:services/ftgo-courier-service/src/main/resources/db/migration migrate

flyway -url=jdbc:mysql://localhost/ftgo_order_service -user=ftgo_user -password=ftgo_password -locations=filesystem:services/ftgo-order-service/src/main/resources/db/migration migrate

flyway -url=jdbc:mysql://localhost/ftgo_restaurant_service -user=ftgo_user -password=ftgo_password -locations=filesystem:services/ftgo-restaurant-service/src/main/resources/db/migration migrate
```

### 2.2 Verify Schema Creation

```sql
-- Check each database has its tables
USE ftgo_consumer_service;
SHOW TABLES;
-- Expected: consumers, consumer_id_sequence, flyway_schema_history

USE ftgo_courier_service;
SHOW TABLES;
-- Expected: courier, courier_actions, courier_id_sequence, flyway_schema_history

USE ftgo_order_service;
SHOW TABLES;
-- Expected: orders, order_line_items, order_id_sequence, flyway_schema_history

USE ftgo_restaurant_service;
SHOW TABLES;
-- Expected: restaurants, restaurant_menu_items, restaurant_id_sequence, flyway_schema_history
```

## Phase 3: Data Migration

### 3.1 Determine Sequence Starting Values

Query the monolith to find max IDs:

```sql
USE ftgo;
SELECT MAX(id) AS max_consumer_id FROM consumers;
SELECT MAX(id) AS max_courier_id FROM courier;
SELECT MAX(id) AS max_id FROM orders;
SELECT MAX(id) AS max_restaurant_id FROM restaurants;
SELECT next_val FROM hibernate_sequence;
```

### 3.2 Migrate Data

```sql
-- Consumer Service
INSERT INTO ftgo_consumer_service.consumers
SELECT * FROM ftgo.consumers;

-- Update sequence to max(monolith_max_id, current_sequence) + buffer
UPDATE ftgo_consumer_service.consumer_id_sequence
SET next_val = (SELECT GREATEST(COALESCE(MAX(id), 0) + 100, 1000) FROM ftgo_consumer_service.consumers);

-- Courier Service
INSERT INTO ftgo_courier_service.courier
SELECT * FROM ftgo.courier;

INSERT INTO ftgo_courier_service.courier_actions
SELECT * FROM ftgo.courier_actions;

UPDATE ftgo_courier_service.courier_id_sequence
SET next_val = (SELECT GREATEST(COALESCE(MAX(id), 0) + 100, 1000) FROM ftgo_courier_service.courier);

-- Order Service
INSERT INTO ftgo_order_service.orders
SELECT * FROM ftgo.orders;

INSERT INTO ftgo_order_service.order_line_items
SELECT * FROM ftgo.order_line_items;

UPDATE ftgo_order_service.order_id_sequence
SET next_val = (SELECT GREATEST(COALESCE(MAX(id), 0) + 100, 1000) FROM ftgo_order_service.orders);

-- Restaurant Service
INSERT INTO ftgo_restaurant_service.restaurants
SELECT * FROM ftgo.restaurants;

INSERT INTO ftgo_restaurant_service.restaurant_menu_items
SELECT * FROM ftgo.restaurant_menu_items;

UPDATE ftgo_restaurant_service.restaurant_id_sequence
SET next_val = (SELECT GREATEST(COALESCE(MAX(id), 0) + 100, 1000) FROM ftgo_restaurant_service.restaurants);
```

### 3.3 Verify Data Counts

```sql
-- Compare row counts between monolith and per-service databases
SELECT 'consumers' AS tbl,
    (SELECT COUNT(*) FROM ftgo.consumers) AS monolith_count,
    (SELECT COUNT(*) FROM ftgo_consumer_service.consumers) AS service_count;

SELECT 'courier' AS tbl,
    (SELECT COUNT(*) FROM ftgo.courier) AS monolith_count,
    (SELECT COUNT(*) FROM ftgo_courier_service.courier) AS service_count;

SELECT 'orders' AS tbl,
    (SELECT COUNT(*) FROM ftgo.orders) AS monolith_count,
    (SELECT COUNT(*) FROM ftgo_order_service.orders) AS service_count;

SELECT 'restaurants' AS tbl,
    (SELECT COUNT(*) FROM ftgo.restaurants) AS monolith_count,
    (SELECT COUNT(*) FROM ftgo_restaurant_service.restaurants) AS service_count;
```

## Phase 4: Service Cutover

### 4.1 Update Service Configuration

Each service's `application.properties` should already point to its own database:

```properties
spring.datasource.url=jdbc:mysql://${DOCKER_HOST_IP:localhost}:3306/ftgo_{service}_service
```

### 4.2 Start Services Against New Databases

```bash
# Start each service and verify it connects to its own database
# JPA ddl-auto=validate will confirm schema matches entities
```

### 4.3 Smoke Test

- [ ] Consumer Service: Create and retrieve a consumer
- [ ] Courier Service: Create and retrieve a courier
- [ ] Order Service: Create and retrieve an order
- [ ] Restaurant Service: Create and retrieve a restaurant

## Phase 5: Validation

### 5.1 Cross-Service Reference Validation

Verify cross-service IDs are consistent:

```sql
-- Check all order.restaurant_id values exist in restaurant service
SELECT o.id, o.restaurant_id
FROM ftgo_order_service.orders o
WHERE o.restaurant_id NOT IN (
    SELECT r.id FROM ftgo_restaurant_service.restaurants r
);
-- Should return 0 rows

-- Check all order.assigned_courier_id values exist in courier service
SELECT o.id, o.assigned_courier_id
FROM ftgo_order_service.orders o
WHERE o.assigned_courier_id IS NOT NULL
AND o.assigned_courier_id NOT IN (
    SELECT c.id FROM ftgo_courier_service.courier c
);
-- Should return 0 rows
```

### 5.2 Flyway History Check

```sql
-- Verify each database has migration history
SELECT * FROM ftgo_consumer_service.flyway_schema_history;
SELECT * FROM ftgo_courier_service.flyway_schema_history;
SELECT * FROM ftgo_order_service.flyway_schema_history;
SELECT * FROM ftgo_restaurant_service.flyway_schema_history;
```

## Rollback Plan

### Scenario: Single Service Failure

1. Stop the failing service
2. Point the service back to the monolith `ftgo` database
3. Investigate and fix the per-service database
4. Retry migration for that service

### Scenario: Full Rollback

1. Stop all microservices
2. Restore monolith database from backup:
   ```bash
   mysql -u root -p ftgo < ftgo_backup_YYYYMMDD_HHMMSS.sql
   ```
3. Restart monolith application
4. Verify monolith is serving traffic

### Data Consistency During Rollback

If data was written to per-service databases during the migration window:

1. Export new/modified records from each per-service database
2. Apply changes to the monolith database
3. Verify data integrity

## Post-Migration Cleanup

After successful migration and monitoring period (recommended: 2 weeks):

1. Remove the monolith `ftgo` database (or keep as read-only archive)
2. Remove `ftgo-flyway` module from the build
3. Remove cross-service FK references from documentation
4. Update Docker Compose to provision per-service databases
