# FTGO Shared Libraries

This directory contains shared/common modules used across multiple FTGO microservices.

## Modules

| Module | Description | Consumers |
|--------|-------------|-----------|
| `ftgo-common` | Common value objects (Money, Address, PersonName), utilities, and JSON configuration | All services |
| `ftgo-domain` | Core domain entities (Order, Consumer, Restaurant, Courier) and domain events | All services |
| `ftgo-common-jpa` | Shared JPA configuration, base entities, and persistence utilities | Services with database access |
| `ftgo-openapi` | SpringDoc OpenAPI 3.0 auto-configuration, Swagger UI, and standard API response models (replaces legacy common-swagger) | All services exposing REST APIs |

## Dependency Chain

```
shared/ftgo-common          (base: value objects, utilities)
    ^
    |
shared/ftgo-common-jpa      (JPA ORM mappings for common types)
    ^            ^
    |            |
shared/ftgo-domain           (entities, repositories, DTOs)
```

## Versioning Strategy

Shared libraries are versioned independently from services. When a shared library changes:

1. Update the library version in its `build.gradle`
2. Run the full test suite to verify backward compatibility
3. Update dependent services to reference the new version

Current version: **1.0.0** (managed via `ftgoCommonVersion` in `gradle.properties`)

## Package Naming Convention

- `net.chrisrichardson.ftgo.common` - Common utilities and value objects
- `net.chrisrichardson.ftgo.common` (orm.xml) - JPA persistence mappings for common types
- `net.chrisrichardson.ftgo.domain` - Core domain entities and repositories
- `net.chrisrichardson.ftgo.domain.dto` - Cross-service DTO contracts

## Publishing

```bash
# Publish all shared libraries to local Maven repo
./gradlew :shared:ftgo-common:publishToMavenLocal
./gradlew :shared:ftgo-common-jpa:publishToMavenLocal
./gradlew :shared:ftgo-domain:publishToMavenLocal
```

## Documentation

- [Entity-to-Service Ownership Mapping](../docs/entity-service-ownership.md)
- [Shared Library Migration Plan](../docs/shared-library-migration-plan.md)
- [ADR-0001: Mono-Repo Structure](../docs/adr/0001-mono-repo-structure-and-naming-conventions.md)
