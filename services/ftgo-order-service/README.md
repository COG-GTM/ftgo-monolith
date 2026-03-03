# FTGO Order Service

## Overview

The Order Service manages the complete order lifecycle within the FTGO platform, including order creation, revision, cancellation, and fulfillment tracking.

## Bounded Context

**Order** - Responsible for:
- Order creation and validation
- Order state management (APPROVED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> DELIVERED)
- Order revision and cancellation
- Coordination with Consumer, Restaurant, and Courier services

## Package Structure

```
com.ftgo.order
  ├── domain/          # Order aggregate, OrderLineItem, OrderState
  ├── service/         # OrderService - business logic
  ├── web/             # OrderController - REST API
  ├── repository/      # OrderRepository - data access
  └── config/          # Spring configuration
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/orders` | Create a new order |
| GET | `/orders/{orderId}` | Get order details |
| PUT | `/orders/{orderId}/revise` | Revise an existing order |
| PUT | `/orders/{orderId}/cancel` | Cancel an order |

## Dependencies

- `shared/ftgo-common` - Common value objects
- `shared/ftgo-domain` - Core domain entities
- `shared/ftgo-common-jpa` - JPA persistence utilities
