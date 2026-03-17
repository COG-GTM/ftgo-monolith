# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status

Proposed

## Date

2026-03-17

## Context

The FTGO application is currently a monolithic Java application built with Gradle, consisting of 14 modules in a flat directory structure. As part of the microservices migration (Phase 1), we need to define how the repository will be organized to support independent service development while maintaining the benefits of a unified codebase during the transition period.

### Current Monolith Structure

The existing `settings.gradle` includes these modules in a flat layout:

- **Shared**: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util`
- **Services**: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`
- **APIs**: `ftgo-order-service-api`, `ftgo-consumer-service-api`, `ftgo-restaurant-service-api`, `ftgo-courier-service-api`
- **App**: `ftgo-application` (monolith assembly)
- **DB**: `ftgo-flyway`
- **Tests**: `ftgo-end-to-end-tests`, `ftgo-end-to-end-tests-common`

Package root: `net.chrisrichardson.ftgo`

### Decision Drivers

- Need clear service boundaries during incremental migration
- Must preserve existing monolith build while adding microservice structure
- Must support independent service builds and deployments
- Should minimize disruption to existing developer workflows
- Should be easy for new services to be scaffolded quickly

## Decision

### 1. Repository Strategy: Structured Mono-Repo

We will use a **structured mono-repo** with explicit top-level directories to separate concerns. This is preferred over multi-repo during the migration phase because:

- Atomic cross-cutting changes (shared library updates) remain simple
- Unified CI/CD pipeline management during the transition
- Easier dependency management and version alignment
- Developers can see the full system in one place
- Gradual extraction to independent repos is possible later

### 2. Top-Level Directory Layout

```
ftgo-monolith/                          # Repository root
├── services/                           # All microservices
│   ├── consumer-service/               # Consumer bounded context
│   │   ├── build.gradle
│   │   ├── src/
│   │   │   ├── main/java/              # com.ftgo.consumerservice.*
│   │   │   ├── main/resources/
│   │   │   ├── test/java/
│   │   │   └── test/resources/
│   │   ├── config/                     # Spring Boot application configs
│   │   │   └── application.yml
│   │   ├── docker/                     # Container definitions
│   │   │   └── Dockerfile
│   │   └── k8s/                        # Kubernetes manifests
│   │       └── deployment.yml
│   ├── consumer-service-api/           # Consumer public API (DTOs, interfaces)
│   │   ├── build.gradle
│   │   └── src/main/java/
│   ├── restaurant-service/             # Restaurant bounded context
│   ├── restaurant-service-api/
│   ├── order-service/                  # Order bounded context
│   ├── order-service-api/
│   ├── courier-service/                # Courier bounded context
│   └── courier-service-api/
│
├── shared-libraries/                   # Shared/common modules
│   ├── ftgo-common/                    # Value objects, utilities
│   ├── ftgo-common-jpa/                # JPA base classes
│   ├── ftgo-domain/                    # Core domain entities
│   ├── common-swagger/                 # API documentation config
│   └── ftgo-test-util/                 # Test utilities
│
├── template/                           # Service archetype/template
│   └── service-template/               # Copy to create a new service
│
├── docs/                               # Project documentation
│   └── adr/                            # Architecture Decision Records
│
├── ftgo-application/                   # [Legacy] Monolith assembly
├── ftgo-flyway/                        # [Legacy] Database migrations
├── ftgo-end-to-end-tests/              # [Legacy] E2E tests
├── ftgo-end-to-end-tests-common/       # [Legacy] E2E test utilities
│
├── settings.gradle                     # Gradle multi-project config
├── build.gradle                        # Root build configuration
└── README.md                           # Project documentation
```

### 3. Package Naming Convention

**New convention**: `com.ftgo.<servicename>.<layer>`

| Layer | Package | Purpose |
|-------|---------|---------|
| Domain | `com.ftgo.<servicename>.domain` | Entities, repositories, domain services, value objects |
| Web | `com.ftgo.<servicename>.web` | REST controllers, request/response DTOs |
| Main | `com.ftgo.<servicename>.main` | Spring Boot configuration, `@SpringBootApplication` |
| Messaging | `com.ftgo.<servicename>.messaging` | Event publishers and consumers |
| API (public) | `com.ftgo.<servicename>.api` | Public DTOs and interfaces (in `-api` modules) |
| API Events | `com.ftgo.<servicename>.api.events` | Domain event DTOs |
| API Web | `com.ftgo.<servicename>.api.web` | Web request/response DTOs |

