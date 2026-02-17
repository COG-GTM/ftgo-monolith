# FTGO Microservices Repository Structure

## Overview

This document describes the repository structure for the FTGO microservices platform. The project uses a **structured mono-repo** where each bounded context has its own service directory with a standardized layout. See [ADR-0001](adr/0001-microservices-repository-structure-and-naming-conventions.md) for the full rationale.

## Repository Layout

```
ftgo-monolith/
|
|-- docs/                                   # Project-wide documentation
|   |-- adr/                                # Architecture Decision Records
|   |   |-- 0001-microservices-repository-  # Repo structure & naming ADR
|   |   |       structure-and-naming-
|   |   |       conventions.md
|   |-- microservices-structure.md          # This file
|   |-- package-naming-conventions.md       # Package naming reference
|
|-- libs/                                   # Shared libraries
|   |-- ftgo-common/                        # Money, Address, PersonName, etc.
|   |-- ftgo-common-jpa/                    # Shared JPA base config
|   |-- ftgo-domain/                        # Shared domain (Order, Consumer, etc.)
|   |-- ftgo-test-util/                     # Test helpers and mothers
|   |-- common-swagger/                     # Swagger/OpenAPI shared config
|
|-- services/                               # Microservices (bounded contexts)
|   |-- _template/                          # Archetype for new services
|   |-- ftgo-consumer-service/              # Consumer context
|   |-- ftgo-order-service/                 # Order context
|   |-- ftgo-restaurant-service/            # Restaurant context
|   |-- ftgo-courier-service/               # Courier context
|
|-- apis/                                   # Service API contracts (DTOs, events)
|   |-- ftgo-consumer-service-api/
|   |-- ftgo-order-service-api/
|   |-- ftgo-restaurant-service-api/
|   |-- ftgo-courier-service-api/
|
|-- infrastructure/                         # Shared infra config
|   |-- docker/                             # Root docker-compose for full stack
|   |-- k8s/                                # Cluster-level K8s manifests
|   |-- flyway/                             # DB migrations (from ftgo-flyway)
|   |-- scripts/                            # Build/deploy utility scripts
|
|-- ftgo-application/                       # Monolith app (kept during migration)
|-- buildSrc/                               # Gradle build plugins
|-- build.gradle                            # Root build config
|-- settings.gradle                         # Module declarations
|-- gradle.properties                       # Shared properties
```

## Bounded Contexts

| Bounded Context | Service Directory                   | API Module                         | Description                        |
|----------------|-------------------------------------|------------------------------------|------------------------------------|
| Consumer       | `services/ftgo-consumer-service/`   | `apis/ftgo-consumer-service-api/`  | Consumer registration, validation  |
| Order          | `services/ftgo-order-service/`      | `apis/ftgo-order-service-api/`     | Order creation, lifecycle, tickets |
| Restaurant     | `services/ftgo-restaurant-service/` | `apis/ftgo-restaurant-service-api/`| Restaurant management, menus       |
| Courier        | `services/ftgo-courier-service/`    | `apis/ftgo-courier-service-api/`   | Courier management, availability   |

## Service Internal Structure

Every service follows a standardized layout:

```
services/ftgo-<name>-service/
|-- src/
|   |-- main/
|   |   |-- java/com/ftgo/<name>service/
|   |   |   |-- domain/              # Entities, aggregates, value objects,
|   |   |   |                        # repository interfaces, domain services
|   |   |   |-- application/         # Use cases, application services
|   |   |   |-- web/                 # REST controllers, request/response DTOs
|   |   |   |-- config/              # Spring configuration classes
|   |   |   |-- events/              # Domain event classes
|   |   |   |-- messaging/           # Async message handlers
|   |   |   |-- <Name>ServiceApplication.java
|   |   |-- resources/
|   |       |-- application.yml      # Default Spring config
|   |-- test/
|       |-- java/com/ftgo/<name>service/
|       |   |-- domain/              # Domain unit tests
|       |   |-- web/                 # Controller tests
|       |-- resources/
|           |-- application-test.yml
|-- docker/
|   |-- Dockerfile                   # Multi-stage build
|   |-- docker-compose.yml           # Service + local dependencies
|-- k8s/
|   |-- deployment.yaml
|   |-- service.yaml
|   |-- configmap.yaml
|-- config/
|   |-- application.yml              # Externalized config
|   |-- application-local.yml        # Local dev overrides
|   |-- application-prod.yml         # Production overrides
|-- build.gradle
|-- README.md
```

## Gradle Project Paths

After the restructure, Gradle project paths reflect the directory hierarchy:

| Module                         | Gradle Path                              |
|-------------------------------|------------------------------------------|
| Consumer Service              | `:services:ftgo-consumer-service`        |
| Order Service                 | `:services:ftgo-order-service`           |
| Restaurant Service            | `:services:ftgo-restaurant-service`      |
| Courier Service               | `:services:ftgo-courier-service`         |
| Consumer Service API          | `:apis:ftgo-consumer-service-api`        |
| Order Service API             | `:apis:ftgo-order-service-api`           |
| Restaurant Service API        | `:apis:ftgo-restaurant-service-api`      |
| Courier Service API           | `:apis:ftgo-courier-service-api`         |
| Common Library                | `:libs:ftgo-common`                      |
| Common JPA                    | `:libs:ftgo-common-jpa`                  |
| Domain Library                | `:libs:ftgo-domain`                      |
| Test Utilities                | `:libs:ftgo-test-util`                   |
| Swagger Config                | `:libs:common-swagger`                   |
| Flyway Migrations             | `:infrastructure:flyway`                 |

## Migration Approach

The repository restructure follows the **Strangler Fig** pattern:

1. **Phase 1 (Current):** Create the target directory structure alongside the existing monolith
2. **Phase 2:** Move existing service code into `services/` directories, shared code into `libs/`
3. **Phase 3:** Add independent Spring Boot main classes to each service
4. **Phase 4:** Add Docker and K8s configuration per service
5. **Phase 5:** Retire `ftgo-application` once all services run independently

During migration, both the old flat structure and new nested structure may coexist. The `settings.gradle` file will be updated incrementally to reference new paths.

## Creating a New Service

1. Copy `services/_template/` to `services/ftgo-<name>-service/`
2. Rename packages from `com.ftgo.template` to `com.ftgo.<name>service`
3. Update `build.gradle` with service-specific dependencies
4. Add the service to `settings.gradle`
5. Add API module under `apis/ftgo-<name>-service-api/` if inter-service communication is needed
6. Update `infrastructure/docker/docker-compose.yml` with the new service
