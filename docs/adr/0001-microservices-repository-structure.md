# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status
Accepted

## Date
2026-03-04

## Context

The FTGO application is currently a monolith with 14 Gradle modules in a flat structure. As part of the migration to microservices, we need to define a clear repository structure that:

- Supports gradual migration from monolith to microservices
- Provides clear boundaries between services
- Allows the monolith and microservices to coexist during the transition
- Establishes conventions that scale as new services are added

### Current Monolith Structure

The existing monolith uses a flat multi-module Gradle layout:

```
ftgo-monolith/
  ├── ftgo-common/                    # Shared utilities
  ├── ftgo-common-jpa/                # Shared JPA helpers
  ├── ftgo-domain/                    # Shared domain objects
  ├── common-swagger/                 # Swagger configuration
  ├── ftgo-test-util/                 # Test utilities
  ├── ftgo-order-service/             # Order bounded context
  ├── ftgo-order-service-api/         # Order API definitions
  ├── ftgo-consumer-service/          # Consumer bounded context
  ├── ftgo-consumer-service-api/      # Consumer API definitions
  ├── ftgo-restaurant-service/        # Restaurant bounded context
  ├── ftgo-restaurant-service-api/    # Restaurant API definitions
  ├── ftgo-courier-service/           # Courier bounded context
  ├── ftgo-courier-service-api/       # Courier API definitions
  ├── ftgo-application/               # Monolith composition
  ├── ftgo-flyway/                    # Database migrations
  ├── ftgo-end-to-end-tests/          # E2E tests
  └── ftgo-end-to-end-tests-common/   # E2E test utilities
```

Package root: `net.chrisrichardson.ftgo`

## Decision

### 1. Mono-Repo Strategy

We adopt a **mono-repo** approach where all microservices, shared libraries, infrastructure, and documentation live in a single repository. This provides:

- Atomic cross-service changes during migration
- Unified CI/CD pipeline
- Simplified dependency management
- Easier code review across service boundaries

### 2. Directory Structure

```
ftgo-monolith/                          # Repository root
  │
  ├── [existing monolith modules]       # Unchanged during migration
  │   ├── ftgo-common/
  │   ├── ftgo-common-jpa/
  │   ├── ftgo-domain/
  │   ├── common-swagger/
  │   ├── ftgo-test-util/
  │   ├── ftgo-order-service/
  │   ├── ftgo-order-service-api/
  │   ├── ftgo-consumer-service/
  │   ├── ftgo-consumer-service-api/
  │   ├── ftgo-restaurant-service/
  │   ├── ftgo-restaurant-service-api/
  │   ├── ftgo-courier-service/
  │   ├── ftgo-courier-service-api/
  │   ├── ftgo-application/
  │   ├── ftgo-flyway/
  │   ├── ftgo-end-to-end-tests/
  │   └── ftgo-end-to-end-tests-common/
  │
  ├── services/                         # Microservices (new)
  │   ├── ftgo-order-service/
  │   │   ├── src/
  │   │   │   ├── main/java/com/ftgo/order/
  │   │   │   ├── main/resources/
  │   │   │   ├── test/java/com/ftgo/order/
  │   │   │   └── test/resources/
  │   │   ├── config/                   # Spring config files
  │   │   ├── docker/                   # Dockerfile
  │   │   ├── k8s/                      # Service-specific K8s manifests
  │   │   ├── tests/                    # Integration/contract tests
  │   │   ├── build.gradle
  │   │   └── README.md
  │   ├── ftgo-consumer-service/
  │   ├── ftgo-restaurant-service/
  │   └── ftgo-courier-service/
  │
  ├── shared/                           # Shared libraries (new)
  │   ├── ftgo-common/
  │   ├── ftgo-common-jpa/
  │   └── ftgo-domain/
  │
  ├── docs/                             # Documentation (new)
  │   ├── adr/                          # Architecture Decision Records
  │   └── architecture/                 # Architecture diagrams
  │
  ├── k8s/                              # Platform-level K8s config (new)
  │   ├── base/
  │   └── overlays/
  │       ├── dev/
  │       ├── staging/
  │       └── prod/
  │
  ├── deploy/                           # Deployment scripts (new)
  │   ├── scripts/
  │   └── terraform/
  │
  ├── settings.gradle                   # Updated with new modules
  ├── build.gradle                      # Root build configuration
  └── README.md                         # Updated with structure overview
```

