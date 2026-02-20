# Order Service

Manages order lifecycle including creation, modification, cancellation, and fulfillment tracking.

## Bounded Context

Order Management - handles the complete order lifecycle from placement through delivery.

## Package Structure

```
com.ftgo.order
  +-- domain/        # Domain entities, value objects, aggregates
  +-- service/       # Application services and use cases
  +-- web/           # REST controllers and DTOs
  +-- config/        # Spring configuration classes
  +-- messaging/     # Event publishers and consumers
```

## Running Locally

```bash
./gradlew :services:order-service:bootRun
```

## API Port

Default: `8081`
