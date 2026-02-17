# FTGO Microservices Repository Structure

## Overview

This document defines the repository layout, naming conventions, and directory structure for the FTGO microservices platform. See [ADR-0001](adr/0001-microservices-repository-structure.md) for the decision rationale.

## Directory Layout

```
ftgo-monolith/
│
├── services/                          # Microservice implementations
│   ├── ftgo-consumer-service/         # Consumer bounded context
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/             # Application code
│   │   │   │   └── resources/        # Configuration files
│   │   │   └── test/
│   │   │       ├── java/             # Unit & integration tests
│   │   │       └── resources/        # Test configuration
│   │   ├── docker/
│   │   │   └── Dockerfile            # Service container image
│   │   ├── k8s/
│   │   │   ├── deployment.yaml       # Kubernetes deployment
│   │   │   ├── service.yaml          # Kubernetes service
│   │   │   └── configmap.yaml        # Environment configuration
│   │   └── build.gradle              # Service-specific build config
│   │
│   ├── ftgo-restaurant-service/       # Restaurant bounded context
│   ├── ftgo-order-service/            # Order bounded context
│   ├── ftgo-courier-service/          # Courier bounded context
│   └── template-service/             # Archetype for creating new services
│
├── libs/                              # Shared libraries
│   ├── ftgo-common-lib/              # Value objects (Money, Address, PersonName)
│   ├── ftgo-common-jpa-lib/          # JPA utilities and base entities
│   └── ftgo-domain-lib/             # Domain entities and aggregates
│
├── infrastructure/                    # Infrastructure configuration
│   ├── docker/                       # Docker Compose for local development
│   ├── k8s/                          # Shared Kubernetes manifests
│   └── helm/                         # Helm charts for deployment
│
├── buildSrc-platform/                # Shared Gradle convention plugins
│
├── docs/                             # Documentation
│   ├── adr/                          # Architecture Decision Records
│   └── repository-structure.md       # This file
│
└── [existing monolith modules]       # Preserved during migration
    ├── ftgo-common/
    ├── ftgo-common-jpa/
    ├── ftgo-domain/
    ├── ftgo-application/
    ├── ftgo-order-service/
    ├── ftgo-consumer-service/
    ├── ftgo-restaurant-service/
    ├── ftgo-courier-service/
    └── ...
```

## Bounded Contexts

| Context    | Service Directory                    | Package Root                                    |
|------------|--------------------------------------|-------------------------------------------------|
| Consumer   | `services/ftgo-consumer-service/`    | `net.chrisrichardson.ftgo.consumer`              |
| Restaurant | `services/ftgo-restaurant-service/`  | `net.chrisrichardson.ftgo.restaurant`            |
| Order      | `services/ftgo-order-service/`       | `net.chrisrichardson.ftgo.order`                 |
| Courier    | `services/ftgo-courier-service/`     | `net.chrisrichardson.ftgo.courier`               |

## Package Naming Convention

```
net.chrisrichardson.ftgo.<context>.<layer>
```

### Layers

| Layer         | Purpose                                      | Example                                              |
|---------------|----------------------------------------------|------------------------------------------------------|
| `domain`      | Entities, value objects, domain events        | `net.chrisrichardson.ftgo.order.domain.Order`        |
| `api`         | DTOs, request/response objects, API contracts | `net.chrisrichardson.ftgo.order.api.CreateOrderRequest` |
| `service`     | Application services, use cases               | `net.chrisrichardson.ftgo.order.service.OrderService`  |
| `repository`  | Data access, JPA repositories                 | `net.chrisrichardson.ftgo.order.repository.OrderRepository` |
| `web`         | REST controllers, exception handlers          | `net.chrisrichardson.ftgo.order.web.OrderController`   |
| `config`      | Spring configuration classes                  | `net.chrisrichardson.ftgo.order.config.OrderConfig`    |
| `messaging`   | Event publishers, message handlers            | `net.chrisrichardson.ftgo.order.messaging.OrderEventPublisher` |

### Shared Libraries

| Library              | Package                                | Purpose                             |
|----------------------|----------------------------------------|-------------------------------------|
| `ftgo-common-lib`    | `net.chrisrichardson.ftgo.common`      | Money, Address, PersonName          |
| `ftgo-common-jpa-lib`| `net.chrisrichardson.ftgo.common.jpa`  | JPA base entities, utilities        |
| `ftgo-domain-lib`    | `net.chrisrichardson.ftgo.domain`      | Shared domain abstractions          |

## Gradle Module Naming

| Type              | Pattern                      | Example                        |
|-------------------|------------------------------|--------------------------------|
| Service           | `ftgo-<context>-service`     | `ftgo-order-service`           |
| Service API       | `ftgo-<context>-service-api` | `ftgo-order-service-api`       |
| Shared Library    | `ftgo-<name>-lib`            | `ftgo-common-lib`              |

## Service Directory Structure

Each microservice follows this standard layout:

```
ftgo-<context>-service/
├── build.gradle                    # Service build config (< 30 lines)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── net/chrisrichardson/ftgo/<context>/
│   │   │       ├── domain/         # Entities, aggregates, value objects
│   │   │       ├── api/            # DTOs, API contracts
│   │   │       ├── service/        # Application services
│   │   │       ├── repository/     # Data access layer
│   │   │       ├── web/            # REST controllers
│   │   │       ├── messaging/      # Event handlers and publishers
│   │   │       └── config/         # Spring configuration
│   │   └── resources/
│   │       ├── application.yml     # Service configuration
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/       # Flyway migrations (per-service)
│   └── test/
│       ├── java/
│       │   └── net/chrisrichardson/ftgo/<context>/
│       │       ├── domain/         # Unit tests
│       │       ├── service/        # Service layer tests
│       │       ├── web/            # Controller tests
│       │       └── integration/    # Integration tests
│       └── resources/
│           └── application-test.yml
├── docker/
│   └── Dockerfile                  # Multi-stage build
└── k8s/
    ├── deployment.yaml
    ├── service.yaml
    └── configmap.yaml
```

## Creating a New Service

1. Copy `services/template-service/` to `services/ftgo-<context>-service/`
2. Update `build.gradle` with service-specific dependencies
3. Rename packages from `template` to `<context>`
4. Add the service to `settings.gradle`
5. Create initial Flyway migration in `src/main/resources/db/migration/`
6. Add Kubernetes manifests in `k8s/`
7. Add Dockerfile in `docker/`
