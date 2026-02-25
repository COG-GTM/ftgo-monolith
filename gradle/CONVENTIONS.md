# FTGO Gradle Build Conventions

This document describes how new microservices consume the shared Gradle build configuration.

## Overview

The FTGO platform uses a centralized build configuration consisting of:

1. **Version Catalog** (`gradle/libs.versions.toml`) — Centralized dependency versions
2. **Convention Plugins** (`buildSrc/`) — Reusable build configurations
3. **Root Build** (`build.gradle`) — Legacy backward compatibility

## Quick Start: Creating a New Microservice

A new microservice `build.gradle` requires only service-specific configuration:

```groovy
// services/ftgo-my-service/build.gradle

plugins {
    id 'org.springframework.boot'       // Boot JAR packaging (from pluginManagement)
    id 'ftgo.spring-boot-conventions'   // BOM + dependency management + Java 17
    id 'ftgo.testing-conventions'       // JUnit 5 + integration tests
    id 'ftgo.docker-conventions'        // Jib container builds
}

dependencies {
    // Use version catalog references (no hardcoded versions!)
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.mysql.connector
    implementation libs.springdoc.openapi.starter.webmvc.ui

    // Observability
    implementation libs.micrometer.registry.prometheus

    // Project dependencies
    implementation project(':shared:ftgo-common')
    implementation project(':shared:ftgo-domain')

    // Testing (provided by ftgo.testing-conventions, add extras here)
    testImplementation libs.rest.assured.spring.mock.mvc
    testImplementation libs.testcontainers.mysql
}
```

That's it — under 30 lines for a fully configured microservice!

## Convention Plugins

### `ftgo.java-conventions`

Base Java compilation settings applied by all other convention plugins.

**Provides:**
- Java 17 source and target compatibility
- UTF-8 encoding for all compilation tasks
- Compiler warnings for deprecation and unchecked operations
- `-parameters` flag for runtime parameter name access
- Source and Javadoc JAR generation
- Maven Central repository

**Usage:**
```groovy
plugins {
    id 'ftgo.java-conventions'
}
```

### `ftgo.spring-boot-conventions`

Spring Boot 3.x dependency management configuration. Includes `ftgo.java-conventions`.

**Provides:**
- Spring dependency management with Spring Boot 3.2.3 BOM
- Java conventions (via ftgo.java-conventions)
- Consistent dependency versions for all Spring-managed libraries

**Note:** The Spring Boot Gradle plugin (`org.springframework.boot`) must be applied
separately by each microservice. This is because the plugin requires Java 17+ and
cannot be included in buildSrc's compile classpath. The plugin version is centrally
managed via `pluginManagement` in `settings.gradle`.

**Usage:**
```groovy
plugins {
    id 'org.springframework.boot'       // Boot JAR packaging
    id 'ftgo.spring-boot-conventions'   // BOM + dependency management
}

dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
}
```

### `ftgo.testing-conventions`

JUnit 5 testing configuration with integration test support. Includes `ftgo.java-conventions`.

**Provides:**
- JUnit 5 (Jupiter) as default test platform
- Integration test source set (`src/integration-test/java`)
- `integrationTest` task
- Detailed test logging
- HTML and JUnit XML reports

**Usage:**
```groovy
plugins {
    id 'ftgo.testing-conventions'
}

dependencies {
    testImplementation libs.junit.jupiter
    testImplementation libs.rest.assured
    testImplementation libs.testcontainers.mysql
}
```

Run tests:
```bash
./gradlew test                  # Unit tests
./gradlew integrationTest       # Integration tests
./gradlew check                 # Both
```

### `ftgo.docker-conventions`

Container image builds using Google Jib. Includes `ftgo.java-conventions`.

**Provides:**
- Jib plugin (no Docker daemon required)
- Eclipse Temurin JDK 17 base image
- Standard container configuration (port 8080, JVM flags)
- OCI image format
- Build metadata labels

