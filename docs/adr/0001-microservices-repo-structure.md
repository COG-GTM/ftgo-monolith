# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status

Proposed

## Date

2026-02-20

## Context

The FTGO application is currently a monolith with 14 Gradle modules in a flat structure under a single `settings.gradle`. All modules share the package root `net.chrisrichardson.ftgo` and are tightly coupled through direct project dependencies and the `FtgoApplicationMain.java` composite application class.

As part of the microservices migration, we need to establish a clear repository structure and naming conventions that:

- Support independent service development and deployment
- Maintain shared library reuse across services
- Preserve the existing monolith during the incremental migration
- Provide a consistent, discoverable project layout for the team

### Current Monolith Structure

```
ftgo-monolith/
  +-- ftgo-common/                    # Shared utilities
  +-- ftgo-common-jpa/                # JPA base classes
  +-- ftgo-domain/                    # Domain primitives (Money, Address)
  +-- common-swagger/                 # Swagger configuration
  +-- ftgo-test-util/                 # Test helpers
  +-- ftgo-order-service/             # Order bounded context
  +-- ftgo-order-service-api/         # Order API/events
  +-- ftgo-consumer-service/          # Consumer bounded context
  +-- ftgo-consumer-service-api/      # Consumer API/events
  +-- ftgo-restaurant-service/        # Restaurant bounded context
  +-- ftgo-restaurant-service-api/    # Restaurant API/events
  +-- ftgo-courier-service/           # Courier bounded context
  +-- ftgo-courier-service-api/       # Courier API/events
  +-- ftgo-application/               # Monolith composite app
  +-- ftgo-flyway/                    # Database migrations
  +-- ftgo-end-to-end-tests/          # E2E tests
  +-- ftgo-end-to-end-tests-common/   # E2E test utilities
  +-- settings.gradle                 # Flat module includes
```

Package root: `net.chrisrichardson.ftgo`

## Decision

### 1. Repository Strategy: Mono-repo with Service Folders

We will use a **structured mono-repo** approach rather than multi-repo. The existing monolith modules remain at the root level untouched, while new microservice modules are organized under clearly separated top-level directories.

**Rationale:**
- Enables atomic cross-service refactoring during migration
- Simplifies dependency management for shared libraries
- Single CI/CD pipeline can build affected services only
- Easier code review across service boundaries
- Reduces tooling overhead (no repo-per-service management)
- Allows incremental migration — old and new coexist

### 2. Directory Structure

```
ftgo-monolith/                          # Repository root
  |
  |-- [existing monolith modules]       # Unchanged during migration
  |   +-- ftgo-order-service/
  |   +-- ftgo-consumer-service/
  |   +-- ftgo-restaurant-service/
  |   +-- ftgo-courier-service/
  |   +-- ftgo-common/
  |   +-- ftgo-common-jpa/
  |   +-- ftgo-domain/
  |   +-- common-swagger/
  |   +-- ftgo-test-util/
  |   +-- ftgo-application/
  |   +-- ftgo-flyway/
  |   +-- ftgo-end-to-end-tests/
  |   +-- ftgo-end-to-end-tests-common/
  |
  +-- services/                         # Microservice implementations
  |   +-- order-service/
  |   |   +-- src/main/java/...         # Standard Gradle/Maven layout
  |   |   +-- src/main/resources/
  |   |   +-- src/test/java/...
  |   |   +-- src/test/resources/
  |   |   +-- docker/                   # Dockerfile and compose overrides
  |   |   +-- k8s/                      # Kubernetes manifests
  |   |   +-- build.gradle
  |   |   +-- README.md
  |   +-- consumer-service/
  |   +-- restaurant-service/
  |   +-- courier-service/
  |   +-- _template-service/            # Archetype for new services
  |
  +-- libs/                             # Shared libraries (migrated)
  |   +-- ftgo-common/
  |   +-- ftgo-common-jpa/
  |   +-- ftgo-domain/
  |   +-- ftgo-common-swagger/
  |   +-- ftgo-test-util/
  |
  +-- infrastructure/                   # Deployment and environment config
  |   +-- docker/                       # Docker Compose files
  |   +-- kubernetes/
  |   |   +-- base/                     # Base K8s manifests
  |   |   +-- overlays/
  |   |       +-- dev/
  |   |       +-- staging/
  |   |       +-- prod/
  |   +-- config/                       # Environment-specific configs
  |   |   +-- dev/
  |   |   +-- staging/
  |   |   +-- prod/
  |   +-- scripts/                      # Build/deploy helper scripts
  |
  +-- docs/                             # Project documentation
  |   +-- adr/                          # Architecture Decision Records
  |
  +-- settings.gradle                   # Includes both old and new modules
  +-- build.gradle                      # Root build configuration
```

