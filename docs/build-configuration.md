# FTGO Shared Build Configuration

## Overview

This document describes the shared Gradle build configuration for FTGO microservices.
The configuration centralizes dependency versions, compiler settings, and build conventions
using Gradle convention plugins, replacing per-service duplication with a standardized approach.

## Architecture

```
gradle/
  libs.versions.toml          # Version catalog (forward-looking, Gradle 7+ native support)

buildSrc/
  src/main/groovy/
    FtgoVersions.groovy        # Centralized version constants (Gradle 4.x compatibility)
    FtgoDependencies.groovy    # Pre-built dependency coordinate strings
    FtgoJavaConventionsPlugin.groovy
    FtgoSpringBootConventionsPlugin.groovy
    FtgoTestingConventionsPlugin.groovy
    FtgoDockerConventionsPlugin.groovy
    FtgoPublishingConventionsPlugin.groovy
  src/main/resources/META-INF/gradle-plugins/
    ftgo.java-conventions.properties
    ftgo.spring-boot-conventions.properties
    ftgo.testing-conventions.properties
    ftgo.docker-conventions.properties
    ftgo.publishing-conventions.properties
```

## Convention Plugins

### ftgo.java-conventions

Base plugin for all FTGO modules. Applies:

- Java plugin with source/target compatibility 1.8
- UTF-8 encoding for all Java compilation tasks
- Compiler warnings (`-Xlint:unchecked`, `-Xlint:deprecation`)
- Group set to `com.ftgo`
- Maven Central repository

**Usage:**
```groovy
apply plugin: 'ftgo.java-conventions'
```

### ftgo.spring-boot-conventions

For microservices that run as Spring Boot applications. Automatically applies
`ftgo.java-conventions`. Adds:

- `org.springframework.boot` plugin
- `io.spring.dependency-management` plugin

**Usage:**
```groovy
apply plugin: 'ftgo.spring-boot-conventions'
```

### ftgo.testing-conventions

Configures the testing framework. Automatically applies `ftgo.java-conventions`. Adds:

- JUnit 5 (Jupiter) API, engine, and parameterized tests
- Rest-Assured for API testing
- Spring Boot Test support
- JUnit Platform enabled on all Test tasks
- Test logging with pass/skip/fail events

**Usage:**
```groovy
apply plugin: 'ftgo.testing-conventions'
```

### ftgo.docker-conventions

Provides Docker image build tasks. Automatically applies `ftgo.java-conventions`. Creates:

- `buildDockerImage` task: builds image from `docker/Dockerfile`
- `pushDockerImage` task: pushes image to registry
- Image tagged as `ftgo/<service-name>:<version>`

Requires a `docker/Dockerfile` in the project directory. Tasks are skipped if no Dockerfile exists.

**Usage:**
```groovy
apply plugin: 'ftgo.docker-conventions'
```

### ftgo.publishing-conventions

Configures Maven publishing for shared libraries. Automatically applies `ftgo.java-conventions`. Sets up:

- `maven-publish` plugin
- `mavenJava` publication from Java component
- Local repository at `${rootProject.buildDir}/repo`

**Usage:**
```groovy
apply plugin: 'ftgo.publishing-conventions'
```

## Version Management

### Current Approach (Gradle 4.10.2)

Versions are centralized in two places that must be kept in sync:

1. **`gradle/libs.versions.toml`** - The canonical version reference file
2. **`buildSrc/src/main/groovy/FtgoVersions.groovy`** - Runtime version constants used by plugins

Convention plugins use `FtgoVersions` constants. Service `build.gradle` files reference
dependencies via `FtgoDependencies` constants.

### Future Migration (Gradle 7+)

When Gradle is upgraded to 7.0 or later, the `libs.versions.toml` file becomes the native
version catalog. At that point:

1. Remove `FtgoVersions.groovy`
2. Remove `FtgoDependencies.groovy`
3. Update convention plugins to use `libs.<alias>` syntax
4. Update service `build.gradle` files to use catalog references

### Managed Versions

| Dependency | Version | Constant |
|---|---|---|
| Spring Boot | 2.0.3.RELEASE | `FtgoVersions.SPRING_BOOT` |
| Micrometer | 1.0.4 | `FtgoVersions.MICROMETER` |
| Flyway | 6.0.0 | `FtgoVersions.FLYWAY` |
| Jackson | 2.9.7 | `FtgoVersions.JACKSON` |
| JUnit 5 | 5.2.0 | `FtgoVersions.JUNIT5` |
| Rest-Assured | 3.0.6 | `FtgoVersions.REST_ASSURED` |
| MySQL Connector | 5.1.39 | `FtgoVersions.MYSQL_CONNECTOR` |
| Springfox Swagger | 2.8.0 | `FtgoVersions.SPRINGFOX_SWAGGER` |

## Examples

### Microservice build.gradle

A typical microservice `build.gradle` using convention plugins:

```groovy
// Order Service - Microservice build configuration

apply plugin: 'ftgo.spring-boot-conventions'
apply plugin: 'ftgo.testing-conventions'
apply plugin: 'ftgo.docker-conventions'

dependencies {
    compile project(":shared-libraries:ftgo-common")
    compile project(":shared-libraries:ftgo-domain")

    compile FtgoDependencies.SPRING_BOOT_STARTER_WEB
    compile FtgoDependencies.SPRING_BOOT_STARTER_DATA_JPA
    compile FtgoDependencies.SPRING_BOOT_STARTER_ACTUATOR

    compile FtgoDependencies.MICROMETER_PROMETHEUS
    compile FtgoDependencies.FLYWAY_CORE
}
```

### Shared library build.gradle

A typical shared library `build.gradle`:

```groovy
// Shared Library: ftgo-common

apply plugin: 'ftgo.java-conventions'
apply plugin: 'ftgo.testing-conventions'
apply plugin: 'ftgo.publishing-conventions'

dependencies {
    compile FtgoDependencies.JACKSON_CORE
    compile FtgoDependencies.JACKSON_DATABIND
    compile FtgoDependencies.COMMONS_LANG
}
```

### Creating a New Service

1. Copy `services/service-template/` to `services/<your-service>/`
2. The `build.gradle` already applies convention plugins
3. Uncomment and adjust dependencies for your service
4. Register in `settings.gradle`: `include "services:<your-service>"`
5. Run `./gradlew :services:<your-service>:compileJava` to verify

### Adding a New Version

1. Add the version to `gradle/libs.versions.toml` under `[versions]`
2. Add the corresponding constant to `FtgoVersions.groovy`
3. Optionally add a dependency string to `FtgoDependencies.groovy`
4. Optionally add a library entry to `libs.versions.toml` under `[libraries]`

## Compatibility Notes

- Convention plugins are compatible with Gradle 4.10.2 and Java 8
- Existing monolith modules (`ftgo-*`) are unaffected; they continue using the
  root `build.gradle` `subprojects {}` configuration
- New microservice modules (`services/*`, `shared-libraries/*`) use convention plugins
- Both configurations coexist during the migration period
