# FTGO Consumer Service

## Overview
The Consumer Service manages consumer (customer) accounts, profiles, and verification within the FTGO platform.

## Bounded Context
- **Domain**: Consumer Management
- **Aggregates**: Consumer
- **Key Entities**: ConsumerProfile, PaymentMethod

## Package Structure
```
com.ftgo.consumer
  ├── api/          # REST controllers and DTOs
  ├── domain/       # Domain model (aggregates, entities, value objects)
  ├── service/      # Application services / business logic
  ├── repository/   # Data access layer
  ├── config/       # Spring configuration
  └── messaging/    # Event publishing and consuming
```

## Running Locally
```bash
# From repository root
./gradlew :services:ftgo-consumer-service:bootRun
```

## Configuration
- Default port: 8082
- Config file: `config/application.yml`

## API Documentation
Once running, Swagger UI is available at: `http://localhost:8082/swagger-ui.html`

## Migration Notes
This service is being migrated from the monolith module `ftgo-consumer-service`.
- **Legacy package**: `net.chrisrichardson.ftgo.consumerservice`
- **New package**: `com.ftgo.consumer`
