# FTGO Common Library

Standalone, versioned shared library containing common value objects, utilities, and Jackson serialization modules used across all FTGO microservices.

**Version**: 1.0.0
**Group**: `net.chrisrichardson.ftgo`
**Artifact**: `ftgo-common`

## API Overview

### Value Objects (JPA `@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). JPA `@Embeddable` with custom Jackson serialization. |
| `Address` | 5-field address (street1, street2, city, state, zip). JPA `@Embeddable`. |
| `PersonName` | First/last name value object. JPA `@Embeddable`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Custom Jackson `SimpleModule` with serializer/deserializer for `Money` (serializes as plain string, e.g. `"12.34"`). |
| `CommonJsonMapperInitializer` | Spring `@PostConstruct` bean that registers `MoneyModule` and `JavaTimeModule` on the shared `ObjectMapper`. |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` class that provides an `ObjectMapper` bean and `CommonJsonMapperInitializer`. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted. Includes the current state in the message. |
| `NotYetImplementedException` | Marker exception for unimplemented features. |

## Usage

### Gradle Dependency

Add to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared:ftgo-common')
}
```

Or, when consuming from a Maven repository:

```groovy
dependencies {
    compile 'net.chrisrichardson.ftgo:ftgo-common:1.0.0'
}
```

### Examples

**Using Money:**
```java
Money price = new Money("12.34");
Money tax = new Money("1.50");
Money total = price.add(tax);                  // 13.84
Money lineTotal = price.multiply(3);           // 37.02
boolean canAfford = balance.isGreaterThanOrEqual(total);
String display = total.asString();             // "13.84"
```

**Jackson serialization (automatic with CommonConfiguration):**
```java
// Money serializes as a plain string: {"price":"12.34"}
// To manually register:
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new MoneyModule());
```

**Using Address:**
```java
Address addr = new Address("123 Main St", "Apt 4", "Springfield", "IL", "62704");
```

## Building

```bash
# Compile
./gradlew :shared:ftgo-common:compileJava

# Run tests
./gradlew :shared:ftgo-common:test

# Publish to local Maven repository
./gradlew :shared:ftgo-common:publishToMavenLocal

# Publish to project-local repository (build/repo)
./gradlew :shared:ftgo-common:publish
```

## Dependencies

- `javax.persistence` (JPA annotations via Spring Boot Data JPA)
- `com.fasterxml.jackson` (Core, Databind, JSR310 JavaTimeModule) - 2.9.7
- `commons-lang:commons-lang` - 2.6 (EqualsBuilder, HashCodeBuilder, ToStringBuilder)
- `mysql:mysql-connector-java` - 5.1.39 (runtime)

## Version Management

The library version is managed centrally in `gradle.properties` via the `ftgoCommonVersion` property. To release a new version:

1. Update `ftgoCommonVersion` in `gradle.properties`
2. Run the full test suite: `./gradlew :shared:ftgo-common:test`
3. Publish: `./gradlew :shared:ftgo-common:publishToMavenLocal`
4. Update dependent services to reference the new version
