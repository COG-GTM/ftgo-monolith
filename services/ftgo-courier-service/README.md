# FTGO Courier Service

## Overview
The Courier Service manages courier (delivery driver) information, availability, and assignments within the FTGO platform.

## Bounded Context
- **Domain**: Courier/Delivery Management
- **Aggregates**: Courier
- **Key Entities**: CourierAvailability, DeliveryAction, CourierPlan

## Package Structure
```
com.ftgo.courier
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
./gradlew :services:ftgo-courier-service:bootRun
```

## Configuration
- Default port: 8084
- Config file: `config/application.yml`

## API Documentation
Once running, Swagger UI is available at: `http://localhost:8084/swagger-ui.html`

## Migration Notes
This service is being migrated from the monolith module `ftgo-courier-service`.
- **Legacy package**: `net.chrisrichardson.ftgo.courierservice`
- **New package**: `com.ftgo.courier`
