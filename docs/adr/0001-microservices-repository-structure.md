# ADR-0001: Microservices Repository Structure

## Status

Accepted

## Date

2026-02-17

## Context

The FTGO application is currently a monolith with 14 Gradle modules in a flat structure under a single repository. As we migrate to microservices, we need to decide on a repository strategy and define clear boundaries for each service.

### Current Monolith Structure

```
ftgo-monolith/
  ftgo-common/          # Shared value objects (Money, Address, PersonName)
  ftgo-common-jpa/      # Shared JPA utilities
  ftgo-domain/          # Domain entities and aggregates
  common-swagger/       # Swagger configuration
  ftgo-test-util/       # Test utilities
  ftgo-order-service/   # Order bounded context
  ftgo-consumer-service/# Consumer bounded context
  ftgo-restaurant-service/ # Restaurant bounded context
  ftgo-courier-service/ # Courier bounded context
  ftgo-*-service-api/   # API contracts per service
  ftgo-application/     # Monolith composition layer
  ftgo-flyway/          # Database migrations (single shared DB)
  ftgo-end-to-end-tests/ # E2E tests
```

### Decision Drivers

- Services share common libraries (value objects, JPA utilities)
- Team is small; coordinated releases are feasible during migration
- Need atomic refactoring across services during transition period
- CI/CD pipeline should validate cross-service compatibility
- Eventually services may move to independent repositories

## Decision

We adopt a **mono-repo with clear service boundaries** during the migration phase. Each microservice lives under `services/`, shared libraries under `libs/`, and infrastructure configuration under `infrastructure/`.

### Repository Layout

```
ftgo-monolith/
  # ── Existing monolith modules (preserved during migration) ──
  ftgo-common/
  ftgo-common-jpa/
  ftgo-domain/
  ftgo-application/
  ftgo-order-service/
  ftgo-consumer-service/
  ftgo-restaurant-service/
  ftgo-courier-service/
  ftgo-*-service-api/
  ftgo-flyway/
  ftgo-test-util/
  ftgo-end-to-end-tests/

  # ── New microservices structure ──
  services/
    ftgo-consumer-service/     # Consumer bounded context microservice
    ftgo-restaurant-service/   # Restaurant bounded context microservice
    ftgo-order-service/        # Order bounded context microservice
    ftgo-courier-service/      # Courier bounded context microservice
    template-service/          # Archetype for new services

  libs/
    ftgo-common-lib/           # Extracted shared value objects
    ftgo-common-jpa-lib/       # Extracted shared JPA utilities
    ftgo-domain-lib/           # Extracted domain entities

  infrastructure/
    docker/                    # Docker Compose files for local dev
    k8s/                       # Kubernetes manifests
    helm/                      # Helm charts

  buildSrc-platform/           # Shared Gradle convention plugins

  docs/
    adr/                       # Architecture Decision Records
```

### Package Naming Convention

All microservices follow a consistent Java package structure:

```
net.chrisrichardson.ftgo.<service>.<layer>
```

Where:
- `<service>` is the bounded context name: `consumer`, `restaurant`, `order`, `courier`
- `<layer>` is one of: `domain`, `api`, `service`, `repository`, `config`, `web`, `messaging`

Examples:
- `net.chrisrichardson.ftgo.consumer.domain` - Consumer domain entities
- `net.chrisrichardson.ftgo.order.web` - Order REST controllers
- `net.chrisrichardson.ftgo.restaurant.repository` - Restaurant data access

Shared libraries retain the existing package root:
- `net.chrisrichardson.ftgo.common` - Common value objects
- `net.chrisrichardson.ftgo.common.jpa` - JPA utilities

### Module Naming Convention

Gradle subproject names follow the pattern:

- Services: `ftgo-<context>-service` (e.g., `ftgo-order-service`)
- Service APIs: `ftgo-<context>-service-api` (e.g., `ftgo-order-service-api`)
- Shared libraries: `ftgo-<name>-lib` (e.g., `ftgo-common-lib`)

## Consequences

### Positive

- Atomic commits across services during migration phase
- Single CI pipeline validates cross-service compatibility
- Shared build configuration (version catalog, convention plugins)
- Easy to extract to independent repos later (each `services/*` directory is self-contained)
- Existing monolith code preserved alongside new structure

### Negative

- Larger repository size
- Risk of accidental cross-service coupling if boundaries not enforced
- All services share the same CI pipeline (slower feedback for individual changes)

### Mitigations

- Enforce service boundaries via Gradle module dependencies (services cannot depend on each other)
- Use path-based CI triggers to only build affected services
- Plan for eventual extraction to independent repos once migration stabilizes

## References

- [Mono-repo vs Multi-repo](https://monorepo.tools/)
- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns/)
