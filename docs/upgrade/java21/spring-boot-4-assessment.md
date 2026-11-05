# Spring Boot 4.0 follow-up assessment (AB-574)

**Scope:** assessment only, against this branch's Boot 3.5.16 / Java 21 code;
no dependency or runtime migration attempted. Official sources below were checked
on 2026-11-05. Effort is relative (S: focused change, M: multi-module work,
L: cross-cutting compatibility work), not a delivery estimate.

**Recommendation:** a separate, contract-led epic is feasible, but not a version
bump. Jackson and Boot's modularized configuration/testing are the largest work
items. Retain Java 21 and the existing one-app/one-MySQL topology. Per
[ADR 0002](../../adr/0002-java-21-spring-boot-3.md), **3.5.x OSS support ended
2026-06-30; 4.0.x OSS support runs to 2026-12-31**. These are the ADR's recorded
dates, not a fresh vendor support-policy validation. The short remaining 4.0
window warrants rechecking the supported target line before scheduling; this
assessment is specifically for 4.0, not approval to land on an almost-EOL line.

## Impact inventory

| Area | Current repository evidence | Verified Boot 4 change / follow-up | Effort | Risk |
|---|---|---|---|---|
| Runtime/build | `build.gradle`: Java 21 toolchain/release; wrapper 8.14.4; catalog Boot 3.5.16. | Framework 7 / Jakarta EE 11 baseline; Java 17 minimum, Gradle 8.14+ or 9.x [1,2]. Existing JDK/wrapper satisfy the minimum: do not combine with Gradle 9 unnecessarily. | S | Low |
| JSON configuration | `ftgo-common/.../CommonConfiguration.java` exposes `com.fasterxml.jackson.databind.ObjectMapper`; `CommonJsonMapperInitializer` mutates it after construction, registering `MoneyModule` / `JavaTimeModule` and disabling timestamps. | Jackson 3 is preferred; core/databind packages and group IDs move to `tools.jackson`, but **annotations stay `com.fasterxml.jackson.annotation`** [1]. Immutable mapper construction requires builder-time configuration [4]; merely renaming imports will not suffice. | L | High |
| Money, dates, records | `MoneyModule` uses Jackson 2 scalar serializers/deserializers and empty-string-to-null behavior. Initializer uses `StdDateFormat.withColonInTimeZone(false)`; properties require UTC `+0000` error dates. `CreateOrderRequest` record has `@JsonPropertyOrder`. | Port custom module APIs, preserve Money strings/nulls and record missing-field defaults/order. Keep the annotation's package. Jackson 3 incorporates Java-time support [4]; do not blindly move the old jsr310 module dependency. Exact replacement for the no-colon date formatter is **to verify in a runnable spike**; do not assume Jackson 3's date defaults preserve the wire format. | L | High |
| OpenAPI | `common-swagger` uses catalog springdoc 2.10.0; application explicitly publishes OpenAPI 3.0. | Official compatibility matrix pairs **Boot 4.0.x with springdoc 3.0.x**, not 2.x [3]. Select an exact compatible patch, keep `openapi_3_0`, and compare all operations, schemas, required lists and property order. | M | High |
| Persistence | `ftgo-domain` uses `jakarta.persistence`, Hibernate `@DynamicUpdate`, explicit consumer `hibernate_sequence`; `ftgo-common-jpa/.../META-INF/orm.xml` declares ORM 3.0 mappings for Money/Address. Current Hibernate is 6.6.53.Final. | Boot 4 BOM moves to Hibernate 7.x and Persistence 3.2 [5]. Namespace is already Jakarta, but validate mapping/SQL/generator behavior against MySQL; older XML compatibility and any needed descriptor edits are **to verify**, not an automatic schema rewrite. | M | High |
| Servlet/web compatibility | Tomcat 10.1.55; `WebCompatibilityConfiguration` uses Jakarta filters, ordered original-URI capture, `UrlHandlerFilter` trailing-slash wrapping and custom error attributes. | Boot 4 requires Servlet 6.1+; embedded Tomcat is 11.0.x [2]. `UrlHandlerFilter` is still present in Framework 7 [6], **not a removed API**. Audit moved Boot imports and preserve slash, original URI, static 404 and error-body contracts. | M | High |
| Flyway / MySQL | `ftgo-flyway/build.gradle` runs Gradle Flyway 10.22.0 with `flyway-mysql`; an isolated Flyway 6 legacy-history test remains. Application does not currently declare a runtime Flyway starter. MySQL 8.4; explicitly pinned Connector/J **8.4.0**, not ADR's planned 9.x. | Boot 4's runtime Flyway integration now needs `spring-boot-starter-flyway` [1]; **do not add it blindly** to this externally migrated app. Retain DB-specific module and separately align plugin/runtime versions if runtime migration is chosen. Boot's BOM does not upgrade the Gradle plugin automatically. Driver/pinning remediation is a separate task [7]. | M | Medium |
| Boot modularity | Catalog has `spring-boot-starter-web`, data-jpa, actuator, test; shared `CommonConfiguration` and `DomainConfiguration` use Boot `EntityScan`; tests import Boot test slices. | Auto-configuration and test infrastructure move into technology modules/packages; web starter is renamed to `spring-boot-starter-webmvc` (old starter deprecated). Audit imports and add technology-specific test starters; classic starters are a documented **temporary** bridge [1], not the final dependency plan. | M | High |
| Tests / removals | BOM-managed JUnit Jupiter 5.12.2 / Mockito 5.17.0; REST Assured explicitly 5.5.7, Awaitility 4.2.2. `StaticPathGoldenTest` injects Boot `TestRestTemplate`; `ApiGoldenTest` already uses Framework `@MockitoBean`; no `@MockBean` or direct `RestTemplate` construction found. | Boot removes `@MockBean`/`@SpyBean`; no remaining replacement here. `TestRestTemplate` moves package and needs explicit test configuration/dependencies; `@SpringBootTest` no longer supplies it automatically [1]. The checked 4.0 dependency table lists JUnit 6.0.3 / Mockito 5.20.0 [5]. REST Assured 5.5.7 + Jackson 3 interoperability is **to verify**; don't assume BOM alignment fixes its explicit pin or custom Jackson 2 mapper. | M | High |

