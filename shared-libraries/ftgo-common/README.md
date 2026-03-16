# ftgo-common Shared Library

**Version:** 1.0.0

Common utilities, value objects, and JSON configuration shared across all FTGO microservices. Extracted from the monolith module `ftgo-common`.

## Contents

### Value Objects (JPA `@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). Uses `@Embeddable` + `@Access(AccessType.FIELD)` and custom Jackson serialization. |
| `Address` | 5-field address value object (`street1`, `street2`, `city`, `state`, `zip`). `@Embeddable`. |
| `PersonName` | First/last name value object. `@Embeddable`. |

### Utilities

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` that provides an `ObjectMapper` bean and `CommonJsonMapperInitializer`. |
| `CommonJsonMapperInitializer` | Registers `MoneyModule` and `JavaTimeModule` on the Jackson `ObjectMapper`; disables `WRITE_DATES_AS_TIMESTAMPS`. |
| `MoneyModule` | Custom Jackson `SimpleModule` with serializer/deserializer for `Money` (serializes as plain string). |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when an invalid state transition is attempted. |
| `NotYetImplementedException` | Placeholder for unimplemented features. |

## Package

```
net.chrisrichardson.ftgo.common
```

The original monolith package name is preserved for backward compatibility.

## Dependencies

| Dependency | Version | Scope |
|------------|---------|-------|
| `spring-boot-starter-data-jpa` | 2.0.3.RELEASE | compile |
| `jackson-core` | 2.9.7 | compile |
| `jackson-databind` | 2.9.7 | compile |
| `jackson-datatype-jsr310` | 2.9.7 | compile |
| `commons-lang` | 2.6 | compile |
| `jaxb-api` | 2.2.11 | runtime |
| `jaxb-core` | 2.2.11 | runtime |
| `jaxb-impl` | 2.2.11 | runtime |
| `activation` | 1.1.1 | runtime |
| `spring-boot-starter-test` | 2.0.3.RELEASE | test |

## Usage

### Gradle Dependency

Add to your service's `build.gradle`:

```groovy
dependencies {
    compile project(':shared-libraries:ftgo-common')
}
```

Or, if consuming from a Maven repository after publishing:

```groovy
dependencies {
    compile 'net.chrisrichardson.ftgo:ftgo-common:1.0.0'
}
```

### Examples

**Money arithmetic:**

```java
import net.chrisrichardson.ftgo.common.Money;

Money price = new Money("12.99");
Money tax = new Money("1.04");
Money total = price.add(tax);               // 14.03
Money doubled = price.multiply(2);          // 25.98
boolean canAfford = total.isGreaterThanOrEqual(price); // true
String display = total.asString();          // "14.03"
```

**Jackson serialization (with MoneyModule):**

```java
import net.chrisrichardson.ftgo.common.MoneyModule;
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new MoneyModule());

// Serializes Money as a plain string: {"price":"12.99"}
String json = mapper.writeValueAsString(order);
```

**Spring Configuration:**

```java
import net.chrisrichardson.ftgo.common.CommonConfiguration;

@Import(CommonConfiguration.class)
@SpringBootApplication
public class MyServiceApplication { ... }
```

## Building

```bash
# Compile
./gradlew :shared-libraries:ftgo-common:compileJava

# Run tests
./gradlew :shared-libraries:ftgo-common:test
```

## Publishing

The library is configured with the Gradle `maven-publish` plugin. To publish to the local repository:

```bash
./gradlew :shared-libraries:ftgo-common:publishMavenJavaPublicationToLocalRepository
```

Published artifacts are written to `${rootProject.buildDir}/repo`.

## Tests

| Test Class | Description |
|------------|-------------|
| `MoneyTest` | Unit tests for `Money` arithmetic (`add`, `multiply`, `isGreaterThanOrEqual`, `asString`). |
| `MoneySerializationTest` | Jackson serialization/deserialization tests for `Money` via `MoneyModule`. |
