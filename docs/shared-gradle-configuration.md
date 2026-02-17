# Shared Gradle Configuration for Microservices

## Overview

The FTGO microservices platform uses a centralized Gradle configuration to standardize dependency management, build settings, and plugin versions across all services. This replaces the monolith's single root `build.gradle` with a modular, composable approach.

## Components

### 1. Version Catalog (`gradle/platform/libs.versions.toml`)

Centralizes all dependency versions in a single file. All microservices reference versions from this catalog instead of hardcoding version strings.

**Key versions:**

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.0 | Application framework |
| Java | 17 | Runtime target |
| Flyway | 10.4.1 | Database migrations |
| JUnit Jupiter | 5.10.1 | Testing framework |
| Micrometer | 1.12.2 | Metrics and observability |
| SpringDoc | 2.3.0 | OpenAPI documentation |
| Testcontainers | 1.19.3 | Integration test infrastructure |
| Rest-Assured | 5.4.0 | API testing |

### 2. Convention Plugins (`buildSrc-platform/`)

Reusable Gradle plugins that encode build conventions. Each service applies the plugins it needs.

| Plugin | Applies | Purpose |
|--------|---------|---------|
| `ftgo.java-conventions` | `java` | Java 17, UTF-8, compiler flags |
| `ftgo.spring-boot-conventions` | `java-conventions` + Spring Boot | Spring Boot app packaging |
| `ftgo.testing-conventions` | `java-conventions` + JUnit 5 | Test configuration, parallel execution |
| `ftgo.docker-conventions` | `spring-boot-conventions` + Jib | Container image builds |
| `ftgo.publishing-conventions` | `java-conventions` + Maven Publish | Library publishing |

## Usage Examples

### Microservice `build.gradle` (< 30 lines)

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

version = '0.1.0-SNAPSHOT'

dependencies {
    implementation project(':libs:ftgo-common-lib')

    implementation libs.bundles.spring.web
    implementation libs.bundles.spring.data
    implementation libs.bundles.observability
    implementation libs.springdoc.openapi.starter

    runtimeOnly libs.mysql.connector

    testImplementation libs.bundles.spring.test
    testImplementation libs.testcontainers.mysql
    testImplementation libs.testcontainers.junit
}
```

### Shared Library `build.gradle`

```groovy
plugins {
    id 'ftgo.publishing-conventions'
}

version = '1.0.0'

dependencies {
    implementation libs.jackson.databind
    implementation libs.commons.lang3
}
```

## Convention Plugin Details

### `ftgo.java-conventions`

Applied by all Java modules (services and libraries).

- Sets Java 17 source and target compatibility
- Configures UTF-8 encoding
- Enables `-parameters` flag (needed for Spring parameter name discovery)
- Enables `unchecked` and `deprecation` warnings
- Configures Maven Central repository

### `ftgo.spring-boot-conventions`

Applied by all Spring Boot microservices.

- Extends `ftgo.java-conventions`
- Applies Spring Boot and dependency management plugins
- Generates build info for `/actuator/info` endpoint
- Configures bootJar as the primary artifact

### `ftgo.testing-conventions`

Applied by all modules with tests.

- Configures JUnit 5 platform
- Adds standard test dependencies (JUnit, Mockito, Spring Boot Test)
- Enables parallel test execution
- Creates `integrationTest` task with `@Tag("integration")` filtering
- Configures HTML and JUnit XML test reports

### `ftgo.docker-conventions`

Applied by services that need container images.

- Uses Jib for container image building (no Dockerfile needed)
- Base image: `eclipse-temurin:17-jre-alpine`
- Tags with version and `latest`
- Configures JVM memory flags and exposed ports
- Adds OCI labels

### `ftgo.publishing-conventions`

Applied by shared libraries.

- Configures Maven publishing
- Generates source and javadoc JARs
- Publishes to local repository (configurable for remote)

## Migrating from Monolith Configuration

| Monolith (`build.gradle`) | Microservices (Convention Plugins) |
|---------------------------|-----------------------------------|
| `sourceCompatibility = '1.8'` | `ftgo.java-conventions` sets Java 17 |
| `springBootVersion=2.0.3.RELEASE` | Version catalog: `spring-boot = "3.2.0"` |
| `restAssuredVersion=2.9.0` | Version catalog: `rest-assured = "5.4.0"` |
| `micrometerVersion=1.0.4` | Version catalog: `micrometer = "1.12.2"` |
| Root `build.gradle` subprojects block | Convention plugins applied per-module |
| `buildSrc/` custom plugins | `buildSrc-platform/` convention plugins |
