# Database Migration Runbook

> **Jira:** EM-29 · **Phase:** 1 – Project Structure & Shared Libraries
> **Status:** Draft · **Last Updated:** 2026-03-03

---

## Table of Contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Pre-Migration Checklist](#3-pre-migration-checklist)
4. [Step-by-Step Migration Procedure](#4-step-by-step-migration-procedure)
5. [Post-Migration Verification](#5-post-migration-verification)
6. [Rollback Procedures](#6-rollback-procedures)
7. [Troubleshooting](#7-troubleshooting)
8. [Service-Specific Notes](#8-service-specific-notes)

---

## 1. Overview

This runbook provides step-by-step instructions for migrating from the shared monolith
database (`ftgo`) to per-service databases. Each service is migrated independently
following the same general procedure.

**Migration order (recommended):**

| Order | Service | Risk | Rationale |
|-------|---------|------|-----------|
| 1 | Consumer Service | Low | Simplest schema (1 table, no FKs) |
| 2 | Restaurant Service | Low | Self-contained schema (2 tables, intra-service FK only) |
| 3 | Courier Service | Medium | Has cross-service reference to orders (order_id in courier_actions) |
| 4 | Order Service | High | Most complex; references consumer, restaurant, and courier IDs |

**Estimated timeline:** 1–2 hours per service in staging; 2–4 hours per service in production (including verification).

---

## 2. Prerequisites

### 2.1 Access Requirements

- [ ] MySQL root or admin access on the target MySQL instance
- [ ] Read access to the monolith `ftgo` database
- [ ] Credentials for per-service database users (see Section 4.1)
- [ ] Access to application deployment pipeline
- [ ] Access to monitoring dashboards (Prometheus/Grafana)

### 2.2 Tools

- [ ] MySQL client (v8.0+)
- [ ] Flyway CLI or Spring Boot application with Flyway auto-migration
- [ ] `mysqldump` for data export
- [ ] Monitoring access (Prometheus, Grafana, application logs)

### 2.3 Environment Variables

```bash
# Monolith database
MONOLITH_DB_HOST=localhost
MONOLITH_DB_PORT=3306
MONOLITH_DB_NAME=ftgo
MONOLITH_DB_USER=root
MONOLITH_DB_PASS=rootpassword

# Per-service database credentials (example for Consumer Service)
SERVICE_DB_HOST=localhost
SERVICE_DB_PORT=3306
SERVICE_DB_NAME=ftgo_consumer_service
SERVICE_DB_USER=ftgo_consumer
SERVICE_DB_PASS=<generated_password>
```

---

## 3. Pre-Migration Checklist

Execute this checklist **before** starting the migration for any service.

### 3.1 Backups

- [ ] Full backup of monolith `ftgo` database completed
  ```bash
  mysqldump -h $MONOLITH_DB_HOST -u $MONOLITH_DB_USER -p$MONOLITH_DB_PASS \
    --single-transaction --routines --triggers $MONOLITH_DB_NAME \
    > ftgo_backup_$(date +%Y%m%d_%H%M%S).sql
  ```
- [ ] Backup verified (restore to test instance and validate)
- [ ] Backup stored in designated backup location

### 3.2 Communication

- [ ] Migration window scheduled and communicated to stakeholders
- [ ] On-call team notified
- [ ] Rollback plan reviewed with team

### 3.3 Environment Validation

- [ ] Target MySQL instance is accessible and has sufficient disk space
- [ ] Flyway migration files are present in the service JAR/classpath
- [ ] Application configuration points to the new service database
- [ ] Monitoring and alerting are active

---

## 4. Step-by-Step Migration Procedure

> **Repeat this procedure for each service in the recommended order.**
> The examples below use the Consumer Service. Adjust database names, table names,
> and credentials for each service.

### 4.1 Create Service Database and User

```sql
-- Connect as MySQL admin
mysql -h $SERVICE_DB_HOST -u root -p

-- Create the service database
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create a dedicated database user for the service
CREATE USER IF NOT EXISTS 'ftgo_consumer'@'%' IDENTIFIED BY '<generated_password>';

-- Grant privileges (least-privilege principle)
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
  ON ftgo_consumer_service.*
  TO 'ftgo_consumer'@'%';

-- Flyway needs CREATE TABLE and ALTER for schema management
-- The above grants cover this

FLUSH PRIVILEGES;
```

**Database and user names per service:**

| Service | Database | User | Tables |
|---------|----------|------|--------|
| Consumer | `ftgo_consumer_service` | `ftgo_consumer` | `consumers` |
| Courier | `ftgo_courier_service` | `ftgo_courier` | `courier`, `courier_actions` |
| Order | `ftgo_order_service` | `ftgo_order` | `orders`, `order_line_items` |
| Restaurant | `ftgo_restaurant_service` | `ftgo_restaurant` | `restaurants`, `restaurant_menu_items` |

### 4.2 Run Flyway Migration (Schema Only)

**Option A: Spring Boot auto-migration (recommended)**

Start the service application with Flyway enabled. It will automatically apply the V1
migration on startup.

```bash
# Set environment variables
export DB_HOST=$SERVICE_DB_HOST
export DB_PORT=$SERVICE_DB_PORT
export DB_USER=ftgo_consumer
export DB_PASSWORD=<generated_password>

# Start the service (Flyway runs on startup)
java -jar services/ftgo-consumer-service/build/libs/ftgo-consumer-service.jar
```

**Option B: Flyway CLI**

```bash
flyway -url=jdbc:mysql://$SERVICE_DB_HOST:$SERVICE_DB_PORT/ftgo_consumer_service \
       -user=ftgo_consumer \
       -password=<generated_password> \
       -locations=filesystem:services/ftgo-consumer-service/src/main/resources/db/migration \
       migrate
```

**Verification:**

```sql
-- Check that tables were created
USE ftgo_consumer_service;
SHOW TABLES;
-- Expected: consumers, flyway_schema_history

-- Check Flyway history
SELECT * FROM flyway_schema_history;
-- Expected: 1 row, version=1, success=true
```

### 4.3 Export Data from Monolith

```bash
# Export Consumer Service data
mysqldump -h $MONOLITH_DB_HOST -u $MONOLITH_DB_USER -p$MONOLITH_DB_PASS \
  --single-transaction \
  --no-create-info \
  --complete-insert \
  --skip-triggers \
  $MONOLITH_DB_NAME consumers \
  > consumer_data_export.sql

# Verify export
echo "Exported rows:"
grep -c "^INSERT" consumer_data_export.sql
```

**Export commands per service:**

| Service | Tables to Export | Command Suffix |
|---------|-----------------|---------------|
| Consumer | `consumers` | `$MONOLITH_DB_NAME consumers` |
| Courier | `courier courier_actions` | `$MONOLITH_DB_NAME courier courier_actions` |
| Order | `orders order_line_items` | `$MONOLITH_DB_NAME orders order_line_items` |
| Restaurant | `restaurants restaurant_menu_items` | `$MONOLITH_DB_NAME restaurants restaurant_menu_items` |

### 4.4 Import Data into Service Database

```bash
# Import into the service database
mysql -h $SERVICE_DB_HOST -u ftgo_consumer -p$SERVICE_DB_PASS \
  ftgo_consumer_service < consumer_data_export.sql
```

**Verification:**

```sql
-- Compare row counts
SELECT 'monolith' AS source, COUNT(*) AS row_count FROM ftgo.consumers
UNION ALL
SELECT 'service' AS source, COUNT(*) AS row_count FROM ftgo_consumer_service.consumers;
```

### 4.5 Reset AUTO_INCREMENT Counters

After importing data with existing IDs, reset AUTO_INCREMENT to avoid conflicts:

```sql
-- For Consumer Service
SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_consumer_service.consumers;
SET @sql = CONCAT('ALTER TABLE ftgo_consumer_service.consumers AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

**For services with multiple tables (Order, Courier):**

```sql
-- Order Service
SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_order_service.orders;
SET @sql = CONCAT('ALTER TABLE ftgo_order_service.orders AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_order_service.order_line_items;
SET @sql = CONCAT('ALTER TABLE ftgo_order_service.order_line_items AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Courier Service
SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_courier_service.courier;
SET @sql = CONCAT('ALTER TABLE ftgo_courier_service.courier AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT @max_id := COALESCE(MAX(id), 0) + 1 FROM ftgo_courier_service.courier_actions;
SET @sql = CONCAT('ALTER TABLE ftgo_courier_service.courier_actions AUTO_INCREMENT = ', @max_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
```

### 4.6 Enable Dual-Write (Transition Period)

During the transition, the monolith application writes to both the old and new databases.

1. Deploy application update with dual-write enabled (feature flag)
2. Monitor for write failures to the new database
3. Run reconciliation checks (see Section 5.2)
4. After verification period (24–72 hours), proceed to cutover

### 4.7 Cutover

1. **Stop writes** to the monolith tables for the migrating service
2. **Final data sync** — export and import any remaining rows
3. **Switch reads** to the new service database
4. **Verify** application functionality with the new database
5. **Disable dual-write** for this service
6. **Monitor** for 24 hours

### 4.8 Decommission Monolith Tables

After successful cutover and monitoring period:

```sql
-- Only after all verification is complete and rollback window has passed
-- CAUTION: This is irreversible

-- Step 1: Drop cross-service FKs referencing this table (if any)
-- (Example: Order Service tables reference restaurants)
ALTER TABLE ftgo.orders DROP FOREIGN KEY orders_restaurant_id;

-- Step 2: Drop the table from the monolith
-- DROP TABLE ftgo.consumers;  -- UNCOMMENT ONLY WHEN READY
```

> **WARNING:** Do not drop monolith tables until the rollback window (minimum 2 weeks)
> has passed and all verification checks are green.

---

## 5. Post-Migration Verification

### 5.1 Data Integrity Checks

Run these checks immediately after data import and again after cutover.

```sql
-- 1. Row count comparison
SELECT 'consumers' AS tbl,
       (SELECT COUNT(*) FROM ftgo.consumers) AS monolith_count,
       (SELECT COUNT(*) FROM ftgo_consumer_service.consumers) AS service_count,
       (SELECT COUNT(*) FROM ftgo.consumers) -
       (SELECT COUNT(*) FROM ftgo_consumer_service.consumers) AS diff;

-- 2. ID range check (ensure no gaps/overlaps)
SELECT 'consumers' AS tbl,
       (SELECT MIN(id) FROM ftgo_consumer_service.consumers) AS min_id,
       (SELECT MAX(id) FROM ftgo_consumer_service.consumers) AS max_id,
       (SELECT AUTO_INCREMENT FROM information_schema.TABLES
        WHERE TABLE_SCHEMA='ftgo_consumer_service' AND TABLE_NAME='consumers') AS auto_inc;

-- 3. Sample data comparison (spot check)
SELECT m.id, m.first_name, m.last_name,
       s.id AS s_id, s.first_name AS s_first_name, s.last_name AS s_last_name
FROM ftgo.consumers m
JOIN ftgo_consumer_service.consumers s ON m.id = s.id
LIMIT 10;
```

### 5.2 Reconciliation Checks

Run periodically during the dual-write period:

```sql
-- Find rows in monolith but not in service DB
SELECT m.id FROM ftgo.consumers m
LEFT JOIN ftgo_consumer_service.consumers s ON m.id = s.id
WHERE s.id IS NULL;

-- Find rows in service DB but not in monolith
SELECT s.id FROM ftgo_consumer_service.consumers s
LEFT JOIN ftgo.consumers m ON s.id = m.id
WHERE m.id IS NULL;
```

### 5.3 Application Health Checks

- [ ] Service starts successfully with new database
- [ ] Flyway migration history shows version 1 as `success=true`
- [ ] API endpoints return correct data (smoke test)
- [ ] No errors in application logs related to database connectivity
- [ ] Prometheus metrics show normal query latency
- [ ] No increase in error rates on Grafana dashboards

### 5.4 Cross-Service Reference Validation

After all services are migrated, verify cross-service references are intact:

```sql
-- Orders referencing valid consumers
SELECT o.id, o.consumer_id
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_consumer_service.consumers c ON o.consumer_id = c.id
WHERE c.id IS NULL AND o.consumer_id IS NOT NULL;

-- Orders referencing valid restaurants
SELECT o.id, o.restaurant_id
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_restaurant_service.restaurants r ON o.restaurant_id = r.id
WHERE r.id IS NULL AND o.restaurant_id IS NOT NULL;

-- Orders referencing valid couriers
SELECT o.id, o.assigned_courier_id
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_courier_service.courier c ON o.assigned_courier_id = c.id
WHERE c.id IS NULL AND o.assigned_courier_id IS NOT NULL;

-- Courier actions referencing valid orders
SELECT ca.courier_id, ca.order_id
FROM ftgo_courier_service.courier_actions ca
LEFT JOIN ftgo_order_service.orders o ON ca.order_id = o.id
WHERE o.id IS NULL AND ca.order_id IS NOT NULL;
```

> **Note:** These cross-database JOINs work only when all service databases are on the
> same MySQL instance. For separate instances, use application-level reconciliation.

---

## 6. Rollback Procedures

### 6.1 Rollback During Schema Migration (Step 4.2)

If the Flyway migration fails:

```bash
# Drop the service database and recreate
mysql -u root -p -e "DROP DATABASE IF EXISTS ftgo_consumer_service;"
mysql -u root -p -e "CREATE DATABASE ftgo_consumer_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Fix the migration file, then re-run
```

### 6.2 Rollback During Data Import (Steps 4.3–4.5)

If data import fails or validation shows mismatches:

```sql
-- Truncate service tables and retry
TRUNCATE TABLE ftgo_consumer_service.consumers;

-- Re-run export and import from step 4.3
```

### 6.3 Rollback During Dual-Write (Step 4.6)

If inconsistencies are detected during dual-write:

1. Disable the dual-write feature flag
2. Application reverts to writing only to monolith database
3. Investigate and fix the root cause
4. Re-sync data from monolith to service database
5. Re-enable dual-write

### 6.4 Full Rollback (After Cutover)

If critical issues arise after cutover:

```bash
# 1. Reconfigure application to use monolith database
export DB_HOST=$MONOLITH_DB_HOST
export DB_NAME=$MONOLITH_DB_NAME
export DB_USER=$MONOLITH_DB_USER
export DB_PASSWORD=$MONOLITH_DB_PASS

# 2. Restart the application
# (deploy previous application version or toggle feature flag)

# 3. Sync any new data from service DB back to monolith
# (manual step — run reconciliation queries to identify new/modified rows)
```

---

## 7. Troubleshooting

### 7.1 Common Issues

| Issue | Symptom | Resolution |
|-------|---------|------------|
| Flyway checksum mismatch | `FlywayValidateException` on startup | Do NOT modify applied migrations; create a new V2 migration for fixes |
| AUTO_INCREMENT collision | Duplicate key error on INSERT | Reset AUTO_INCREMENT to `MAX(id) + 1` (Step 4.5) |
| Connection refused | `CommunicationsException` | Verify DB host, port, firewall rules, and user permissions |
| Access denied | `Access denied for user` | Verify user credentials and GRANT statements (Step 4.1) |
| Character encoding issues | Garbled text in imported data | Ensure both source and target use `utf8mb4`; add `--default-character-set=utf8mb4` to mysqldump |
| Foreign key constraint failure | `Cannot add or update a child row` | Import parent tables before child tables (e.g., `courier` before `courier_actions`) |

### 7.2 Emergency Contacts

| Role | Contact | Escalation |
|------|---------|-----------|
| Database Admin | (fill in) | First responder for DB issues |
| Service Owner | (fill in) | Application-level issues |
| Platform Team | (fill in) | Infrastructure and deployment |

---

## 8. Service-Specific Notes

### 8.1 Consumer Service

- **Simplest migration** — single table, no foreign keys, no cross-service references
- Good candidate for **pilot migration** to validate the process
- `consumers.id` in the monolith uses `hibernate_sequence` (not AUTO_INCREMENT);
  ensure IDs are imported explicitly and AUTO_INCREMENT is reset

### 8.2 Restaurant Service

- Two tables: `restaurants` and `restaurant_menu_items`
- `restaurant_menu_items` has a FK to `restaurants` (intra-service) — import
  `restaurants` first
- The monolith's `orders.restaurant_id` FK must be dropped **before or simultaneously
  with** the Restaurant Service migration (see Strategy doc, Section 5.3)

### 8.3 Courier Service

- Two tables: `courier` and `courier_actions`
- `courier_actions` references `orders.id` (cross-service) — this FK is **not**
  created in the new schema
- `courier_actions` in the monolith lacks a primary key; the new schema adds an
  `id AUTO_INCREMENT` column — data import script must account for this
- Import `courier` before `courier_actions` (FK dependency)

**Special import step for courier_actions:**

```sql
-- The monolith courier_actions table has no id column.
-- When importing, let AUTO_INCREMENT generate the id.
INSERT INTO ftgo_courier_service.courier_actions (courier_id, order_id, time, type)
SELECT courier_id, order_id, time, type
FROM ftgo.courier_actions;
```

### 8.4 Order Service

- Two tables: `orders` and `order_line_items`
- **Most complex** — has 3 cross-service references (`consumer_id`, `restaurant_id`,
  `assigned_courier_id`)
- Migrate **last** to ensure all referenced services are already operational
- `order_line_items` in the monolith lacks a primary key; the new schema adds an
  `id AUTO_INCREMENT` column — same approach as courier_actions

**Special import step for order_line_items:**

```sql
-- The monolith order_line_items table has no id column.
-- When importing, let AUTO_INCREMENT generate the id.
INSERT INTO ftgo_order_service.order_line_items (order_id, menu_item_id, name, price, quantity)
SELECT order_id, menu_item_id, name, price, quantity
FROM ftgo.order_line_items;
```

**Cross-service reference validation (run after all services migrated):**

```sql
-- Verify all consumer_ids in orders exist in Consumer Service
SELECT COUNT(*) AS orphaned_consumer_refs
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_consumer_service.consumers c ON o.consumer_id = c.id
WHERE c.id IS NULL AND o.consumer_id IS NOT NULL;

-- Verify all restaurant_ids in orders exist in Restaurant Service
SELECT COUNT(*) AS orphaned_restaurant_refs
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_restaurant_service.restaurants r ON o.restaurant_id = r.id
WHERE r.id IS NULL AND o.restaurant_id IS NOT NULL;

-- Verify all assigned_courier_ids in orders exist in Courier Service
SELECT COUNT(*) AS orphaned_courier_refs
FROM ftgo_order_service.orders o
LEFT JOIN ftgo_courier_service.courier c ON o.assigned_courier_id = c.id
WHERE c.id IS NULL AND o.assigned_courier_id IS NOT NULL;
```

All counts should return `0`. Non-zero values indicate orphaned references that
must be investigated before decommissioning the monolith database.
