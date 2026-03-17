# FTGO Monolith — Microservices Migration

This is the monolithic version of the [FTGO application](https://github.com/microservices-patterns/ftgo-application) from the book [Microservices Patterns](https://microservices.io/book). It is used to illustrate how to refactor a monolithic application to microservices.

## Repository Structure

This mono-repo contains both the **legacy monolith** modules (at the root level) and the **new microservices** structure (under `services/`).

```
ftgo-monolith/
│
├── # ─── Legacy Monolith Modules ───────────────────────
├── ftgo-common/                    # Shared utilities (Money, Address, etc.)
├── ftgo-common-jpa/                # Shared JPA configuration
├── ftgo-domain/                    # Core domain entities
├── common-swagger/                 # API documentation config
├── ftgo-test-util/                 # Test utilities
├── ftgo-order-service/             # Order business logic (monolith)
├── ftgo-order-service-api/         # Order API contract (monolith)
├── ftgo-consumer-service/          # Consumer business logic (monolith)
├── ftgo-consumer-service-api/      # Consumer API contract (monolith)
├── ftgo-restaurant-service/        # Restaurant business logic (monolith)
├── ftgo-restaurant-service-api/    # Restaurant API contract (monolith)
├── ftgo-courier-service/           # Courier business logic (monolith)
├── ftgo-courier-service-api/       # Courier API contract (monolith)
├── ftgo-application/               # Monolith assembly (composes all services)
├── ftgo-flyway/                    # Database migrations
├── ftgo-end-to-end-tests/          # E2E tests
├── ftgo-end-to-end-tests-common/   # Shared E2E test utilities
│
├── # ─── New Microservices Structure ───────────────────
├── services/
│   ├── consumer-service/           # Consumer bounded context
│   │   ├── consumer-service-api/   #   Public API contract
│   │   ├── consumer-service-impl/  #   Implementation
│   │   ├── config/                 #   Environment-specific config
│   │   ├── docker/                 #   Dockerfile
│   │   └── k8s/                    #   Kubernetes manifests
│   ├── restaurant-service/         # Restaurant bounded context
│   │   ├── restaurant-service-api/
│   │   ├── restaurant-service-impl/
│   │   ├── config/
│   │   ├── docker/
│   │   └── k8s/
│   ├── order-service/              # Order bounded context
│   │   ├── order-service-api/
│   │   ├── order-service-impl/
│   │   ├── config/
│   │   ├── docker/
│   │   └── k8s/
│   ├── courier-service/            # Courier bounded context
│   │   ├── courier-service-api/
│   │   ├── courier-service-impl/
│   │   ├── config/
│   │   ├── docker/
│   │   └── k8s/
│   └── service-template/           # Template for creating new services
│
├── # ─── Documentation ─────────────────────────────────
├── docs/
│   └── adr/                        # Architecture Decision Records
│       └── 0001-microservices-repository-structure.md
│
├── # ─── Build & Infrastructure ────────────────────────
├── build.gradle                    # Root build configuration
├── settings.gradle                 # Module includes (monolith + services)
├── docker-compose.yml              # Local development environment
├── deployment/                     # Kubernetes deployment configs
└── mysql/                          # Database container setup
```

## Bounded Context to Service Mapping

| Bounded Context | Service                | Gradle Path                          | Port  |
|-----------------|------------------------|--------------------------------------|-------|
| Consumer        | `consumer-service`     | `:services:consumer-service`         | 8082  |
| Restaurant      | `restaurant-service`   | `:services:restaurant-service`       | 8083  |
| Order           | `order-service`        | `:services:order-service`            | 8084  |
| Courier         | `courier-service`      | `:services:courier-service`          | 8085  |

## Naming Conventions

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

## Package Structure (New Services)

```
com.ftgo.<servicename>/
  ├── api/
  │   ├── events/       # Domain events published by this service
  │   └── web/          # Request/response DTOs
  ├── domain/           # Entities, aggregates, domain services
  ├── repository/       # Spring Data JPA repositories
  ├── web/              # REST controllers
  ├── config/           # Spring configuration classes
  └── messaging/        # Event publishing and consumption
```

> **Note:** Legacy monolith modules use the `net.chrisrichardson.ftgo` package root. New microservice modules use `com.ftgo` to clearly distinguish migrated code.

## Creating a New Service

See [`services/service-template/README.md`](services/service-template/README.md) for instructions on bootstrapping a new microservice from the template.

## Build & Test

```bash
# Build and test everything (monolith + services)
./gradlew clean build test

# Build only a specific service
./gradlew :services:consumer-service:consumer-service-impl:build
```

## Architecture Decision Records

- [ADR-0001: Microservices Repository Structure and Naming Conventions](docs/adr/0001-microservices-repository-structure.md)

## Learn More

- [Microservices Patterns (book)](https://microservices.io/book)
- [Refactoring to Microservices](https://microservices.io/refactoring/index.html)
