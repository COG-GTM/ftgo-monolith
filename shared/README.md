# FTGO Shared Libraries

Shared libraries used across FTGO microservices. These modules are published as internal artifacts and consumed by individual services.

## Core Libraries

| Module | Version | Description | Package |
|--------|---------|-------------|---------|
| `ftgo-common` | 1.0.0 | Common value objects (Money, Address, PersonName), utilities, exceptions, Jackson serialization | `net.chrisrichardson.ftgo.common` |
| `ftgo-common-jpa` | 1.0.0 | JPA ORM mappings for common value objects, persistence utilities | `net.chrisrichardson.ftgo.common` (orm.xml) |
| `ftgo-domain` | 1.0.0 | JPA entities, Spring Data repositories, domain configuration | `net.chrisrichardson.ftgo.domain` |

## API/DTO Libraries (Cross-Service Communication)

| Module | Version | Description | Package |
|--------|---------|-------------|---------|
| `ftgo-order-service-api` | 1.0.0 | Order Service request/response DTOs and event objects | `net.chrisrichardson.ftgo.orderservice.api` |
| `ftgo-consumer-service-api` | 1.0.0 | Consumer Service request/response DTOs | `net.chrisrichardson.ftgo.consumerservice.api` |
| `ftgo-restaurant-service-api` | 1.0.0 | Restaurant Service request/response DTOs and event objects | `net.chrisrichardson.ftgo.restaurantservice.events` |
| `ftgo-courier-service-api` | 1.0.0 | Courier Service request/response DTOs | `net.chrisrichardson.ftgo.courierservice.api` |

## Dependency Graph

```
ftgo-common (value objects, utilities)
  └── ftgo-common-jpa (JPA ORM mappings)
        └── ftgo-domain (entities, repositories)

ftgo-common
  ├── ftgo-order-service-api (DTOs)
  ├── ftgo-consumer-service-api (DTOs)
  ├── ftgo-restaurant-service-api (DTOs)
  └── ftgo-courier-service-api (DTOs)
```

## Usage

Services depend on shared libraries via Gradle project references:
```groovy
dependencies {
    implementation project(':shared-ftgo-common')
    implementation project(':shared-ftgo-common-jpa')
    implementation project(':shared-ftgo-domain')
    implementation project(':shared-ftgo-order-service-api')
}
```

## Entity Ownership

See [docs/entity-service-ownership.md](../docs/entity-service-ownership.md) for the
complete entity-to-service ownership mapping.
