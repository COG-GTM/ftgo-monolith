# FTGO Platform - Microservices Migration

[![CI: Order Service](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-order-service.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-order-service.yml)
[![CI: Consumer Service](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-consumer-service.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-consumer-service.yml)
[![CI: Restaurant Service](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-restaurant-service.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-restaurant-service.yml)
[![CI: Courier Service](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-courier-service.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-courier-service.yml)
[![CI: Shared Libraries](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-shared-libraries.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-shared-libraries.yml)
[![CI: Monolith Build](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-monolith.yml/badge.svg?branch=feat/microservices-migration-v3)](https://github.com/COG-GTM/ftgo-monolith/actions/workflows/ci-monolith.yml)

This repository contains the FTGO food delivery application, currently undergoing migration from a monolithic architecture to microservices.

## Repository Structure

```
ftgo-monolith/
├── services/                               # NEW: Microservice implementations
│   ├── ftgo-order-service/                 #   Order bounded context
│   ├── ftgo-consumer-service/              #   Consumer bounded context
│   ├── ftgo-restaurant-service/            #   Restaurant bounded context
│   ├── ftgo-courier-service/               #   Courier bounded context
│   └── ftgo-service-template/              #   Template for new services
│
├── shared/                                 # NEW: Shared libraries & API contracts
│   ├── ftgo-common/                        #   Common utilities and value objects
│   ├── ftgo-common-jpa/                    #   Shared JPA utilities
│   ├── ftgo-domain/                        #   Shared domain model
│   ├── common-swagger/                     #   Swagger/OpenAPI configuration
│   ├── ftgo-test-util/                     #   Test utilities
│   ├── ftgo-order-service-api/             #   Order service API contracts
│   ├── ftgo-consumer-service-api/          #   Consumer service API contracts
│   ├── ftgo-restaurant-service-api/        #   Restaurant service API contracts
│   └── ftgo-courier-service-api/           #   Courier service API contracts
│
├── infrastructure/                         # NEW: Deployment & infrastructure
│   ├── docker/                             #   Docker Compose for services
│   ├── k8s/                                #   Kubernetes manifests
│   │   ├── base/                           #     Base manifests
│   │   └── overlays/{dev,staging,prod}/    #     Environment overlays
│   ├── scripts/                            #   Utility scripts
│   └── ci/                                 #   CI/CD configuration
│
├── docs/                                   # NEW: Documentation
│   └── adr/                                #   Architecture Decision Records
│
├── buildSrc/                               # Gradle build plugins
├── ftgo-application/                       # LEGACY: Monolith application
├── ftgo-order-service/                     # LEGACY: Order service (flat)
├── ftgo-consumer-service/                  # LEGACY: Consumer service (flat)
├── ftgo-restaurant-service/                # LEGACY: Restaurant service (flat)
├── ftgo-courier-service/                   # LEGACY: Courier service (flat)
├── ftgo-flyway/                            # Database migrations
└── ...                                     # Other legacy modules
```

## Bounded Contexts

| Context    | Service                        | Port | Package Root         |
|-----------|--------------------------------|------|----------------------|
| Order     | `services/ftgo-order-service`     | 8081 | `com.ftgo.order`     |
| Consumer  | `services/ftgo-consumer-service`  | 8082 | `com.ftgo.consumer`  |
| Restaurant| `services/ftgo-restaurant-service`| 8083 | `com.ftgo.restaurant`|
| Courier   | `services/ftgo-courier-service`   | 8084 | `com.ftgo.courier`   |

## Package Naming Convention

All packages follow: `com.ftgo.<bounded-context>.<layer>`

Layers: `domain`, `web`, `repository`, `config`, `api`

## Gradle Module Paths

```
# Services
:services:ftgo-order-service
:services:ftgo-consumer-service
:services:ftgo-restaurant-service
:services:ftgo-courier-service

# Shared Libraries
:shared:ftgo-common
:shared:ftgo-common-jpa
:shared:ftgo-domain
:shared:common-swagger
:shared:ftgo-test-util

# API Contracts
:shared:ftgo-order-service-api
:shared:ftgo-consumer-service-api
:shared:ftgo-restaurant-service-api
:shared:ftgo-courier-service-api
```

## Building

```bash
# Build everything (monolith + new microservices)
./gradlew clean build

# Build a specific microservice
./gradlew :services:ftgo-order-service:build

# Build all shared libraries
./gradlew shared:build

# Run tests
./gradlew test
```

## Creating a New Service

1. Copy the template: `cp -r services/ftgo-service-template services/ftgo-<name>-service`
2. Update package names, build.gradle, and configuration files
3. Add to `settings.gradle`: `include "services:ftgo-<name>-service"`
4. See `services/ftgo-service-template/README.md` for detailed instructions

## Architecture Decision Records

- [ADR-0001: Microservices Repository Structure and Naming Conventions](docs/adr/0001-microservices-repository-structure-and-naming-conventions.md)

## Migration Status

The repository currently contains both the legacy monolith modules (flat structure) and the new microservices structure (hierarchical). Legacy modules will be removed once migration is complete.

## Learn More

- [Microservices Patterns by Chris Richardson](https://microservices.io/book)
- [FTGO Application (original)](https://github.com/microservices-patterns/ftgo-application)
