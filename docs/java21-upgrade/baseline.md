# Java 21 upgrade — JDK 8 baseline (plan step s1.1)

Snapshot of `master` @ `1fb9c78d` before any upgrade work, recorded 2026-11-05 on the Devin dev VM.
Use it to tell regressions caused by the upgrade apart from problems that were already on `master`.

**Verdict: master is not green on JDK 8.** Everything compiles and every unit test passes, but
`FtgoApplicationTest` (2/2) and the end-to-end suite (2/2) fail every time with HTTP 500 on
`POST /orders`. This is an existing schema-mapping bug (see [Known failures](#known-failures-on-master)),
not something caused by the upgrade.

## Environment

| Item | Value |
|---|---|
| JDK | OpenJDK 1.8.0_504 (`/usr/lib/jvm/java-8-openjdk-amd64`), Parallel GC, default max heap 7.8 GiB (1/4 of 32 GB RAM) |
| Gradle | 4.10.2 (wrapper), Groovy 2.4.15 |
| Spring Boot | 2.0.3.RELEASE (Spring Framework 5.0.7) |
| MySQL | `mysql:5.7.13` via `docker compose up -d --build mysql` (host MySQL 8 stopped first: `sudo systemctl stop mysql`) |
| Flyway | Gradle plugin 6.0.0; applied `V1__create_ftgo_db`, `V2__add_courier_optimization_and_api_tracking` |
| Host | 8 vCPU, 32 GB RAM, Ubuntu 22.04; port 8080 taken by nginx, app run on 8081 |

## Commands run

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 PATH=$JAVA_HOME/bin:$PATH DOCKER_HOST_IP=localhost
sudo systemctl stop mysql
docker compose up -d --build mysql                       # 10.7s
./gradlew waitForMySql :ftgo-flyway:flywayMigrate         # BUILD SUCCESSFUL, 19.7s (includes daemon start)
./gradlew clean
./gradlew -x :ftgo-end-to-end-tests:test build --profile --continue   # BUILD FAILED, 39.6s
java -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081
./run-end-to-end-tests.sh                                 # BUILD FAILED, 3.0s
```

`--continue` was added so that one failing module would not hide the results of the others; `--profile`
gives the per-module times. Without `--continue`, the documented command stops at `:ftgo-application:test`.

## Per-module results

Times are from the Gradle profile report (clean build, warm daemon, warm dependency cache).
"Tests" are JUnit 4 results from `build/test-results/test`.

| Module | Compiles | Tests run | Passed | Failed | Skipped | Task time |
|---|---|---|---|---|---|---|
| `common-swagger` | yes | – (no tests) | – | – | – | 7.1s |
| `ftgo-common` | yes | 7 (`MoneyTest` 4, `MoneySerializationTest` 3) | 7 | 0 | 0 | 3.8s |
| `ftgo-common-jpa` | yes (no sources) | – | – | – | – | 0.0s |
| `ftgo-domain` | yes | 9 (`CourierAssignmentStrategyTest`) | 9 | 0 | 0 | 0.8s |
| `ftgo-consumer-service-api` | yes | – | – | – | – | 0.7s |
| `ftgo-consumer-service` | yes | – | – | – | – | 0.8s |
| `ftgo-restaurant-service-api` | yes | – | – | – | – | 0.1s |
| `ftgo-restaurant-service` | yes | – | – | – | – | 0.1s |
| `ftgo-courier-service-api` | yes | – | – | – | – | 0.1s |
| `ftgo-courier-service` | yes | – | – | – | – | 0.8s |
| `ftgo-order-service-api` | yes | – | – | – | – | 0.1s |
| `ftgo-order-service` | yes | 2 (`OrderControllerTest`) | 2 | 0 | 0 | 5.4s |
| `ftgo-test-util` | yes | – | – | – | – | 0.0s |
| `ftgo-end-to-end-tests-common` | yes | – | – | – | – | 3.4s |
| `ftgo-application` | yes (boot jar built, 47 MB) | 2 (`FtgoApplicationTest`, needs MySQL) | 0 | **2** | 0 | 15.6s (test 10.0s) |
| `ftgo-flyway` | n/a (migrations only) | – | – | – | – | 0.0s |
| `ftgo-end-to-end-tests` | yes | excluded from `build`; see below | | | | |
| **Total** | **16/16 compile** | **20** | **18** | **2** | **0** | **39.5s total build** |

`ftgo-order-service` sets up an `integrationTest` source set (`IntegrationTestsPlugin`), but
`src/integration-test` does not exist, so it has no integration tests.

## End-to-end result

`./run-end-to-end-tests.sh` against the jar on :8081: **2 run, 0 passed, 2 failed, 0 skipped** (3.0s).

| Test | Result |
|---|---|
| `EndToEndTests.shouldCreateReviseAndCancelOrder` | FAILED: `Expected status code <200> doesn't match actual status code <500>` at `AbstractEndToEndTests.createOrder` |
| `EndToEndTests.shouldDeliverOrder` | FAILED: same |

The steps before order creation pass: consumer and restaurant creation, plus their GET checks.

## Known failures on master

**Not flaky. It fails the same way every time:** 3 out of 3 runs of both `:ftgo-application:test` and
`./run-end-to-end-tests.sh` failed in the same way.

- Symptom: `POST /orders` → 500. The app log shows
  `SQLSyntaxErrorException: Unknown column 'latitude' in 'field list'` on
  `insert into orders (..., delivery_address_city, latitude, longitude, delivery_address_state, ...)`.
- Cause: commit `420f9057` ("Optimize courier assignment and add API tracking") added `latitude`/`longitude`
  to the `@Embeddable` `ftgo-common/.../Address.java`, and `V2__...sql` added the columns
  `orders.delivery_address_latitude` / `delivery_address_longitude`. But `ftgo-domain/.../DeliveryInformation.java`
  only has `@AttributeOverride`s for `street1/street2/city/state/zip`, so Hibernate maps the two new fields to
  `latitude`/`longitude` on `orders`, and those columns do not exist. `restaurants` and `courier` are fine
  because their columns are actually named `latitude`/`longitude`.
- Affected: `FtgoApplicationTest` (2), `EndToEndTests` (2), and any real order creation through the API.
- Not fixed here, as the ticket asked. These 4 failures are the "already red" set: the upgrade must not be blamed
  for them, and a green run after the upgrade should only be expected once they are fixed separately.

No other test failed or was skipped, and nothing was flaky across the 3 runs.

## Runtime baseline (`java -jar ... --server.port=8081`, no `-Xmx`)

| Metric | Value |
|---|---|
| `Started FtgoApplicationMain in` | 5.846s (JVM running for 6.36s) |
| Time from launch to first `200` on `/actuator/health` | 6.8s |
| RSS right after startup | 1,531 MB (1,530,980 KB) |
| RSS after 3 e2e + 2 app-test runs against it | 1,635 MB |
| `/actuator/health` | `{"status":"UP"}` |
| `/swagger-ui.html` | Renders (springfox 2.8.0). Controllers listed: api-tracking, consumer, courier, order, restaurant, sentry-config |
| Dashboard `/` | Renders. **It runs in mock/demo mode** (`static/js/app.js` "Mock API Client (Demo Mode)", `seedDemoData()`): the 12 orders / 5 restaurants / 4 couriers it shows come from in-browser seed data, not MySQL (`orders` table has 0 rows). A dashboard that renders is therefore no evidence that the backend works. |

Note: `docker-compose.yml` runs the app container with `JAVA_OPTS: -Xmx192m`. The figures above are for the
host jar with the default heap, so compare against them using the same flags.

Screenshots of `/actuator/health`, `/swagger-ui.html` and `/` are in the PR that adds this file.

## Dependency inventory (resolved, `:ftgo-application:runtimeClasspath`)

| Area | Artifact | Version | Java 21 / Boot 4 note |
|---|---|---|---|
| Build | Gradle wrapper | 4.10.2 | Does not run on JDK 11+ for this build; needs Gradle 8.x+. Uses `compile`/`testCompile`/`runtime` configurations (removed in Gradle 7) |
| Build | `spring-boot-gradle-plugin` / `io.spring.dependency-management` | 2.0.3.RELEASE / 1.0.3.RELEASE | |
| Build | `com.github.ben-manes.versions` | 0.20.0 | |
| Build | `org.flywaydb.flyway` Gradle plugin | 6.0.0 | |
| Build | `jcenter()` repository | – | Read-only/sunset; remove |
| Framework | Spring Boot | 2.0.3.RELEASE | |
| Framework | Spring Framework (`spring-core`, `spring-webmvc`, `spring-orm`) | 5.0.7.RELEASE | `javax.*` → `jakarta.*` at Boot 3 |
| Web | Tomcat embed | 8.5.31 | |
| Persistence | Hibernate ORM | 5.2.17.Final | `javax.persistence` |
| Persistence | HikariCP | 2.7.9 | |
| Persistence | MySQL driver | `mysql:mysql-connector-java:8.0.33` (relocated to `com.mysql:mysql-connector-j:8.0.33`) | Also hard-coded in `buildSrc` and `ftgo-common` |
| Validation | hibernate-validator | 6.0.10.Final | |
| JSON | jackson-databind / jsr310 | 2.9.6 (2.9.7 requested, downgraded by Boot BOM) | |
| API docs | springfox-swagger2 / swagger-ui | 2.8.0 | No Boot 3+ support; replace (e.g. springdoc) |
| Metrics | micrometer-core / registry-prometheus | 1.0.5 (1.0.4 requested) | |
| XML | `javax.xml.bind:jaxb-api` 2.3.0, `com.sun.xml.bind:jaxb-core/impl` 2.2.11, `javax.activation` 1.1.1 | | |
| Misc | `javax.el:javax.el-api` | 2.2.5 | |
| Misc | commons-lang | 2.6 | |
| Misc | guava | 20.0 | |
| Misc | snakeyaml | 1.19 | |
| Logging | logback-classic | 1.2.3 | |
| Test | JUnit | 4.12 (via `ftgo-test-util`, `spring-boot-starter-test`) | |
| Test | REST Assured | `com.jayway.restassured` 2.9.0 (consumer, restaurant, e2e-common) and `io.rest-assured` 3.0.6 (order, courier) | Two generations in one build |
| Test | `io.eventuate.util:eventuate-util-test` | 0.1.0.RELEASE | Only in `ftgo-end-to-end-tests-common` |
| Toolchain | `sourceCompatibility`/`targetCompatibility` | `1.8` (root `build.gradle`) | |

## Edge cases noted (not fixed)

1. **Already-failing tests on master**: the 4 `POST /orders` failures above. Nothing flaky was seen in 3 runs.
2. **Commented-out docker-compose plugin in `ftgo-order-service/build.gradle`**: the
   `com.avast.gradle:gradle-docker-compose-plugin:0.14.13` buildscript, `apply plugin: 'docker-compose'`, the
   `dockerCompose { integrationTests { ... } }` block and `integrationTest.dependsOn(integrationTestsComposeUp)` are all
   commented out ("Temporarily commenting out docker-compose plugin to allow basic building"). As a result
   `integrationTest` does not start MySQL on its own (and there are no integration tests anyway). `dockerComposePluginVersion=0.6.6`
   in `gradle.properties` is unused.
3. **CircleCI does not run.** `.circleci/config.yml` (config `version: 2`, `machine: true`, last changed 2018-12-13) is
   still in the repo, but there are no CircleCI statuses or checks on `master` (`commits/master/status` has 0 statuses,
   and there are 0 check runs). The 5 most recently closed PRs only show `security/snyk`, `license/snyk`, `Devin Review`
   and Socket Security. The repo has no `.github/workflows`. **So there is no CI that builds or tests this repo today**,
   and every build/test result for the upgrade has to come from local runs like this one until CI is added.
   `build-and-test-all.sh` (the CircleCI entry point) also uses the v1 `docker-compose` binary by default.
4. **`mysql:5.7.13` image**: it pulled and built fine on this VM. The repo's environment blueprint initialize step
   expects it to fail ("schema1 image no longer pulls") and that step errored during snapshot build, so pulling this
   image is a reproducibility risk for fresh machines.
5. **Default JDK on the VM is 21.** Without `JAVA_HOME` set to JDK 8, Gradle 4.10.2 does not run, so every baseline
   command above exports it.
6. Small things: `docker-compose.yml` `version` attribute is obsolete (compose v2 warns), `waitForMySql` loads the
   deprecated `com.mysql.jdbc.Driver`, the build uses Gradle features deprecated for Gradle 5.0, and `gradle.properties`
   still points `eventuateMavenRepoUrl` at the shut-down Bintray.
