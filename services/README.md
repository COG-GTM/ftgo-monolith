# FTGO Microservices

This directory contains the individual microservice modules for the FTGO platform.

## Services

| Service | Description | Bounded Context |
|---------|-------------|-----------------|
| `ftgo-order-service` | Order lifecycle management (create, revise, cancel, fulfill) | Order |
| `ftgo-consumer-service` | Consumer registration, validation, and profile management | Consumer |
| `ftgo-restaurant-service` | Restaurant onboarding, menu management, and order acceptance | Restaurant |
| `ftgo-courier-service` | Courier availability, assignment, and delivery tracking | Courier |

## Directory Structure

Each service follows a standard layout:

```
ftgo-<service>-service/
  src/
    main/
      java/com/ftgo/<service>/
        domain/          # Domain entities and aggregates
        service/         # Business logic / application services
        web/             # REST controllers
        repository/      # Data access layer
        config/          # Spring configuration classes
      resources/
        application.yml  # Service-specific configuration
    test/
      java/com/ftgo/<service>/
        domain/          # Unit tests for domain logic
        service/         # Service layer tests
        web/             # Controller / integration tests
  config/                # Environment-specific configuration overrides
  docker/                # Dockerfile and docker-compose overrides
  k8s/                   # Kubernetes manifests (Deployment, Service, ConfigMap)
  tests/                 # Additional test resources (e.g., Postman collections)
  build.gradle           # Service-specific Gradle build file
```

## Package Naming Convention

All services use the base package `com.ftgo.<service>`:

- `com.ftgo.order` - Order Service
- `com.ftgo.consumer` - Consumer Service
- `com.ftgo.restaurant` - Restaurant Service
- `com.ftgo.courier` - Courier Service

See [ADR-0001](../docs/adr/0001-mono-repo-structure-and-naming-conventions.md) for the full naming conventions.
