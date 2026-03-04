# FTGO Restaurant Service

## Overview
The Restaurant Service manages restaurant information, menus, and availability within the FTGO platform.

## Bounded Context
- **Domain**: Restaurant Management
- **Aggregates**: Restaurant
- **Key Entities**: MenuItem, RestaurantMenu, OpeningHours

## Package Structure
```
com.ftgo.restaurant
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
./gradlew :services:ftgo-restaurant-service:bootRun
```

## Configuration
- Default port: 8083
- Config file: `config/application.yml`

## API Documentation
Once running, Swagger UI is available at: `http://localhost:8083/swagger-ui.html`

## Migration Notes
This service is being migrated from the monolith module `ftgo-restaurant-service`.
- **Legacy package**: `net.chrisrichardson.ftgo.restaurantservice`
- **New package**: `com.ftgo.restaurant`
