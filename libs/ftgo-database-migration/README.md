# FTGO Database Migration Library

Shared Flyway configuration library for FTGO per-service database migrations.

## Overview

This library provides a standardized Flyway migration setup for all FTGO microservices. It includes:

- Spring Boot auto-configuration for Flyway
- Database naming conventions for per-service databases
- Configurable migration properties via `ftgo.flyway.*`

## Usage

Add this library as a dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-database-migration')
}
```

Place migration scripts under `src/main/resources/db/migration/` following Flyway naming conventions:

```
V1__create_initial_schema.sql
V2__add_indexes.sql
```

## Configuration

Default properties can be overridden in `application.yml`:

```yaml
ftgo:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: "0"
    validate-on-migrate: true
    out-of-order: false
    locations: classpath:db/migration
    table: flyway_schema_history
    clean-disabled: true
```

## Database Naming Convention

Each service uses its own database following the pattern: `ftgo_{service_name}_db`

| Service             | Database Name              |
|---------------------|----------------------------|
| order-service       | ftgo_order_service_db      |
| consumer-service    | ftgo_consumer_service_db   |
| courier-service     | ftgo_courier_service_db    |
| restaurant-service  | ftgo_restaurant_service_db |
