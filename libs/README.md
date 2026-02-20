# Shared Libraries

This directory contains shared libraries used across microservices. These are the migrated equivalents of the monolith's shared modules.

## Libraries

| Library | Package | Description | Monolith Equivalent |
|---------|---------|-------------|---------------------|
| ftgo-common | `com.ftgo.common` | Common utilities and base classes | `ftgo-common` |
| ftgo-common-jpa | `com.ftgo.common.jpa` | JPA base entities and repositories | `ftgo-common-jpa` |
| ftgo-domain | `com.ftgo.domain` | Shared domain primitives (Money, Address) | `ftgo-domain` |
| ftgo-common-swagger | `com.ftgo.common.swagger` | API documentation configuration | `common-swagger` |
| ftgo-test-util | `com.ftgo.testutil` | Test helpers and fixtures | `ftgo-test-util` |

## Usage

Services declare dependencies on shared libraries in their `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-common')
    implementation project(':libs:ftgo-domain')
}
```
