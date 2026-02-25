# ADR-0001: Microservices Repository Structure and Naming Conventions

## Status

**Accepted**

## Date

2026-02-25

## Context

The FTGO application is currently a monolith built as a multi-module Gradle project with 14 modules in a flat structure under a single `settings.gradle`. The current structure uses the package root `net.chrisrichardson.ftgo` and organizes code into:

- **Shared modules**: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util`
- **Service modules**: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`
- **API modules**: `ftgo-order-service-api`, `ftgo-consumer-service-api`, `ftgo-restaurant-service-api`, `ftgo-courier-service-api`
- **Application**: `ftgo-application` (composes all services via `@Import`)
- **Database**: `ftgo-flyway`
- **Tests**: `ftgo-end-to-end-tests`, `ftgo-end-to-end-tests-common`

As we migrate to microservices, we need to:
1. Establish clear service boundaries in the repository structure
2. Define consistent naming conventions for packages, modules, and directories
3. Support independent service development, testing, and deployment
4. Maintain a smooth migration path from monolith to microservices

## Decision

### 1. Repository Strategy: Mono-Repo with Service Folders

We will use a **mono-repo** approach with clearly defined service boundaries rather than splitting into multiple repositories.

**Rationale:**
- Simplifies cross-cutting changes during the migration period
- Enables atomic commits that span service boundaries (important during migration)
- Shared libraries can be versioned and tested together
- Single CI/CD pipeline with selective builds per service
- Easier dependency management during the transition
- Team can gradually adopt independent deployment without repository overhead

The mono-repo is organized into four top-level directories:

```
ftgo-monolith/                          # Repository root
├── services/                           # Microservice implementations
│   ├── ftgo-order-service/
│   ├── ftgo-consumer-service/
│   ├── ftgo-restaurant-service/
│   ├── ftgo-courier-service/
│   └── ftgo-service-template/          # Archetype for new services
├── shared/                             # Shared libraries and API contracts
│   ├── ftgo-common/
│   ├── ftgo-common-jpa/
│   ├── ftgo-domain/
│   ├── common-swagger/
│   ├── ftgo-test-util/
│   ├── ftgo-order-service-api/
│   ├── ftgo-consumer-service-api/
│   ├── ftgo-restaurant-service-api/
│   └── ftgo-courier-service-api/
├── infrastructure/                     # Deployment and infrastructure config
│   ├── docker/
│   ├── k8s/
│   │   ├── base/
│   │   └── overlays/{dev,staging,prod}/
│   ├── scripts/
│   └── ci/
├── docs/                               # Documentation and ADRs
│   └── adr/
├── buildSrc/                           # Gradle build plugins (existing)
├── ftgo-application/                   # Monolith app (kept during migration)
├── ftgo-flyway/                        # Database migrations (kept during migration)
├── ftgo-end-to-end-tests/              # E2E tests (kept during migration)
├── ftgo-end-to-end-tests-common/       # E2E test utilities (kept during migration)
└── [existing monolith modules]         # Preserved until migration complete
```

### 2. Directory Structure for Each Microservice

Each microservice follows a standardized directory layout:

```
services/ftgo-<service>-service/
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/<service>/
│   │   │   ├── api/                    # REST API DTOs, request/response objects
│   │   │   ├── config/                 # Spring @Configuration classes
│   │   │   ├── domain/                 # Domain entities, value objects, domain services
│   │   │   ├── repository/             # JPA repositories
│   │   │   └── web/                    # REST controllers
│   │   └── resources/
│   │       └── application.properties
│   ├── test/
│   │   ├── java/com/ftgo/<service>/
│   │   │   ├── domain/                 # Domain unit tests
│   │   │   ├── web/                    # Controller tests
│   │   │   └── repository/             # Repository tests
│   │   └── resources/
│   └── integration-test/
│       ├── java/com/ftgo/<service>/
│       └── resources/
├── config/                             # External configuration files
│   ├── application.properties          # Default config
│   └── application-test.properties     # Test profile config
├── docker/
│   └── Dockerfile                      # Service-specific Dockerfile
├── k8s/
│   └── deployment.yaml                 # Kubernetes Deployment + Service
├── build.gradle                        # Module build configuration
└── README.md                           # Service documentation (optional)
```

### 3. Package Naming Convention

**New package root:** `com.ftgo.<bounded-context>.<layer>`

This replaces the monolith's `net.chrisrichardson.ftgo.<service>` convention with a cleaner, organization-owned namespace.

| Bounded Context | Service Package Root     | Layers                                          |
|----------------|--------------------------|--------------------------------------------------|
| Order          | `com.ftgo.order`         | `.domain`, `.web`, `.repository`, `.config`, `.api` |
| Consumer       | `com.ftgo.consumer`      | `.domain`, `.web`, `.repository`, `.config`, `.api` |
| Restaurant     | `com.ftgo.restaurant`    | `.domain`, `.web`, `.repository`, `.config`, `.api` |
| Courier        | `com.ftgo.courier`       | `.domain`, `.web`, `.repository`, `.config`, `.api` |

