# AB-570: Spring Boot 3.5 / Java 21 / Jakarta cut-over

Base: `devin/ab-561-java21` (Boot 2.7.18, Java 8 bytecode). Result: Spring Boot 3.5.16
(Spring Framework 6.2, Hibernate 6.6, Micrometer 1.15), springdoc-openapi 2.10.0, Mockito 5,
every module compiled with `--release 21` on a Java 21 toolchain.

Mechanical part: OpenRewrite `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5`
(rewrite-spring 6.x) for the `javax.*` -> `jakarta.*` imports and `orm.xml` namespace.
Its build/CI/Dockerfile edits (Java 17, unrelated `@GetMapping` rewrites) were discarded and
done by hand.

## Behaviour kept compatible (`WebCompatibilityConfiguration`, `GlobalExceptionHandler`)

| Spring 6 / Boot 3 change | Handling |
|---|---|
| Trailing-slash match removed | `UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest()` (Q6): `/orders/` == `/orders` |
| Unknown static path raises `NoResourceFoundException` | Still dispatched to `/error` with the AB-563 body (`"message": "No message available"`) |
| Spring MVC exceptions (bad/missing parameter, unreadable body, 405, 415, ...) | `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`; 4xx status with the FTGO `ErrorResponse` body (previously the catch-all 500) |
| `org.springframework.web.ErrorResponse` name clash | FTGO body is always `net.chrisrichardson.ftgo.common.ErrorResponse` (explicit import); Spring's type is referenced fully qualified |
| springdoc 2.10 defaults to OpenAPI 3.1 | `springdoc.api-docs.version=openapi_3_0` keeps the 3.0.1 document from AB-566 |
| Parameter names (`LocalVariableTableParameterNameDiscoverer` removed) | `-parameters` for every `JavaCompile` |
| Jackson 2.19 removed `DeserializationContext.mappingException` | `MoneyModule` uses `handleUnexpectedToken` (still a `JsonMappingException`) |

Properties migration: `spring-boot-properties-migrator` was added to `ftgo-application`, the
application was started, and it reported no renamed/removed keys; it was removed again.

## Actuator

`/actuator/health` returns `{"status":"UP"}`. The Boot 2.7 configuration only exposed the
default web endpoint (`health`); `management.endpoints.web.exposure.include=health,prometheus`
now exposes `/actuator/prometheus` explicitly.

Prometheus metric names, Boot 2.7.18 (JDK 8) vs Boot 3.5.16 (JDK 21), same requests:

| Boot 2.7 | Boot 3.5 |
|---|---|
| `jvm_gc_overhead_percent` | `jvm_gc_overhead` |
| `jvm_memory_usage_after_gc_percent` | `jvm_memory_usage_after_gc` |
| — | new: `http_server_requests_active_seconds_{count,sum,max}`, `jdbc_connections_{active,idle}`, `jvm_compilation_time_ms_total`, `jvm_info`, `jvm_threads_started_threads_total`, `process_cpu_time_ns_total` |

All other names (`http_server_requests_seconds_*`, `hikaricp_*`, `jvm_memory_*`, `tomcat_*`, ...) are unchanged.

## Verification (JDK 21, MySQL 8.4, app on port 8081)

```text
./gradlew build -x :ftgo-end-to-end-tests:test     BUILD SUCCESSFUL (62 tests, 0 failures)
  ApiGoldenTest 36/36, StaticPathGoldenTest 1/1, OpenApiDocumentationTest 1/1,
  JpaSchemaValidationTest 2/2 (ddl-auto=validate on Hibernate 6.6), MySqlDateTimeTest 1/1, FtgoApplicationTest 2/2
./run-end-to-end-tests.sh                           EndToEndTests 2/2
python3 docs/upgrade/java21/compare-openapi.py v3-api-docs.json
  PASS: 24 paths, 25 operations, 26 schemas unchanged
  PASS: 8 request bodies and 25 documented response schemas unchanged
```

The four `latitude`/`longitude` failures recorded in `test-baseline.csv`
(`FtgoApplicationTest`/`EndToEndTests` `shouldDeliverOrder`, `shouldCreateReviseAndCancelOrder`)
now pass: the Hibernate 6.6 SQL uses the `delivery_address_`-prefixed columns of the Flyway schema.

## Left for later tickets

- AB-571: `ftgo-application/Dockerfile` still uses `openjdk:8u171-jre-alpine`, which cannot run
  the Java 21 jar; switch to `eclipse-temurin:21-jre` (Q9) there.
- Hibernate 6.6 logs `HHH000207` for `Money.amount` in `orm.xml`; the column mapping still
  validates and works (schema-validate and e2e green).
