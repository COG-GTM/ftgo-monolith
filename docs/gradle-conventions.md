# FTGO Gradle Build Conventions

## Overview

The FTGO platform uses a **Gradle version catalog** and **convention plugins** to standardize
build configuration across all microservices and shared libraries. This replaces the legacy
monolith root `build.gradle` that applied global settings to all subprojects.

## Architecture

```
gradle/
  libs.versions.toml        # Centralized dependency versions (Version Catalog)

buildSrc/
  build.gradle              # Convention plugin dependencies
  src/main/groovy/
    ftgo.java-conventions.gradle          # Java 17, UTF-8, compiler options
    ftgo.spring-boot-conventions.gradle   # Spring Boot + dependency management
    ftgo.testing-conventions.gradle       # JUnit 5, Rest-Assured, integration tests
    ftgo.docker-conventions.gradle        # Docker image builds (Spring Boot Buildpacks)
    ftgo.publishing-conventions.gradle    # Maven publishing for shared libraries
    WaitForMySql.java                     # Legacy MySQL readiness check task
    WaitForMySqlPlugin.java               # Legacy plugin (registered at root)
```

## Version Catalog (`gradle/libs.versions.toml`)

All dependency versions are centralized in a single file. This ensures consistent versions
across all modules and makes upgrades a one-line change.

### Key Versions

| Dependency          | Version   |
|---------------------|-----------|
| Java                | 17        |
| Spring Boot         | 3.2.2     |
| Micrometer          | 1.12.2    |
| JUnit Jupiter       | 5.10.1    |
| Rest-Assured        | 5.4.0     |
| Jackson             | 2.16.1    |
| Flyway              | 9.22.3    |
| springdoc-openapi   | 2.3.0     |

### Usage in `build.gradle`

```groovy
dependencies {
    // Single library
    implementation libs.spring.boot.starter.web

    // Bundle (pre-defined group of libraries)
    implementation libs.bundles.spring.boot.service
    testImplementation libs.bundles.testing
}
```

### Available Bundles

| Bundle                   | Contents                                             |
|--------------------------|------------------------------------------------------|
| `spring-boot-service`   | starter-web, starter-data-jpa, starter-actuator      |
| `testing`               | spring-boot-starter-test, JUnit 5, Mockito           |
| `rest-assured-testing`  | rest-assured, spring-mock-mvc, json-path             |
| `jackson`               | jackson-core, jackson-databind, jackson-datatype-jsr310 |

## Convention Plugins

### `ftgo.java-conventions`

Base plugin for all Java modules. Applies:
- `java-library` plugin
- Java 17 source/target compatibility
- UTF-8 encoding
- Compiler flags: `-parameters`, `-Xlint:deprecation`, `-Xlint:unchecked`
- Reproducible builds (stable file order/timestamps in JARs)
- Maven Central repository

### `ftgo.spring-boot-conventions`

For modules that use Spring Boot. Extends `ftgo.java-conventions` and adds:
- `org.springframework.boot` plugin
- `io.spring.dependency-management` plugin
- Default: `bootJar` disabled, plain `jar` enabled (library mode)
- Override for executable services: set `bootJar { enabled = true }` and `jar { enabled = false }`

### `ftgo.testing-conventions`

Standardized test configuration. Extends `ftgo.java-conventions` and adds:
- JUnit 5 platform for all test tasks
- Default test dependencies: `spring-boot-starter-test`, JUnit Jupiter, Mockito
- Integration test source set (`src/integration-test/java`)
- `integrationTest` task

### `ftgo.docker-conventions`

Docker image build support. Extends `ftgo.spring-boot-conventions` and adds:
- Spring Boot Buildpacks configuration
- Default image name pattern: `ftgo/<service-name>:<version>`
- Java 17 buildpack target

### `ftgo.publishing-conventions`

Maven publishing for shared libraries. Extends `ftgo.java-conventions` and adds:
- `maven-publish` plugin
- Sources JAR and Javadoc JAR generation
- POM metadata configuration
- Local repository publishing

## Creating a New Microservice

A new microservice `build.gradle` requires minimal configuration:

```groovy
// services/my-new-service/build.gradle
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
}

dependencies {
    implementation project(":shared-libraries:ftgo-common")
    implementation project(":shared-libraries:ftgo-domain")

    implementation libs.bundles.spring.boot.service

    testImplementation project(":shared-libraries:ftgo-test-util")
}
```

Then register it in `settings.gradle`:

```groovy
include "services:my-new-service"
```

That's it! The convention plugins handle Java version, Spring Boot configuration,
test setup, and all other standard settings automatically.

## Creating a New Shared Library

```groovy
// shared-libraries/my-library/build.gradle
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.publishing-conventions'
}

dependencies {
    // Use 'api' for dependencies that consumers need on their classpath
    api libs.some.library

    // Use 'implementation' for internal-only dependencies
    implementation libs.internal.only.dep
}
```

## Making an Executable Service (bootJar)

By default, `ftgo.spring-boot-conventions` produces a plain JAR. To create an
executable Spring Boot JAR (fat JAR), override in your `build.gradle`:

```groovy
bootJar { enabled = true }
jar { enabled = false }
```

## Migration Notes

### Spring Boot 2.x to 3.x

- `javax.*` packages migrated to `jakarta.*` (JPA, Servlet, Annotation, etc.)
- Springfox replaced with springdoc-openapi
- `commons-lang` replaced with `commons-lang3`
- JUnit 4 migrated to JUnit 5 (Jupiter)
- `compile`/`testCompile` replaced with `implementation`/`testImplementation`/`api`
- Gradle upgraded from 4.x to 8.5 (required for version catalogs)
