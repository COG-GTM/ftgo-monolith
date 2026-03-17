# FTGO Microservices Platform

This repository contains the FTGO (Food To Go) application, transitioning from a monolithic architecture to microservices. It is based on the [FTGO application](https://github.com/microservices-patterns/ftgo-application) from the book [Microservices Patterns](https://microservices.io/book).

## Repository Structure

```
ftgo-monolith/
│
├── services/                              # Microservices (new structure)
│   ├── consumer-service/                  # Consumer bounded context
│   │   ├── build.gradle
│   │   ├── src/main/java/                 # com.ftgo.consumerservice.*
│   │   ├── src/test/java/
│   │   ├── config/application.yml
│   │   ├── docker/Dockerfile
│   │   └── k8s/deployment.yml
│   ├── consumer-service-api/              # Consumer public API DTOs
│   ├── restaurant-service/                # Restaurant bounded context
│   ├── restaurant-service-api/
│   ├── order-service/                     # Order bounded context
│   ├── order-service-api/
│   ├── courier-service/                   # Courier bounded context
│   └── courier-service-api/
│
├── shared-libraries/                      # Shared/common modules
│   ├── ftgo-common/                       # Value objects, utilities
│   ├── ftgo-common-jpa/                   # JPA base classes
│   ├── ftgo-domain/                       # Core domain entities
│   ├── common-swagger/                    # API documentation config
│   └── ftgo-test-util/                    # Test utilities
│
├── template/                              # Service archetype/template
│   └── service-template/                  # Copy to create a new service
│
├── docs/                                  # Documentation
│   └── adr/                               # Architecture Decision Records
│       └── 0001-microservices-repository-structure.md
│
├── ftgo-application/                      # [Legacy] Monolith assembly
├── ftgo-consumer-service/                 # [Legacy] Consumer service
├── ftgo-consumer-service-api/             # [Legacy] Consumer API
├── ftgo-restaurant-service/               # [Legacy] Restaurant service
├── ftgo-restaurant-service-api/           # [Legacy] Restaurant API
├── ftgo-order-service/                    # [Legacy] Order service
├── ftgo-order-service-api/                # [Legacy] Order API
├── ftgo-courier-service/                  # [Legacy] Courier service
├── ftgo-courier-service-api/              # [Legacy] Courier API
├── ftgo-common/                           # [Legacy] Common utilities
├── ftgo-common-jpa/                       # [Legacy] JPA utilities
├── ftgo-domain/                           # [Legacy] Domain entities
├── ftgo-flyway/                           # [Legacy] DB migrations
├── ftgo-test-util/                        # [Legacy] Test utilities
├── ftgo-end-to-end-tests/                 # [Legacy] E2E tests
├── ftgo-end-to-end-tests-common/          # [Legacy] E2E test utils
│
├── settings.gradle                        # Multi-project configuration
├── build.gradle                           # Root build configuration
└── README.md                              # This file
```

## Bounded Contexts

| Context | Service | API Module | Description |
|---------|---------|------------|-------------|
| Consumer | `services/consumer-service` | `services/consumer-service-api` | Customer management and validation |
| Restaurant | `services/restaurant-service` | `services/restaurant-service-api` | Restaurant and menu operations |
| Order | `services/order-service` | `services/order-service-api` | Order lifecycle management |
| Courier | `services/courier-service` | `services/courier-service-api` | Delivery and courier management |

## Naming Conventions

### Package Naming

All new code uses the package root `com.ftgo.<servicename>.<layer>`:

| Layer | Package Pattern | Example |
|-------|----------------|---------|
| Domain | `com.ftgo.<svc>.domain` | `com.ftgo.orderservice.domain` |
| Web | `com.ftgo.<svc>.web` | `com.ftgo.orderservice.web` |
| Main | `com.ftgo.<svc>.main` | `com.ftgo.orderservice.main` |
| Messaging | `com.ftgo.<svc>.messaging` | `com.ftgo.orderservice.messaging` |
| API | `com.ftgo.<svc>.api` | `com.ftgo.orderservice.api` |

### Gradle Module Naming

| Type | Pattern | Example |
|------|---------|---------|
| Service | `:services:<name>-service` | `:services:order-service` |
| Service API | `:services:<name>-service-api` | `:services:order-service-api` |
| Shared library | `:shared-libraries:<name>` | `:shared-libraries:ftgo-common` |

### Docker & Kubernetes

| Artifact | Pattern | Example |
|----------|---------|---------|
| Docker image | `ftgo/<name>-service` | `ftgo/order-service` |
| K8s deployment | `<name>-service` | `order-service` |

## Creating a New Service

1. Copy the template: `cp -r template/service-template services/<your-service>`
2. Replace all `<service-name>` placeholders
3. Create Java package structure under `src/main/java/`
4. Register in `settings.gradle`
5. See `template/service-template/README.md` for full instructions

## Building

```bash
# Build all modules (legacy + new)
./gradlew clean build test

# Build a specific new service
./gradlew :services:order-service:build

# Build a specific shared library
./gradlew :shared-libraries:ftgo-common:build
```

## Architecture Decisions

Architecture Decision Records are maintained in `docs/adr/`:

- [ADR-0001: Microservices Repository Structure and Naming Conventions](docs/adr/0001-microservices-repository-structure.md)

## Migration Status

This repository is in active migration from monolith to microservices. Legacy modules (prefixed with `ftgo-`) at the root level will be incrementally migrated to the `services/` and `shared-libraries/` directories.

## Learn More

- [Microservices Patterns book](https://microservices.io/book)
- [Refactoring to Microservices](https://microservices.io/refactoring/index.html)
