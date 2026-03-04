# Shared Gradle Configuration for FTGO Microservices

## Overview

This document describes the shared Gradle build configuration established for the FTGO microservices migration. It covers the version catalog, convention plugins, and how new services consume the shared configuration.

## Architecture

```
ftgo-monolith/
├── gradle/
│   └── libs.versions.toml          # Centralized version catalog
├── buildSrc/
│   ├── build.gradle                 # buildSrc dependencies
│   └── src/main/
│       ├── groovy/
│       │   ├── ftgo/conventions/    # Convention plugins (NEW)
│       │   │   ├── FtgoVersions.groovy
│       │   │   ├── FtgoJavaConventionsPlugin.groovy
│       │   │   ├── FtgoSpringBootConventionsPlugin.groovy
│       │   │   ├── FtgoTestingConventionsPlugin.groovy
│       │   │   ├── FtgoDockerConventionsPlugin.groovy
│       │   │   └── FtgoPublishingConventionsPlugin.groovy
│       │   ├── WaitForMySqlPlugin.java       # Legacy plugin (unchanged)
│       │   ├── WaitForMySql.java             # Legacy plugin (unchanged)
│       │   ├── IntegrationTestsPlugin.groovy # Legacy plugin (unchanged)
│       │   └── FtgoServicePlugin.groovy      # Legacy plugin (unchanged)
│       └── resources/META-INF/gradle-plugins/
│           ├── ftgo.java-conventions.properties
│           ├── ftgo.spring-boot-conventions.properties
│           ├── ftgo.testing-conventions.properties
│           ├── ftgo.docker-conventions.properties
│           └── ftgo.publishing-conventions.properties
├── gradle.properties                # Shared version properties
├── build.gradle                     # Root build file
└── settings.gradle                  # Module includes + version catalog config
```

## Version Catalog

### File: `gradle/libs.versions.toml`

The version catalog centralizes all dependency versions for the new microservices. Key versions:

| Dependency | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.5 | Application framework |
| Java | 17 | Language target |
| Micrometer | 1.12.5 | Metrics and observability |
| JUnit Jupiter | 5.10.2 | Unit and integration testing |
| Rest-Assured | 5.4.0 | REST API testing |
| Flyway | 10.11.0 | Database migrations |
| Jackson | 2.17.0 | JSON serialization |
| Testcontainers | 1.19.7 | Integration test containers |
| Mockito | 5.11.0 | Mocking framework |
| AssertJ | 3.25.3 | Fluent assertions |

### Current Usage

The version catalog is currently consumed via the `FtgoVersions` constants class in `buildSrc/src/main/groovy/ftgo/conventions/FtgoVersions.groovy`. This class mirrors the versions defined in `libs.versions.toml`.

When the project upgrades to Gradle 7.5+, the TOML file will be natively consumed by Gradle's version catalog feature (see [Upgrade Path](#gradle-upgrade-path) below).

## Convention Plugins

### `ftgo.java-conventions`

Base Java configuration for all microservice modules.

**What it configures:**
- Java 17 source and target compatibility
- UTF-8 encoding for all source files
- Compiler arguments: `-parameters`, `-Xlint:unchecked`, `-Xlint:deprecation`
- Maven Central repository
- SLF4J logging API
- Lombok (compile-only + annotation processor)

**Usage:**
```groovy
// In a module's build.gradle
apply plugin: 'ftgo.java-conventions'
```

### `ftgo.spring-boot-conventions`

Spring Boot 3.x dependency management. Automatically applies `ftgo.java-conventions`.

**What it configures:**
- Spring Boot Web, Data JPA, Actuator, Validation starters
- Micrometer Prometheus registry
- MySQL connector and Flyway migrations
- Jackson JSON serialization
- SpringDoc OpenAPI documentation

**Usage:**
```groovy
// In a service module's build.gradle
apply plugin: 'ftgo.spring-boot-conventions'

// Additional service-specific dependencies
dependencies {
    implementation project(':shared-ftgo-common')
    implementation project(':shared-ftgo-domain')
}
```

### `ftgo.testing-conventions`

Test framework configuration with JUnit 5.

**What it configures:**
- JUnit 5 (Jupiter) test engine
- Spring Boot Test starter
- Rest-Assured for API testing
- Mockito for mocking
- AssertJ for fluent assertions
- Test logging with PASSED/SKIPPED/FAILED events
- Integration test source set (`src/integration-test/java`)
- `integrationTest` Gradle task

**Usage:**
```groovy
// In a module's build.gradle
apply plugin: 'ftgo.testing-conventions'

// Tests automatically use JUnit 5 platform
// Integration tests go in src/integration-test/java/
// Run with: ./gradlew integrationTest
```

### `ftgo.docker-conventions`

Docker image build configuration.

**What it configures:**
- Standard Docker image naming: `{registry}/{project.name}:{version}`
- `dockerBuild` task (uses Dockerfile if present)
- `dockerPush` task
- Configurable registry via `dockerRegistry` project property
- Configurable tag via `dockerTag` project property

**Usage:**
```groovy
// In a service module's build.gradle
apply plugin: 'ftgo.docker-conventions'

// Build: ./gradlew :services-ftgo-order-service:dockerBuild
// Push:  ./gradlew :services-ftgo-order-service:dockerPush
// Custom registry: ./gradlew dockerBuild -PdockerRegistry=myregistry.io/ftgo
```

### `ftgo.publishing-conventions`

