# FTGO Per-Service Database Migration Strategy

## Overview

Each FTGO microservice owns its own database schema, enforcing data isolation and independent deployability. Schema migrations use Flyway with versioned SQL scripts.

## Database Ownership

| Service | Database | Schema Prefix |
|---------|----------|---------------|
| order-service | ftgo_order_db | `orders`, `order_line_items` |
| consumer-service | ftgo_consumer_db | `consumers` |
| restaurant-service | ftgo_restaurant_db | `restaurants`, `menu_items` |
| courier-service | ftgo_courier_db | `couriers`, `courier_actions` |

## Flyway Configuration

Each service configures Flyway in `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

Migration scripts live at:
```
services/<service-name>/src/main/resources/db/migration/
  V1__init_<service>_schema.sql
  V2__add_<feature>.sql
```

## Naming Conventions

- **Version format**: `V<number>__<description>.sql` (double underscore)
- **Description**: lowercase, underscores, descriptive (e.g., `V2__add_order_audit_trail.sql`)
- **Repeatable migrations**: `R__<description>.sql` for views or stored procedures

## Migration Rules

1. **Never modify** a migration that has been applied to any environment
2. **Always add new** migration files with the next version number
3. **Test migrations** against a copy of production data before deploying
4. **Include rollback** comments in each migration describing how to undo changes
5. **One concern per migration** — don't mix table creation with data migration

## Cross-Service Data Access

Services must never directly query another service's database. Use:
- **API calls** for synchronous reads
- **Events** for data replication (eventual consistency)
- **ID references** instead of foreign keys across service boundaries

## Local Development

For local development, each service's Docker Compose config creates its own MySQL database:

```yaml
services:
  order-db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ftgo_order_db
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:-rootpassword}
    ports:
      - "3306:3306"
```

## Migration from Monolith

The monolith uses a single `ftgo` database managed by `ftgo-flyway/`. During migration:

1. **Phase 1**: New services use separate databases from the start
2. **Phase 2**: Monolith tables are copied to service databases via one-time data migration scripts
3. **Phase 3**: Monolith writes are redirected to service APIs
4. **Phase 4**: Monolith database tables are deprecated and eventually dropped
