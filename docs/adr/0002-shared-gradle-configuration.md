# ADR-0002: Shared Gradle Configuration for Microservices

## Status

Accepted

## Date

2026-03-03

## Context

As part of the microservices migration (EM-28), each extracted service needs a
standardized build configuration. The legacy monolith uses a single root
`build.gradle` with Gradle 4.10.2, Java 8, and Spring Boot 2.0.3. New
microservices target Java 17+ and Spring Boot 3.x.

Rather than duplicating build logic in every service, we introduce a **shared
build-logic** project containing Gradle convention plugins and a **version
catalog** (`gradle/libs.versions.toml`) that centralizes all dependency versions.

## Decision

### 1. Gradle Version Catalog

All dependency versions for new microservices are declared in
`gradle/libs.versions.toml`. This file is the single source of truth for:

| Category        | Key Libraries                                      |
|-----------------|----------------------------------------------------|
| Platform        | Java 17                                            |
| Spring Boot     | 3.2.5, Spring Dependency Management 1.1.5          |
| Database        | Flyway 10.11.0, MySQL Connector 8.3.0              |
| Observability   | Micrometer 1.12.5, Micrometer Tracing 1.2.5        |
| Serialization   | Jackson 2.17.0                                     |
| API Docs        | SpringDoc OpenAPI 2.4.0                            |
| Testing         | JUnit 5.10.2, REST-Assured 5.4.0, Mockito 5.11.0  |
| Containers      | Testcontainers 1.19.7                              |
| Utilities       | Commons Lang3 3.14.0, Lombok 1.18.32, MapStruct 1.5.5 |

### 2. Convention Plugins (build-logic/)

Five convention plugins standardize build configuration:

| Plugin                         | Purpose                                           |
|--------------------------------|---------------------------------------------------|
| `ftgo.java-conventions`        | Java 17 toolchain, UTF-8, group, repositories     |
| `ftgo.spring-boot-conventions` | Spring Boot 3.x, actuator, web, observability     |
| `ftgo.testing-conventions`     | JUnit 5, Mockito, integration test source set     |
| `ftgo.docker-conventions`      | Docker image build and push tasks                 |
| `ftgo.publishing-conventions`  | Maven publishing for shared libraries             |

### 3. Build-Logic as Standalone Build

The `build-logic/` directory is a self-contained Gradle project with its own
Gradle 8.7 wrapper. It produces precompiled script plugins using the
`groovy-gradle-plugin`.

**Why standalone?** The root project currently uses Gradle 4.10.2, which does
not support version catalogs or the `groovy-gradle-plugin`. The build-logic
will be integrated via `includeBuild('build-logic')` in `settings.gradle` after
the root Gradle wrapper is upgraded to 7.0+ (recommended: 8.7).

### 4. Coexistence with Legacy Build

- Legacy modules at the repo root continue to use `gradle.properties` and
  `buildSrc/` for version management and custom plugins.
- New modules under `services/` and `shared/` have their convention plugin
  references commented out, ready to be activated after the root Gradle upgrade.
- No existing build files, source code, or configurations are modified.

## How to Use

### Verifying Convention Plugins

```bash
cd build-logic
JAVA_HOME=/path/to/java17 ./gradlew build
```

### Applying Plugins to a New Microservice

After the root Gradle is upgraded to 8.7+ and `includeBuild('build-logic')` is
enabled in `settings.gradle`:

**For a Spring Boot service** (e.g., `services/ftgo-order-service/build.gradle`):

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

dependencies {
    // Only service-specific dependencies needed
    implementation project(':shared:ftgo-domain')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Database
    runtimeOnly libs.mysql.connector
    implementation libs.flyway.core
    implementation libs.flyway.mysql

    // REST API testing
    testImplementation libs.rest.assured
    testImplementation libs.rest.assured.spring.mock.mvc
}
```

**For a shared library** (e.g., `shared/ftgo-common/build.gradle`):

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.publishing-conventions'
}

dependencies {
    implementation libs.jackson.databind
    implementation libs.jackson.datatype.jsr310
    implementation libs.commons.lang3
}
```

### Using Version Catalog References

In any `build.gradle` that has access to the version catalog:

```groovy
dependencies {
    // Single library
    implementation libs.spring.boot.starter.web

    // Bundle (multiple related libraries)
    implementation libs.bundles.spring.boot.web
    testImplementation libs.bundles.testing
}
```

### What Each Convention Plugin Provides

#### ftgo.java-conventions
- Java 17 source/target compatibility
- UTF-8 encoding for compilation and Javadoc
- `-parameters` compiler flag for Spring parameter name discovery
- `com.ftgo` group
- Maven Central repository

#### ftgo.spring-boot-conventions
- Everything from `ftgo.java-conventions`
- Spring Boot plugin with dependency management
- Spring Boot Web, Actuator, and Validation starters
- Micrometer Prometheus metrics
- SpringDoc OpenAPI documentation
- Boot JAR packaging (fat JAR)

#### ftgo.testing-conventions
- Everything from `ftgo.java-conventions`
- JUnit 5 platform configuration
- Mockito and AssertJ test dependencies
- Integration test source set (`src/integration-test/java`)
- `integrationTest` task wired into the `check` lifecycle

#### ftgo.docker-conventions
- `dockerBuild` task: builds Docker image with standardized naming
- `dockerPush` task: pushes image to configured registry
- Configurable via `dockerRegistry` and `dockerImageTag` properties

#### ftgo.publishing-conventions
- Everything from `ftgo.java-conventions`
- Maven publication with source and Javadoc JARs
- Local repository publishing (`build/repo`)
- POM metadata with MIT license

## Migration Steps

To fully activate the shared build configuration:

1. **Upgrade root Gradle wrapper** to 8.7:
   ```bash
   ./gradlew wrapper --gradle-version 8.7
   ```

2. **Enable includeBuild** in `settings.gradle`:
   ```groovy
   includeBuild('build-logic')
   ```

3. **Enable version catalog** in `settings.gradle`:
   ```groovy
   dependencyResolutionManagement {
       versionCatalogs {
           libs {
               from(files('gradle/libs.versions.toml'))
           }
       }
   }
   ```

4. **Uncomment plugin applications** in each service and shared library
   `build.gradle` file.

5. **Migrate legacy modules** from `buildSrc` plugins to convention plugins
   as each service is extracted.

## Consequences

### Positive
- Single source of truth for dependency versions across all microservices
- New services require < 30 lines of build configuration (only service-specific deps)
- Consistent Java 17, Spring Boot 3.x, and JUnit 5 across all new services
- Convention plugins enforce organizational standards automatically
- Version catalog provides IDE auto-completion for dependency references

### Negative
- Two build systems coexist during migration (legacy buildSrc + new build-logic)
- Convention plugins cannot be activated until root Gradle is upgraded
- Developers must keep `build-logic/build.gradle` versions in sync with
  `gradle/libs.versions.toml` manually

### Risks
- Root Gradle upgrade may introduce breaking changes to legacy modules
- Spring Boot 2.x to 3.x migration requires javax -> jakarta namespace changes

## Related

- [ADR-0001: Mono-Repo Structure and Naming Conventions](./0001-mono-repo-structure-and-naming-conventions.md)
- [Gradle Version Catalog](../../gradle/libs.versions.toml)
- [Build Logic Convention Plugins](../../build-logic/)