Repository searches used `rg 'com.fasterxml|UrlHandlerFilter|RestTemplate|TestRestTemplate|MockBean|MockitoBean' --glob '*.java'`,
plus the version catalog, wrapper, service/API Gradle files, entity mappings and
tests. Jackson 2 also appears in `MoneySerializationTest`, API/static/OpenAPI and
record tests, `OrderControllerTest`, and
`ftgo-end-to-end-tests-common/.../AbstractEndToEndTests.java`'s custom REST Assured
mapper. These must migrate deliberately together; a Jackson 2 bean alone does
not configure Boot 4's Jackson 3 HTTP mapper. Boot offers deprecated
`spring-boot-jackson2` and `spring.jackson.use-jackson2-defaults` for transition
[1]; neither establishes parity. No blanket claim that all deprecations were
audited, or that RestTemplate was removed, is made.

## Sequencing and suggested tickets

1. **Target/support + ARB gate:** verify vendor support dates and selected 4.x
   patch; run ARB triage for the major framework migration and amend/draft the
   follow-up ADR as required. Preserve endpoints, schema and topology; explicitly
   exclude unrelated driver, pool and virtual-thread changes from the cutover.
2. **3.5 preparation / dependency inventory:** capture resolved dependencies,
   deprecation output and current JSON/OpenAPI snapshots; choose exact springdoc,
   Hibernate/Flyway/test versions and map technology starters/imports. Finish
   compatibility decisions for REST Assured before the atomic cutover ticket.
3. **Jackson 3 contract spike:** migrate builder/module configuration and test
   mappers; prove Money, empty/null/missing fields, LocalDateTime and `+0000`
   dates, record ordering and dashboard JSON unchanged. Discard or time-box any
   Jackson 2 bridge; don't weaken goldens to accommodate changed defaults.
4. **Boot 4 integration cutover:** combine tested JSON changes with modular
   starters/imports, springdoc 3.0.x, Hibernate/JPA and Tomcat changes. Keep this
   atomic if intermediate dependency combinations cannot compile. Validate
   existing Flyway history, schema validation and consumer sequence allocation.
5. **Parity / operational acceptance:** run the non-E2E build, both real-MySQL
   E2E lifecycle flows, static/API golden tests, UTC and non-UTC datetime tests,
   and `compare-openapi.py` against a new `/v3/api-docs` capture. Add direct
   before/after schema-order checks; exercise Swagger/dashboard in a browser and
   repeat the load/pinning trial. Keep VT off until its separate gate passes.

## Sources and limits

[1] [Official Boot 4.0 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
— Jackson, modularity, removals, Flyway and TestRestTemplate.
[2] [Boot 4.0 system requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html).
[3] [springdoc compatibility matrix](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot).
[4] [Jackson 3.0 release notes](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0).
[5] [Boot 4.0 managed dependencies](https://docs.spring.io/spring-boot/4.0/appendix/dependency-versions/coordinates.html)
— live maintenance-line table, **not** a claim about every 4.0 patch's versions.
[6] [Framework 7 UrlHandlerFilter API](https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/web/filter/UrlHandlerFilter.html).
[7] [Local virtual-thread trial](virtual-threads-trial.md).

This assessment was checked against source and official documentation, not a
Boot 4 build. Binary compatibility, runtime parity and the target's support window
remain acceptance gates for the follow-up epic, not completed work here.
