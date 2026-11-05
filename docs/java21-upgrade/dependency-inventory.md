# Java 21 dependency inventory

## Scope and decisions

Audited on **2026-11-05** against `master` at
`1fb9c78da64bd0bf7a1562abc990e660f2f0c544`. This is a declaration inventory,
not a lockfile or a claim that the proposed upgrades have passed integration tests.
It covers the root build, wrapper, all properties, `buildSrc` (including custom
plugins), and all **17 included subprojects**. Repeated coordinates are consolidated
below; the file coverage table maps them back to every declaration. Selected
transitive components are shown separately because they are migration-critical.

The two target columns are **Spring Boot 2.7.18** (temporary `javax` stepping stone)
and **Spring Boot 4.1.2** (latest stable 4.x verified in the official documentation
and published BOM on the audit date). Boot 4.2.0-RC2 is a preview, not the target.
No dependency or application configuration is changed by this report.

**How to read the minimum column:** a Java bytecode baseline (for example, Java 8+)
does not establish the first release tested on Java 21. **NP** means upstream does
not publish, or the cited sources do not establish, a first Java-21-compatible
release. In those cases the target is a conservative migration candidate, not an
invented compatibility minimum. **BOM** means use that exact Boot BOM's version
and remove the local override. Dependencies with reflection, bytecode generation,
Groovy, or framework integrations still require tests on Java 21.

## Build tools and plugins

