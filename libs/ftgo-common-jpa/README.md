# ftgo-common-jpa

Shared JPA utility library providing common persistence infrastructure for all FTGO microservices.

## Package

`com.ftgo.common.jpa`

## Contents

### JPA Converters

| Class | Description |
|-------|-------------|
| `MoneyConverter` | JPA `@Converter` (autoApply) that converts `Money` value objects to/from `BigDecimal` for database storage. |

### Configuration

| Class | Description |
|-------|-------------|
| `JpaConfiguration` | Spring `@Configuration` that enables `@EntityScan` and `@EnableJpaRepositories` for the `com.ftgo` base package. |

### ORM Mappings

| Resource | Description |
|----------|-------------|
| `META-INF/orm.xml` | Jakarta Persistence 3.1 ORM mappings for `Money` and `Address` embeddable types. |

## Usage

Add as a project dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-common-jpa')
}
```

## Build

```bash
./gradlew :libs:ftgo-common-jpa:build
```

## Dependencies

- `ftgo-common` (api)
- Spring Boot Starter Data JPA 3.2.5
- Jakarta Persistence API 3.1
- JUnit 5 / AssertJ (test)
