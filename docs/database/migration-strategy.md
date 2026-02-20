# Database Schema Migration Strategy

## Overview

FTGO uses Flyway for database schema migrations following a database-per-service pattern. Each microservice owns and manages its own database schema independently.

## Database-per-Service Architecture

Each microservice has its own dedicated database. This ensures:

- **Loose coupling**: Services are not coupled through shared database schemas
- **Independent deployability**: Schema changes in one service do not affect others
- **Technology flexibility**: Each service can choose its own database technology

## Database Naming Conventions

### Database Names

All databases follow the naming pattern: `ftgo_{service_name}_db`

| Service             | Database Name              | Port  |
|---------------------|----------------------------|-------|
| order-service       | ftgo_order_service_db      | 8081  |
| consumer-service    | ftgo_consumer_service_db   | 8082  |
| restaurant-service  | ftgo_restaurant_service_db | 8083  |
| courier-service     | ftgo_courier_service_db    | 8084  |

### Table Naming

- Use **snake_case** for all table and column names
- Use **plural nouns** for table names (e.g., `orders`, `consumers`, `restaurants`)
- Collection tables use the pattern `{parent}_{child}` (e.g., `order_line_items`, `restaurant_menu_items`)
- Foreign key columns use `{referenced_table_singular}_id` (e.g., `order_id`, `restaurant_id`)

### Column Conventions

- Primary keys: `id BIGINT AUTO_INCREMENT`
- Foreign keys: `{entity}_id BIGINT`
- Timestamps: `DATETIME(6)` for microsecond precision
- Money fields: `DECIMAL(19, 2)`
- Enumerations: `VARCHAR(50)` storing enum name strings
- Boolean fields: `BOOLEAN`

## Flyway Configuration

### Migration File Location

Each service stores its migration scripts under:

```
services/{service-name}/src/main/resources/db/migration/
```

### Migration File Naming

Flyway migration files follow the standard naming convention:

```
V{version}__{description}.sql
```

- `V` prefix indicates a versioned migration
- Version numbers are integers (e.g., `V1`, `V2`, `V3`)
- Double underscore `__` separates version from description
- Description uses snake_case

Examples:
```
V1__create_orders_schema.sql
V2__add_payment_fields.sql
V3__create_audit_table.sql
```

### Repeatable Migrations

For views or stored procedures that need to be recreated:

```
R__{description}.sql
```

### Spring Boot Integration

The `ftgo-database-migration` library provides Spring Boot auto-configuration. Services include it as a dependency:

```groovy
dependencies {
    implementation project(':libs:ftgo-database-migration')
}
```

### Configuration Properties

Default Flyway behavior is configured via `ftgo.flyway.*` properties:

| Property              | Default                   | Description                         |
|-----------------------|---------------------------|-------------------------------------|
| `enabled`             | `true`                    | Enable/disable Flyway migrations    |
| `baseline-on-migrate` | `true`                    | Baseline on first migration         |
| `baseline-version`    | `0`                       | Version to baseline at              |
| `validate-on-migrate` | `true`                    | Validate migrations before running  |
| `out-of-order`        | `false`                   | Allow out-of-order migrations       |
| `locations`           | `classpath:db/migration`  | Migration script locations          |
| `table`               | `flyway_schema_history`   | Schema history table name           |
| `clean-disabled`      | `true`                    | Disable `flyway clean` for safety   |

### Per-Environment Configuration

Override properties per environment using Spring profiles:

```yaml
# application-dev.yml
spring:
  flyway:
    baseline-on-migrate: true

# application-prod.yml
spring:
  flyway:
    baseline-on-migrate: false
    validate-on-migrate: true
```

## Service Schema Details

### Order Service (`ftgo_order_service_db`)

Tables:
- `orders` - Core order entity with state machine fields
- `order_line_items` - Line items for each order (embedded collection)

### Consumer Service (`ftgo_consumer_service_db`)

Tables:
- `consumers` - Consumer profiles with name fields

### Courier Service (`ftgo_courier_service_db`)

Tables:
- `courier` - Courier entity with address and availability
- `courier_actions` - Pickup/dropoff actions for delivery plans

### Restaurant Service (`ftgo_restaurant_service_db`)

Tables:
- `restaurants` - Restaurant entity with address
- `restaurant_menu_items` - Menu items for each restaurant (embedded collection)

## Migration Best Practices

1. **Never modify existing migrations** - Create new migration files for schema changes
2. **Make migrations idempotent** - Use `IF NOT EXISTS` and `IF EXISTS` guards
3. **Keep migrations small** - One logical change per migration file
4. **Test migrations locally** - Run against a local database before committing
5. **Include rollback plan** - Document how to reverse each migration
6. **Version control** - All migrations are committed to source control
7. **Avoid data migrations in schema files** - Separate schema DDL from data DML
8. **Use explicit column types** - Avoid database-specific defaults

## Local Development

### Setting Up Databases

Each service expects its own MySQL database. Use the following to create them:

```sql
CREATE DATABASE IF NOT EXISTS ftgo_order_service_db;
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service_db;
CREATE DATABASE IF NOT EXISTS ftgo_courier_service_db;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service_db;

CREATE USER IF NOT EXISTS 'ftgo'@'%' IDENTIFIED BY 'ftgo';
GRANT ALL PRIVILEGES ON ftgo_order_service_db.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_consumer_service_db.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_courier_service_db.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service_db.* TO 'ftgo'@'%';
FLUSH PRIVILEGES;
```

### Running Migrations

Migrations run automatically on application startup when Flyway is enabled. To run manually:

```bash
./gradlew :services:order-service:flywayMigrate
```

### Verifying Migration Status

Check the `flyway_schema_history` table in each service database:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```