Maven publishing for shared library modules.

**What it configures:**
- `maven-publish` plugin
- Standard Maven publication from Java component
- POM metadata (name, description, license)
- Local repository target (`build/repo/`)
- Remote repository placeholder (configure via properties)

**Usage:**
```groovy
// In a shared library's build.gradle
apply plugin: 'ftgo.publishing-conventions'

// Publish locally: ./gradlew :shared-ftgo-common:publishToMavenLocal
// Publish to build/repo: ./gradlew :shared-ftgo-common:publish
```

## Example: Complete Service Module

Here is a complete example of a microservice module's `build.gradle` using the convention plugins:

```groovy
// services/ftgo-order-service/build.gradle (microservices version)

apply plugin: 'ftgo.spring-boot-conventions'
apply plugin: 'ftgo.testing-conventions'
apply plugin: 'ftgo.docker-conventions'

dependencies {
    // Shared libraries
    implementation project(':shared-ftgo-common')
    implementation project(':shared-ftgo-domain')

    // Service-specific dependencies
    implementation project(':shared-ftgo-common-jpa')
}
```

## Example: Shared Library Module

```groovy
// shared/ftgo-common/build.gradle

apply plugin: 'ftgo.java-conventions'
apply plugin: 'ftgo.testing-conventions'
apply plugin: 'ftgo.publishing-conventions'

dependencies {
    implementation "com.fasterxml.jackson.core:jackson-core:${ftgo.conventions.FtgoVersions.JACKSON}"
    implementation "com.fasterxml.jackson.core:jackson-databind:${ftgo.conventions.FtgoVersions.JACKSON}"
    implementation "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${ftgo.conventions.FtgoVersions.JACKSON}"
    implementation "org.apache.commons:commons-lang3:${ftgo.conventions.FtgoVersions.COMMONS_LANG3}"
}
```

## Plugin Composition

The convention plugins are designed to be composed:

| Module Type | Plugins to Apply |
|---|---|
| Microservice (under `services/`) | `ftgo.spring-boot-conventions` + `ftgo.testing-conventions` + `ftgo.docker-conventions` |
| Shared Library (under `shared/`) | `ftgo.java-conventions` + `ftgo.testing-conventions` + `ftgo.publishing-conventions` |
| Shared Library with JPA | Add `ftgo.spring-boot-conventions` instead of `ftgo.java-conventions` |

Note: `ftgo.spring-boot-conventions` automatically applies `ftgo.java-conventions`, so you do not need to apply both.

## Coexistence with Legacy Modules

The convention plugins are designed exclusively for new microservice modules under `services/` and `shared/`. They do **not** affect legacy monolith modules.

- Legacy modules continue to use `compile`/`testCompile` configurations with Spring Boot 2.0.3
- New modules use `implementation`/`testImplementation` configurations with Spring Boot 3.2.5
- Both module types coexist in the same Gradle build

## Gradle Upgrade Path

The current Gradle wrapper is 4.10.2. The version catalog TOML file (`gradle/libs.versions.toml`) is ready for native Gradle version catalog support, which requires Gradle 7.5+.

### Steps to Upgrade

1. **Upgrade Gradle wrapper** to 7.6+ or 8.x:
   ```bash
   ./gradlew wrapper --gradle-version=8.7
   ```

2. **Enable version catalog** in `settings.gradle` by uncommenting:
   ```groovy
   dependencyResolutionManagement {
       versionCatalogs {
           libs {
               from(files('gradle/libs.versions.toml'))
           }
       }
   }
   ```

3. **Migrate legacy configurations** (`compile` -> `implementation`) in all legacy module build files.

4. **Update Spring Boot plugin** version for legacy modules to 2.7.x+ (Gradle 7 compatible).

5. **Refactor convention plugins** to use native version catalog references:
   ```groovy
   // Before (FtgoVersions constants):
   implementation "org.springframework.boot:spring-boot-starter-web:${FtgoVersions.SPRING_BOOT}"
   
   // After (native version catalog):
   implementation libs.spring.boot.starter.web
   ```

6. **Remove `FtgoVersions.groovy`** once all plugins use native catalog references.

### Prerequisites for Gradle Upgrade
- Java 11+ for Gradle 8.x (Java 8 sufficient for Gradle 7.x)
- Spring Boot Gradle plugin 2.7+ for legacy modules (compatible with Gradle 7)
- All `compile`/`testCompile` usages migrated to `implementation`/`testImplementation`

## Adding a New Convention Plugin

1. Create a Groovy class in `buildSrc/src/main/groovy/ftgo/conventions/` implementing `Plugin<Project>`
2. Add a registration file in `buildSrc/src/main/resources/META-INF/gradle-plugins/`:
   ```properties
   # ftgo.my-new-conventions.properties
   implementation-class=ftgo.conventions.FtgoMyNewConventionsPlugin
   ```
3. Add any new version constants to `FtgoVersions.groovy` and `gradle/libs.versions.toml`
4. Update this documentation

## Related Files

- `gradle/libs.versions.toml` — Version catalog
- `buildSrc/src/main/groovy/ftgo/conventions/` — Convention plugin source
- `buildSrc/src/main/resources/META-INF/gradle-plugins/` — Plugin registrations
- `gradle.properties` — Version properties (legacy + new microservice prefixed with `ms`)
- `build.gradle` — Root build configuration
- `settings.gradle` — Module includes and version catalog configuration
