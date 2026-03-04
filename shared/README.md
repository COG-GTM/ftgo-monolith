# FTGO Shared Libraries

Shared libraries used across FTGO microservices. These modules are published as internal artifacts and consumed by individual services.

## Modules

| Module | Description | New Package |
|--------|-------------|-------------|
| `ftgo-common` | Common utilities, exceptions, base classes | `com.ftgo.common` |
| `ftgo-common-jpa` | JPA base entities, repository helpers | `com.ftgo.common.jpa` |
| `ftgo-domain` | Shared domain value objects (Money, Address) | `com.ftgo.domain` |

## Usage

Services depend on shared libraries via Gradle project references:
```groovy
dependencies {
    implementation project(':shared-ftgo-common')
    implementation project(':shared-ftgo-domain')
}
```
