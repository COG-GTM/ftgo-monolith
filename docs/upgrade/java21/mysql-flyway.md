# MySQL 8.4 / Flyway 10 (AB-569)

The local image and GitHub Actions services use `mysql:8.4` (LTS, linux/amd64 and
linux/arm64). The image still runs `mysql/schema.sql` on first initialization to
create `ftgo` and grant `mysqluser` access. V1 and V2 are unchanged.

## Start a fresh database

Run Gradle on **JDK 17+ (21 recommended)**: Flyway 10 cannot run on JDK 8. Keep
JDK 8 installed for the bridge's Java 8 toolchain and default tests. CI installs
8 first, then 21, so `JAVA_HOME` points at 21 while both toolchains are available.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 # use your local JDK 21 path
export PATH="$JAVA_HOME/bin:$PATH"
export DOCKER_HOST_IP=localhost
docker compose up -d --build mysql
./gradlew waitForMySql :ftgo-flyway:flywayMigrate :ftgo-flyway:flywayValidate
./gradlew build -x :ftgo-end-to-end-tests:test
java -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081
# In another terminal with DOCKER_HOST_IP=localhost:
./run-end-to-end-tests.sh
```

If a host MySQL service occupies 3306, stop that service before starting Compose.
Do not force `platform: linux/amd64` on an arm64 machine; the base image is multi-arch.

`ftgo-flyway` pins the Gradle plugin and buildscript `flyway-mysql` module to the
same version (10.22.0). The module belongs on the **plugin** classpath, not just
the application classpath. The local Flyway URL uses `useSSL=false` and
`allowPublicKeyRetrieval=true` for MySQL 8's caching_sha2_password authentication.
These are existing development root credentials, not a production TLS policy.
Production must use verified TLS and externally managed credentials.

## Reset an existing MySQL 5.7 development volume

**Destructive: this removes all databases/volumes in this Compose project. Back up
anything needed first.** Never mount a 5.7 data directory directly into 8.4.

```bash
docker compose down -v
docker compose up -d --build mysql
./gradlew waitForMySql :ftgo-flyway:flywayMigrate :ftgo-flyway:flywayValidate
```

If data must be kept, plan a separate backed-up logical export/import or the
supported staged MySQL upgrade path (5.7 -> 8.0 -> 8.4); this reset is for disposable
development data only. Do not edit historical migrations or run `flywayRepair` to
hide a checksum mismatch.

## Repeatable compatibility checks

```bash
./ftgo-flyway/verify-compatibility.sh
```

This builds the same image and creates two disposable MySQL 8.4 instances on
random localhost ports. One is migrated/validated by Flyway 10 from scratch; the
other is first migrated/validated by the actual **Flyway 6.0.0 engine**, then
migrated/validated by Flyway 10. The legacy source set has an isolated classpath
and never adds Flyway 6 to the app or the Flyway plugin. It checks that V1/V2 history
checksums are identical before and after, without repair/baseline. Both scenarios
report V1 `712113782`, V2 `-206675981`. Containers and their anonymous volumes are
removed on exit. This tests Flyway history compatibility, not a physical MySQL
5.7 volume upgrade. GitHub Actions runs this check too.

On an amd64 host with QEMU/binfmt support, the arm64 MySQL image can be checked via:

```bash
DOCKER_DEFAULT_PLATFORM=linux/arm64 ./ftgo-flyway/verify-compatibility.sh
```

This exercises the arm64 database binaries under emulation; it is not a native
arm64 JVM/Gradle test. No platform override is needed on a native arm64 host.

### DATETIME / Connector-J

`MySqlDateTimeTest` uses the real Hibernate mapping for `Courier.lastLocationUpdate`,
Connector/J 8.4.0 (the AB-567 catalog version), and a migrated MySQL database. It flushes, checks the literal
stored SQL `DATETIME`, clears the persistence context, and reloads the entity.
It covers winter/summer, both sides of the DST gap, and the autumn overlap, with
session time zones UTC, +05:30 and -08:00. Run on UTC and non-UTC JVMs:

```bash
./gradlew :ftgo-application:test --tests '*MySqlDateTimeTest'
./gradlew :ftgo-application:test --tests '*MySqlDateTimeTest' -PtestTimeZone=America/Los_Angeles
# Also supported during the bridge: -PtestJavaVersion=21
```

Valid local wall-clock values retain their exact stored/reloaded value; no
`serverTimezone=UTC` conversion or schema edit is needed. JDBC session settings
are restored and test inserts roll back.

**AB-570 handoff / known limitations:**

- Observed: Hibernate 5.6 on a Los Angeles JVM normalizes the nonexistent local
  `2026-03-08T02:30:00` to `03:30:00` via its timestamp conversion. This is not a
  valid wall-clock time in that JVM zone. Sev 3: edge case with the UTC JVM
  workaround. No converter is introduced in AB-569; reassess during the Hibernate
  6 cut-over. The regression cases intentionally use valid local times.
- Flyway 10.22.0 emits a warning that MySQL 8.4 is newer than its tested-through
  8.1 version. Actual migrations and validation pass. The inspected Flyway 11.8.2
  and 11.14.1 sources use the same version guard; do not suppress the warning or
  mistake it for a checksum failure.
- Per coordinator confirmation, AB-402 has not replaced the legacy Kubernetes
  StatefulSet. Only its image is pinned to `mysql:8.4` and the rejected
  `--ignore-db-dir` flag removed here. No Kubernetes deployment was performed.
  AB-571 must coordinate with AB-402 for chart/bootstrap handling: the official
  image does not contain our `schema.sql`, unlike the old custom image, so a fresh
  PVC requires schema/user-grant initialization before Flyway runs.

## Architecture review

Manual triage: **ARB_REQUIRED**, T2 (MySQL engine 5.7 -> 8.4) and T7 (Flyway major
upgrade / Gradle runtime 17+). These are already covered by the proposed
[ADR 0002](../../adr/0002-java-21-spring-boot-3.md), including its diagram, NFRs,
security, ownership and cost questions. No duplicate ADR is introduced.
ARB ticket remains **TO BE CREATED** (there is no ARB Jira project). No Jira write
or merge is part of this ticket. The mechanical detector was blocked by an
environment guardrail; the diff was reviewed manually against the same T1-T9 rules.

References: [Flyway Gradle plugin / MySQL classpath](https://documentation.red-gate.com/flyway/reference/usage/gradle-task),
[Connector/J datetime processing](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-datetime-types-processing.html).
