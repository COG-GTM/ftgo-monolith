# FTGO Dependency Audit: Monolith to Microservices

## Overview

This document maps all current dependencies in the FTGO monolith to their recommended state in the microservices architecture. Each dependency is categorized with one of the following actions:

| Action     | Description                           |
|------------|---------------------------------------|
| UPGRADE    | Update to newer version               |
| REPLACE    | Replace with alternative library      |
| REFACTOR   | Encapsulate in shared library         |
| REMOVE     | No longer needed                      |
| KEEP       | Maintain as-is                        |

---

## 1. Core Framework Dependencies

### Spring Boot
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `springBootVersion` | `2.0.3.RELEASE` | `3.2.1` | **UPGRADE** |

**Migration Notes:**
- Requires Java 17+ (current: Java 8)
- `javax.*` packages migrated to `jakarta.*`
- Spring Security configuration API completely changed (lambda DSL)
- `spring-boot-starter-data-jpa` now uses Hibernate 6.x
- Auto-configuration class names moved to `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- `@ConstructorBinding` no longer required when single constructor exists

### Spring Dependency Management Plugin
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `springDependencyManagementPluginVersion` | `1.0.3.RELEASE` | `1.1.4` | **UPGRADE** |

---

## 2. Database Dependencies

### MySQL JDBC Driver
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| Driver class | `com.mysql.jdbc.Driver` | `com.mysql.cj.jdbc.Driver` | **UPGRADE** |
| Artifact | `mysql:mysql-connector-java:5.1.39` | `com.mysql:mysql-connector-j:8.2.0` | **UPGRADE** |

**Migration Notes:**
- Maven artifact coordinates changed from `mysql:mysql-connector-java` to `com.mysql:mysql-connector-j`
- Driver class name changed; Spring Boot 3.x auto-detects the driver
- Connection URL should include `allowPublicKeyRetrieval=true` for MySQL 8.x

### Spring Data JPA
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `spring-boot-starter-data-jpa` | `2.0.3.RELEASE` | `3.2.1` | **UPGRADE** |

**Migration Notes:**
- Hibernate upgraded from 5.x to 6.x
- `javax.persistence` -> `jakarta.persistence`
- ID generation strategy changes may affect existing sequences

### Flyway
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `org.flywaydb.flyway` plugin | `6.0.0` | `9.22.3` | **UPGRADE** |

**Migration Notes:**
- Flyway 9.x requires separate `flyway-mysql` module
- Each microservice gets its own migration scripts (database-per-service pattern)
- Baseline migration required for existing databases

### Database Credentials
| Item | Current | Target | Action |
|------|---------|--------|--------|
| Credentials | Hardcoded (`mysqluser`/`mysqlpw`) | Environment variables | **REFACTOR** |

**Migration Notes:**
- All credentials externalized via environment variables
- Production should use Kubernetes Secrets or HashiCorp Vault
- Each service gets its own database and credentials

---

## 3. Metrics & Monitoring Dependencies

### Micrometer
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `micrometerVersion` | `1.0.4` | `1.12.1` | **UPGRADE** + **REFACTOR** |

**Migration Notes:**
- Refactored into `ftgo-observability-lib` shared library
- Current `Optional<MeterRegistry>` pattern standardized to always-present `MeterRegistry`
- `MeterRegistryCustomizer` pattern preserved with common tags (service name, environment)

### Spring Boot Actuator
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `spring-boot-starter-actuator` | `2.0.3.RELEASE` | `3.2.1` | **KEEP** (upgrade with Spring Boot) |

**Migration Notes:**
- Keep in all services for health checks and metrics endpoints
- Endpoint exposure configuration moved to `management.endpoints.web.exposure.include`

---

## 4. Serialization Dependencies

### Jackson
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `jackson-core` | `2.9.7` | `2.15.3` | **UPGRADE** |
| `jackson-databind` | `2.9.7` | `2.15.3` | **UPGRADE** |
| `jackson-datatype-jsr310` | `2.9.7` | `2.15.3` | **UPGRADE** |

**Migration Notes:**
- Managed by Spring Boot BOM; explicit version declarations can be removed
- Used in `ftgo-common-lib` and `ftgo-messaging-lib`

---

## 5. Utility Dependencies

### Apache Commons Lang
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `commons-lang:commons-lang:2.6` | `2.6` | Removed | **REMOVE** |

**Migration Notes:**
- Used in `Money.java` for `EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder`
- Replaced with `java.util.Objects` in the modernized `ftgo-common-lib`
- No external dependency needed for these utilities in Java 17+

### JAXB
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `javax.xml.bind:jaxb-api:2.2.11` | `2.2.11` | Removed | **REMOVE** |
| `com.sun.xml.bind:jaxb-core:2.2.11` | `2.2.11` | Removed | **REMOVE** |
| `com.sun.xml.bind:jaxb-impl:2.2.11` | `2.2.11` | Removed | **REMOVE** |
| `javax.activation:activation:1.1.1` | `1.1.1` | Removed | **REMOVE** |

**Migration Notes:**
- These were workarounds for Java 9+ module system when running on Java 8
- Java 17 with Jakarta EE handles these natively; no longer needed

### Java EL API
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `javax.el:javax.el-api:2.2.5` | `2.2.5` | Removed | **REMOVE** |

**Migration Notes:**
- Was needed for Bean Validation in Spring Boot 2.x
- Spring Boot 3.x includes `jakarta.el` transitively via `spring-boot-starter-validation`

---

## 6. Testing Dependencies

### REST Assured
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `restAssuredVersion` | `2.9.0` | `5.4.0` | **UPGRADE** |

**Migration Notes:**
- Package changed from `com.jayway.restassured` to `io.rest-assured`
- `rest-assured` and `spring-mock-mvc` modules still available
- Current mix of `com.jayway.restassured:2.9.0` and `io.rest-assured:3.0.6` should be unified to `5.4.0`

### Spring Boot Test
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `spring-boot-starter-test` | `2.0.3.RELEASE` | `3.2.1` | **UPGRADE** |

**Migration Notes:**
- JUnit 5 is now the default (JUnit 4 was default in 2.x)
- `@ExtendWith(SpringExtension.class)` replaces `@RunWith(SpringRunner.class)`

### JUnit
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| JUnit (implicit via spring-boot-starter-test) | JUnit 4/5 | JUnit 5.10.1 | **UPGRADE** |

---

## 7. API Documentation Dependencies

### Swagger
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `common-swagger` (custom module) | Custom | `springdoc-openapi-starter-webmvc-ui:2.3.0` | **REPLACE** |

**Migration Notes:**
- Current custom `CommonSwaggerConfiguration` replaced with SpringDoc
- SpringDoc provides auto-generated OpenAPI 3.0 documentation
- Each microservice gets its own Swagger UI at `/swagger-ui.html`

---

## 8. Eventuate Dependencies

### Eventuate Util
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `eventuateUtilVersion` | `0.1.0.RELEASE` | Removed | **REMOVE** |

**Migration Notes:**
- Eventuate libraries were referenced but repositories are no longer available
- Event-driven communication replaced with `ftgo-messaging-lib` (Spring Kafka)
- Saga pattern implementation can use Spring State Machine or custom implementation

---

## 9. Build Tool Dependencies

### Gradle Docker Compose Plugin
| Property | Current | Target | Action |
|----------|---------|--------|--------|
| `dockerComposePluginVersion` | `0.6.6` | Removed | **REMOVE** |

**Migration Notes:**
- Docker Compose for tests handled via GitHub Actions services and Testcontainers
- `com.avast.gradle:gradle-docker-compose-plugin:0.14.13` was also present but commented out

### Gradle Versions Plugin
| Module | Current | Target | Action |
|--------|---------|--------|--------|
| `com.github.ben-manes.versions` | `0.20.0` | `0.50.0` | **UPGRADE** |

---

## 10. New Dependencies (Not in Monolith)

These dependencies are required for the microservices architecture but were absent from the monolith:

| Dependency | Version | Purpose | Library |
|------------|---------|---------|---------|
| `spring-boot-starter-security` | `3.2.1` | Authentication/Authorization | `ftgo-security-lib` |
| `io.jsonwebtoken:jjwt-api` | `0.12.3` | JWT token handling | `ftgo-security-lib` |
| `micrometer-tracing-bridge-brave` | `1.2.1` | Distributed tracing | `ftgo-observability-lib` |
| `zipkin-reporter-brave` | `2.17.1` | Trace export to Zipkin | `ftgo-observability-lib` |
| `logstash-logback-encoder` | `7.4` | Structured JSON logging | `ftgo-observability-lib` |
| `spring-kafka` | `3.1.1` | Kafka messaging | `ftgo-messaging-lib` |
| `spring-cloud-starter-gateway` | `4.1.1` | API Gateway | `infrastructure` |
| `spring-boot-starter-validation` | `3.2.1` | Bean validation | All services |
| `flyway-mysql` | `9.22.3` | MySQL-specific Flyway support | All services |

---

## 11. Dependency Version Summary

| Dependency | Monolith Version | Microservices Version | Action |
|------------|------------------|-----------------------|--------|
| Spring Boot | 2.0.3.RELEASE | 3.2.1 | UPGRADE |
| Java | 1.8 | 17 | UPGRADE |
| MySQL Connector | 5.1.39 | 8.2.0 | UPGRADE |
| Micrometer | 1.0.4 | 1.12.1 | UPGRADE + REFACTOR |
| Jackson | 2.9.7 | 2.15.3 | UPGRADE |
| REST Assured | 2.9.0 / 3.0.6 | 5.4.0 | UPGRADE |
| Flyway | 6.0.0 | 9.22.3 | UPGRADE |
| Commons Lang | 2.6 | Removed | REMOVE |
| JAXB | 2.2.11 | Removed | REMOVE |
| javax.el | 2.2.5 | Removed | REMOVE |
| Eventuate | 0.1.0.RELEASE | Removed | REMOVE |
| Swagger | Custom module | springdoc 2.3.0 | REPLACE |
| Spring Security | N/A | 6.2.1 | NEW |
| JJWT | N/A | 0.12.3 | NEW |
| Spring Kafka | N/A | 3.1.1 | NEW |
| Spring Cloud Gateway | N/A | 4.1.1 | NEW |
| Brave/Zipkin | N/A | 2.17.1 | NEW |

---

## 12. Risk Assessment

### High Risk
- **Spring Boot 2.x to 3.x**: Major version upgrade requiring `javax` to `jakarta` namespace migration across all Java files
- **Hibernate 5.x to 6.x**: Potential ID generation and query compatibility issues
- **Database-per-service**: Requires removing all foreign key constraints between service boundaries

### Medium Risk
- **MySQL driver upgrade**: Connection string and driver class changes
- **Flyway upgrade**: Migration script compatibility and baseline handling
- **REST Assured upgrade**: API changes in test code

### Low Risk
- **Jackson upgrade**: Generally backward compatible
- **Micrometer upgrade**: API largely stable
- **Removing utility libraries**: Simple code-level replacements
