# ftgo-common

Shared common library for FTGO microservices. Contains value objects, utilities, exceptions, and Jackson serialization modules extracted from the FTGO monolith.

**Version:** 1.0.0
**Package:** `net.chrisrichardson.ftgo.common`

## Contents

### Value Objects (JPA @Embeddable)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). Uses `@Embeddable` + `@Access(AccessType.FIELD)`. Includes custom Jackson serialization via `MoneyModule`. |
| `Address` | 5-field address value object (`street1`, `street2`, `city`, `state`, `zip`). `@Embeddable`. |
| `PersonName` | Name value object (`firstName`, `lastName`). `@Embeddable`. |

### Utilities

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` class that provides `ObjectMapper` and `CommonJsonMapperInitializer` beans. |
| `CommonJsonMapperInitializer` | Configures Jackson `ObjectMapper` with `MoneyModule` and `JavaTimeModule`, disables `WRITE_DATES_AS_TIMESTAMPS`. |
| `MoneyModule` | Custom Jackson serializer/deserializer for `Money`. Serializes as plain string (e.g., `"12.34"`). |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted. Extends `RuntimeException`. |
| `NotYetImplementedException` | Marker exception for unimplemented features. Extends `RuntimeException`. |

## Dependencies

- `javax.persistence` (JPA annotations) via `spring-boot-starter-data-jpa`
- `commons-lang:commons-lang:2.6` (EqualsBuilder, HashCodeBuilder, ToStringBuilder)
- `com.fasterxml.jackson.core:jackson-core:2.9.7`
- `com.fasterxml.jackson.core:jackson-databind:2.9.7`
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.7`
- `org.springframework.boot:spring-boot-starter`

## Usage

### Gradle Dependency

Add to your microservice's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-ftgo-common')
}
```

Or, when published to a Maven repository:

```groovy
dependencies {
    compile 'net.chrisrichardson.ftgo:ftgo-common:1.0.0'
}
```

### Money Example

```java
import net.chrisrichardson.ftgo.common.Money;

Money price = new Money("19.99");
Money tax = new Money("1.60");
Money total = price.add(tax);

boolean canAfford = total.isGreaterThanOrEqual(new Money("20.00"));

// Jackson serialization produces: "19.99"
String json = objectMapper.writeValueAsString(price);
```

### Address Example

```java
import net.chrisrichardson.ftgo.common.Address;

Address addr = new Address("123 Main St", "Apt 4", "Springfield", "IL", "62701");
```

### Spring Configuration

Import `CommonConfiguration` to auto-configure Jackson with `MoneyModule`:

```java
@Import(CommonConfiguration.class)
public class MyServiceConfiguration {
    // ObjectMapper will be pre-configured with MoneyModule and JavaTimeModule
}
```

## Publishing

Publish to the local repository:

```bash
./gradlew :shared-ftgo-common:publishMavenJavaPublicationToLocalRepository
```

## Testing

Run unit tests:

```bash
./gradlew :shared-ftgo-common:test
```

Tests included:
- `MoneyTest` — Arithmetic operations, string conversion, comparison
- `MoneySerializationTest` — Jackson serialization/deserialization round-trip
