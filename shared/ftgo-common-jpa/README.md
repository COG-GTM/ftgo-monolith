# ftgo-common-jpa — Shared JPA Utilities Library

Standalone versioned library extracted from the FTGO monolith.  
Provides JPA ORM mappings for shared value objects and Spring JPA configuration used across all FTGO microservices.

## Coordinates

```
group:    com.ftgo
artifact: ftgo-common-jpa
version:  1.0.0-SNAPSHOT
```

## Quick Start

**Declare the dependency** in any microservice `build.gradle`:

```groovy
dependencies {
    implementation 'com.ftgo:ftgo-common-jpa:1.0.0-SNAPSHOT'
}
```

## API Reference

### ORM Mappings (`META-INF/orm.xml`)

Provides JPA ORM metadata for common value objects from `ftgo-common`:

| Value Object | JPA Mapping |
|-------------|-------------|
| `Money` | `@Embeddable` with `amount` column mapping |
| `Address` | `@Embeddable` with street1, street2, city, state, zip mappings |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonJpaConfiguration` | `@Configuration` that enables entity scanning for `com.ftgo.common` package and imports `CommonConfiguration`. Use via `@Import(CommonJpaConfiguration.class)`. |

## Dependencies

This library depends on:
- `ftgo-common` (1.0.0-SNAPSHOT) — shared value objects (Money, Address, PersonName)
- Spring Boot Starter Data JPA — JPA/Hibernate support
- Apache Commons Lang — builder utilities

## Building

```bash
# Compile + run tests
./gradlew :shared:ftgo-common-jpa:build

# Publish to local Maven repo (~/.m2)
./gradlew :shared:ftgo-common-jpa:publishToMavenLocal

# Publish to project-local repo (build/repo)
./gradlew :shared:ftgo-common-jpa:publish
```

## Versioning

This library follows [Semantic Versioning](https://semver.org/):

- **MAJOR** — breaking API changes
- **MINOR** — new features, backward-compatible
- **PATCH** — backward-compatible bug fixes

Current version is managed in `build.gradle` (`version = '1.0.0-SNAPSHOT'`) and mirrored in `gradle.properties`.

## Package Structure

```
com.ftgo.common.jpa
└── CommonJpaConfiguration.java

META-INF/
└── orm.xml  (Money, Address ORM mappings)
```
