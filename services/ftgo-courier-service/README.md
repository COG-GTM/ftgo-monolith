# FTGO Courier Service

## Overview

The Courier Service manages courier availability, assignment, and delivery tracking within the FTGO platform.

## Bounded Context

**Courier** - Responsible for:
- Courier registration and availability management
- Delivery assignment and dispatch
- Delivery status tracking and updates

## Package Structure

```
com.ftgo.courier
  ├── domain/          # Courier aggregate, Delivery
  ├── service/         # CourierService - business logic
  ├── web/             # CourierController - REST API
  ├── repository/      # CourierRepository - data access
  └── config/          # Spring configuration
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/couriers` | Register a new courier |
| GET | `/couriers/{courierId}` | Get courier details |
| PUT | `/couriers/{courierId}/availability` | Update courier availability |

## Dependencies

- `shared/ftgo-common` - Common value objects
- `shared/ftgo-domain` - Core domain entities
- `shared/ftgo-common-jpa` - JPA persistence utilities
