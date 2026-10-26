# Java 17 Upgrade Plan — FTGO Monolith

## 1. Executive Summary

The entire FTGO monolith project currently targets **Java 1.8** via the root `build.gradle` (`sourceCompatibility = '1.8'`, `targetCompatibility = '1.8'`). All **17 subprojects** plus the root project and `buildSrc` require upgrading to Java 17. This document catalogs every module that needs changes, the specific blockers in each, and a prioritized execution plan.

Key version constraints:
- **Gradle**: 4.10.2 (must upgrade to 7.6+ minimum; recommend 8.x)
- **Spring Boot**: 2.0.3.RELEASE (must upgrade to 3.x which requires Java 17 minimum)
- **Java source/target**: 1.8 → 17

---

## 2. Build Infrastructure Changes (affects ALL modules)

| Area | Current | Target | Notes |
|------|---------|--------|-------|
| Gradle Wrapper | 4.10.2 | 7.6+ (recommend 8.x) | Minimum for Java 17 support |
| `jcenter()` repository | Present in root `build.gradle` line 28 | Remove, use `mavenCentral()` only | jcenter is deprecated and shut down |
| Dependency configurations | `compile`, `testCompile`, `runtime` | `implementation`, `testImplementation`, `runtimeOnly` | Deprecated since Gradle 3.4, removed in 7.0 |
| Spring Boot | 2.0.3.RELEASE | 3.x (3.0+ requires Java 17) | Major migration: javax→jakarta namespace |
| Spring Dependency Management Plugin | 1.0.3.RELEASE | 1.1.x+ | Required for Spring Boot 3 compatibility |
| `com.github.ben-manes.versions` plugin | 0.20.0 | 0.49+ | Compatibility with Gradle 8.x |
| `sourceCompatibility` / `targetCompatibility` | `'1.8'` | `'17'` | Root `build.gradle` lines 21-22 |

### Gradle Wrapper Upgrade Steps
```bash
# In root project directory:
./gradlew wrapper --gradle-version=8.5
./gradlew wrapper --gradle-version=8.5  # Run twice to update wrapper itself
```

### Root `build.gradle` Changes Required
- Line 21: `sourceCompatibility = '1.8'` → `sourceCompatibility = '17'`
- Line 22: `targetCompatibility = '1.8'` → `targetCompatibility = '17'`
- Line 28: Remove `jcenter()` (already have `mavenCentral()`)
- All `compile` → `implementation`, `testCompile` → `testImplementation`, `runtime` → `runtimeOnly`

### `gradle.properties` Changes Required
```properties
# Current → Target
springBootVersion=2.0.3.RELEASE → springBootVersion=3.2.0
restAssuredVersion=2.9.0 → restAssuredVersion=5.4.0
springDependencyManagementPluginVersion=1.0.3.RELEASE → springDependencyManagementPluginVersion=1.1.4
micrometerVersion=1.0.4 → micrometerVersion=1.12.0
```

---

## 3. Module-by-Module Breakdown

### 3.1 Root Project (`build.gradle`)

| Issue | Location | Fix |
|-------|----------|-----|
| Java 8 source/target | Lines 21-22 | Change to `'17'` |
| Gradle 4.10.2 | `gradle-wrapper.properties` line 6 | Upgrade to 8.x |
| `jcenter()` deprecated | Line 28 | Remove |
| Deprecated configurations | Throughout | Migrate to `implementation`/etc. |
| `com.github.ben-manes.versions` 0.20.0 | Line 14 | Upgrade to 0.49+ |

### 3.2 `buildSrc/build.gradle`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Lines 8-9 | Change to `implementation` |
| `mysql:mysql-connector-java` | Line 8 | Rename to `com.mysql:mysql-connector-j` |
| `commons-lang:commons-lang:2.6` | Line 9 | Consider `org.apache.commons:commons-lang3:3.14+` |

### 3.3 `common-swagger`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Lines 2, 5, 8 | Change to `implementation` |
| `springfox-swagger2:2.8.0` | Lines 2, 5 | Migrate to `springdoc-openapi-starter-webmvc-ui:2.3+` (Spring Boot 3 compatible) |

