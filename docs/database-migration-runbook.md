# Database Migration Runbook

## Purpose

Step-by-step procedures for transitioning from the shared monolith database (`ftgo`) to
independent per-service databases. This runbook covers the full lifecycle: preparation,
execution, verification, and rollback.

**Related documents:**
- [Database Migration Strategy](database-migration-strategy.md) - Design and rationale
- [Entity-Service Ownership](entity-service-ownership.md) - Table-to-service mapping

---

## Prerequisites

- [ ] MySQL 8.0+ available for per-service databases
- [ ] Flyway 6.0+ configured in each microservice
- [ ] Per-service Flyway V1 migrations reviewed and tested
- [ ] Database backup tooling ready (mysqldump or equivalent)
- [ ] Monitoring/alerting configured for each service database
- [ ] Event infrastructure (Kafka/RabbitMQ) deployed and tested
- [ ] All teams briefed on the migration plan and rollback procedures

---

## Phase 1: Preparation

### 1.1 Backup the Monolith Database

```bash
# Full backup of the monolith database
mysqldump -h ${MONOLITH_DB_HOST} -u root -p \
  --single-transaction \
  --routines --triggers \
  --databases ftgo \
  > ftgo_monolith_backup_$(date +%Y%m%d_%H%M%S).sql

# Verify backup integrity
mysql -h ${MONOLITH_DB_HOST} -u root -p -e "SELECT COUNT(*) FROM ftgo.consumers;"
mysql -h ${MONOLITH_DB_HOST} -u root -p -e "SELECT COUNT(*) FROM ftgo.orders;"
mysql -h ${MONOLITH_DB_HOST} -u root -p -e "SELECT COUNT(*) FROM ftgo.courier;"
mysql -h ${MONOLITH_DB_HOST} -u root -p -e "SELECT COUNT(*) FROM ftgo.restaurants;"
```

### 1.2 Create Per-Service Databases

```sql
-- Create databases for each service
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_order_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_courier_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 1.3 Create Per-Service Database Users

```sql
-- Consumer Service
CREATE USER IF NOT EXISTS 'ftgo_consumer'@'%' IDENTIFIED BY '${CONSUMER_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'ftgo_consumer'@'%';

-- Order Service
CREATE USER IF NOT EXISTS 'ftgo_order'@'%' IDENTIFIED BY '${ORDER_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'ftgo_order'@'%';

-- Courier Service
CREATE USER IF NOT EXISTS 'ftgo_courier'@'%' IDENTIFIED BY '${COURIER_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'ftgo_courier'@'%';

-- Restaurant Service
CREATE USER IF NOT EXISTS 'ftgo_restaurant'@'%' IDENTIFIED BY '${RESTAURANT_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'ftgo_restaurant'@'%';

FLUSH PRIVILEGES;
```

---

## Phase 2: Schema Migration (Run Flyway)

### 2.1 Run Per-Service Flyway Migrations

Each service runs its own Flyway migration on startup, or it can be triggered manually:

```bash
# Consumer Service
cd services/ftgo-consumer-service
./gradlew flywayMigrate \
  -Dflyway.url=jdbc:mysql://${DB_HOST}/ftgo_consumer_service \
  -Dflyway.user=ftgo_consumer \
  -Dflyway.password=${CONSUMER_DB_PASSWORD}

# Order Service
cd services/ftgo-order-service
./gradlew flywayMigrate \
  -Dflyway.url=jdbc:mysql://${DB_HOST}/ftgo_order_service \
  -Dflyway.user=ftgo_order \
  -Dflyway.password=${ORDER_DB_PASSWORD}

# Courier Service
cd services/ftgo-courier-service
./gradlew flywayMigrate \
  -Dflyway.url=jdbc:mysql://${DB_HOST}/ftgo_courier_service \
  -Dflyway.user=ftgo_courier \
  -Dflyway.password=${COURIER_DB_PASSWORD}

