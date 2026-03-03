# FTGO Consumer Service

## Overview

The Consumer Service manages consumer (customer) registration, profile management, and order validation within the FTGO platform.

## Bounded Context

**Consumer** - Responsible for:
- Consumer registration and profile management
- Order validation for consumers
- Consumer payment method management

## Package Structure

```
com.ftgo.consumer
  ├── domain/          # Consumer aggregate
  ├── service/         # ConsumerService - business logic
  ├── web/             # ConsumerController - REST API
  ├── repository/      # ConsumerRepository - data access
  └── config/          # Spring configuration
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/consumers` | Register a new consumer |
| GET | `/consumers/{consumerId}` | Get consumer details |

## Dependencies

- `shared/ftgo-common` - Common value objects
- `shared/ftgo-domain` - Core domain entities
- `shared/ftgo-common-jpa` - JPA persistence utilities