### 3.4 `ftgo-flyway`

| Issue | Location | Fix |
|-------|----------|-----|
| Flyway plugin 6.0.0 | Line 2 | Upgrade to 9.x+ (Java 17 + MySQL 8 support) |
| No deprecated configs | — | Minimal changes needed |

### 3.5 `ftgo-test-util`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Line 2 | Change to `implementation` |
| `junit:junit:4.12` | Line 2 | Upgrade to JUnit 5 or keep with `junit-vintage-engine` |

### 3.6 `ftgo-common`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile`/`runtime` | Lines 5-20 | Migrate configurations |
| Jackson 2.9.7 | Lines 8-11 | Upgrade to 2.16+ |
| `commons-lang:commons-lang:2.6` | Line 10 | Migrate to `commons-lang3` |
| `javax.xml.bind:jaxb-api:2.2.11` | Line 17 | Replace with `jakarta.xml.bind:jakarta.xml.bind-api:4.0+` |
| `com.sun.xml.bind:jaxb-core/impl:2.2.11` | Lines 18-19 | Replace with `org.glassfish.jaxb:jaxb-runtime:4.0+` |
| `javax.activation:activation:1.1.1` | Line 20 | Replace with `jakarta.activation:jakarta.activation-api:2.1+` |
| `javax.persistence.*` imports | `Money.java`, `Address.java`, `PersonName.java`, `ApiRequestLog.java` | Migrate to `jakarta.persistence.*` |
| `javax.servlet.*` imports | `ApiTrackingInterceptor.java` | Migrate to `jakarta.servlet.*` |

### 3.7 `ftgo-common-jpa`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Lines 3-4 | Change to `implementation` |
| Spring Data JPA 2.x (via Spring Boot 2.0.3) | Line 3 | Handled by Spring Boot 3 upgrade |
| `javax.persistence` (transitive) | Via Spring Data JPA | Automatically migrates with Spring Boot 3 |

### 3.8 `ftgo-domain`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile` | Lines 2-5 | Migrate configurations |
| `junit:junit:4.12` | Line 5 | Upgrade to JUnit 5 or vintage engine |
| `javax.persistence.*` imports in ALL entities | `Order.java`, `Action.java`, `OrderLineItem.java`, `OrderLineItems.java`, `Plan.java`, `Restaurant.java`, `Courier.java`, `MenuItem.java`, `Consumer.java`, `DeliveryInformation.java`, `PaymentInformation.java`, `RestaurantMenu.java` | Migrate to `jakarta.persistence.*` |

**12 Java files** require javax→jakarta changes in this module alone.

### 3.9 `ftgo-order-service`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile` | Lines 37-57 | Migrate configurations |
| `javax.el:javax.el-api:2.2.5` | Line 49 | Replace with `jakarta.el:jakarta.el-api:5.0+` |
| `io.micrometer:micrometer-registry-prometheus:$micrometerVersion` | Line 47 | Updated via `gradle.properties` |
| `io.rest-assured:rest-assured:3.0.6` | Lines 53-55 | Upgrade to 5.4+ |

### 3.10 `ftgo-order-service-api`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Line 2 | Change to `implementation` |

**Minimal change**: 1 line only.

### 3.11 `ftgo-consumer-service-api`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Line 2 | Change to `implementation` |

**Minimal change**: 1 line only.

### 3.12 `ftgo-consumer-service`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile` | Lines 2-14 | Migrate configurations |
| `javax.el:javax.el-api:2.2.5` | Line 10 | Replace with `jakarta.el:jakarta.el-api:5.0+` |
| `com.jayway.restassured:rest-assured:$restAssuredVersion` | Line 13 | Replace with `io.rest-assured:rest-assured:5.4+` |

### 3.13 `ftgo-restaurant-service-api`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Line 2 | Change to `implementation` |
| `javax.persistence.*` imports | `MenuItemDTO.java` | Migrate to `jakarta.persistence.*` |

