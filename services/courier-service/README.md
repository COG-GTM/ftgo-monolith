# Courier Service

Manages courier availability, assignments, and delivery tracking.

## Bounded Context

Courier/Delivery Management - handles courier registration, availability, and delivery fulfillment.

## Package Structure

```
com.ftgo.courier
  +-- domain/        # Domain entities, value objects, aggregates
  +-- service/       # Application services and use cases
  +-- web/           # REST controllers and DTOs
  +-- config/        # Spring configuration classes
  +-- messaging/     # Event publishers and consumers
```

## Running Locally

```bash
./gradlew :services:courier-service:bootRun
```

## API Port

Default: `8084`
