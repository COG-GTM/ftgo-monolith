# FTGO Build Logic — Convention Plugins

Shared Gradle convention plugins for FTGO microservices. These plugins standardize
build configuration so that each microservice `build.gradle` stays under 30 lines.

## Architecture

```
build-logic/
  settings.gradle.kts          # Loads version catalog from ../gradle/libs.versions.toml
  build.gradle.kts              # kotlin-dsl plugin + Spring Boot / Jib plugin deps
  src/main/kotlin/
    ftgo.java-conventions.gradle.kts
    ftgo.spring-boot-conventions.gradle.kts
    ftgo.testing-conventions.gradle.kts
    ftgo.docker-conventions.gradle.kts
    ftgo.publishing-conventions.gradle.kts
```

## Version Catalog

All dependency versions are centralized in `gradle/libs.versions.toml`.
Services reference dependencies via type-safe accessors (e.g. `libs.spring.boot.starter.web`).

Key upgraded versions (from monolith):

| Dependency | Monolith | Microservices |
|---|---|---|
| Java | 8 | 17 |
| Spring Boot | 2.0.3 | 3.2.5 |
| JUnit | 4 | 5.10.2 |
| Micrometer | 1.0.4 | 1.12.5 |
| Rest-Assured | 2.9.0 | 5.4.0 |
| Flyway | — | 10.11.0 |
| Jackson | — | 2.17.0 |

## Convention Plugins

### ftgo.java-conventions

Base plugin applied by all modules. Provides:
- Java 17 toolchain, source/target compatibility
- UTF-8 encoding for compilation and Javadoc
- Compiler args: `-parameters`, `-Xlint:unchecked`, `-Xlint:deprecation`
- `group = "com.ftgo"`
- Maven Central repository

### ftgo.spring-boot-conventions

For Spring Boot application modules. Extends `ftgo.java-conventions` and adds:
- `org.springframework.boot` plugin
- `io.spring.dependency-management` plugin
- Spring Boot BOM import
- Configures `bootJar` and `jar` classifiers

### ftgo.testing-conventions

Standardizes test setup. Extends `ftgo.java-conventions` and adds:
- JUnit 5 platform
- Mockito + AssertJ
- Rest-Assured (including Spring MockMvc support)
- `spring-boot-starter-test` (excludes JUnit Vintage)
- Integration test task with `@Tag("integration")` support
- JVM arg for dynamic agent loading

### ftgo.docker-conventions

Container image build via Jib. Extends `ftgo.java-conventions` and adds:
- `eclipse-temurin:17-jre-alpine` base image
- OCI image format
- Container-aware JVM flags
- Configurable registry/tag via Gradle properties (`dockerRegistry`, `dockerTag`)

### ftgo.publishing-conventions

Maven publishing for shared library modules. Extends `ftgo.java-conventions` and adds:
- Sources and Javadoc JARs
- `MavenPublication` with POM metadata
- Local repository output

## How to Consume

### 1. Include build-logic in your settings.gradle(.kts)

```kotlin
pluginManagement {
    includeBuild("../../build-logic")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
```

### 2. Apply plugins in your build.gradle

A typical microservice needs only:

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    implementation libs.bundles.spring.boot.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.flyway.core

    runtimeOnly libs.mysql.connector

    testImplementation libs.rest.assured.spring.mock.mvc
}
```

A shared library module uses:

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.publishing-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    implementation libs.jackson.databind
}
```

### 3. Available dependency bundles

| Bundle | Contents |
|---|---|
| `libs.bundles.spring.boot.web` | starter-web, starter-actuator, starter-validation |
| `libs.bundles.spring.boot.data` | starter-data-jpa, flyway-core |
| `libs.bundles.jackson` | jackson-databind, jackson-datatype-jsr310 |
| `libs.bundles.testing` | starter-test, junit-jupiter-api/engine, mockito, assertj |
| `libs.bundles.rest.assured.all` | rest-assured, json-path, spring-mock-mvc |
| `libs.bundles.micrometer` | micrometer-core, micrometer-registry-prometheus |