### 3.14 `ftgo-restaurant-service`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile` | Lines 2-14 | Migrate configurations |
| `javax.el:javax.el-api:2.2.5` | Line 10 | Replace with `jakarta.el:jakarta.el-api:5.0+` |
| `com.jayway.restassured:rest-assured:$restAssuredVersion` | Line 13 | Replace with `io.rest-assured:rest-assured:5.4+` |

### 3.15 `ftgo-courier-service-api`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Line 2 | Change to `implementation` |

**Minimal change**: 1 line only.

### 3.16 `ftgo-courier-service`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`testCompile` | Lines 2-19 | Migrate configurations |
| `javax.el:javax.el-api:2.2.5` | Line 11 | Replace with `jakarta.el:jakarta.el-api:5.0+` |
| `io.rest-assured:rest-assured:3.0.6` | Lines 15-17 | Upgrade to 5.4+ |

### 3.17 `ftgo-application`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile`/`runtime`/`testCompile` | Lines 4-14 | Migrate configurations |
| `FtgoServicePlugin` usage | Line 1 | Verify plugin compatibility with Gradle 8 |
| `mysql:mysql-connector-java:8.0.33` | Line 12 | Rename to `com.mysql:mysql-connector-j:8.0.33+` |
| `javax.servlet.*` import | `GlobalExceptionHandler.java` | Migrate to `jakarta.servlet.*` |

### 3.18 `ftgo-end-to-end-tests-common`

| Issue | Location | Fix |
|-------|----------|-----|
| `compile` configuration | Lines 2-11 | Change to `implementation` |
| `com.jayway.restassured:rest-assured:$restAssuredVersion` | Line 10 | Replace with `io.rest-assured:rest-assured:5.4+` |
| `eventuate-util-test:$eventuateUtilVersion` | Line 6 | Verify compatibility or find replacement |

### 3.19 `ftgo-end-to-end-tests`

| Issue | Location | Fix |
|-------|----------|-----|
| `testCompile` configuration | Line 2 | Change to `testImplementation` |

---

## 4. javax → jakarta Migration

Spring Boot 3.x requires the Jakarta EE 9+ namespace. All `javax.*` imports must migrate to `jakarta.*`.

### Files Requiring Migration

#### `ftgo-common` (4 files)
| File | Imports |
|------|---------|
| `Money.java` | `javax.persistence.Access`, `AccessType`, `Embeddable` |
| `Address.java` | `javax.persistence.Access`, `AccessType`, `Embeddable` |
| `PersonName.java` | `javax.persistence.Embeddable` |
| `ApiRequestLog.java` | `javax.persistence.*` |
| `ApiTrackingInterceptor.java` | `javax.servlet.http.HttpServletRequest`, `HttpServletResponse` |

#### `ftgo-domain` (12 files)
| File | Imports |
|------|---------|
| `Order.java` | `javax.persistence.*` |
| `Action.java` | `javax.persistence.Embeddable`, `EnumType`, `Enumerated`, `ManyToOne` |
| `OrderLineItem.java` | `javax.persistence.*` |
| `OrderLineItems.java` | `javax.persistence.CollectionTable`, `ElementCollection`, `Embeddable` |
| `Plan.java` | `javax.persistence.ElementCollection` |
| `Restaurant.java` | `javax.persistence.*` |
| `Courier.java` | `javax.persistence.*` |
| `MenuItem.java` | `javax.persistence.*` |
| `Consumer.java` | `javax.persistence.*` |
| `DeliveryInformation.java` | `javax.persistence.*` |
| `PaymentInformation.java` | `javax.persistence.Access`, `AccessType` |
| `RestaurantMenu.java` | `javax.persistence.*` |

#### `ftgo-restaurant-service-api` (1 file)
| File | Imports |
|------|---------|
| `MenuItemDTO.java` | `javax.persistence.Access`, `AccessType`, `Embeddable` |

#### `ftgo-application` (1 file)
| File | Imports |
|------|---------|
| `GlobalExceptionHandler.java` | `javax.servlet.http.HttpServletRequest` |

**Total: 18 Java files** require javax→jakarta namespace migration.

