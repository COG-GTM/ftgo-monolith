# Shared Library Migration Plan

## Overview

This document describes the migration plan for extracting `ftgo-common-jpa` and
`ftgo-domain` from legacy root-level modules into versioned shared libraries under
the `shared/` directory.

## Current State

### Legacy Modules (root level)
- `ftgo-common-jpa/` - JPA ORM mappings for common value objects (Money, Address)
- `ftgo-domain/` - 22 Java files: entities, repositories, enums, configuration

### Extracted Shared Libraries
- `shared/ftgo-common/` - Already extracted (Batch 2, EM-32), version 1.0.0
- `shared/ftgo-common-jpa/` - Extracted in this batch, version 1.0.0
- `shared/ftgo-domain/` - Extracted in this batch, version 1.0.0

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

## Migration Phases

### Phase 1: Extract Libraries (Current - Batch 3)

**Status**: Complete

1. **shared/ftgo-common-jpa** extracted with:
   - `META-INF/orm.xml` - JPA ORM mappings for `Money` and `Address` embeddables
   - Dependencies: `shared:ftgo-common`, `spring-boot-starter-data-jpa`
   - Maven publishing configured (group: `net.chrisrichardson.ftgo`, artifact: `ftgo-common-jpa`)

2. **shared/ftgo-domain** extracted with:
   - All 22 domain Java files (entities, repositories, enums, configuration)
   - 7 cross-service DTO classes in `dto` sub-package
   - Dependencies: `shared:ftgo-common`, `shared:ftgo-common-jpa`, `spring-boot-starter-data-jpa`, `commons-lang`
   - Maven publishing configured (group: `net.chrisrichardson.ftgo`, artifact: `ftgo-domain`)

### Phase 2: Service Migration (Future Batches)

Each service will be migrated to depend on the shared libraries instead of the
legacy root-level modules. Migration order:

1. **ftgo-consumer-service** - Simplest service, depends only on `Consumer` entity
2. **ftgo-restaurant-service** - Depends on `Restaurant`, `MenuItem`, `RestaurantMenu`
3. **ftgo-courier-service** - Depends on `Courier`, `Plan`, `Action`
4. **ftgo-order-service** - Most complex, depends on `Order` and cross-entity references

For each service:
```
# Before (legacy dependency)
compile project(":ftgo-domain")

# After (shared library dependency)
compile project(":shared:ftgo-domain")
```

### Phase 3: Break Cross-Entity JPA References (Future)

Replace direct JPA entity references with ID-based references and API calls:

1. `Order.restaurant` (`@ManyToOne Restaurant`) → `Order.restaurantId` (Long)
2. `Order.assignedCourier` (`@ManyToOne Courier`) → `Order.courierId` (Long)
3. `Action.order` (`@ManyToOne Order`) → `Action.orderId` (Long)

Each service will use the DTO contracts (`net.chrisrichardson.ftgo.domain.dto`)
for cross-service data transfer instead of JPA entity references.

### Phase 4: Split Domain Module Per Service (Future)

Eventually, the shared domain module should be split so each service owns its entities:

```
shared/ftgo-domain/                    # Shared types only (enums, DTOs)
services/ftgo-order-service/domain/    # Order, OrderLineItem, etc.
services/ftgo-consumer-service/domain/ # Consumer
services/ftgo-restaurant-service/domain/ # Restaurant, MenuItem, RestaurantMenu
services/ftgo-courier-service/domain/  # Courier, Plan, Action
```

### Phase 5: Remove Legacy Modules (Final)

Once all services depend on `shared/` libraries:
1. Remove `ftgo-common-jpa/` from root
2. Remove `ftgo-domain/` from root
3. Remove legacy includes from `settings.gradle`

## Versioning Strategy

- All shared libraries start at version `1.0.0`
- Version is managed centrally via `ftgoCommonVersion` in `gradle.properties`
- Breaking changes require a major version bump
- Backward-compatible additions require a minor version bump
- Bug fixes require a patch version bump

## Publishing

Libraries are published to:
1. **Local Maven repository**: `./gradlew :shared:ftgo-common-jpa:publishToMavenLocal`
2. **Project-local repository**: `./gradlew :shared:ftgo-common-jpa:publish` (→ `build/repo/`)

## Build Verification

```bash
# Compile check (excludes end-to-end tests)
./gradlew compileJava \
  -x :ftgo-end-to-end-tests-common:compileJava \
  -x :ftgo-end-to-end-tests:compileJava

# Test check (excludes end-to-end and application tests)
./gradlew test \
  -x :ftgo-end-to-end-tests-common:test \
  -x :ftgo-end-to-end-tests:test \
  -x :ftgo-application:test
```
