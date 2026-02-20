# Restaurant Service

Manages restaurant information, menus, and availability.

## Bounded Context

Restaurant Management - handles restaurant registration, menu management, and kitchen operations.

## Package Structure

```
com.ftgo.restaurant
  +-- domain/        # Domain entities, value objects, aggregates
  +-- service/       # Application services and use cases
  +-- web/           # REST controllers and DTOs
  +-- config/        # Spring configuration classes
  +-- messaging/     # Event publishers and consumers
```

## Running Locally

```bash
./gradlew :services:restaurant-service:bootRun
```

## API Port

Default: `8083`