**Migration mapping** from legacy to new packages:

| Legacy Package | New Package |
|----------------|-------------|
| `net.chrisrichardson.ftgo.orderservice.domain` | `com.ftgo.orderservice.domain` |
| `net.chrisrichardson.ftgo.orderservice.web` | `com.ftgo.orderservice.web` |
| `net.chrisrichardson.ftgo.consumerservice.domain` | `com.ftgo.consumerservice.domain` |
| `net.chrisrichardson.ftgo.common` | `com.ftgo.common` |
| `net.chrisrichardson.ftgo.domain` | `com.ftgo.domain` |

**Shared library packages**:

| Module | Package |
|--------|---------|
| `ftgo-common` | `com.ftgo.common` |
| `ftgo-common-jpa` | `com.ftgo.common.jpa` |
| `ftgo-domain` | `com.ftgo.domain` |
| `common-swagger` | `com.ftgo.common.swagger` |
| `ftgo-test-util` | `com.ftgo.testutil` |

### 4. Gradle Module Naming Convention

Modules use Gradle's hierarchical project path notation:

| Type | Gradle Path | Directory |
|------|-------------|-----------|
| Service | `:services:order-service` | `services/order-service/` |
| Service API | `:services:order-service-api` | `services/order-service-api/` |
| Shared library | `:shared-libraries:ftgo-common` | `shared-libraries/ftgo-common/` |
| Legacy module | `:ftgo-application` | `ftgo-application/` (flat, unchanged) |

### 5. Service Directory Structure Standard

Every microservice directory MUST contain:

```
<service-name>/
├── build.gradle           # Dependencies and build config
├── config/
│   └── application.yml    # Spring Boot configuration
├── docker/
│   └── Dockerfile         # Container image definition
├── k8s/
│   └── deployment.yml     # Kubernetes deployment + service
├── src/
│   ├── main/
│   │   ├── java/          # Production source code
│   │   └── resources/     # Application resources (Spring configs, etc.)
│   └── test/
│       ├── java/          # Unit and integration tests
│       └── resources/     # Test-specific resources
```

### 6. Naming Rules Summary

| Artifact | Convention | Example |
|----------|-----------|---------|
| Service directory | `<context>-service` | `order-service` |
| API module | `<context>-service-api` | `order-service-api` |
| Java package | `com.ftgo.<contextservice>.<layer>` | `com.ftgo.orderservice.domain` |
| Docker image | `ftgo/<context>-service` | `ftgo/order-service` |
| K8s deployment | `<context>-service` | `order-service` |
| Gradle path | `:services:<context>-service` | `:services:order-service` |
| Shared lib path | `:shared-libraries:<lib-name>` | `:shared-libraries:ftgo-common` |

## Consequences

### Positive

- **Clear boundaries**: Services and shared libraries are visually and structurally separated
- **Incremental migration**: Legacy modules remain at root level; new structure exists alongside
- **Consistent conventions**: All services follow the same directory layout and naming patterns
- **Template-driven**: New services can be created quickly by copying the template
- **Future-proof**: Mono-repo can be split into multi-repo per service when needed
- **Independent deployability**: Each service has its own Dockerfile and K8s manifests

### Negative

- **Dual structure during migration**: Both legacy flat modules and new hierarchical modules coexist temporarily
- **Settings.gradle complexity**: Must maintain both old and new module paths
- **Potential confusion**: Developers must understand which modules are legacy vs. new

### Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| Developers add code to wrong location | CI linting rules to enforce package conventions |
| Shared library changes break services | Versioned shared libraries with compatibility tests |
| Mono-repo grows too large | Clear extraction plan for multi-repo if needed |
| Legacy modules become stale | Migration tracker to ensure all modules are migrated |

## References

- [Microservices Patterns by Chris Richardson](https://microservices.io/book)
- [Mono-Repo vs Multi-Repo](https://microservices.io/post/refactoring/2024/06/03/how-dark-energy-and-dark-matter-illuminate-microservice-design.html)
- [ADR specification](https://adr.github.io/)
