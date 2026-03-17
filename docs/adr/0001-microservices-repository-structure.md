# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status

Accepted

## Date

2026-03-17

## Context

The FTGO application is currently a modular monolith built with a multi-module Gradle layout containing 14 flat modules under the root project directory. As we migrate to a microservices architecture, we need to define a clear repository structure, naming conventions, and module organization that supports independent service development while maintaining a unified build during the transition period.

### Current Monolith Structure

The existing monolith uses a flat Gradle multi-module layout:

```
ftgo-monolith/
  ├── ftgo-common/                  # Shared utilities
  ├── ftgo-common-jpa/              # Shared JPA configuration
  ├── ftgo-domain/                  # Core domain entities
  ├── common-swagger/               # API documentation
  ├── ftgo-test-util/               # Test utilities
  ├── ftgo-order-service/           # Order business logic
  ├── ftgo-order-service-api/       # Order API contract
  ├── ftgo-consumer-service/        # Consumer business logic
  ├── ftgo-consumer-service-api/    # Consumer API contract
  ├── ftgo-restaurant-service/      # Restaurant business logic
  ├── ftgo-restaurant-service-api/  # Restaurant API contract
  ├── ftgo-courier-service/         # Courier business logic
  ├── ftgo-courier-service-api/     # Courier API contract
  ├── ftgo-application/             # Monolith assembly
  ├── ftgo-flyway/                  # Database migrations
  ├── ftgo-end-to-end-tests/        # E2E tests
  └── ftgo-end-to-end-tests-common/ # Shared E2E test utilities
```

Package root: `net.chrisrichardson.ftgo`

### Key Decisions Required

1. Mono-repo vs. multi-repo strategy
2. Directory layout for microservices
3. Package naming conventions for the new services
4. Gradle module naming and path conventions
5. Coexistence strategy with the existing monolith

## Decision

### 1. Mono-Repo with Service Boundaries

We adopt a **mono-repo** approach with clear service boundaries under a top-level `services/` directory.

**Rationale:**
- Enables atomic cross-service refactoring during the migration period
- Shared build infrastructure (Gradle, CI/CD) reduces overhead
- Easier dependency management across services during transition
- Existing monolith modules remain untouched at root level
- Allows extraction to independent repos later if needed

### 2. Directory Structure per Service

Each microservice follows a standardized directory layout:

```
services/<service-name>/
  ├── <service-name>-api/           # Public API contract module
  │   ├── build.gradle
  │   └── src/main/java/com/ftgo/<servicename>/api/
  │       ├── events/               # Domain events published by service
  │       └── web/                  # Request/response DTOs
  ├── <service-name>-impl/          # Implementation module
  │   ├── build.gradle
  │   └── src/
  │       ├── main/java/com/ftgo/<servicename>/
  │       │   ├── domain/           # Entities, aggregates, domain services
  │       │   ├── repository/       # Spring Data JPA repositories
  │       │   ├── web/              # REST controllers
  │       │   ├── config/           # Spring configuration classes
  │       │   └── messaging/        # Event publishing and consumption
  │       ├── main/resources/       # application.properties, migrations
  │       └── test/                 # Unit and integration tests
  ├── config/                       # Environment-specific configuration
  │   └── application.properties
  ├── docker/                       # Container build files
  │   └── Dockerfile
  └── k8s/                          # Kubernetes deployment manifests
      └── deployment.yaml
```

**Rationale:**
- API and implementation separation enforces clean contracts between services
- Standard sub-package layout (domain/web/repository/config/messaging) promotes consistency
- Deployment artifacts (Docker, K8s) co-located with service source for easy maintenance
- Environment config separate from source resources allows per-environment overrides

### 3. Package Naming Convention

**New services** use the package root `com.ftgo.<servicename>.<layer>`:

