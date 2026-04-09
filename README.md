# FTGO Monolith

The monolithic version of the [FTGO application](https://github.com/microservices-patterns/ftgo-application) from the book [Microservices Patterns](https://microservices.io/book) by Chris Richardson. This repo is used as a demo for illustrating how to refactor a monolithic application into microservices.

## Tech Stack

- **Java 8** (source/target compatibility)
- **Spring Boot 2.0.3**
- **Gradle 4.10.2** (via wrapper)
- **MySQL 5.7** (via Docker)
- **Flyway** for database migrations
- **Swagger UI** for API documentation

## Project Structure

```
ftgo-monolith/
├── ftgo-application/          # Main Spring Boot app (aggregates all services)
├── ftgo-domain/               # Shared domain model
├── ftgo-common/               # Common utilities
├── ftgo-common-jpa/           # Shared JPA configuration
├── ftgo-order-service/        # Order service module
├── ftgo-order-service-api/    # Order service API contracts
├── ftgo-consumer-service/     # Consumer service module
├── ftgo-consumer-service-api/ # Consumer service API contracts
├── ftgo-restaurant-service/   # Restaurant service module
├── ftgo-restaurant-service-api/ # Restaurant service API contracts
├── ftgo-courier-service/      # Courier service module
├── ftgo-courier-service-api/  # Courier service API contracts
├── ftgo-flyway/               # Flyway DB migration scripts
├── ftgo-test-util/            # Test utilities
├── ftgo-end-to-end-tests/     # End-to-end tests (see Known Issues)
├── common-swagger/            # Swagger/OpenAPI config
├── buildSrc/                  # Custom Gradle plugins (WaitForMySql, FtgoService)
├── mysql/                     # MySQL Docker image with schema init
└── docker-compose.yml         # Docker Compose for MySQL + app
```

## Quick Start

### Prerequisites

- **Java 8+** (builds on Java 17 as well)
- **Docker** (for MySQL)

### Build

```bash
# Build all modules (excluding e2e tests)
./gradlew assemble -x :ftgo-end-to-end-tests-common:compileJava \
  -x :ftgo-end-to-end-tests:compileJava \
  -x :ftgo-end-to-end-tests-common:assemble \
  -x :ftgo-end-to-end-tests:assemble
```

### Run with Docker Compose

```bash
# Start MySQL
docker compose up -d --build mysql

# Wait for MySQL to be ready
./gradlew waitForMySql

# Run Flyway migrations
./gradlew :ftgo-flyway:flywayMigrate

# Start everything
docker compose up -d --build
```

The application will be available at:
- **API**: `http://localhost:8081`
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **Health Check**: `http://localhost:8081/actuator/health`

### Run Locally (without Docker for the app)

```bash
# Start only MySQL via Docker
docker compose up -d --build mysql
./gradlew waitForMySql
./gradlew :ftgo-flyway:flywayMigrate

# Run the Spring Boot app directly
./gradlew :ftgo-application:bootRun
```

## API Overview

The monolith exposes REST APIs for:

- **Consumers** - Customer registration and management
- **Restaurants** - Restaurant and menu management
- **Orders** - Order placement, acceptance, and lifecycle
- **Couriers** - Courier availability and delivery assignment

## Database

- **MySQL 5.7** with a single `ftgo` schema
- Flyway migrations in `ftgo-flyway/src/main/resources/db/migration/`
- Default credentials: `mysqluser` / `mysqlpw` (root: `rootpassword`)

## Known Issues

- **End-to-end tests do not compile**: The `ftgo-end-to-end-tests` and `ftgo-end-to-end-tests-common` modules depend on `io.eventuate.util:eventuate-util-test:0.1.0.RELEASE`, which is no longer available (the Bintray/Eventuate Maven repositories have been shut down). All build commands should exclude these modules.
- **Unit tests on Java 17**: The `ftgo-order-service` tests use Mockito with ByteBuddy, which has compatibility issues with Java 17+ module access restrictions. Tests pass on Java 8.

## Learn More

- [Microservices Patterns book](https://microservices.io/book)
- [Refactoring to Microservices guide](https://microservices.io/refactoring/index.html)
- [Original FTGO microservices application](https://github.com/microservices-patterns/ftgo-application)