| Plugin/tool and declaration | Current version | Minimum supporting Java 21 / evidence limit | Boot 2.7.18 choice | Boot 4.1.2 choice | Notes |
| --- | --- | --- | --- | --- | --- |
| Gradle wrapper | 4.10.2 | **8.5** to run Gradle on Java 21; 8.4 only supports Java 21 toolchains [G] | **8.14.3** | **8.14.3** | Shared pinned 8.x version; Boot 4 requires 8.14+ or 9.x [B4]. Do not run the current wrapper with Java 21. |
| `org.springframework.boot:spring-boot-gradle-plugin` / `org.springframework.boot` | 2.0.3.RELEASE | 2.7.18 is explicitly documented compatible through Java 21 [B27]; first historical patch not established | **2.7.18** | **4.1.2** | Applied to the app by `FtgoServicePlugin`; align plugin, starters, and BOM. Boot 4 uses Spring Framework 7, not 6. |
| `io.spring.gradle:dependency-management-plugin` / `io.spring.dependency-management` | 1.0.3.RELEASE | NP; 1.1.7 documents Java 8+ and Gradle 6.8–8.x [DM] | **1.1.7** | **1.1.7** | Applied by `FtgoServicePlugin` and directly in order-service. A Boot plugin on the app does not import the BOM into every sibling. Import it in all library/test modules. |
| `org.flywaydb.flyway` (Gradle plugin, not Boot's runtime dependency) | 6.0.0 | NP; current upstream supports Java 17+ and Gradle 7.6/8.x, without identifying the first Java 21 patch [F] | **9.22.3** (build-only candidate) | **12.4.0** (align with target BOM) | Upgrade/test the standalone migration task, including MySQL plugin-classpath driver. Modern Flyway needs `org.flywaydb:flyway-mysql` at the same version. Boot 2.7's runtime BOM is 8.5.13; do not override runtime Flyway to 9 merely because the isolated Gradle plugin uses it. |
| `com.github.ben-manes.versions` | 0.20.0 | NP; 0.52.0 fixes deprecated Gradle APIs [V] | **0.52.0** | **0.52.0** | Build-time dependency report only; smoke-test `dependencyUpdates` on the selected wrapper. |
| Gradle `java` plugin | Bundled with 4.10.2 | Follows Gradle: 8.5 to run on Java 21 [G] | Bundled with **8.14.3** | Bundled with **8.14.3** | Applied to every subproject. Replace `compile`, `runtime`, `testCompile` with supported configurations. Set Java toolchain/release explicitly. |
| Implicit `buildSrc` Java/Groovy build support | Gradle 4.10.2 / embedded Groovy 2.4.15 | Follows wrapper; not a separately pinned plugin | Bundled with **8.14.3** | Bundled with **8.14.3** | Build logic runs in Gradle's JVM. Do not confuse embedded Groovy with REST Assured's test-runtime Groovy. |
| Custom `WaitForMySqlPlugin` / `WaitForMySql` | In-repo, unversioned | No published matrix; recompile/test with chosen Gradle | Port/retest in-repo | Port/retest in-repo | Root `waitForMySql` task uses buildSrc's JDBC driver and defaults to legacy `com.mysql.jdbc.Driver`; change to `com.mysql.cj.jdbc.Driver`. |
| Custom `FtgoServicePlugin` | In-repo, unversioned | No published matrix | Retain/retest | Retain/retest | Applies Boot and dependency-management; does not itself declare a library version. |
| Custom `IntegrationTestsPlugin` | In-repo, unversioned | **No version-only fix** | Port to Gradle 8 API | Same port | Uses `testCompile`, `testRuntime`, `testClassesDir`, `output.classesDir`, and `reports.html.destination`. Use supported configurations, `testClassesDirs` / `classesDirs`, and report output locations. |
| `com.avast.gradle:gradle-docker-compose-plugin` / `docker-compose` | **Inactive**: property 0.6.6; commented classpath 0.14.13 | N/A while disabled | Remove stale property/comments; use external Compose | Same | Not on the active build classpath. Do not report either version as installed. |

## Every directly declared external library

Abbreviations: **common** = `ftgo-common`; **consumer**, **courier**, **order**,
**restaurant** = respective service modules; **e2e-common** =
`ftgo-end-to-end-tests-common`; **app** = `ftgo-application`.

| Library coordinate | Current declared version; location/scope | Minimum supporting Java 21 / evidence limit | Boot 2.7.18 choice | Boot 4.1.2 choice | Notes |
| --- | --- | --- | --- | --- | --- |
| `org.springframework.boot:spring-boot-starter-web` | 2.0.3.RELEASE; common-swagger, common, app, all four services; compile | Boot 2.7.18 documented [B27] | **2.7.18** | Replace with **`spring-boot-starter-webmvc:4.1.2`** | Boot 4 modularized starter; deprecated old web starter is not the desired endpoint. |
| `org.springframework.boot:spring-boot-starter-data-jpa` | 2.0.3.RELEASE; common, common-jpa, all four services; compile | Stack-dependent; see Hibernate caveat below | **2.7.18** | **4.1.2** | Brings Hibernate 5.6 vs 7.4 and changes persistence/transaction namespaces. |
| `org.springframework.boot:spring-boot-starter-actuator` | 2.0.3.RELEASE; app, all four services; compile | Boot 2.7.18 documented [B27] | **2.7.18** | **4.1.2** | Retain focused starter and BOM-managed Micrometer. |
| `org.springframework.boot:spring-boot-starter-test` | 2.0.3.RELEASE; common, all four services (testCompile), e2e-common (compile) | Boot 2.7.18 documented; mock/agent caveat below | **2.7.18** | **4.1.2** plus focused test starters | e2e-common is shared test code published from its main source set; do not blindly change its compile scope to testImplementation. |
| `io.springfox:springfox-swagger2` | 2.8.0; common-swagger; compile, excludes `org.springframework` | No supported Springfox Boot 4/Jakarta version; NP for Java 21 | Replace with **`org.springdoc:springdoc-openapi-ui:1.8.0`** | Replace with **`org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0`** | springdoc 1.x is the Boot 2 bridge; 3.x supports Boot 4, and 3.1–3.2 supports Boot 4.1 [SD]. Migrate Docket/configuration and Swagger annotations; verify API docs/UI routes. |
| `io.springfox:springfox-swagger-ui` | 2.8.0; common-swagger; compile, same exclusion | Same as Springfox above | Remove; provided by springdoc **1.8.0** | Remove; provided by springdoc **3.2.0** | Springfox 3.0.0 is not a Jakarta/Boot 4 solution; avoid a permanent path-matching workaround. |
| `com.fasterxml.jackson.core:jackson-core` | **2.9.7**, pinned; common; compile | NP for earliest 2.x release; Jackson 3.0+ requires Java 17 [J] | **2.13.5**, BOM | **`tools.jackson.core:jackson-core:3.1.7`**, BOM | Remove explicit 2.9.7 pins. These are declarations, not proof of the finally resolved version. |
| `com.fasterxml.jackson.core:jackson-databind` | **2.9.7**, pinned; common; compile | NP for earliest 2.x release; Jackson 3.0+ Java 17+ [J] | **2.13.5**, BOM | **`tools.jackson.core:jackson-databind:3.1.7`**, BOM | Port `CommonConfiguration`, initializer, `MoneyModule`, and test mappers to Jackson 3 builders/customizers and APIs. `jackson-annotations` remains `com.fasterxml.jackson` 2.x; do not mechanically rename annotations. |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | **2.9.7**, pinned; common; compile | NP for earliest 2.x release | **2.13.5**, BOM | **Remove separate jar**; Java time support is built into Jackson **3.1.7** databind [J] | Remove old `JavaTimeModule` registration when moving to Jackson 3; test Money/date JSON wire compatibility. |
| `mysql:mysql-connector-java` | **8.0.33**; buildSrc + common (compile), app (runtime) | NP for first Java 21 release; Java 8+ baseline alone is not certification | **`com.mysql:mysql-connector-j:8.0.33`**, BOM in app; explicit pin in buildSrc | **`com.mysql:mysql-connector-j:9.7.0`**, BOM in app; explicit pin in buildSrc | Old artifact is a relocation POM. 9.7 supports MySQL **8.0+**, not the checked-in 5.7.13 image [MY]. Database upgrade is a Boot 4 prerequisite, not a change made here. |
| `commons-lang:commons-lang` | **2.6**; buildSrc + common; compile | NP; pure library is not blocked solely by Java 21, but 2.x is obsolete | Replace with **`org.apache.commons:commons-lang3:3.12.0`**, BOM; explicit in buildSrc | **`org.apache.commons:commons-lang3:3.20.0`**, BOM; explicit in buildSrc | Update `org.apache.commons.lang.*` imports to `lang3.*`. buildSrc currently declares it but has no matching import. Remove unused buildSrc dependency rather than carry it forward. |
| `javax.xml.bind:jaxb-api` | **2.2.11**; common; runtime | JDK no longer supplies JAXB since Java 11; earliest Java 21 release NP | Replace with **`jakarta.xml.bind:jakarta.xml.bind-api:2.3.3`**, BOM (`javax.xml.bind` packages) | Remove old jar; **`jakarta.xml.bind:jakarta.xml.bind-api:4.0.5`**, BOM via ORM/runtime if needed | Jakarta artifact names in 2.3 do **not** mean Jakarta packages. Boot 4 uses `jakarta.xml.bind`. No application JAXB imports found. |
| `com.sun.xml.bind:jaxb-core` | **2.2.11**; common; runtime | NP; do not retain legacy implementation split | Replace old split with **`org.glassfish.jaxb:jaxb-runtime:2.3.9`**, BOM | Replace with **`org.glassfish.jaxb:jaxb-runtime:4.0.9`**, BOM | Use one coherent runtime; its matching core is transitive. `com.sun.xml.bind` here is an external artifact group, not a JDK-internal source import. |
| `com.sun.xml.bind:jaxb-impl` | **2.2.11**; common; runtime | NP; same runtime replacement | Same **jaxb-runtime 2.3.9**, not an additional copy | Same **jaxb-runtime 4.0.9**, not an additional copy | Remove explicit legacy implementation pins; prefer ORM-managed runtime if no direct JAXB usage. |
| `javax.activation:activation` | **1.1.1**; common; runtime | External activation required after JDK 11 removal if used; earliest Java 21 release NP | Replace with **`com.sun.activation:jakarta.activation:1.2.2`**, BOM (still `javax.activation`) if needed | Remove old jar; **`jakarta.activation:jakarta.activation-api:2.1.4`**, BOM with runtime provider if needed | Old activation is **1.1.1**, not 2.2.11. Let JAXB's dependency graph supply a consistent API/provider rather than duplicate jars. |
| `javax.el:javax.el-api` | **2.2.5**; all four services; compile | NP; no application EL imports | **Remove**; embedded Tomcat EL **9.0.83**, BOM | **Remove**; embedded Tomcat EL **11.0.27**, BOM (`jakarta.el`) | An old API-only jar can conflict with the container implementation. If validation is used, add the matching validation starter explicitly. |
| `com.jayway.restassured:rest-assured` | **2.9.0** via property; consumer, restaurant (testCompile), e2e-common (compile) | Legacy group is discontinued; NP for historical Java 21 minimum | Replace with **`io.rest-assured:rest-assured:4.5.1`**, BOM | Replace with **`io.rest-assured:rest-assured:6.0.1`**, explicit pin [RA] | Rewrite `com.jayway.restassured.*` imports (e2e-common) and mapper factories. Boot 4 BOM does not manage REST Assured. |
| `io.rest-assured:rest-assured` | **3.0.6**; courier, order; testCompile | NP for first Java 21 version; runtime Groovy matters | **4.5.1**, BOM; Groovy **3.0.19** | **6.0.1**, explicit; Groovy **5.0.8** from Boot BOM | Use one REST Assured family, not both groups. Java 17+/Spring 7/Jackson 3 integration in 6.0.x; 6.0.1 fixes Jakarta-only MockMvc and mapper selection [RA]. |
| `io.rest-assured:spring-mock-mvc` | **3.0.6**; courier, order; testCompile | NP for Java 21 alone; Spring 7 integration in 6.0.x [RA] | **4.5.1**, BOM | **6.0.1**, explicit | Keep in lockstep with REST Assured; do not allow old transitive Spring 5 into Boot 4 tests. |
| `io.rest-assured:json-path` | **3.0.6**; courier, order; testCompile | NP; follows REST Assured/Groovy compatibility | **4.5.1**, BOM | **6.0.1**, explicit | This is REST Assured's JSON path implementation, **not** Jayway JsonPath. |
| `com.jayway.jsonpath:json-path` | **2.3.0**; consumer, restaurant (testCompile), e2e-common (compile) | NP; selected Boot BOM supplies stack-aligned version | **2.7.0**, BOM | **2.10.0**, BOM | Separate coordinate and DSL from REST Assured's `json-path`. Retain only if actually needed after test migration. |
| `junit:junit` | **4.12**; domain, test-util; testCompile | NP; **4.13.2** is last JUnit 4 release, not a Java 21 certification claim | **4.13.2**, BOM, plus **`org.junit.vintage:junit-vintage-engine:5.8.2`** when using JUnit Platform | Port to **`org.junit.jupiter:junit-jupiter:6.0.3`**, BOM | Boot 4 target is JUnit 6/Jupiter, not JUnit 4. Also migrate JUnit 4 imports in common, order, app, e2e tests, and e2e-common's **main** source set; preserve the latter's compile-time access to assertions. |
| `io.micrometer:micrometer-registry-prometheus` | **1.0.4** via property; courier, order; compile | NP for first Java 21 release; use stack BOM | **1.9.17**, BOM | **1.17.2**, BOM | Remove `micrometerVersion` override; app's Boot 2.0.3 BOM has Micrometer 1.0.5, so declared and resolved versions may differ. Check scrape names/output after upgrade. |
| `io.eventuate.util:eventuate-util-test` | **0.1.0.RELEASE** in repo property; e2e-common; compile. This VM overrides it to **0.9.0.RELEASE** | **Unknown**: no established Java 21 support matrix; 0.1.0 POM unavailable from Central mirror | **Remove**; replace `Eventually` in the one importing class with bounded JDK polling or a separately reviewed test utility | Same | Seven call sites in `AbstractEndToEndTests`: six `eventually` calls and one `eventuallyReturning` for a courier ID. Preserve assertion retry, timeout, and returned-value semantics. VM override is not a repository fix or Java 21 certification. |

## BOM-managed components that change the migration

These are not extra direct declarations. Current values below come from the
published Boot 2.0.3 BOM, **not** a successfully resolved dependency lock.

| Component | Current BOM version | Java 21 minimum/support limit | Boot 2.7.18 choice | Boot 4.1.2 choice | Notes |
| --- | --- | --- | --- | --- | --- |
| Spring Framework | **5.0.7.RELEASE** | 5.3 line gained Java 21 support; use documented Boot 2.7.18 stack [B27] | **5.3.31**, BOM | **7.0.10**, BOM | Retain `javax` only in bridge; migrate Spring API changes at Boot 4 cutover. |
| Hibernate ORM | **5.2.17.Final** | Hibernate **6.2** lists Java 21 [H62]; **5.6 does not officially list Java 21** [H56] | **5.6.15.Final**, BOM, **compatibility risk** | **`org.hibernate.orm:hibernate-core:7.4.13.Final`**, BOM | Boot's Java 21 statement does not certify every transitive library. Treat 2.7+Hibernate 5.6 as a tested, short-lived bridge; do not put Hibernate 6/7 under Boot 2.7 just to obtain a support matrix. |
| Tomcat / servlet | Tomcat **8.5.31** / Servlet **3.1** | NP for earliest compatible Tomcat 9 patch; Boot-tested stack below | **9.0.83** / Servlet **4.0** | **11.0.27** / Servlet **6.1** | Update servlet imports to Jakarta only at Boot 4 cutover. |
| Mockito / Byte Buddy | **2.15.0** / **1.7.11** | Byte Buddy **1.14.3** supports Java 21 class files [BB]; Mockito version alone is not sufficient | Mockito **4.5.1**, BOM; override **Byte Buddy + agent to 1.14.12** if targeting Java 21 bytecode | Mockito **5.23.0** / Byte Buddy **1.18.15**, BOM | Boot 2.7 BOM's Byte Buddy **1.12.23** is below the published Java 21 floor. Verify both core and agent; use a proper test JVM agent if required, not `net.bytebuddy.experimental`. |
| Jackson annotations | **2.9.0** (BOM; common has newer core/databind pins) | NP | BOM's Jackson **2.13.5** family | Boot's Jackson 3 BOM supplies compatible **2.x annotations** | The annotation namespace is the deliberate Jackson 3 exception [J]. |
| JAXB/activation/persistence/annotation APIs | JDK 8 platform + legacy runtime jars / JPA 2.1 API / annotation-api **1.3.2** | Namespace and removed-JDK-module issue, not just version | JPA API **2.2.3**, annotation API **1.3.5**, JAXB API **2.3.3** (Jakarta coordinates, `javax` packages) | JPA API **3.2.0**, annotation API **3.0.0**, JAXB API **4.0.5**, all Jakarta packages | `javax.persistence`, `javax.servlet`, `javax.annotation` do not move into Java 21's JDK: they come from dependencies. |

### Boot 4 modularization checklist

- Replace `spring-boot-starter-web` with `spring-boot-starter-webmvc`; keep
  `spring-boot-starter-data-jpa` and `spring-boot-starter-actuator` at 4.1.2.
- Use **`spring-boot-starter-jackson:4.1.2`** integration for Jackson 3 (normally
  supplied by the web starter); do not keep 2.9.7 jars to satisfy old imports.
- Keep base `spring-boot-starter-test` and add **`spring-boot-starter-webmvc-test`**
  and **`spring-boot-starter-data-jpa-test`** where their test slices are used,
  all 4.1.2. Test annotation packages can move into technology-specific modules.
- Database migrations currently run in the separate Gradle module, not at app
  startup. **Only if moving them into Boot**, add
  **`spring-boot-starter-flyway:4.1.2`**, plus matching Flyway MySQL support.
  Adding `flyway-core` alone no longer opts into Boot 4 auto-configuration [M4].
- Stage a database upgrade: checked-in MySQL is **5.7.13**, while selected
  Connector/J 9.7 requires MySQL 8.0+ and Hibernate 7 also requires a modern
  dialect/database. Confirm migrations and reconciliation before changing it.

## Declaration coverage by file

This table includes modules with **only project dependencies** so that no build
file disappears from the audit. Project edges are unversioned outputs of this
same build, not external Java 21 version candidates.

| File | External declarations / project dependencies / other inputs |
| --- | --- |
| `build.gradle` | Boot plugin, dependency-management classpaths; versions plugin; Java plugin for all subprojects; custom wait plugin; source/target **1.8**; Maven Central, **JCenter**, disabled Bintray/Spring milestone repositories, local `build/repo`. Remove JCenter/dead fallbacks in a later build change. |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle **4.10.2-all** distribution. |
| `gradle.properties` | Boot **2.0.3.RELEASE**, REST Assured **2.9.0**, dependency-management **1.0.3.RELEASE**, Eventuate **0.1.0.RELEASE**, Micrometer **1.0.4**; unused Compose **0.6.6**; `deployUrl` and disabled `eventuateMavenRepoUrl` reference a developer's local path/Bintray, not libraries. |
| `buildSrc/build.gradle` | MySQL connector **8.0.33**, commons-lang **2.6**; buildSrc tests explicitly disabled. See all three custom plugins above. |
| `common-swagger/build.gradle` | Springfox Swagger2/UI **2.8.0**, Boot web. |
| `ftgo-application/build.gradle` | Custom service plugin; Boot actuator/web, MySQL runtime; projects consumer, order, restaurant, courier; test project e2e-common. |
| `ftgo-common/build.gradle` | Boot JPA/web/test; Jackson core/databind/jsr310 **2.9.7**, commons-lang **2.6**, MySQL **8.0.33**, JAXB API/core/impl **2.2.11**, activation **1.1.1**. |
| `ftgo-common-jpa/build.gradle` | Boot JPA; project common. |
| `ftgo-domain/build.gradle` | JUnit **4.12**; projects common, common-jpa. |
| `ftgo-consumer-service-api/build.gradle` | Project common only. |
| `ftgo-courier-service-api/build.gradle` | Project common only. |
| `ftgo-order-service-api/build.gradle` | Project common only. |
| `ftgo-restaurant-service-api/build.gradle` | Project common only. |
| `ftgo-consumer-service/build.gradle` | Boot JPA/web/actuator/test; EL **2.2.5**, Jayway REST Assured **2.9.0**, Jayway JsonPath **2.3.0**; projects common-swagger, consumer-api, domain. |
| `ftgo-restaurant-service/build.gradle` | Same external coordinates as consumer; projects common-swagger, restaurant-api, domain. |
| `ftgo-courier-service/build.gradle` | Boot JPA/web/actuator/test; Micrometer Prometheus **1.0.4**, EL **2.2.5**; `io.rest-assured` rest-assured/spring-mock-mvc/json-path **3.0.6**; projects courier-api, common-swagger, domain, test-util. |
| `ftgo-order-service/build.gradle` | Same external libraries as courier; dependency-management + custom integration-test plugin; inactive Compose **0.14.13** classpath; projects common-swagger, domain, consumer-api, restaurant-api, order-api, consumer-service, test-util. |
| `ftgo-test-util/build.gradle` | JUnit **4.12** only. |
| `ftgo-end-to-end-tests-common/build.gradle` | Eventuate test **0.1.0.RELEASE**, Boot test, Jayway REST Assured **2.9.0**, Jayway JsonPath **2.3.0**, all compile; four service-api projects. |
| `ftgo-end-to-end-tests/build.gradle` | Test project e2e-common only. |
| `ftgo-flyway/build.gradle` | Flyway Gradle plugin **6.0.0**; MySQL connection/schema settings; no explicit runtime Flyway/driver dependency in this module. |
| `settings.gradle` | Includes exactly the 17 modules above; no external plugins/libraries. |

**BOM caveat:** Boot/dependency-management is not uniformly applied across
subprojects today. For example, explicitly pinned versions in common are not
automatically replaced by the app's BOM when compiling common itself. Conversely,
the app/order module may resolve different BOM-selected versions than those
declared in libraries, including **downgrading** explicit Jackson pins. The
target columns assume consistent BOM imports in all modules. These selected
reports are not a complete resolved SBOM.

### Observed resolution in this VM

The following Gradle dependency reports completed successfully on JDK 8.
The pre-existing user-home Gradle init script redirects Central/JCenter to
Google's Central mirror; user-home `gradle.properties` sets
`eventuateUtilVersion=0.9.0.RELEASE`. Neither change is tracked in this repo.
Reproducing this VM's baseline therefore requires acknowledging these inputs,
not assuming the checked-in 0.1.0 artifact suddenly became available.

| Configuration | Observed declaration → resolved selection |
| --- | --- |
| `:ftgo-common:compileClasspath` | Jackson core/databind **2.9.7** retained; no app-level BOM downgrade at this compile boundary. |
| `:ftgo-application:runtimeClasspath` | Jackson core/databind **2.9.7 → 2.9.6**; Jackson annotations **2.9.0**; Micrometer registry/core **1.0.4 → 1.0.5**; Hibernate **5.2.17.Final**; MySQL old coordinate relocates to **com.mysql:mysql-connector-j:8.0.33**. |
| `:ftgo-application:testRuntimeClasspath` | Eventuate **0.9.0.RELEASE** (user-home override); Jayway REST Assured **2.9.0**; Jayway JsonPath **2.3.0 → 2.4.0**; Groovy **2.4.15**; JAXB API **2.2.11 → 2.3.0**. |
| `:ftgo-order-service:testRuntimeClasspath` | `io.rest-assured` family **3.0.6**, Groovy **2.4.12**, Prometheus registry **1.0.4**, JAXB API **2.2.11 → 2.2.12**. Different from the app's graph. |

**Sentry:** no Java SDK coordinate or Java SDK import is present.
`ftgo-application/.../SentryConfigController.java` only reads `sentry.dsn`,
`sentry.environment`, and `sentry.send-default-pii` properties and returns browser
configuration from `/config/sentry`. The SDK is loaded by the browser assets;
there is no Java Sentry compatibility upgrade to schedule here.

## Removed/internal API scan

Scanned tracked Java and Groovy sources (including `buildSrc`), plus build and
properties files, rather than generated Gradle caches. There are **105 `.java`
files and 2 `.groovy` files**. These are textual source findings, not a transitive
`jdeps` or runtime-reflection certification.

| Pattern | Source hits | Build/dependency hits | Interpretation |
| --- | --- | --- | --- |
| `sun.*` / `com.sun.*` | **0** | `ftgo-common/build.gradle:18–19`: `com.sun.xml.bind:jaxb-core` and `jaxb-impl` | External JAXB artifacts, not JDK-internal API access. |
| `javax.xml.bind` | **0** | `ftgo-common/build.gradle:17`: `javax.xml.bind:jaxb-api:2.2.11` | Runtime dependency remains even without source imports; JDK removal is relevant. |
| `Thread.stop`, any `.stop(` call | **0** | **0** | No textual stop-call candidate. `Thread.sleep` in wait logic is unrelated. |
| `finalize(` declarations/calls | **0** | **0** | No finalizer implementation found. |
| `new Integer(` (whitespace-tolerant) | **0** | **0** | No deprecated boxed-integer constructor found. |
| `SecurityManager`, `getSecurityManager`, `setSecurityManager`, `java.security.Policy`, `Security Manager` | **0** | **0** | No textual Security Manager dependency. |

### `javax` hypothesis: packages confirmed, file count corrected

There are **33 import statements in 20 distinct files**, **not 33 files**:
`javax.persistence` = **29 imports / 17 files**,
`javax.servlet` = **3 imports / 2 files**, and
`javax.annotation` = **1 import / 1 file**. There are **no other `javax.*` imports**
in tracked Java/Groovy sources. None of these packages is supplied by Java 21;
retain their `javax` APIs for the Boot 2 bridge, migrate to `jakarta` for Boot 4.

Exact file list (paths relative to each module's `src/main/java`):

| Module | Java source path | Imports / current lines |
| --- | --- | --- |
| ftgo-application | `net/chrisrichardson/ftgo/GlobalExceptionHandler.java` | `javax.servlet` at 17 |
| ftgo-common | `net/chrisrichardson/ftgo/common/Address.java` | `javax.persistence` at 3–5 |
| ftgo-common | `net/chrisrichardson/ftgo/common/Money.java` | `javax.persistence` at 7–9 |
| ftgo-common | `net/chrisrichardson/ftgo/common/PersonName.java` | `javax.persistence` at 3 |
| ftgo-common | `net/chrisrichardson/ftgo/common/tracking/ApiRequestLog.java` | `javax.persistence.*` at 3 |
| ftgo-common | `net/chrisrichardson/ftgo/common/tracking/ApiTrackingInterceptor.java` | `javax.servlet` at 9–10 |
| ftgo-common | `net/chrisrichardson/ftgo/common/CommonJsonMapperInitializer.java` | `javax.annotation.PostConstruct` at 8 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Action.java` | `javax.persistence` at 3–6 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Consumer.java` | `javax.persistence.*` at 7 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Courier.java` | `javax.persistence.*` at 7 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/DeliveryInformation.java` | `javax.persistence.*` at 6 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/MenuItem.java` | `javax.persistence.*` at 8 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Order.java` | `javax.persistence.*` at 7 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/OrderLineItem.java` | `javax.persistence.*` at 8 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/OrderLineItems.java` | `javax.persistence` at 5–7 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/PaymentInformation.java` | `javax.persistence` at 3–4 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Plan.java` | `javax.persistence` at 3 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/Restaurant.java` | `javax.persistence.*` at 6 |
| ftgo-domain | `net/chrisrichardson/ftgo/domain/RestaurantMenu.java` | `javax.persistence.*` at 7 |
| ftgo-restaurant-service-api | `net/chrisrichardson/ftgo/restaurantservice/events/MenuItemDTO.java` | `javax.persistence` at 8–10 |

### Reproduction commands

Run from the repository root; `git grep` exit 1 means no matches, not a scan error.

```sh
git ls-files '*build.gradle' gradle.properties settings.gradle 'gradle/wrapper/*'
git grep -nE '^import (javax\.|sun\.|com\.sun\.)' -- '*.java' '*.groovy'
git grep -nE 'sun\.|com\.sun\.|javax\.xml\.bind|Thread[[:space:]]*\.[[:space:]]*stop|\.stop[[:space:]]*\(|finalize[[:space:]]*\(|new[[:space:]]+Integer[[:space:]]*\(|SecurityManager|getSecurityManager|setSecurityManager|java\.security\.Policy|Security Manager' -- '*.java' '*.groovy' '*.gradle' '*.properties'
git grep -nE '^import .*sentry|io\.sentry' -- '*.java' '*.groovy' '*.gradle'

# Baseline only: the old wrapper must use JDK 8.
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --version
DOCKER_HOST_IP=localhost ./gradlew -x :ftgo-end-to-end-tests:test test --console=plain
DOCKER_HOST_IP=localhost ./gradlew -x :ftgo-end-to-end-tests:test test --continue --console=plain
# Capture each relevant module/configuration, e.g.:
./gradlew :ftgo-application:dependencies --configuration runtimeClasspath
./gradlew :ftgo-common:dependencies --configuration compileClasspath
./gradlew :ftgo-order-service:dependencyInsight --dependency byte-buddy --configuration testRuntimeClasspath
```

## Verification boundary

The JDK 8 wrapper version command succeeded: Gradle **4.10.2**, JVM
**1.8.0_504**, embedded Groovy **2.4.15**. Source scans, all 22 declaration/config
files, all 26 external library/buildscript coordinates, and Markdown table widths
were checked against tracked files. No dedicated lint/typecheck task is configured
for this Markdown-only artifact; the suite also compiled the Java sources.

The non-e2e `test --continue` command ran **20 tests: 18 passed, 2 failed,
0 skipped**. Both failures are `FtgoApplicationTest` context startup failures;
the captured cause is MySQL **`Access denied for user 'mysqluser'@'localhost'`**,
followed by Hibernate's missing `DialectResolutionInfo`. The host MySQL listener
is present, but the compose test database was not running. Snapshot build logs
also report the repository setup did not finish at its old MySQL image step.
No database credentials, host service state, or checked-in image were changed to
force a passing result. Test output and dependency reports are recorded with the
PR. End-to-end tests and Java 21 execution were **not run** for this inventory.

This does not replace the separate baseline ticket's module/test/e2e report.
No Java 21 runtime compatibility, schema upgrade, or UI flow is claimed by this
documentation-only change.

## Upstream evidence

Version choices use the published BOMs, not assumptions about what "latest" will
mean when implementation starts. Recheck release metadata before landing upgrades.

- [B0] [Boot 2.0.3 managed dependencies](https://docs.spring.io/spring-boot/docs/2.0.3.RELEASE/reference/html/appendix-dependency-versions.html).
- [B27] [Boot 2.7.18 system requirements](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/getting-started.html#getting-started.system-requirements) and [managed dependency versions](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/dependency-versions.html).
- [B4] [Boot 4.1 requirements](https://docs.spring.io/spring-boot/4.1/system-requirements.html), [managed dependencies](https://docs.spring.io/spring-boot/4.1/appendix/dependency-versions/coordinates.html), and [immutable 4.1.2 BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.2/spring-boot-dependencies-4.1.2.pom).
- [G] [Gradle Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html#java_runtime) and [Gradle 8.5 Java 21 release notes](https://docs.gradle.org/8.5/release-notes.html).
- [DM] [Dependency-management plugin 1.1.7 requirements](https://docs.spring.io/dependency-management-plugin/docs/1.1.7/reference/html/).
- [F] [Flyway Gradle requirements](https://documentation.red-gate.com/flyway/reference/usage/gradle-task), [Java requirements](https://documentation.red-gate.com/flyway/reference/usage/api-java), and [bridge plugin 9.22.3](https://plugins.gradle.org/plugin/org.flywaydb.flyway/9.22.3). Current requirements do not prove historical Java 21 support; selected migration tasks must be exercised.
- [V] [Versions plugin 0.52.0 release](https://github.com/ben-manes/gradle-versions-plugin/releases/tag/v0.52.0).
- [SD] [springdoc current compatibility matrix](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) and [Boot 2 / springdoc 1.x documentation](https://springdoc.org/v1/).
- [J] [Jackson 3 release/namespace requirements](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0) and [migration guide](https://github.com/FasterXML/jackson/wiki/Jackson-3.0-Migration-Guide).
- [RA] [REST Assured changelog](https://github.com/rest-assured/rest-assured/blob/master/changelog.txt), especially 6.0.0/6.0.1 for Spring 7, Jackson 3, Groovy 5, and Jakarta fixes.
- [H56] [Hibernate 5.6 support matrix and EOL](https://hibernate.org/orm/releases/5.6/); [H62] [Hibernate 6.2 Java matrix](https://hibernate.org/orm/releases/6.2/).
- [BB] [Byte Buddy Java class-file support table](https://github.com/raphw/byte-buddy#java-version-compatibility).
- [MY] [Connector/J 9.7 release notes: MySQL 8.0+](https://dev.mysql.com/doc/relnotes/connector-j/en/news-9-7-0.html) and [version-specific Java/MySQL compatibility](https://docs.oracle.com/cd/E17952_01/connector-j-en/connector-j-versions.html).
- [M4] [Boot 4 migration guide: modules, test starters, Jackson, and Flyway](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide).

Maven Central returned HTTP 429 from this VM during metadata inspection; the
published BOM POMs were also read through Google's read-only Maven Central mirror
(`https://maven-central.storage-download.googleapis.com/maven2/`). The Eventuate
0.1.0.RELEASE POM returned 404 there; no unsupported replacement version was inferred.
