# ftgo-common

Shared library containing common value objects, configuration, and utilities used across all FTGO microservices.

## Package

`com.ftgo.common`

## Contents

### Value Objects

| Class | Description |
|-------|-------------|
| `Money` | Immutable value object for monetary calculations. Supports creation from `int`, `String`, and `BigDecimal`. Provides `add`, `multiply`, `isGreaterThanOrEqual`, and `asString` operations. JPA `@Embeddable`. |
| `Address` | JPA-embeddable value object representing a physical address (street1, street2, city, state, zip). |
| `PersonName` | JPA-embeddable value object representing a person's first and last name. |

### Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` class that registers an `ObjectMapper` bean and `CommonJsonMapperInitializer`. |
| `CommonJsonMapperInitializer` | Configures the Jackson `ObjectMapper` with `MoneyModule` and `JavaTimeModule`, disables `WRITE_DATES_AS_TIMESTAMPS`. |

### Jackson Support

| Class | Description |
|-------|-------------|
| `MoneyModule` | Jackson `SimpleModule` providing serializer/deserializer for `Money` (serialized as a plain string). |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Runtime exception thrown when an invalid state transition is attempted. |
| `NotYetImplementedException` | Runtime exception placeholder for unimplemented functionality. |

## Usage

Add as a project dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-common')
}
```

## Build

```bash
./gradlew :libs:ftgo-common:build
```

## Dependencies

- Jakarta Persistence API 3.1
- Jackson Databind 2.15.x
- Spring Boot Starter 3.2.x
- JUnit 5 / AssertJ (test)