# Restaurant Service
cd services/ftgo-restaurant-service
./gradlew flywayMigrate \
  -Dflyway.url=jdbc:mysql://${DB_HOST}/ftgo_restaurant_service \
  -Dflyway.user=ftgo_restaurant \
  -Dflyway.password=${RESTAURANT_DB_PASSWORD}
```

### 2.2 Verify Schema Creation

```sql
-- Verify Consumer Service schema
USE ftgo_consumer_service;
SHOW TABLES;
-- Expected: consumers, consumer_sequence, flyway_schema_history
DESCRIBE consumers;

-- Verify Order Service schema
USE ftgo_order_service;
SHOW TABLES;
-- Expected: orders, order_line_items, order_sequence, flyway_schema_history
DESCRIBE orders;
DESCRIBE order_line_items;

-- Verify Courier Service schema
USE ftgo_courier_service;
SHOW TABLES;
-- Expected: courier, courier_actions, courier_sequence, flyway_schema_history
DESCRIBE courier;
DESCRIBE courier_actions;

-- Verify Restaurant Service schema
USE ftgo_restaurant_service;
SHOW TABLES;
-- Expected: restaurants, restaurant_menu_items, restaurant_sequence, flyway_schema_history
DESCRIBE restaurants;
DESCRIBE restaurant_menu_items;
```

---

## Phase 3: Data Migration

### 3.1 Migrate Consumer Data

```sql
-- Copy consumers from monolith to Consumer Service database
INSERT INTO ftgo_consumer_service.consumers (id, first_name, last_name)
SELECT id, first_name, last_name
FROM ftgo.consumers;

-- Update the auto_increment counter
SELECT MAX(id) + 1 INTO @max_id FROM ftgo_consumer_service.consumers;
SET @sql = CONCAT('ALTER TABLE ftgo_consumer_service.consumers AUTO_INCREMENT = ', IFNULL(@max_id, 1));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update sequence table
UPDATE ftgo_consumer_service.consumer_sequence
SET next_val = (SELECT IFNULL(MAX(id), 0) + 1 FROM ftgo_consumer_service.consumers)
WHERE sequence_name = 'consumer_id_seq';
```

### 3.2 Migrate Order Data

```sql
-- Copy orders from monolith to Order Service database
INSERT INTO ftgo_order_service.orders (
    id, order_state, consumer_id, restaurant_id, assigned_courier_id,
    delivery_address_street1, delivery_address_street2, delivery_address_city,
    delivery_address_state, delivery_address_zip, delivery_time, order_minimum,
    payment_token, accept_time, preparing_time, ready_for_pickup_time,
    picked_up_time, delivered_time, ready_by, previous_ticket_state, version
)
SELECT
    id, IFNULL(order_state, 'APPROVAL_PENDING'), consumer_id, restaurant_id,
    assigned_courier_id, delivery_address_street1, delivery_address_street2,
    delivery_address_city, delivery_address_state, delivery_address_zip,
    delivery_time, order_minimum, payment_token, accept_time, preparing_time,
    ready_for_pickup_time, picked_up_time, delivered_time, ready_by,
    previous_ticket_state, IFNULL(version, 0)
FROM ftgo.orders;

-- Copy order line items
INSERT INTO ftgo_order_service.order_line_items (order_id, menu_item_id, name, price, quantity)
SELECT order_id, menu_item_id, name, price, quantity
FROM ftgo.order_line_items;

-- Update auto_increment counters
SELECT MAX(id) + 1 INTO @max_order_id FROM ftgo_order_service.orders;
SET @sql = CONCAT('ALTER TABLE ftgo_order_service.orders AUTO_INCREMENT = ', IFNULL(@max_order_id, 1));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update sequence table
UPDATE ftgo_order_service.order_sequence
SET next_val = (SELECT IFNULL(MAX(id), 0) + 1 FROM ftgo_order_service.orders)
WHERE sequence_name = 'order_id_seq';
```

### 3.3 Migrate Courier Data

```sql
-- Copy couriers from monolith to Courier Service database
INSERT INTO ftgo_courier_service.courier (
    id, available, first_name, last_name,
    street1, street2, city, state, zip
)
SELECT
    id, available, first_name, last_name,
    street1, street2, city, state, zip
