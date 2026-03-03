# ADR-0001: Mono-Repo Structure and Naming Conventions for Microservices Migration

## Status

Accepted

## Date

2026-03-03

## Context

The FTGO application is currently a monolithic Spring Boot application organized as a multi-module Gradle project with 14 modules in a flat structure at the repository root. The current module layout is:

- **Shared**: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util`
- **Services**: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`
- **APIs**: `ftgo-order-service-api`, `ftgo-consumer-service-api`, `ftgo-restaurant-service-api`, `ftgo-courier-service-api`
- **Application**: `ftgo-application` (composes all services via `@Import`)
- **Database**: `ftgo-flyway`
- **Tests**: `ftgo-end-to-end-tests`, `ftgo-end-to-end-tests-common`

The package root is `net.chrisrichardson.ftgo`.

As part of the microservices migration (Epic EM), we need to define a repository structure that:

1. Clearly separates service boundaries aligned with bounded contexts
2. Supports incremental extraction of services from the monolith
3. Maintains a single source of truth during the transition period
4. Enables independent build, test, and deployment of each service
5. Keeps shared libraries accessible to all services

We evaluated two strategies:

### Option A: Multi-Repo (one repository per service)

**Pros**: Strong isolation, independent versioning, clear ownership boundaries.
**Cons**: Cross-cutting changes require coordinating across repositories, shared library versioning becomes complex, higher operational overhead during migration, harder to do atomic refactors.

### Option B: Mono-Repo with Service Folders

**Pros**: Atomic commits across services, shared tooling and CI, easier refactoring during migration, single source of truth, simpler dependency management for shared libraries.
**Cons**: Requires discipline to maintain service boundaries, CI pipeline must be smart about selective builds, repository size grows over time.

## Decision

We adopt **Option B: Mono-Repo with Service Folders**.

The repository will be organized with top-level directories that group modules by their role:

```
ftgo-monolith/                          # Repository root
├── services/                           # Microservice modules
│   ├── ftgo-order-service/             # Order bounded context
│   │   ├── src/main/java/com/ftgo/order/
│   │   │   ├── domain/                 # Aggregates, entities, value objects
│   │   │   ├── service/                # Application/domain services
│   │   │   ├── web/                    # REST controllers
│   │   │   ├── repository/             # Data access layer
│   │   │   └── config/                 # Spring configuration
│   │   ├── src/main/resources/
│   │   ├── src/test/java/com/ftgo/order/
│   │   ├── config/                     # Environment-specific config overrides
│   │   ├── docker/                     # Dockerfile, docker-compose overrides
│   │   ├── k8s/                        # Kubernetes manifests
│   │   ├── tests/                      # Additional test resources
│   │   └── build.gradle
│   ├── ftgo-consumer-service/          # Consumer bounded context
│   ├── ftgo-restaurant-service/        # Restaurant bounded context
│   └── ftgo-courier-service/           # Courier bounded context
├── shared/                             # Shared libraries
│   ├── ftgo-common/                    # Value objects, utilities
│   ├── ftgo-domain/                    # Core domain entities
│   └── ftgo-common-jpa/               # JPA persistence utilities
├── infrastructure/                     # Infrastructure-as-code
│   ├── docker/                         # Shared Docker configurations
│   ├── kubernetes/                     # Cluster-level K8s manifests
│   └── ci/                            # CI/CD pipeline definitions
├── docs/                              # Project documentation
│   └── adr/                           # Architecture Decision Records
├── ftgo-order-service/                 # (Legacy) existing monolith modules
├── ftgo-consumer-service/              #   retained during migration
├── ftgo-restaurant-service/            #   will be removed after extraction
├── ftgo-courier-service/               #   is complete
├── ftgo-application/                   # Monolith application assembly
├── ftgo-flyway/                        # Database migrations
├── settings.gradle                     # Gradle multi-project configuration
└── build.gradle                        # Root build configuration
```

### Package Naming Convention

We transition from the legacy `net.chrisrichardson.ftgo` package root to a new convention:

| Scope | Convention | Example |
|-------|-----------|---------|
| Service base package | `com.ftgo.<service>` | `com.ftgo.order` |
| Domain layer | `com.ftgo.<service>.domain` | `com.ftgo.order.domain` |
| Service layer | `com.ftgo.<service>.service` | `com.ftgo.order.service` |
| Web/API layer | `com.ftgo.<service>.web` | `com.ftgo.order.web` |
| Repository layer | `com.ftgo.<service>.repository` | `com.ftgo.order.repository` |
| Configuration | `com.ftgo.<service>.config` | `com.ftgo.order.config` |
| Shared common | `com.ftgo.common` | `com.ftgo.common.Money` |
| Shared domain | `com.ftgo.domain` | `com.ftgo.domain.Order` |
| Shared JPA | `com.ftgo.common.jpa` | `com.ftgo.common.jpa.BaseEntity` |

### Gradle Module Naming Convention

| Location | Gradle Project Path | Artifact ID |
|----------|-------------------|-------------|
| `services/ftgo-order-service` | `:services:ftgo-order-service` | `ftgo-order-service` |
| `services/ftgo-consumer-service` | `:services:ftgo-consumer-service` | `ftgo-consumer-service` |
| `services/ftgo-restaurant-service` | `:services:ftgo-restaurant-service` | `ftgo-restaurant-service` |
| `services/ftgo-courier-service` | `:services:ftgo-courier-service` | `ftgo-courier-service` |
| `shared/ftgo-common` | `:shared:ftgo-common` | `ftgo-common` |
| `shared/ftgo-domain` | `:shared:ftgo-domain` | `ftgo-domain` |
| `shared/ftgo-common-jpa` | `:shared:ftgo-common-jpa` | `ftgo-common-jpa` |

### Service-to-Bounded-Context Mapping

| Bounded Context | Service Module | API Module (Legacy) | Database Schema |
|----------------|---------------|-------------------|----------------|
| **Order** | `services/ftgo-order-service` | `ftgo-order-service-api` | `ftgo_order_service` |
| **Consumer** | `services/ftgo-consumer-service` | `ftgo-consumer-service-api` | `ftgo_consumer_service` |
| **Restaurant** | `services/ftgo-restaurant-service` | `ftgo-restaurant-service-api` | `ftgo_restaurant_service` |
| **Courier** | `services/ftgo-courier-service` | `ftgo-courier-service-api` | `ftgo_courier_service` |

## Migration Strategy

The migration follows an incremental approach:

1. **Phase 1 (Current)**: Create the new directory structure (`services/`, `shared/`, `infrastructure/`, `docs/`) alongside the existing monolith modules. The existing modules remain the active, buildable code.

2. **Phase 2**: Extract shared libraries into `shared/` by moving source code from the root-level `ftgo-common`, `ftgo-domain`, and `ftgo-common-jpa` modules. Update `settings.gradle` to point to the new locations.

3. **Phase 3**: Extract services one at a time into `services/`. Each extraction involves:
   - Moving source code to the new location
   - Adding service-specific `Dockerfile` and `application.yml`
   - Adding Kubernetes manifests
   - Updating `settings.gradle`
   - Removing the root-level module after verification

4. **Phase 4**: Remove the `ftgo-application` monolith assembly once all services are extracted and running independently.

During the transition, both the legacy root-level modules and the new `services/`/`shared/` directories coexist. The legacy modules remain the active build targets until each service is fully extracted and verified.

## Consequences

### Positive

- **Single repository** keeps all code in one place during migration, simplifying cross-cutting changes
- **Clear directory structure** makes service boundaries visible and enforceable
- **Standard layout** (src, config, docker, k8s, tests) per service ensures consistency
- **New package convention** (`com.ftgo.<service>`) is cleaner and aligns with industry standards
- **Incremental migration** allows the monolith to continue running while services are extracted one at a time
- **Shared libraries** in `shared/` are directly available to all services without publishing to a registry

### Negative

- **Dual structure** during migration (legacy root modules + new `services/` directories) may cause confusion; clear documentation and naming mitigate this
- **Mono-repo scaling** may require build tool optimizations (Gradle build cache, selective builds) as the codebase grows
- **Discipline required** to prevent cross-service dependencies from leaking through shared code
- **Package rename** from `net.chrisrichardson.ftgo` to `com.ftgo` requires updating all import statements during extraction

### Risks

- Teams may accidentally modify legacy modules instead of new service directories; CI checks and code review should enforce correct targets
- Shared library changes could break multiple services; versioning and backward compatibility testing are essential

## References

- [Microservices Patterns](https://microservices.io/patterns/) by Chris Richardson
- [MonoRepo vs MultiRepo](https://www.atlassian.com/git/tutorials/monorepos) - Atlassian
- Migration Execution Log: `migration_execution_log.md`