| Layer         | Package                              | Purpose                                 |
|---------------|--------------------------------------|-----------------------------------------|
| API events    | `com.ftgo.<svc>.api.events`          | Domain events published by service      |
| API web       | `com.ftgo.<svc>.api.web`             | DTOs for REST API                       |
| Domain        | `com.ftgo.<svc>.domain`              | Entities, aggregates, domain services   |
| Repository    | `com.ftgo.<svc>.repository`          | Spring Data JPA repositories            |
| Web           | `com.ftgo.<svc>.web`                 | REST controllers                        |
| Config        | `com.ftgo.<svc>.config`              | Spring configuration                    |
| Messaging     | `com.ftgo.<svc>.messaging`           | Event publishing/consumption            |

Where `<svc>` is the service name without hyphens (e.g., `consumerservice`, `orderservice`).

**Migration note:** The existing monolith modules retain their `net.chrisrichardson.ftgo` package root. New microservice modules use `com.ftgo` to clearly distinguish migrated code from legacy code.

### 4. Gradle Module Naming

Gradle project paths follow a hierarchical convention:

```
:services:<service-name>:<service-name>-api
:services:<service-name>:<service-name>-impl
```

Examples:
```
:services:consumer-service:consumer-service-api
:services:consumer-service:consumer-service-impl
:services:order-service:order-service-api
:services:order-service:order-service-impl
```

Dependencies reference these paths:
```groovy
compile project(":services:consumer-service:consumer-service-api")
```

### 5. Additional Naming Conventions

| Aspect              | Convention                                  | Example                                       |
|---------------------|---------------------------------------------|-----------------------------------------------|
| Service directory   | `<context>-service`                         | `consumer-service`                             |
| API module          | `<context>-service-api`                     | `consumer-service-api`                         |
| Impl module         | `<context>-service-impl`                    | `consumer-service-impl`                        |
| Base package        | `com.ftgo.<context>service`                 | `com.ftgo.consumerservice`                     |
| Gradle path         | `:services:<svc>:<submodule>`               | `:services:consumer-service:consumer-service-api` |
| Docker image        | `ftgo/<service-name>`                       | `ftgo/consumer-service`                        |
| K8s deployment      | `<service-name>`                            | `consumer-service`                             |
| Database            | `ftgo_<context>_service`                    | `ftgo_consumer_service`                        |
| Config profile      | `application-{env}.properties`              | `application-prod.properties`                  |
| Port allocation     | 8082-8099 range for services                | consumer=8082, restaurant=8083, order=8084, courier=8085 |

### 6. Bounded Context to Service Mapping

| Bounded Context | Service Name         | Gradle Path Prefix                    | Port  |
|-----------------|----------------------|---------------------------------------|-------|
| Consumer        | consumer-service     | `:services:consumer-service`          | 8082  |
| Restaurant      | restaurant-service   | `:services:restaurant-service`        | 8083  |
| Order           | order-service        | `:services:order-service`             | 8084  |
| Courier         | courier-service      | `:services:courier-service`           | 8085  |

## Consequences

### Positive

- **Clear separation**: Service code is isolated under `services/` with well-defined boundaries
- **Parallel development**: Teams can work on different services independently
- **Consistent structure**: Template ensures all services follow the same layout and conventions
- **Incremental migration**: Monolith modules remain intact; services are built alongside
- **Deployment flexibility**: Each service has its own Docker and K8s configuration
- **Discoverability**: Predictable naming makes it easy to find code across services

### Negative

- **Build time**: More Gradle modules increase overall build time (mitigated by Gradle build cache)
- **Duplication risk**: Some code may initially exist in both monolith and service modules during migration
- **Cognitive overhead**: Developers must understand both the legacy flat layout and the new hierarchical layout during the transition

### Risks

- If services are not properly extracted, the `services/` directory could become a second monolith
- Package name change (`net.chrisrichardson.ftgo` -> `com.ftgo`) requires careful handling during code migration

## References

- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns)
- [ADR GitHub Organization](https://adr.github.io/)
- [Mono-repo vs Multi-repo](https://microservices.io/post/microservices/2024/11/11/mono-or-multi-repo.html)