**Shared library packages:**

| Library                  | Package                      |
|--------------------------|------------------------------|
| ftgo-common              | `com.ftgo.common`            |
| ftgo-common-jpa          | `com.ftgo.common.jpa`        |
| ftgo-domain              | `com.ftgo.domain`            |
| common-swagger           | `com.ftgo.common.swagger`    |
| ftgo-test-util           | `com.ftgo.testutil`          |
| ftgo-order-service-api   | `com.ftgo.order.api`         |
| ftgo-consumer-service-api| `com.ftgo.consumer.api`      |
| ftgo-restaurant-service-api | `com.ftgo.restaurant.api` |
| ftgo-courier-service-api | `com.ftgo.courier.api`       |

**Layer responsibilities:**

| Layer        | Purpose                                                    |
|-------------|-------------------------------------------------------------|
| `domain`    | Domain entities, value objects, domain services, exceptions |
| `web`       | REST controllers, exception handlers                        |
| `repository`| JPA repository interfaces                                   |
| `config`    | Spring `@Configuration` classes, bean definitions           |
| `api`       | REST DTOs, request/response objects, API contracts          |

### 4. Module Naming Conventions for Gradle Subprojects

Gradle project paths use the directory hierarchy with `:` separators:

| Module Type    | Gradle Path                                  | Directory                              |
|---------------|----------------------------------------------|----------------------------------------|
| Service       | `:services:ftgo-order-service`               | `services/ftgo-order-service/`         |
| Service       | `:services:ftgo-consumer-service`            | `services/ftgo-consumer-service/`      |
| Service       | `:services:ftgo-restaurant-service`          | `services/ftgo-restaurant-service/`    |
| Service       | `:services:ftgo-courier-service`             | `services/ftgo-courier-service/`       |
| Shared Lib    | `:shared:ftgo-common`                        | `shared/ftgo-common/`                  |
| Shared Lib    | `:shared:ftgo-common-jpa`                    | `shared/ftgo-common-jpa/`             |
| Shared Lib    | `:shared:ftgo-domain`                        | `shared/ftgo-domain/`                  |
| Shared Lib    | `:shared:common-swagger`                     | `shared/common-swagger/`               |
| Shared Lib    | `:shared:ftgo-test-util`                     | `shared/ftgo-test-util/`              |
| API Contract  | `:shared:ftgo-order-service-api`             | `shared/ftgo-order-service-api/`       |
| API Contract  | `:shared:ftgo-consumer-service-api`          | `shared/ftgo-consumer-service-api/`    |
| API Contract  | `:shared:ftgo-restaurant-service-api`        | `shared/ftgo-restaurant-service-api/`  |
| API Contract  | `:shared:ftgo-courier-service-api`           | `shared/ftgo-courier-service-api/`     |

Dependencies between modules use the Gradle path notation:
```groovy
// Service depending on shared library
compile project(":shared:ftgo-domain")

// Service depending on API contract
compile project(":shared:ftgo-order-service-api")
```

### 5. Migration Coexistence Strategy

During the migration period, both the old flat module structure and the new hierarchical structure coexist in `settings.gradle`. This allows:

- Existing monolith modules to continue building and running
- New microservice modules to be developed in parallel
- Gradual migration of code from old modules to new service directories
- The `ftgo-application` module to continue composing all services until migration is complete

Once migration is complete, the old flat modules will be removed.

## Consequences

### Positive
- Clear separation of concerns between services
- Standardized structure makes it easy to create new services (template provided)
- Shared libraries are explicitly separated from service implementations
- Infrastructure configuration is centralized and environment-aware
- Gradle multi-project build supports selective compilation and testing
- Package naming is clean, consistent, and organization-owned
- Migration can proceed incrementally without breaking the existing monolith

### Negative
- Mono-repo requires discipline to maintain service boundaries
- Build times may increase as more services are added (mitigated by Gradle's incremental builds)
- Dual structure during migration adds temporary complexity to `settings.gradle`
- Team needs to learn new directory conventions

### Risks
- Services in a mono-repo may develop hidden coupling if boundaries aren't enforced
- Shared libraries could become a bottleneck if too many cross-cutting concerns accumulate

### Mitigations
- Code reviews should enforce service boundary rules
- Shared libraries should be kept minimal and focused
- Consider moving to multi-repo once migration is complete and team is comfortable with microservices
- Use Gradle's dependency constraints to prevent circular dependencies

## References

- [Microservices Patterns by Chris Richardson](https://microservices.io/book)
- [Mono-repo vs Multi-repo](https://microservices.io/post/architecture/2023/01/12/mono-repo-vs-multi-repo.html)
- [ADR Template by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
