# Data Synchronization Strategy

## Overview

With per-service databases, cross-service data consistency can no longer be enforced via foreign keys. This document defines the event-driven approach to maintaining data consistency across FTGO microservices.

## Architecture Decision

**Chosen approach: Event-Driven Eventual Consistency**

| Approach | Pros | Cons | Selected |
|----------|------|------|----------|
| Synchronous API calls | Simple, immediate consistency | Tight coupling, cascading failures | No |
| Event-driven (domain events) | Loose coupling, resilient | Eventual consistency, complexity | **Yes** |
| CDC (Change Data Capture) | No code changes needed | Infrastructure overhead, latency | Future consideration |
| Saga pattern | Handles distributed transactions | Complex orchestration | For complex flows |

## Domain Events

### Event Publishing

Each service publishes domain events when its entities change state:

#### Consumer Service Events
| Event | Trigger | Payload |
|-------|---------|---------|
| `ConsumerCreated` | New consumer registered | `consumerId`, `name` |
| `ConsumerUpdated` | Consumer profile changed | `consumerId`, changed fields |
| `ConsumerDeleted` | Consumer account removed | `consumerId` |

#### Courier Service Events
| Event | Trigger | Payload |
|-------|---------|---------|
| `CourierCreated` | New courier registered | `courierId`, `name`, `location` |
| `CourierAvailabilityChanged` | Courier goes on/off duty | `courierId`, `available` |
| `CourierLocationUpdated` | Courier moves | `courierId`, `location` |
| `DeliveryActionRecorded` | Courier performs delivery action | `courierId`, `orderId`, `actionType` |

#### Order Service Events
| Event | Trigger | Payload |
|-------|---------|---------|
| `OrderCreated` | New order placed | `orderId`, `consumerId`, `restaurantId`, `lineItems` |
| `OrderAccepted` | Restaurant accepts order | `orderId`, `acceptTime` |
| `OrderAssigned` | Courier assigned to order | `orderId`, `courierId` |
| `OrderPickedUp` | Courier picks up order | `orderId`, `pickedUpTime` |
| `OrderDelivered` | Order delivered | `orderId`, `deliveredTime` |
| `OrderCancelled` | Order cancelled | `orderId`, `reason` |

#### Restaurant Service Events
| Event | Trigger | Payload |
|-------|---------|---------|
| `RestaurantCreated` | New restaurant onboarded | `restaurantId`, `name`, `menu` |
| `RestaurantMenuUpdated` | Menu items changed | `restaurantId`, updated items |
| `RestaurantDeleted` | Restaurant removed | `restaurantId` |

### Event Consumers

Services subscribe to events from other services to maintain local read models:

| Subscribing Service | Subscribed Events | Local Action |
|--------------------|-------------------|--------------|
| Order Service | `RestaurantCreated`, `RestaurantMenuUpdated` | Cache restaurant info for order validation |
| Order Service | `ConsumerCreated` | Cache consumer info for order validation |
| Order Service | `CourierAvailabilityChanged` | Update available couriers for assignment |
| Courier Service | `OrderCreated`, `OrderCancelled` | Update delivery queue |
| Restaurant Service | `OrderCreated` | Prepare for incoming order |

## Cross-Service ID Validation

### At Write Time

When a service stores a cross-service ID (e.g., Order Service stores `restaurant_id`):

1. **Synchronous validation** (during order creation):
   - Call Restaurant Service API to verify restaurant exists
   - If unavailable, reject the request or retry
   
2. **Cached validation** (performance optimization):
   - Maintain a local cache of known restaurant IDs from events
   - Validate against cache first, fall back to API call

### At Read Time

When displaying data that spans services:

1. **API composition**: Gateway aggregates data from multiple services
2. **Local read models**: Services maintain denormalized copies via events

## Consistency Guarantees

### Per-Service (Strong Consistency)
- Intra-service FK constraints enforced by the database
- ACID transactions within a single service boundary
- Flyway migrations ensure schema consistency

### Cross-Service (Eventual Consistency)
- Domain events propagated asynchronously
- Idempotent event handlers ensure at-least-once delivery safety
- Reconciliation jobs detect and resolve inconsistencies

### Reconciliation

Periodic batch jobs verify cross-service data integrity:

```
Schedule: Every 6 hours (configurable)

1. Order Service queries all orders with restaurant_id
2. Calls Restaurant Service to verify each restaurant_id exists
3. Flags orders with invalid restaurant references
4. Alert on-call team if inconsistencies found
```

## Implementation Timeline

| Phase | Scope | Timeline |
|-------|-------|----------|
| Phase 1 | Per-service databases with Flyway migrations | Current (EM-29) |
| Phase 2 | Event publishing infrastructure (Kafka/RabbitMQ) | Future sprint |
| Phase 3 | Event consumers and local read models | Future sprint |
| Phase 4 | Saga orchestration for complex workflows | Future sprint |
| Phase 5 | CDC integration for legacy data sync | Optional |

## Failure Handling

### Event Publishing Failure
- Use transactional outbox pattern: write event to local `outbox` table in same transaction
- Background poller publishes events from outbox table
- Guarantees at-least-once delivery

### Event Processing Failure
- Dead letter queue for failed events
- Automated retry with exponential backoff
- Manual intervention dashboard for persistent failures

### Network Partition
- Services continue operating with local data
- Events queue up and replay when connectivity restores
- Compensating transactions for conflicting state
