# FTGO Restaurant Service

## Overview

The Restaurant Service manages restaurant onboarding, menu management, and order acceptance within the FTGO platform.

## Bounded Context

**Restaurant** - Responsible for:
- Restaurant registration and configuration
- Menu item management
- Order ticket acceptance and preparation tracking

## Package Structure

```
com.ftgo.restaurant
  ├── domain/          # Restaurant aggregate, MenuItem, Ticket
  ├── service/         # RestaurantService - business logic
  ├── web/             # RestaurantController - REST API
  ├── repository/      # RestaurantRepository - data access
  └── config/          # Spring configuration
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/restaurants` | Register a new restaurant |
| GET | `/restaurants/{restaurantId}` | Get restaurant details |

## Dependencies

- `shared/ftgo-common` - Common value objects
- `shared/ftgo-domain` - Core domain entities
- `shared/ftgo-common-jpa` - JPA persistence utilities
