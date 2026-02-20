# Consumer Service

Manages consumer profiles, preferences, and payment methods.

## Bounded Context

Consumer Management - handles consumer registration, profile management, and validation.

## Package Structure

```
com.ftgo.consumer
  +-- domain/        # Domain entities, value objects, aggregates
  +-- service/       # Application services and use cases
  +-- web/           # REST controllers and DTOs
  +-- config/        # Spring configuration classes
  +-- messaging/     # Event publishers and consumers
```

## Running Locally

```bash
./gradlew :services:consumer-service:bootRun
```

## API Port

Default: `8082`
