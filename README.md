# FTGO Monolith — Microservices Migration

[![CI Build](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-build.yml/badge.svg?branch=feat%2Fmicroservices-migration)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-build.yml)
[![Docker Build](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/docker-build.yml/badge.svg?branch=feat%2Fmicroservices-migration)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/docker-build.yml)
[![Deploy](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/deploy.yml/badge.svg?branch=feat%2Fmicroservices-migration)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/deploy.yml)

This repository contains the FTGO (Food To Go) monolithic application alongside the new microservices structure being built as part of the migration effort.

For the original monolith documentation, see [README.adoc](README.adoc).

---

## Repository Structure

```
ftgo-monolith/
│
│   ┌─────────────────────────────────────────────────────────┐
│   │  NEW — Microservices Platform                           │
│   └─────────────────────────────────────────────────────────┘
│
├── services/                              # Microservice modules
│   ├── order-service/                     # Order bounded context
│   │   ├── build.gradle
│   │   ├── src/
│   │   │   ├── main/java/com/ftgo/order/
│   │   │   │   ├── domain/               # Entities, services, repos
│   │   │   │   ├── web/                   # REST controllers, DTOs
│   │   │   │   ├── config/                # Spring configuration
│   │   │   │   └── repository/            # Data access layer
│   │   │   └── main/resources/
│   │   ├── docker/Dockerfile
│   │   └── k8s/deployment.yaml
│   ├── consumer-service/                  # Consumer bounded context
│   ├── restaurant-service/                # Restaurant bounded context
│   ├── courier-service/                   # Courier bounded context
│   └── service-template/                  # Archetype for new services
│
├── shared-libraries/                      # Shared libraries
│   ├── ftgo-common/                       # Utilities, value objects
│   ├── ftgo-common-jpa/                   # JPA shared utilities
│   ├── ftgo-domain/                       # Core domain entities
│   └── ftgo-swagger/                      # API documentation config
│
├── docs/
│   └── adr/                               # Architecture Decision Records
│       └── 001-microservices-repository-structure.md
│
│   ┌─────────────────────────────────────────────────────────┐
│   │  EXISTING — Monolith (kept intact during migration)     │
│   └─────────────────────────────────────────────────────────┘
│
├── ftgo-application/                      # Main monolith application
├── ftgo-order-service/                    # Monolith order module
├── ftgo-consumer-service/                 # Monolith consumer module
├── ftgo-restaurant-service/               # Monolith restaurant module
├── ftgo-courier-service/                  # Monolith courier module
├── ftgo-*-service-api/                    # Monolith API modules
├── ftgo-common/                           # Monolith shared common
├── ftgo-common-jpa/                       # Monolith shared JPA
├── ftgo-domain/                           # Monolith shared domain
├── common-swagger/                        # Monolith Swagger config
├── ftgo-flyway/                           # Database migrations
├── ftgo-test-util/                        # Test utilities
├── ftgo-end-to-end-tests/                 # End-to-end tests
├── ftgo-end-to-end-tests-common/          # Shared test base
│
├── settings.gradle                        # Gradle multi-module config
├── build.gradle                           # Root build config
├── gradle.properties                      # Build properties
├── docker-compose.yml                     # Local dev infrastructure
└── deployment/kubernetes/                 # Monolith k8s configs
```

---

## Naming Conventions

### Package Naming

All new microservice code uses the package root `com.ftgo.<service>.<layer>`:

| Bounded Context | Package Root           | Example                    |
|-----------------|------------------------|----------------------------|
| Order           | `com.ftgo.order`       | `com.ftgo.order.domain`    |
| Consumer        | `com.ftgo.consumer`    | `com.ftgo.consumer.web`    |
| Restaurant      | `com.ftgo.restaurant`  | `com.ftgo.restaurant.config` |
| Courier         | `com.ftgo.courier`     | `com.ftgo.courier.repository` |

**Layer packages:**

| Layer        | Purpose                                              |
|--------------|------------------------------------------------------|
| `domain`     | Domain entities, value objects, domain services, repository interfaces |
| `web`        | REST controllers, request/response DTOs, exception handlers |
| `config`     | Spring `@Configuration` classes, bean definitions     |
| `repository` | Spring Data JPA repository implementations            |

### Gradle Module Naming

Microservice modules use hierarchical Gradle paths:

| Type            | Pattern                            | Example                          |
|-----------------|------------------------------------|----------------------------------|
| Service         | `:services:<service-name>`         | `:services:order-service`        |
| Shared Library  | `:shared-libraries:<library-name>` | `:shared-libraries:ftgo-common`  |

### Service Directory Naming

- Lowercase, hyphenated: `order-service`, `consumer-service`
- Match the bounded context name: `courier-service` (not `delivery-service`)

---

## Bounded Context to Service Mapping

| Bounded Context | Service Directory             | Monolith Origin              |
|-----------------|-------------------------------|------------------------------|
| Consumer        | `services/consumer-service`   | `ftgo-consumer-service`      |
| Restaurant      | `services/restaurant-service` | `ftgo-restaurant-service`    |
| Order           | `services/order-service`      | `ftgo-order-service`         |
| Courier         | `services/courier-service`    | `ftgo-courier-service`       |

---

## Creating a New Service

Use the `services/service-template/` archetype:

1. Copy `services/service-template/` to `services/<your-service-name>/`
2. Rename packages from `com.ftgo.template` to `com.ftgo.<yourservice>`
3. Update `build.gradle`, `application.properties`, `Dockerfile`, and `deployment.yaml`
4. Add `include "services:<your-service-name>"` to `settings.gradle`

See `services/service-template/TEMPLATE_README.md` for detailed instructions.

---

## Architecture Decision Records

Architecture decisions are documented in `docs/adr/`:

- [ADR-001: Microservices Repository Structure and Naming Conventions](docs/adr/001-microservices-repository-structure.md)

---

## Build & Test

```bash
# Build the monolith (existing)
./gradlew compileJava -x :ftgo-end-to-end-tests-common:compileJava -x :ftgo-end-to-end-tests:compileJava

# Run tests (excluding end-to-end and application integration tests)
./gradlew test -x :ftgo-end-to-end-tests-common:test -x :ftgo-end-to-end-tests:test -x :ftgo-application:test
```

---

## Migration Notes

- **Java 8** compatibility is maintained throughout
- The existing monolith modules remain **intact** — the new structure is added alongside
- Once migration is complete, old monolith modules will be removed
- See [ADR-001](docs/adr/001-microservices-repository-structure.md) for full rationale
