# FTGO Platform

FTGO (Food To Go) is an online food delivery platform, originally built as a monolith and currently being migrated to a microservices architecture.

This repository is a **mono-repo** containing both the existing monolith and the new microservices structure, allowing a gradual, side-by-side migration.

## Repository Structure

```
ftgo-monolith/
│
├── ── Monolith (existing) ──────────────────────────────────────────
│   ├── ftgo-application/               # Monolith entry point (@Import composition)
│   ├── ftgo-order-service/             # Order module (monolith)
│   ├── ftgo-order-service-api/         # Order API definitions
│   ├── ftgo-consumer-service/          # Consumer module (monolith)
│   ├── ftgo-consumer-service-api/      # Consumer API definitions
│   ├── ftgo-restaurant-service/        # Restaurant module (monolith)
│   ├── ftgo-restaurant-service-api/    # Restaurant API definitions
│   ├── ftgo-courier-service/           # Courier module (monolith)
│   ├── ftgo-courier-service-api/       # Courier API definitions
│   ├── ftgo-common/                    # Shared utilities
│   ├── ftgo-common-jpa/                # Shared JPA helpers
│   ├── ftgo-domain/                    # Shared domain objects (Money, Address, etc.)
│   ├── common-swagger/                 # Swagger configuration
│   ├── ftgo-test-util/                 # Test utilities
│   ├── ftgo-flyway/                    # Database migrations
│   ├── ftgo-end-to-end-tests/          # End-to-end tests
│   └── ftgo-end-to-end-tests-common/   # E2E test utilities
│
├── ── Microservices (new) ──────────────────────────────────────────
│   ├── services/                       # Independently deployable microservices
│   │   ├── ftgo-order-service/         # Order bounded context
│   │   ├── ftgo-consumer-service/      # Consumer bounded context
│   │   ├── ftgo-restaurant-service/    # Restaurant bounded context
│   │   └── ftgo-courier-service/       # Courier bounded context
│   │
│   └── shared/                         # Shared libraries for microservices
│       ├── ftgo-common/                # Common utilities
│       ├── ftgo-common-jpa/            # JPA helpers
│       └── ftgo-domain/                # Shared domain value objects
│
├── ── Infrastructure & Docs ────────────────────────────────────────
│   ├── docs/                           # Documentation
│   │   ├── adr/                        # Architecture Decision Records
│   │   └── architecture/               # Architecture diagrams
│   ├── k8s/                            # Kubernetes manifests (Kustomize)
│   │   ├── base/                       # Base resources
│   │   └── overlays/                   # Environment-specific overlays
│   │       ├── dev/
│   │       ├── staging/
│   │       └── prod/
│   └── deploy/                         # Deployment scripts and IaC
│       ├── scripts/                    # Build and deploy scripts
│       └── terraform/                  # Infrastructure as Code
│
├── settings.gradle                     # All module definitions
├── build.gradle                        # Root build configuration
└── docker-compose.yml                  # Local development services
```

## Bounded Contexts

| Context | Monolith Module | Microservice Module | Package |
|---------|----------------|---------------------|---------|
| Order | `:ftgo-order-service` | `:services-ftgo-order-service` | `com.ftgo.order` |
| Consumer | `:ftgo-consumer-service` | `:services-ftgo-consumer-service` | `com.ftgo.consumer` |
| Restaurant | `:ftgo-restaurant-service` | `:services-ftgo-restaurant-service` | `com.ftgo.restaurant` |
| Courier | `:ftgo-courier-service` | `:services-ftgo-courier-service` | `com.ftgo.courier` |

## Service Template

Each microservice follows a standard structure (see [ADR-0001](docs/adr/0001-microservices-repository-structure.md)):

```
services/ftgo-<name>-service/
  ├── src/main/java/com/ftgo/<name>/   # Java source (layered packages)
  ├── src/main/resources/              # Application configuration
  ├── src/test/                        # Unit tests
  ├── config/                          # Environment config (application.yml)
  ├── docker/                          # Dockerfile
  ├── k8s/                             # Kubernetes manifests
  ├── tests/                           # Integration/contract tests
  ├── build.gradle                     # Module build file
  └── README.md                        # Service documentation
```

## Package Naming Convention

- **Microservices**: `com.ftgo.<service>.<layer>` (e.g., `com.ftgo.order.domain`)
- **Shared libraries**: `com.ftgo.common`, `com.ftgo.common.jpa`, `com.ftgo.domain`
- **Legacy monolith**: `net.chrisrichardson.ftgo.*` (unchanged)

## Building

```bash
# Build everything (monolith + microservices)
./gradlew clean build

# Build monolith only
./gradlew :ftgo-application:build

# Build a specific microservice
./gradlew :services-ftgo-order-service:build

# Run the monolith application
./gradlew :ftgo-application:bootRun
```

## Documentation

- [ADR-0001: Microservices Repository Structure](docs/adr/0001-microservices-repository-structure.md)

## Learn More

This is the monolithic version of the [FTGO application](https://github.com/microservices-patterns/ftgo-application) from the book [Microservices Patterns](https://microservices.io/book).
Read more at the [Refactoring section of microservices.io](https://microservices.io/refactoring/index.html).
