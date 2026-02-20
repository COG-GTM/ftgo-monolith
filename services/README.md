# Services

This directory contains the independently deployable microservices extracted from the FTGO monolith.

## Directory Layout

Each service follows a consistent structure:

```
<service-name>/
  +-- src/main/java/com/ftgo/<context>/   # Source code by layer
  |   +-- domain/                          # Entities, aggregates, value objects
  |   +-- service/                         # Application services
  |   +-- web/                             # REST controllers, DTOs
  |   +-- config/                          # Spring configuration
  |   +-- messaging/                       # Event pub/sub
  +-- src/main/resources/                  # application.yml, etc.
  +-- src/test/java/                       # Unit and integration tests
  +-- docker/                              # Dockerfile
  +-- k8s/                                 # Kubernetes manifests
  +-- build.gradle                         # Service build config
  +-- README.md                            # Service documentation
```

## Services

| Service | Bounded Context | Port | Description |
|---------|----------------|------|-------------|
| order-service | Order | 8081 | Order lifecycle management |
| consumer-service | Consumer | 8082 | Consumer profiles and validation |
| restaurant-service | Restaurant | 8083 | Restaurant and menu management |
| courier-service | Courier | 8084 | Courier availability and delivery |

## Creating a New Service

Copy `_template-service/` and follow the instructions in its README.
