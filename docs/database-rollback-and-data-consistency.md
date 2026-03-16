# Database Rollback and Data Consistency Strategy

## Overview

This document describes the rollback procedures and data consistency approach for
the database-per-service migration. When moving from a single shared database to
independent per-service schemas, we lose transactional guarantees that foreign keys
and ACID transactions previously provided. This document outlines how we address
those gaps.

## Rollback Strategy

### Migration Script Rollback

Each Flyway V1 migration creates a service's schema from scratch. If a migration
fails partway through, Flyway's built-in behavior handles rollback:

- **MySQL/InnoDB**: DDL statements are not transactional in MySQL. If a V1 migration
  fails mid-execution, the database may be left in a partial state.
- **Mitigation**: Each V1 migration is structured so that table creation statements
  are ordered by dependency (parent tables first, child tables second). If a
  migration fails, the recommended recovery is to drop the per-service database
  and re-run the migration from scratch.

### Per-Service Rollback Procedures

| Service            | Rollback Command                                        |
|--------------------|---------------------------------------------------------|
| Order Service      | `DROP DATABASE IF EXISTS ftgo_order_service;`           |
| Consumer Service   | `DROP DATABASE IF EXISTS ftgo_consumer_service;`        |
| Restaurant Service | `DROP DATABASE IF EXISTS ftgo_restaurant_service;`      |
| Courier Service    | `DROP DATABASE IF EXISTS ftgo_courier_service;`         |

After dropping the database, re-create it and let Flyway re-run all migrations
on the next service startup.

### Full Rollback to Monolith

During the migration period, the monolith database (`ftgo`) remains the source of
truth. To perform a full rollback:

1. Stop all per-service database writes.
2. The monolith `ftgo` database is unchanged and still operational.
3. Reconfigure services to point back to the shared `ftgo` database.
4. Drop per-service databases.
5. Restart services.

This is possible because the per-service schemas are structurally compatible with
the monolith schema — they contain the same tables and columns, minus the
cross-service foreign keys.

## Data Consistency Strategy

### Problem Statement

In the monolith, cross-table foreign keys ensure referential integrity:
- An order's `consumer_id` must reference a valid `consumers` row
- An order's `restaurant_id` must reference a valid `restaurants` row
- An order's `assigned_courier_id` must reference a valid `courier` row
- A `courier_actions.order_id` must reference a valid `orders` row

With database-per-service, these FK constraints cannot exist because the referenced
tables live in different databases. We must replace database-level integrity with
application-level consistency.

### Eventual Consistency Patterns

#### 1. Synchronous Validation at Write Time

Before creating or updating a record that references a cross-service entity, the
service makes a synchronous API call to validate that the referenced entity exists.

**Example — Order Creation:**
```
1. Client sends CreateOrderRequest(consumerId, restaurantId, lineItems)
2. Order Service calls Consumer Service: GET /consumers/{consumerId}
   - If 404 → reject order with "Consumer not found"
3. Order Service calls Restaurant Service: GET /restaurants/{restaurantId}
   - If 404 → reject order with "Restaurant not found"
4. Order Service creates the order locally
```

**Trade-offs:**
- Adds latency (extra API calls)
- Introduces coupling between services at write time
- Acceptable for operations where immediate validation is critical

#### 2. Event-Driven Consistency (Future Phase)

For operations where synchronous validation is too costly or creates tight
coupling, services publish domain events that other services consume to maintain
local read models.

**Example — Courier Assignment:**
```
1. Order Service publishes OrderAccepted event
2. Courier Service consumes event, assigns a courier
3. Courier Service publishes CourierAssigned event
4. Order Service consumes event, updates assigned_courier_id
```

This pattern will be implemented in a later phase when the event infrastructure
(e.g., Apache Kafka or RabbitMQ) is in place.

#### 3. Orphan Detection and Reconciliation

Because cross-service references can become stale (e.g., a consumer is deleted
but orders still reference that consumer_id), a periodic reconciliation process
should be implemented:

- **Scheduled job**: Runs periodically (e.g., nightly) to detect orphaned
  references.
- **Report**: Generates a report of records with invalid cross-service references.
- **Resolution**: Depending on business rules, orphaned records may be:
  - Flagged for manual review
  - Soft-deleted
  - Updated to a sentinel value (e.g., `consumer_id = -1` for "deleted consumer")

### Consistency Guarantees by Service

| Service            | Cross-Service References                        | Validation Strategy             |
|--------------------|-------------------------------------------------|---------------------------------|
| Order Service      | `consumer_id`, `restaurant_id`, `assigned_courier_id` | Sync validation at order creation; event-driven for courier assignment |
| Consumer Service   | None                                            | N/A — no cross-service references |
| Restaurant Service | None                                            | N/A — no cross-service references |
| Courier Service    | `courier_actions.order_id`                      | Event-driven (receives order events) |

### Data Migration Consistency

During the data migration from the monolith database to per-service databases:

1. **Snapshot isolation**: Take a consistent snapshot of the monolith database
   before starting the ETL process.
2. **Validation checksums**: After ETL, compare row counts and checksums between
   the source (monolith) and target (per-service) databases.
3. **Dual-read verification**: Temporarily read from both databases and compare
   results to detect discrepancies.

### ID Consistency Across Services

With per-service ID sequences, there is a risk of ID collisions if services
generate IDs in the same numeric range. Mitigations:

- **Current approach**: Each service uses its own sequence table initialized at 1.
  Since IDs are scoped to a service's own database, collisions are not possible
  within a service.
- **Cross-service references**: When referencing an entity in another service,
  the reference is by the original service's ID. There is no ambiguity because
  the service context is implicit (e.g., `consumer_id` always refers to an ID
  in the Consumer Service).
- **Future**: If a global ID namespace is needed (e.g., for event sourcing or
  cross-service search), consider adopting UUIDs or a distributed ID scheme
  (e.g., Twitter Snowflake).

## Monitoring and Alerting

To detect data consistency issues early, the following monitoring should be
implemented during and after the migration:

| Check                             | Frequency | Action on Failure                    |
|-----------------------------------|-----------|--------------------------------------|
| Cross-service reference validation | Nightly   | Alert + generate orphan report       |
| Row count comparison (during ETL)  | Per-batch | Halt migration + investigate         |
| Flyway migration status            | On deploy | Block deployment if migration fails  |
| API response validation            | Real-time | Circuit breaker + fallback response  |

## Summary

| Concern                  | Monolith Approach          | Microservices Approach                    |
|--------------------------|----------------------------|-------------------------------------------|
| Referential integrity    | Foreign key constraints    | Application-level validation + events     |
| Transaction scope        | Single ACID transaction    | Saga pattern / eventual consistency       |
| ID generation            | Shared `hibernate_sequence`| Per-service sequence tables               |
| Rollback                 | Single database rollback   | Per-service database drop + recreate      |
| Data consistency checks  | Database enforced          | Scheduled reconciliation + monitoring     |

## References

- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Database per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)
- [Event-Driven Architecture](https://microservices.io/patterns/data/event-driven-architecture.html)
- Migration strategy: `docs/database-migration-strategy.md`