FROM ftgo.courier;

-- Copy courier actions
INSERT INTO ftgo_courier_service.courier_actions (courier_id, order_id, time, type)
SELECT courier_id, order_id, time, type
FROM ftgo.courier_actions;

-- Update auto_increment counter
SELECT MAX(id) + 1 INTO @max_courier_id FROM ftgo_courier_service.courier;
SET @sql = CONCAT('ALTER TABLE ftgo_courier_service.courier AUTO_INCREMENT = ', IFNULL(@max_courier_id, 1));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update sequence table
UPDATE ftgo_courier_service.courier_sequence
SET next_val = (SELECT IFNULL(MAX(id), 0) + 1 FROM ftgo_courier_service.courier)
WHERE sequence_name = 'courier_id_seq';
```

### 3.4 Migrate Restaurant Data

```sql
-- Copy restaurants from monolith to Restaurant Service database
INSERT INTO ftgo_restaurant_service.restaurants (
    id, name, street1, street2, city, state, zip
)
SELECT
    id, name, street1, street2, city, state, zip
FROM ftgo.restaurants;

-- Copy restaurant menu items (id column preserved as-is for JPA MenuItem.id compatibility)
INSERT INTO ftgo_restaurant_service.restaurant_menu_items (restaurant_id, id, name, price)
SELECT restaurant_id, id, name, price
FROM ftgo.restaurant_menu_items;

-- Update auto_increment counter
SELECT MAX(id) + 1 INTO @max_restaurant_id FROM ftgo_restaurant_service.restaurants;
SET @sql = CONCAT('ALTER TABLE ftgo_restaurant_service.restaurants AUTO_INCREMENT = ', IFNULL(@max_restaurant_id, 1));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update sequence table
UPDATE ftgo_restaurant_service.restaurant_sequence
SET next_val = (SELECT IFNULL(MAX(id), 0) + 1 FROM ftgo_restaurant_service.restaurants)
WHERE sequence_name = 'restaurant_id_seq';
```

### 3.5 Verify Data Migration

```sql
-- Compare row counts between monolith and per-service databases
SELECT 'consumers' AS tbl,
       (SELECT COUNT(*) FROM ftgo.consumers) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_consumer_service.consumers) AS service_count;

SELECT 'orders' AS tbl,
       (SELECT COUNT(*) FROM ftgo.orders) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_order_service.orders) AS service_count;

SELECT 'order_line_items' AS tbl,
       (SELECT COUNT(*) FROM ftgo.order_line_items) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_order_service.order_line_items) AS service_count;

SELECT 'courier' AS tbl,
       (SELECT COUNT(*) FROM ftgo.courier) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_courier_service.courier) AS service_count;

SELECT 'courier_actions' AS tbl,
       (SELECT COUNT(*) FROM ftgo.courier_actions) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_courier_service.courier_actions) AS service_count;

SELECT 'restaurants' AS tbl,
       (SELECT COUNT(*) FROM ftgo.restaurants) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_restaurant_service.restaurants) AS service_count;

SELECT 'restaurant_menu_items' AS tbl,
       (SELECT COUNT(*) FROM ftgo.restaurant_menu_items) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_restaurant_service.restaurant_menu_items) AS service_count;
