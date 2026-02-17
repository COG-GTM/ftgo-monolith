# Shared Gradle Configuration for Microservices

## Overview

The FTGO microservices platform uses a centralized Gradle configuration to standardize dependency management, build settings, and plugin versions across all services. This replaces the monolith's single root `build.gradle` with a modular, composable approach.

## Architecture

```
ftgo-monolith/
  gradle/libs.versions.toml        # Version catalog (source of truth for all versions)
  buildSrc-platform/                # Convention plugins (included build)
    settings.gradle                 # Standalone project settings
    build.gradle                    # Plugin dependencies
    src/main/groovy/
      ftgo.java-conventions.gradle
      ftgo.spring-boot-conventions.gradle
      ftgo.testing-conventions.gradle
      ftgo.docker-conventions.gradle
      ftgo.publishing-conventions.gradle
  services/
    ftgo-order-service/build.gradle # < 30 lines, uses convention plugins
    ...
```

## Components

### 1. Version Catalog (`gradle/libs.versions.toml`)

Centralizes all dependency versions in a single TOML file at the standard Gradle location. Microservices reference versions from this catalog instead of hardcoding version strings.

**Key versions:**

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.0 | Application framework |
| Java | 17 | Runtime target (via toolchain) |
| Flyway | 10.4.1 | Database migrations |
| JUnit Jupiter | 5.10.1 | Testing framework |
| Micrometer | 1.12.2 | Metrics and observability |
| SpringDoc | 2.3.0 | OpenAPI documentation |
| Testcontainers | 1.19.3 | Integration test infrastructure |
| Rest-Assured | 5.4.0 | API testing |
| Jackson | 2.16.1 | JSON serialization |

### 2. Convention Plugins (`buildSrc-platform/`)

Precompiled Groovy script plugins that encode build conventions. `buildSrc-platform/` is a standalone Gradle project that microservices consume via `pluginManagement { includeBuild }`.

| Plugin | Applies | Purpose |
|--------|---------|---------|
| `ftgo.java-conventions` | `java` | Java 17 toolchain, UTF-8, compiler flags |
| `ftgo.spring-boot-conventions` | `java-conventions` + Spring Boot | Spring Boot app packaging, BOM |
| `ftgo.testing-conventions` | `java-conventions` + JUnit 5 | Test config, parallel execution, Rest-Assured |
| `ftgo.docker-conventions` | `spring-boot-conventions` + Jib | Container image builds (OCI) |
| `ftgo.publishing-conventions` | `java-conventions` + Maven Publish | Library publishing |

## How Microservices Consume the Shared Configuration

Each microservice's `settings.gradle` must include the convention plugins build and enable the version catalog:

```groovy
// settings.gradle (in each microservice or in a shared root)
pluginManagement {
    includeBuild '../../buildSrc-platform'
}

dependencyResolutionManagement {
    versionCatalogs {
        libs {
            from(files('../../gradle/libs.versions.toml'))
        }
    }
}

rootProject.name = 'ftgo-order-service'
```

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

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    runtimeOnly 'com.mysql:mysql-connector-j'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

When the version catalog is enabled, dependencies can also use catalog aliases:

```groovy
dependencies {
    implementation libs.bundles.spring.web
    implementation libs.bundles.spring.data
    implementation libs.bundles.observability
    runtimeOnly libs.mysql.connector
    testImplementation libs.bundles.spring.test
}
```

### Shared Library `build.gradle`

```groovy
plugins {
    id 'ftgo.publishing-conventions'
}

version = '1.0.0'

dependencies {
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'org.apache.commons:commons-lang3'
}
```

## Convention Plugin Details

### `ftgo.java-conventions`

Applied by all Java modules (services and libraries).

- Sets Java 17 via toolchain (auto-downloads JDK if needed)
- Configures UTF-8 encoding for compilation and Javadoc
- Enables `-parameters` flag (needed for Spring parameter name discovery)
- Enables `unchecked` and `deprecation` compiler warnings
- Sets `group = 'net.chrisrichardson.ftgo'`
- Configures Maven Central repository

### `ftgo.spring-boot-conventions`

Applied by all Spring Boot microservices.

- Extends `ftgo.java-conventions`
- Applies Spring Boot and dependency management plugins
- Imports Spring Boot BOM for version management
- Generates build info for `/actuator/info` endpoint
- Configures bootJar as the primary artifact

### `ftgo.testing-conventions`

Applied by all modules with tests.

- Extends `ftgo.java-conventions`
- Configures JUnit 5 platform
- Adds test dependencies: JUnit Jupiter, Mockito, Rest-Assured
- Enables parallel test execution (`maxParallelForks = availableProcessors / 2`)
- Creates `integrationTest` task with `@Tag("integration")` filtering
- Configures HTML and JUnit XML test reports

### `ftgo.docker-conventions`

Applied by services that need container images.

- Extends `ftgo.spring-boot-conventions`
- Uses Google Jib for container image building (no Dockerfile needed)
- Base image: `eclipse-temurin:17-jre-alpine`
- Tags images with version and `latest`
- Configures JVM memory flags (`-Xms256m -Xmx512m`) and port 8080
- Adds OCI labels (maintainer, source)
- Output format: OCI

### `ftgo.publishing-conventions`

Applied by shared libraries.

- Extends `ftgo.java-conventions`
- Configures Maven publishing with POM metadata
- Generates source and javadoc JARs
- Publishes to local repository (configurable for remote)
- Includes Apache 2.0 license in POM

## Migrating from Monolith Configuration

| Monolith (`build.gradle`) | Microservices (Convention Plugins) |
|---------------------------|-----------------------------------|
| `sourceCompatibility = '1.8'` | `ftgo.java-conventions` sets Java 17 toolchain |
| `springBootVersion=2.0.3.RELEASE` | Version catalog: `spring-boot = "3.2.0"` |
| `restAssuredVersion=2.9.0` | Version catalog: `rest-assured = "5.4.0"` |
| `micrometerVersion=1.0.4` | Version catalog: `micrometer = "1.12.2"` |
| Root `build.gradle` subprojects block | Convention plugins applied per-module |
| `buildSrc/` custom plugins | `buildSrc-platform/` convention plugins |
| `gradle.properties` version strings | `gradle/libs.versions.toml` TOML catalog |

## Adding a New Dependency

1. Add the version to `[versions]` in `gradle/libs.versions.toml`
2. Add the library alias to `[libraries]`
3. Optionally add to a `[bundles]` group
4. Reference in service `build.gradle` via `libs.<alias>` or by GAV coordinates
