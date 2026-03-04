# FTGO Order Service

## Overview
The Order Service manages the order lifecycle within the FTGO platform. It handles order creation, validation, and state management.

## Bounded Context
- **Domain**: Order Management
- **Aggregates**: Order, Ticket
- **Key Entities**: OrderLineItem, DeliveryInfo, PaymentInfo

## Package Structure
```
com.ftgo.order
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
./gradlew :services:ftgo-order-service:bootRun
```

## Configuration
- Default port: 8081
- Config file: `config/application.yml`

## API Documentation
Once running, Swagger UI is available at: `http://localhost:8081/swagger-ui.html`

## Migration Notes
This service is being migrated from the monolith module `ftgo-order-service`.
- **Legacy package**: `net.chrisrichardson.ftgo.orderservice`
- **New package**: `com.ftgo.order`