```

---

## Phase 4: Application Cutover

### 4.1 Deploy Microservices with Per-Service Database Config

Update each service's deployment configuration to point to its own database:

```yaml
# Example: Consumer Service (Kubernetes ConfigMap / environment)
DB_HOST: mysql-consumer.database.svc.cluster.local
DB_NAME: ftgo_consumer_service
DB_USER: ftgo_consumer
DB_PASSWORD: <from-secret>
SPRING_FLYWAY_ENABLED: "true"
```

### 4.2 Verification Checklist

- [ ] Each service starts successfully and connects to its own database
- [ ] Flyway reports schema is up to date (no pending migrations)
- [ ] CRUD operations work for each service's entities
- [ ] Cross-service API calls succeed (e.g., Order Service calls Consumer Service)
- [ ] Domain events are published and consumed correctly
- [ ] No references to the old `ftgo` database remain in application configs

### 4.3 Monitoring Period

- Monitor for 24-48 hours after cutover
- Watch for: connection errors, data inconsistencies, elevated error rates
- Keep monolith database in read-only mode during monitoring period

---

## Phase 5: Cleanup

### 5.1 Decommission Monolith Database

After successful monitoring period:

1. Verify no applications are connected to the `ftgo` database
2. Take a final backup
3. Set the monolith database to read-only
4. After 7 days with no issues, drop the monolith database

```sql
-- Final verification - no active connections to ftgo
SELECT * FROM information_schema.PROCESSLIST WHERE DB = 'ftgo';

-- Archive the monolith Flyway configuration (do not delete - keep for reference)
-- ftgo-flyway/ directory remains in the repository as historical reference
```

### 5.2 Remove Shared hibernate_sequence

The shared `hibernate_sequence` table is no longer needed. Each service uses either
`AUTO_INCREMENT` or its own per-service sequence table.

---

## Rollback Procedures

### Rollback: Schema Migration Failed

If a Flyway migration fails on a per-service database:

```bash
# 1. Check Flyway status
./gradlew flywayInfo -Dflyway.url=jdbc:mysql://${DB_HOST}/${DB_NAME}

# 2. Repair the schema history (marks failed migration as resolved)
./gradlew flywayRepair -Dflyway.url=jdbc:mysql://${DB_HOST}/${DB_NAME}

# 3. Fix the migration SQL and re-run
./gradlew flywayMigrate -Dflyway.url=jdbc:mysql://${DB_HOST}/${DB_NAME}
```

### Rollback: Data Migration Failed

If data migration encounters errors:

```bash
# 1. Drop all per-service databases
mysql -h ${DB_HOST} -u root -p -e "
  DROP DATABASE IF EXISTS ftgo_consumer_service;
  DROP DATABASE IF EXISTS ftgo_order_service;
  DROP DATABASE IF EXISTS ftgo_courier_service;
  DROP DATABASE IF EXISTS ftgo_restaurant_service;
"

# 2. Recreate databases and re-run from Phase 2
```

### Rollback: Full Rollback to Monolith

If the entire migration needs to be rolled back:

```bash
# 1. Redeploy monolith application pointing to ftgo database
# 2. Restore monolith database from backup if needed:
mysql -h ${MONOLITH_DB_HOST} -u root -p < ftgo_monolith_backup_YYYYMMDD_HHMMSS.sql

# 3. Drop per-service databases (after confirming monolith is healthy)
```

---

## Reference: Table-to-Service Mapping

| Monolith Table          | Target Service       | Target Database              |
|-------------------------|----------------------|------------------------------|
| `consumers`             | Consumer Service     | `ftgo_consumer_service`      |
| `orders`                | Order Service        | `ftgo_order_service`         |
| `order_line_items`      | Order Service        | `ftgo_order_service`         |
| `courier`               | Courier Service      | `ftgo_courier_service`       |
| `courier_actions`       | Courier Service      | `ftgo_courier_service`       |
| `restaurants`           | Restaurant Service   | `ftgo_restaurant_service`    |
| `restaurant_menu_items` | Restaurant Service   | `ftgo_restaurant_service`    |
| `hibernate_sequence`    | (removed)            | Replaced by per-service sequences / AUTO_INCREMENT |