### 3. Package Naming Convention

**New package root:** `com.ftgo`

The migration transitions from the legacy `net.chrisrichardson.ftgo` namespace to the shorter `com.ftgo` namespace. This signals the architectural shift and avoids confusion between monolith and microservice code.

| Scope | Package Pattern | Example |
|-------|----------------|---------|
| Service domain layer | `com.ftgo.<service>.domain` | `com.ftgo.order.domain` |
| Service application layer | `com.ftgo.<service>.service` | `com.ftgo.order.service` |
| Service web/API layer | `com.ftgo.<service>.web` | `com.ftgo.order.web` |
| Service configuration | `com.ftgo.<service>.config` | `com.ftgo.order.config` |
| Service messaging | `com.ftgo.<service>.messaging` | `com.ftgo.order.messaging` |
| Shared common library | `com.ftgo.common` | `com.ftgo.common` |
| Shared JPA library | `com.ftgo.common.jpa` | `com.ftgo.common.jpa` |
| Shared domain primitives | `com.ftgo.domain` | `com.ftgo.domain` |
| Shared test utilities | `com.ftgo.testutil` | `com.ftgo.testutil` |

**Rules:**
- Service name in the package uses the singular bounded context noun (e.g., `order`, not `orders` or `orderservice`)
- Layer packages are flat — no deep nesting beyond `com.ftgo.<service>.<layer>`
- Sub-packages within layers are allowed when justified (e.g., `com.ftgo.order.domain.event`)

### 4. Module Naming Convention for Gradle

| Module Type | Pattern | Gradle Path | Example |
|-------------|---------|-------------|---------|
| Microservice | `<context>-service` | `:services:<name>` | `:services:order-service` |
| Shared library | `ftgo-<name>` | `:libs:<name>` | `:libs:ftgo-common` |
| Legacy module | `ftgo-<name>` | `:<name>` (root) | `:ftgo-order-service` |

**Rules:**
- Service modules are prefixed with the bounded context and suffixed with `-service`
- Shared libraries retain the `ftgo-` prefix for consistency and discoverability
- Legacy monolith modules remain at the root Gradle path — no path changes
- New modules use hierarchical paths (`:services:*`, `:libs:*`)

### 5. Service Port Assignments

| Service | Port |
|---------|------|
| order-service | 8081 |
| consumer-service | 8082 |
| restaurant-service | 8083 |
| courier-service | 8084 |

Future services should use ports in the 808x range, incrementing from 8085.

### 6. Template Service

A `_template-service` directory is provided under `services/` as an archetype. It contains placeholder files with `__PLACEHOLDER__` tokens that teams replace when scaffolding a new service. See `services/_template-service/README.md` for instructions.

## Consequences

### Positive

- **Coexistence**: The monolith continues to build and run unmodified during migration
- **Discoverability**: Clear separation between services, libraries, and infrastructure
- **Consistency**: Naming conventions eliminate bikeshedding for new services
- **Autonomy**: Each service under `services/` can be built, tested, and deployed independently
- **Scalability**: The structure supports adding new bounded contexts without restructuring

### Negative

- **Dual structure**: During migration, both old (`ftgo-order-service/`) and new (`services/order-service/`) directories exist, which may cause confusion
- **Build complexity**: `settings.gradle` grows to include both legacy and new module paths
- **Package migration**: Moving from `net.chrisrichardson.ftgo` to `com.ftgo` requires careful coordination

### Mitigations

- Clear naming (`services/` vs root-level `ftgo-*`) makes the distinction obvious
- Legacy modules will be deprecated and removed as migration completes
- Package migration will happen service-by-service, tracked via migration tasks

## Related Decisions

- ADR-0002 (future): Inter-service communication patterns (REST vs messaging)
- ADR-0003 (future): Database-per-service migration strategy
- ADR-0004 (future): CI/CD pipeline design for mono-repo
