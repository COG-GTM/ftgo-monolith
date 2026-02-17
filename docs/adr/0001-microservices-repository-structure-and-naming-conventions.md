# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status

Proposed

## Date

2026-02-17

## Context

The FTGO application is currently a monolith built as a multi-module Gradle project with 14 modules in a flat structure. The modules include shared libraries (`ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`), four bounded-context services (`ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`), their corresponding API modules, a composite application module (`ftgo-application`), database migrations (`ftgo-flyway`), and end-to-end tests.

All modules share a single `settings.gradle`, a single build pipeline, and a single deployment artifact. The package root is `net.chrisrichardson.ftgo`.

As we migrate to microservices, we need a repository structure that:
- Enables independent service development, testing, and deployment
- Maintains clear boundaries between bounded contexts
- Preserves shared library reuse without tight coupling
- Supports a gradual migration (Strangler Fig pattern) from monolith to microservices
- Provides a consistent, discoverable project layout for all teams

## Decision

### 1. Repository Strategy: Structured Mono-Repo

We will use a **structured mono-repo** with clear service boundaries rather than splitting into multiple repositories.

**Rationale:**
- The team is small and all four bounded contexts are closely related
- A mono-repo simplifies cross-service refactoring during the migration period
- Shared libraries (`ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`) can be versioned and consumed in-tree without publishing to an artifact repository
- Atomic commits across services and shared code reduce integration risk
- Tooling (CI, IDE support, dependency management) is simpler with a single repo
- Once the migration stabilizes, individual services can be extracted to separate repos if needed

### 2. Directory Structure

```
ftgo-monolith/                          # Repository root
|
|-- docs/                               # Project-wide documentation
|   |-- adr/                            # Architecture Decision Records
|   |-- architecture/                   # Architecture diagrams and docs
|   |-- runbooks/                       # Operational runbooks
|
|-- libs/                               # Shared libraries (published as JARs)
|   |-- ftgo-common/                    # Common utilities (Money, Address, etc.)
|   |-- ftgo-common-jpa/                # Shared JPA configuration
|   |-- ftgo-domain/                    # Shared domain events and value objects
|   |-- ftgo-test-util/                 # Shared test utilities
|   |-- common-swagger/                 # Shared Swagger/OpenAPI config
|
|-- services/                           # Microservices (one per bounded context)
|   |-- ftgo-consumer-service/          # Consumer bounded context
|   |   |-- src/
|   |   |   |-- main/java/...           # Application source
|   |   |   |-- main/resources/         # application.yml, etc.
|   |   |   |-- test/java/...           # Unit and integration tests
|   |   |   |-- test/resources/         # Test configuration
|   |   |-- docker/
|   |   |   |-- Dockerfile              # Service Dockerfile
|   |   |   |-- docker-compose.yml      # Local dev compose (service + deps)
|   |   |-- k8s/
|   |   |   |-- deployment.yaml         # Kubernetes deployment manifest
|   |   |   |-- service.yaml            # Kubernetes service manifest
|   |   |   |-- configmap.yaml          # Environment configuration
|   |   |-- config/
|   |   |   |-- application.yml         # Externalized Spring config
|   |   |   |-- application-local.yml   # Local development overrides
|   |   |   |-- application-prod.yml    # Production overrides
|   |   |-- build.gradle                # Service-specific build file
|   |   |-- README.md                   # Service documentation
|   |
|   |-- ftgo-order-service/             # Order bounded context (same layout)
|   |-- ftgo-restaurant-service/        # Restaurant bounded context (same layout)
|   |-- ftgo-courier-service/           # Courier bounded context (same layout)
|
|-- apis/                               # Service API contracts (DTOs, events)
|   |-- ftgo-consumer-service-api/
|   |-- ftgo-order-service-api/
|   |-- ftgo-restaurant-service-api/
|   |-- ftgo-courier-service-api/
|
|-- infrastructure/                     # Shared infrastructure configuration
|   |-- docker/                         # Root docker-compose for full stack
|   |-- k8s/                            # Cluster-wide K8s manifests (namespaces, ingress)
|   |-- flyway/                         # Database migration scripts (from ftgo-flyway)
|   |-- scripts/                        # Build, deploy, and utility scripts
|
|-- ftgo-application/                   # Monolith application (kept during migration)
|
|-- buildSrc/                           # Gradle build plugins (existing)
|-- build.gradle                        # Root build file
|-- settings.gradle                     # Gradle module declarations
|-- gradle.properties                   # Shared build properties
|-- docker-compose.yml                  # Root-level compose (legacy, delegates to infrastructure/)
```

### 3. Package Naming Convention

Transition from `net.chrisrichardson.ftgo` to `com.ftgo` as the package root for all new microservice code.

**Pattern:**
```
com.ftgo.<service>.<layer>
```

**Layers:**