**Usage:**
```groovy
plugins {
    id 'ftgo.docker-conventions'
}

// Optional: customize image settings
jib {
    to {
        image = "my-registry/${project.name}:${version}"
    }
}
```

Build images:
```bash
./gradlew jibDockerBuild    # Build to local Docker daemon
./gradlew jib               # Build and push to registry
```

### `ftgo.publishing-conventions`

Maven publishing for shared libraries. Includes `ftgo.java-conventions`.

**Provides:**
- Maven publication with POM metadata
- Local repository publishing
- Remote repository support (via `gradle.properties`)

**Usage:**
```groovy
plugins {
    id 'ftgo.publishing-conventions'
}
```

Publish:
```bash
./gradlew publishToMavenLocal           # Local Maven repo
./gradlew publish                       # All configured repos
```

## Version Catalog

The version catalog (`gradle/libs.versions.toml`) centralizes all dependency versions.

### Accessing Dependencies

```groovy
// Single dependency
implementation libs.spring.boot.starter.web
implementation libs.jackson.databind

// Dependency bundles (predefined groups)
implementation libs.bundles.spring.boot.web      // web + actuator + validation
implementation libs.bundles.spring.boot.data     // JPA + MySQL + Flyway
implementation libs.bundles.jackson              // core + databind + jsr310

testImplementation libs.bundles.testing          // JUnit 5 + Mockito + AssertJ
testImplementation libs.bundles.testing.rest     // Rest-Assured suite
```

### Available Bundles

| Bundle | Contents |
|--------|----------|
| `spring-boot-web` | Web, Actuator, Validation starters |
| `spring-boot-data` | JPA, MySQL Connector, Flyway |
| `testing` | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| `testing-rest` | Rest-Assured, Spring Mock MVC, JSON Path |
| `observability` | Actuator, Micrometer Prometheus |
| `jackson` | Jackson Core, Databind, JSR-310 |

### Key Versions

| Dependency | Version | Notes |
|-----------|---------|-------|
| Java | 17 | Minimum target for new services |
| Spring Boot | 3.2.3 | Latest 3.2.x LTS |
| Micrometer | 1.12.3 | Observability |
| JUnit Jupiter | 5.10.2 | Testing framework |
| Rest-Assured | 5.4.0 | API testing |
| Flyway | 10.8.1 | Database migrations |
| Jackson | 2.16.1 | JSON serialization |
| MySQL Connector | 8.3.0 | Database driver |
| Springdoc OpenAPI | 2.3.0 | API documentation |

## Combining Plugins

Convention plugins can be combined as needed:

```groovy
// Standalone Spring Boot microservice
plugins {
    id 'org.springframework.boot'       // Boot JAR (from pluginManagement)
    id 'ftgo.spring-boot-conventions'   // BOM + dependency management
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

// Shared library (no boot JAR, publishable)
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.publishing-conventions'
}
```

## Legacy Compatibility

During the migration period, legacy monolith modules continue to use:
- `compile` / `testCompile` / `runtime` configurations (mapped to modern equivalents)
- Version properties from `gradle.properties` (e.g., `$springBootVersion`)
- The root `subprojects` block for shared settings

These legacy configurations will be removed once migration is complete.

## Plugin Management

The Spring Boot plugin version is managed centrally in `settings.gradle`:

```groovy
pluginManagement {
    plugins {
        id 'org.springframework.boot' version '3.2.3'
    }
}
```

Microservices don't need to specify the version when applying the plugin:
```groovy
plugins {
    id 'org.springframework.boot'   // version from pluginManagement
}
```

## Migration Checklist

When migrating a module from legacy to new convention plugins:

1. Replace `compile` with `implementation`
2. Replace `testCompile` with `testImplementation`
3. Replace `runtime` with `runtimeOnly`
4. Replace explicit version strings with catalog references
5. Add appropriate convention plugins
6. Add `id 'org.springframework.boot'` to the `plugins {}` block
7. Remove any redundant configuration inherited from root `subprojects`
8. Verify build: `./gradlew :your-module:build`