### 3. Package Naming Convention

**New convention**: `com.ftgo.<service>.<layer>`

| Layer | Package | Purpose |
|-------|---------|---------|
| Root | `com.ftgo.<service>` | Service entry point |
| API | `com.ftgo.<service>.api` | REST controllers, DTOs, request/response objects |
| Domain | `com.ftgo.<service>.domain` | Aggregates, entities, value objects, domain events |
| Service | `com.ftgo.<service>.service` | Application services, business logic orchestration |
| Repository | `com.ftgo.<service>.repository` | Data access, JPA repositories |
| Config | `com.ftgo.<service>.config` | Spring configuration classes |
| Messaging | `com.ftgo.<service>.messaging` | Event producers, consumers, message handlers |

**Service name mapping**:

| Bounded Context | Service Name | Package Root | Legacy Package |
|-----------------|-------------|--------------|----------------|
| Order | `ftgo-order-service` | `com.ftgo.order` | `net.chrisrichardson.ftgo.orderservice` |
| Consumer | `ftgo-consumer-service` | `com.ftgo.consumer` | `net.chrisrichardson.ftgo.consumerservice` |
| Restaurant | `ftgo-restaurant-service` | `com.ftgo.restaurant` | `net.chrisrichardson.ftgo.restaurantservice` |
| Courier | `ftgo-courier-service` | `com.ftgo.courier` | `net.chrisrichardson.ftgo.courierservice` |

**Shared library packages**:

| Library | Package |
|---------|---------|
| `ftgo-common` | `com.ftgo.common` |
| `ftgo-common-jpa` | `com.ftgo.common.jpa` |
| `ftgo-domain` | `com.ftgo.domain` |

### 4. Gradle Module Naming Convention

New microservice modules use a **prefixed naming convention** to distinguish them from the existing monolith modules:

| Directory Path | Gradle Module Name | Rationale |
|----------------|-------------------|-----------|
| `services/ftgo-order-service` | `:services-ftgo-order-service` | Prefix with `services-` to avoid conflict with monolith's `:ftgo-order-service` |
| `shared/ftgo-common` | `:shared-ftgo-common` | Prefix with `shared-` to avoid conflict with monolith's `:ftgo-common` |

This is configured in `settings.gradle` using `projectDir`:

```groovy
include 'services-ftgo-order-service'
project(':services-ftgo-order-service').projectDir = file('services/ftgo-order-service')
```

### 5. Service Directory Convention

Each microservice follows a standard internal structure:

```
services/ftgo-<name>-service/
  ├── src/
  │   ├── main/
  │   │   ├── java/com/ftgo/<name>/     # Java source (layered packages)
  │   │   └── resources/                 # application.properties, etc.
  │   └── test/
  │       ├── java/com/ftgo/<name>/     # Unit tests
  │       └── resources/                 # Test configuration
  ├── config/                            # Environment configuration (application.yml)
  ├── docker/                            # Dockerfile for containerization
  ├── k8s/                               # Service-specific K8s manifests
  ├── tests/                             # Integration and contract tests
  ├── build.gradle                       # Module build configuration
  └── README.md                          # Service documentation
```

This structure serves as a **template/archetype** that new services can clone.

## Consequences

### Positive
- Clear separation between monolith and microservices code
- Both can coexist and build together during migration
- Standard structure makes it easy to create new services
- Package naming is concise and follows Java conventions
- Gradle module naming avoids conflicts with existing modules

### Negative
- Some duplication between monolith shared modules and new shared modules during transition
- Developers need to be aware of both naming conventions during migration
- Build times may increase slightly with additional modules

### Risks
- Need to ensure CI/CD handles both monolith and microservice builds
- Shared library versioning needs careful coordination during migration

## References
- [Microservices Patterns by Chris Richardson](https://microservices.io/book)
- [ADR GitHub Organization](https://adr.github.io/)
- [Mono-repo vs Multi-repo](https://microservices.io/post/refactoring/2024/06/10/thoughts-on-monorepo-vs-multi-repo.html)