| Layer        | Package Suffix | Purpose                              |
|-------------|----------------|--------------------------------------|
| domain      | `.domain`      | Entities, aggregates, value objects, domain services, repository interfaces |
| application | `.application` | Use cases, application services, command/query handlers |
| web         | `.web`         | REST controllers, request/response DTOs, exception handlers |
| config      | `.config`      | Spring configuration classes         |
| events      | `.events`      | Domain events, event publishers/subscribers |
| messaging   | `.messaging`   | Message channel adapters, async handlers |

**Examples:**

| Module                    | Package                                     |
|--------------------------|---------------------------------------------|
| ftgo-order-service       | `com.ftgo.orderservice.domain`              |
|                          | `com.ftgo.orderservice.application`         |
|                          | `com.ftgo.orderservice.web`                 |
|                          | `com.ftgo.orderservice.config`              |
| ftgo-consumer-service    | `com.ftgo.consumerservice.domain`           |
|                          | `com.ftgo.consumerservice.web`              |
| ftgo-restaurant-service  | `com.ftgo.restaurantservice.domain`         |
|                          | `com.ftgo.restaurantservice.web`            |
| ftgo-courier-service     | `com.ftgo.courierservice.domain`            |
|                          | `com.ftgo.courierservice.web`               |
| ftgo-order-service-api   | `com.ftgo.orderservice.api.events`          |
|                          | `com.ftgo.orderservice.api.web`             |
| ftgo-common              | `com.ftgo.common`                           |
| ftgo-domain              | `com.ftgo.domain`                           |

**Migration Note:** During the transition period, existing code retains the `net.chrisrichardson.ftgo` package. New microservice code uses `com.ftgo`. A full package migration will be performed in a dedicated task once all services are extracted.

### 4. Gradle Module Naming Conventions

| Category        | Naming Pattern                  | Example                         |
|----------------|--------------------------------|---------------------------------|
| Service         | `ftgo-<context>-service`       | `ftgo-order-service`            |
| Service API     | `ftgo-<context>-service-api`   | `ftgo-order-service-api`        |
| Shared Library  | `ftgo-<name>`                  | `ftgo-common`, `ftgo-domain`    |
| Infrastructure  | `ftgo-<tool>`                  | `ftgo-flyway`                   |

**Gradle project paths** will reflect the directory structure:

| Directory                                | Gradle Project Path                      |
|-----------------------------------------|-----------------------------------------|
| `services/ftgo-order-service`           | `:services:ftgo-order-service`          |
| `apis/ftgo-order-service-api`           | `:apis:ftgo-order-service-api`          |
| `libs/ftgo-common`                      | `:libs:ftgo-common`                     |
| `infrastructure/flyway`                 | `:infrastructure:flyway`                |

### 5. Service Template

Every new microservice MUST include the following structure (see `services/_template/` for a clonable archetype):

```
services/ftgo-<name>-service/
|-- src/
|   |-- main/
|   |   |-- java/com/ftgo/<name>service/
|   |   |   |-- domain/          # Domain model
|   |   |   |-- application/     # Application services
|   |   |   |-- web/             # REST layer
|   |   |   |-- config/          # Spring config
|   |   |   |-- <Name>ServiceApplication.java  # Spring Boot main class
|   |   |-- resources/
|   |       |-- application.yml
|   |-- test/
|       |-- java/com/ftgo/<name>service/
|       |   |-- domain/          # Domain unit tests
|       |   |-- web/             # Controller tests
|       |-- resources/
|           |-- application-test.yml
|-- docker/
|   |-- Dockerfile
|   |-- docker-compose.yml
|-- k8s/
|   |-- deployment.yaml
|   |-- service.yaml
|   |-- configmap.yaml
|-- config/
|   |-- application.yml
|   |-- application-local.yml
|   |-- application-prod.yml
|-- build.gradle
|-- README.md
```

## Consequences

### Positive
- Clear ownership boundaries per bounded context
- Services can be built and tested independently via Gradle subproject builds
- Shared libraries are versioned in-tree, reducing dependency management overhead
- Template enforces consistency across all services
- Gradual migration: monolith and microservices coexist in the same repo
- CI can target changed modules only (build matrix optimization)

### Negative
- Mono-repo requires discipline to maintain service boundaries (no cross-service imports)
- CI pipeline complexity increases as service count grows
- Larger repo checkout size (mitigated by sparse checkout for individual service work)

### Risks
- Teams may inadvertently couple services through shared library changes
- Mitigation: enforce API contracts via the `apis/` modules; shared libs must remain backward-compatible

## References

- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns/)
- [Mono-repo vs Multi-repo](https://www.atlassian.com/git/tutorials/monorepos)
- [Strangler Fig Pattern](https://martinfowler.com/bliki/StranglerFigApplication.html)
- FTGO Monolith source: `settings.gradle`, `build.gradle`