### Migration Command (find & replace)
```bash
find . -name "*.java" -exec sed -i 's/import javax\.persistence/import jakarta.persistence/g' {} +
find . -name "*.java" -exec sed -i 's/import javax\.servlet/import jakarta.servlet/g' {} +
find . -name "*.java" -exec sed -i 's/import javax\.validation/import jakarta.validation/g' {} +
find . -name "*.java" -exec sed -i 's/import javax\.el/import jakarta.el/g' {} +
```

---

## 5. Dependency Upgrade Table

| Dependency | Current Version | Target Version | Reason |
|-----------|----------------|----------------|--------|
| Spring Boot | 2.0.3.RELEASE | 3.2.0+ | Java 17 requirement, jakarta namespace |
| Spring Dependency Management Plugin | 1.0.3.RELEASE | 1.1.4+ | Spring Boot 3 compatibility |
| Gradle | 4.10.2 | 8.5+ | Java 17 support, modern plugin system |
| Jackson | 2.9.7 | 2.16+ | Security fixes, Java 17 compatibility |
| rest-assured (`com.jayway`) | 2.9.0 | 5.4+ (`io.rest-assured`) | Group ID changed, old unsupported |
| rest-assured (`io.rest-assured`) | 3.0.6 | 5.4+ | Java 17 + Spring Boot 3 compatibility |
| JUnit | 4.12 | 5.10+ (or vintage engine) | Modern testing APIs |
| mysql-connector-java | 8.0.33 | `com.mysql:mysql-connector-j:8.3+` | Artifact renamed by MySQL team |
| javax.el-api | 2.2.5 | `jakarta.el:jakarta.el-api:5.0+` | Jakarta namespace migration |
| javax.xml.bind (jaxb-api) | 2.2.11 | `jakarta.xml.bind:jakarta.xml.bind-api:4.0+` | Jakarta namespace migration |
| javax.activation | 1.1.1 | `jakarta.activation:jakarta.activation-api:2.1+` | Jakarta namespace migration |
| Micrometer | 1.0.4 | 1.12+ | Spring Boot 3 compatibility |
| Springfox Swagger | 2.8.0 | `springdoc-openapi-starter-webmvc-ui:2.3+` | Springfox dead; springdoc is successor |
| Flyway | 6.0.0 | 9.22+ | Java 17 + MySQL 8 support |
| commons-lang | 2.6 | `org.apache.commons:commons-lang3:3.14+` | lang2 is EOL |
| Eventuate Util | 0.1.0.RELEASE | Verify latest | Check compatibility |

---

## 6. Docker/Deployment Changes

### `docker-compose.yml`
- **Line 21**: `SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.jdbc.Driver` → `com.mysql.cj.jdbc.Driver`
  - The old driver class is deprecated and removed in newer MySQL Connector/J versions

### Dockerfile (in `ftgo-application/`)
- Update base image from Java 8 to Java 17:
  ```dockerfile
  # From:
  FROM openjdk:8-jre-alpine
  # To:
  FROM eclipse-temurin:17-jre-alpine
  ```

### MySQL Configuration
- Verify MySQL 8 compatibility with Java 17 driver
- Ensure `useSSL=false` parameter in JDBC URLs is updated to `sslMode=DISABLED` for MySQL Connector/J 8.2+

---

## 7. Prioritization (lowest to highest effort)

### Tier 1 — Lowest Effort (1 line change each)
| Module | Change Required |
|--------|----------------|
| `ftgo-order-service-api` | `compile` → `implementation` |
| `ftgo-consumer-service-api` | `compile` → `implementation` |
| `ftgo-courier-service-api` | `compile` → `implementation` |

### Tier 2 — Low Effort (config only, no source changes)
| Module | Changes |
|--------|---------|
| `ftgo-test-util` | `compile` → `implementation`, JUnit 4→5 |
| `common-swagger` | `compile` → `implementation`, springfox → springdoc |
| `ftgo-flyway` | Flyway plugin version bump |
| `ftgo-end-to-end-tests-common` | `compile` → `implementation`, rest-assured upgrade |
| `ftgo-end-to-end-tests` | `testCompile` → `testImplementation` |

