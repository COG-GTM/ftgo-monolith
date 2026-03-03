# FTGO Shared Libraries

This directory contains shared/common modules used across multiple FTGO microservices.

## Modules

| Module | Description | Consumers |
|--------|-------------|-----------|
| `ftgo-common` | Common value objects (Money, Address, PersonName), utilities, and JSON configuration | All services |
| `ftgo-domain` | Core domain entities (Order, Consumer, Restaurant, Courier) and domain events | All services |
| `ftgo-common-jpa` | Shared JPA configuration, base entities, and persistence utilities | Services with database access |

## Versioning Strategy

Shared libraries are versioned independently from services. When a shared library changes:

1. Update the library version in its `build.gradle`
2. Run the full test suite to verify backward compatibility
3. Update dependent services to reference the new version

## Package Naming Convention

- `com.ftgo.common` - Common utilities and value objects
- `com.ftgo.domain` - Core domain entities
- `com.ftgo.common.jpa` - JPA persistence utilities

See [ADR-0001](../docs/adr/0001-mono-repo-structure-and-naming-conventions.md) for full details.
