# ADR-001: Microservices Repository Structure and Naming Conventions

## Status

Accepted

## Date

2026-03-16

## Context

The FTGO application is currently a modular monolith built with a multi-module Gradle layout. The monolith's `settings.gradle` defines 14 modules in a flat structure:

- **Shared**: `ftgo-common`, `ftgo-common-jpa`, `ftgo-domain`, `common-swagger`, `ftgo-test-util`
- **Services**: `ftgo-order-service`, `ftgo-consumer-service`, `ftgo-restaurant-service`, `ftgo-courier-service`
- **APIs**: `ftgo-order-service-api`, `ftgo-consumer-service-api`, `ftgo-restaurant-service-api`, `ftgo-courier-service-api`
- **App**: `ftgo-application`
- **DB**: `ftgo-flyway`
- **Tests**: `ftgo-end-to-end-tests`, `ftgo-end-to-end-tests-common`

The current package root is `net.chrisrichardson.ftgo`.

As we migrate to microservices, we need a well-defined repository structure with clear boundaries between services, shared libraries, and deployment artifacts.

### Decision Drivers

- Team needs clear ownership boundaries for each service
- Must support independent build and deployment of each service
- Must coexist with the existing monolith during the migration period
- Must support shared libraries without creating tight coupling
- Must be easy for developers to understand and navigate

## Decision

### 1. Mono-Repo Strategy

We will use a **mono-repo** with clearly separated top-level directories rather than multi-repo.

**Rationale:**
- Simplifies dependency management during migration (shared code is co-located)
- Atomic commits across service boundaries during the transition
- Easier CI/CD pipeline management with a single repository
- Reduces tooling overhead (no need for cross-repo dependency management)
- Better visibility into the full system for all developers

### 2. Top-Level Directory Structure

```
ftgo-monolith/
├── services/                          # Microservice modules
│   ├── order-service/
│   ├── consumer-service/
│   ├── restaurant-service/
│   ├── courier-service/
│   └── service-template/              # Archetype for new services
├── shared-libraries/                  # Shared libraries
│   ├── ftgo-common/
│   ├── ftgo-common-jpa/
│   ├── ftgo-domain/
│   └── ftgo-swagger/
├── docs/                              # Documentation
│   └── adr/                           # Architecture Decision Records
├── ftgo-application/                  # Existing monolith (kept during migration)
├── ftgo-order-service/                # Existing monolith modules (kept)
├── ftgo-consumer-service/             # Existing monolith modules (kept)
├── ftgo-restaurant-service/           # Existing monolith modules (kept)
├── ftgo-courier-service/              # Existing monolith modules (kept)
├── ftgo-*-service-api/                # Existing API modules (kept)
├── ftgo-common/                       # Existing shared modules (kept)
├── ftgo-common-jpa/                   # Existing shared modules (kept)
├── ftgo-domain/                       # Existing shared modules (kept)
└── ...
```

### 3. Service Directory Layout

Each microservice follows a standard internal structure:

```
services/<service-name>/
├── build.gradle                       # Service-specific build config
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/<service>/   # Java source root
│   │   │   ├── domain/                # Domain entities, services, repos
│   │   │   ├── web/                   # REST controllers, DTOs
│   │   │   ├── config/                # Spring configuration
│   │   │   └── repository/            # Data access layer
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/ftgo/<service>/
│       │   ├── domain/
│       │   └── web/
│       └── resources/
├── docker/
│   └── Dockerfile
└── k8s/
    └── deployment.yaml
```

### 4. Package Naming Convention

**New package root:** `com.ftgo.<service>.<layer>`

| Monolith Package | Microservice Package |
|---|---|
| `net.chrisrichardson.ftgo.orderservice.domain` | `com.ftgo.order.domain` |
| `net.chrisrichardson.ftgo.orderservice.web` | `com.ftgo.order.web` |
| `net.chrisrichardson.ftgo.consumerservice.domain` | `com.ftgo.consumer.domain` |
| `net.chrisrichardson.ftgo.restaurantservice.domain` | `com.ftgo.restaurant.domain` |
| `net.chrisrichardson.ftgo.courierservice.domain` | `com.ftgo.courier.domain` |

**Layer packages:**
- `domain` - Domain entities, value objects, domain services, repository interfaces
- `web` - REST controllers, request/response DTOs, exception handlers
- `config` - Spring `@Configuration` classes, bean definitions
- `repository` - Spring Data JPA repository implementations

### 5. Gradle Module Naming

Gradle subprojects use a hierarchical naming convention with the directory structure:

| Directory | Gradle Module Name |
|---|---|
| `services/order-service` | `:services:order-service` |
| `services/consumer-service` | `:services:consumer-service` |
| `services/restaurant-service` | `:services:restaurant-service` |
| `services/courier-service` | `:services:courier-service` |
| `shared-libraries/ftgo-common` | `:shared-libraries:ftgo-common` |
| `shared-libraries/ftgo-common-jpa` | `:shared-libraries:ftgo-common-jpa` |
| `shared-libraries/ftgo-domain` | `:shared-libraries:ftgo-domain` |
| `shared-libraries/ftgo-swagger` | `:shared-libraries:ftgo-swagger` |

### 6. Bounded Context to Service Mapping

| Bounded Context | Service Name | Gradle Module | Package Root |
|---|---|---|---|
| Consumer | `consumer-service` | `:services:consumer-service` | `com.ftgo.consumer` |
| Restaurant | `restaurant-service` | `:services:restaurant-service` | `com.ftgo.restaurant` |
| Order | `order-service` | `:services:order-service` | `com.ftgo.order` |
| Courier | `courier-service` | `:services:courier-service` | `com.ftgo.courier` |

## Consequences

### Positive

- Clear separation between monolith and microservice code
- Developers can easily find and navigate service code
- New services can be created quickly using the template
- Gradle module hierarchy provides logical grouping
- Package naming convention is concise and consistent
- Docker and k8s configs are co-located with service code

### Negative

- Repository size will grow as both monolith and microservice code coexist
- Build times may increase as more modules are added
- Developers need to understand both the old and new structure during migration

### Risks

- Shared libraries could become a source of coupling if not carefully managed
- Package renaming from `net.chrisrichardson.ftgo` to `com.ftgo` requires careful migration

## Notes

- The existing monolith modules remain intact and are NOT modified
- The new structure is added alongside the monolith
- Once migration is complete, the old monolith modules can be removed
- Java 8 compatibility is maintained throughout
