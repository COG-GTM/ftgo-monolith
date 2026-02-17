# ftgo-common-lib

Shared value objects, utilities, and exceptions for the FTGO microservices platform.

## Version

`1.0.0`

## Coordinates

```
Group:    net.chrisrichardson.ftgo
Artifact: ftgo-common-lib
Version:  1.0.0
```

## Usage

### Gradle Dependency

For modules within this mono-repo:

```groovy
dependencies {
    compile project(':ftgo-common-lib')
}
```

For external consumers (after publishing):

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common-lib:1.0.0'
}
```

## API Reference

### Value Objects (`@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). JPA `@Embeddable` with `@Access(AccessType.FIELD)`. Custom Jackson serialization via `MoneyModule`. |
| `Address` | 5-field address value object (`street1`, `street2`, `city`, `state`, `zip`). JPA `@Embeddable`. |
| `PersonName` | First/last name value object. JPA `@Embeddable`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Jackson `SimpleModule` providing custom serializer/deserializer for `Money`. Serializes `Money` as a plain string (e.g., `"12.34"`). |
| `CommonJsonMapperInitializer` | Spring-managed bean that registers `MoneyModule` and `JavaTimeModule` on the application `ObjectMapper`. |
| `CommonConfiguration` | Spring `@Configuration` that creates `ObjectMapper` and `CommonJsonMapperInitializer` beans. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted. Accepts the current `Enum` state. |
| `NotYetImplementedException` | Marker exception for unimplemented functionality. |

## Package Structure

All classes reside in:

```
net.chrisrichardson.ftgo.common
```

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-data-jpa` | JPA annotations (`@Embeddable`, `@Access`) |
| `jackson-core` / `jackson-databind` | JSON serialization |
| `jackson-datatype-jsr310` | Java 8 date/time support |
| `commons-lang` | `EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder` |

## Publishing

Publish to the local repository:

```bash
./gradlew :ftgo-common-lib:publish
```

This publishes the artifact to `build/repo/` which can be consumed by other modules via the local Maven repository.

## Testing

```bash
./gradlew :ftgo-common-lib:test
```

Tests included:
- `MoneyTest` -- Unit tests for Money arithmetic (add, multiply, compare, string conversion)
- `MoneySerializationTest` -- Jackson serialization/deserialization round-trip tests