### Tier 3 — Medium Effort (config + limited source changes)
| Module | Changes |
|--------|---------|
| `ftgo-common` | Dependency upgrades + 5 Java files javax→jakarta |
| `ftgo-common-jpa` | Config migration (source auto-handled by Spring Boot 3) |
| `ftgo-application` | Config migration + 1 Java file javax→jakarta + Dockerfile |

### Tier 4 — Highest Effort (extensive javax→jakarta source changes)
| Module | Changes |
|--------|---------|
| `ftgo-domain` | 12 Java files javax→jakarta + JUnit migration |
| `ftgo-order-service` | Config + javax.el + persistence changes |
| `ftgo-consumer-service` | Config + javax.el + persistence changes |
| `ftgo-restaurant-service` | Config + javax.el + persistence changes |
| `ftgo-restaurant-service-api` | 1 Java file javax→jakarta |
| `ftgo-courier-service` | Config + javax.el + persistence changes |

### Build Infrastructure (must be done FIRST)
| Component | Changes |
|-----------|---------|
| `gradle-wrapper.properties` | Gradle 4.10.2 → 8.5 |
| `gradle.properties` | All version bumps |
| Root `build.gradle` | java 17, remove jcenter, update plugin |
| `buildSrc/build.gradle` | `compile` → `implementation`, mysql artifact rename |

---

## 8. Execution Order (dependency-aware)

```
Phase 1 (Sequential - MUST go first):
  └─ Build Infrastructure (root build.gradle, gradle wrapper, gradle.properties, buildSrc)

Phase 2 (Parallel - no inter-dependencies):
  ├─ ftgo-order-service-api (Tier 1)
  ├─ ftgo-consumer-service-api (Tier 1)
  ├─ ftgo-courier-service-api (Tier 1)
  ├─ ftgo-test-util (Tier 2)
  ├─ ftgo-flyway (Tier 2)
  └─ ftgo-end-to-end-tests (Tier 2)

Phase 3 (Parallel - depend on ftgo-common being ready):
  ├─ ftgo-common (Tier 3) ─── MUST complete before Phase 4
  ├─ common-swagger (Tier 2)
  └─ ftgo-end-to-end-tests-common (Tier 2)

Phase 4 (Parallel - depend on ftgo-common + ftgo-common-jpa):
  ├─ ftgo-common-jpa (Tier 3)
  └─ ftgo-restaurant-service-api (Tier 4)

Phase 5 (Parallel - depend on ftgo-domain):
  └─ ftgo-domain (Tier 4) ─── MUST complete before Phase 6

Phase 6 (Parallel - depend on ftgo-domain + service APIs):
  ├─ ftgo-order-service (Tier 4)
  ├─ ftgo-consumer-service (Tier 4)
  ├─ ftgo-restaurant-service (Tier 4)
  └─ ftgo-courier-service (Tier 4)

Phase 7 (Sequential - aggregates all):
  └─ ftgo-application (Tier 3)

Phase 8 (Final):
  └─ Docker/deployment changes
```

---

## 9. Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Spring Boot 2→3 is a major version jump | High | Follow official migration guide, test incrementally |
| Eventuate library may not support Spring Boot 3 | Medium | Check for updated version or fork/replace |
| Springfox is abandoned | Low | Replace with springdoc-openapi (well-documented migration) |
| Test breakage from JUnit 4→5 | Medium | Use vintage engine initially, migrate tests later |
| jcenter dependencies no longer available | Low | All should be on mavenCentral already |
| Flyway migration script compatibility | Low | SQL scripts are DB-specific, not Java-version-specific |

---

## 10. Verification Checklist

- [ ] All `build.gradle` files use `implementation`/`testImplementation`/`runtimeOnly`
- [ ] No `javax.*` imports remain in any `.java` file (except `javax.sql` which is in the JDK)
- [ ] `./gradlew build` succeeds with Java 17
- [ ] `./gradlew test` passes
- [ ] Docker Compose starts successfully
- [ ] Application starts and responds to health checks
- [ ] Flyway migrations run without errors
- [ ] Swagger/OpenAPI UI accessible
- [ ] End-to-end tests pass
