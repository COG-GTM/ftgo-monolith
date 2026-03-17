# ftgo-common-lib

Shared value objects, utilities, and exceptions for the FTGO microservices platform.

## Version

**Current version:** `1.0.0`

## Contents

### Value Objects (`@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | Wraps `BigDecimal` with arithmetic operations (`add`, `multiply`, `isGreaterThanOrEqual`). Uses `@Embeddable` + `@Access(AccessType.FIELD)` with custom Jackson serialization. |
| `Address` | 5-field address (street1, street2, city, state, zip). `@Embeddable`. |
| `PersonName` | First name / last name. `@Embeddable`. |

### Utilities

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` that creates an `ObjectMapper` bean and registers `CommonJsonMapperInitializer`. |
| `CommonJsonMapperInitializer` | Registers `MoneyModule` and `JavaTimeModule` on the Spring-managed `ObjectMapper`. |
| `MoneyModule` | Custom Jackson `SimpleModule` with serializer/deserializer for `Money` (serializes as plain string, e.g. `"12.34"`). |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when a domain entity receives an invalid state transition. |
| `NotYetImplementedException` | Marker exception for unimplemented features. |

## Package

All classes live under:

```
net.chrisrichardson.ftgo.common
```

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `javax.persistence:javax.persistence-api` | 2.2 | JPA `@Embeddable` annotations |
| `com.fasterxml.jackson.core:jackson-core` | 2.9.7 | JSON processing |
| `com.fasterxml.jackson.core:jackson-databind` | 2.9.7 | JSON data binding |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | 2.9.7 | Java 8 date/time support |
| `commons-lang:commons-lang` | 2.6 | `EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder` |
| `org.springframework.boot:spring-boot-starter` | 2.0.3.RELEASE | Spring context, DI |

## Usage

### Gradle dependency (from local Maven repository)

```groovy
dependencies {
    compile 'net.chrisrichardson.ftgo:ftgo-common-lib:1.0.0'
}
```

### Intra-project dependency (within the mono-repo)

```groovy
dependencies {
    compile project(":shared-libraries:ftgo-common")
}
```

### Publishing to local repository

```bash
./gradlew :shared-libraries:ftgo-common:publishToMavenLocal
```

Or publish to the project-level repository:

```bash
./gradlew :shared-libraries:ftgo-common:publish
```

## Tests

| Test Class | Coverage |
|------------|----------|
| `MoneyTest` | Arithmetic operations: `add`, `multiply`, `isGreaterThanOrEqual`, `asString` |
| `MoneySerializationTest` | Jackson serialization/deserialization of `Money` via `MoneyModule` |

Run tests:

```bash
./gradlew :shared-libraries:ftgo-common:test
```
