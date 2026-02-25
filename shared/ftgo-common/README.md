# ftgo-common — Shared Common Library

Standalone versioned library extracted from the FTGO monolith.  
Provides shared value objects, exception types, and Jackson serialization modules used across all FTGO microservices.

## Coordinates

```
group:    com.ftgo
artifact: ftgo-common
version:  1.0.0-SNAPSHOT
```

## Quick Start

**Declare the dependency** in any microservice `build.gradle`:

```groovy
dependencies {
    implementation 'com.ftgo:ftgo-common:1.0.0-SNAPSHOT'
}
```

## API Reference

### Value Objects (`@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic (`add`, `multiply`, `isGreaterThanOrEqual`). JPA `@Embeddable` with `@Access(AccessType.FIELD)`. |
| `Address` | 5-field address: street1, street2, city, state, zip. JPA `@Embeddable`. |
| `PersonName` | firstName / lastName. JPA `@Embeddable`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Custom Jackson `SimpleModule` — serializes `Money` as a plain string (`"12.34"`), deserializes from the same format. |
| `CommonJsonMapperInitializer` | Spring-managed bean that registers `MoneyModule` and `JavaTimeModule` on the shared `ObjectMapper` via `@PostConstruct`. |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | `@Configuration` class that provides `ObjectMapper` and `CommonJsonMapperInitializer` beans. Import via `@Import(CommonConfiguration.class)`. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when a domain state machine receives an invalid transition. Accepts the current `Enum` state. |
| `NotYetImplementedException` | Marker exception for unfinished code paths. |

## Building

```bash
# Compile + run tests
./gradlew :shared:ftgo-common:build

# Publish to local Maven repo (~/.m2)
./gradlew :shared:ftgo-common:publishToMavenLocal

# Publish to project-local repo (build/repo)
./gradlew :shared:ftgo-common:publish
```

## Versioning

This library follows [Semantic Versioning](https://semver.org/):

- **MAJOR** — breaking API changes
- **MINOR** — new features, backward-compatible
- **PATCH** — backward-compatible bug fixes

Current version is managed in `build.gradle` (`version = '1.0.0-SNAPSHOT'`) and mirrored in `gradle.properties`.

## Package Structure

```
com.ftgo.common
├── Address.java
├── CommonConfiguration.java
├── CommonJsonMapperInitializer.java
├── Money.java
├── MoneyModule.java
├── NotYetImplementedException.java
├── PersonName.java
└── UnsupportedStateTransitionException.java
```
