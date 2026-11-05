# Java 21 operator upgrade and rollback

## Release contents and prerequisites

The integration branch moves Spring Boot 2.0.3 through the 2.7.18 bridge to
3.5.16, Spring Framework 6.2, Hibernate 6.6, Micrometer 1.15 and springdoc 2.10.0.
Application persistence/validation imports and ORM XML now use Jakarta rather
than javax. All modules compile with Java 21 (`--release 21`, `-parameters`).
The wrapper is Gradle 8.14.4. Run the current build on JDK 21, not JDK 8.
Docker and the Compose v2 plugin (`docker compose`) are required locally.

Sources: `git diff origin/master...HEAD`, `build.gradle`,
`gradle/libs.versions.toml`, [cut-over notes](ab-570-cutover.md),
[API comparison](ab-566-spec-diff.md), [MySQL/Flyway notes](mysql-flyway.md).
The production base image moves from `openjdk:8u171-jre-alpine` to
`eclipse-temurin:21-jre` in **AB-571**, not this ticket. Until that lands,
the checked-in application Dockerfile cannot run the Java 21 jar; use the host
command below or the explicit Temurin container used in the rehearsal.

## Configuration and monitoring

These are additions in the actual application.properties diff, not a blanket
list of Spring Boot renames. AB-570 ran the properties migrator and found no
renamed/removed keys in the application's existing configuration.

| Key | Current value / operational effect |
| --- | --- |
| `server.error.include-message` | `always`; preserves the Boot 2.0 error message field |
| `spring.jackson.date-format` | `yyyy-MM-dd'T'HH:mm:ss.SSSZ`; preserves the numeric timestamp offset |
| `spring.jackson.time-zone` | `UTC` |
| `springdoc.packages-to-scan` | `net.chrisrichardson.ftgo`; replaces the Springfox Java Docket package filter |
| `springdoc.api-docs.version` | `openapi_3_0`; prevents springdoc 2.10's default OpenAPI 3.1 switch |
| `management.endpoints.web.exposure.include` | `health,prometheus`; explicitly exposes Prometheus in addition to health |

Springfox Swagger 2 `/v2/api-docs` is replaced by OpenAPI 3 `/v3/api-docs`.
`/swagger-ui.html` remains a 302 redirect to `/swagger-ui/index.html`.
Health is `/actuator/health` (not `/health`), metrics export is
`/actuator/prometheus`. Update probes, spec clients and dashboards accordingly.

Micrometer's logical HTTP timer is `http.server.requests`; Prometheus exports
`http_server_requests_seconds_{count,sum,max}`. The AB-570 measured **2.7 bridge
to 3.5** comparison retains those names but changes `jvm_gc_overhead_percent` to
`jvm_gc_overhead` and `jvm_memory_usage_after_gc_percent` to
`jvm_memory_usage_after_gc`. New series include
`http_server_requests_active_seconds_{count,sum,max}`, `jdbc_connections_{active,idle}`,
`jvm_compilation_time_ms_total`, `jvm_info`, `jvm_threads_started_threads_total`
and `process_cpu_time_ns_total`.

The 3.5 rehearsal observed HTTP labels `error="none", exception="none",
method="GET", outcome="SUCCESS", service="ftgo-application", status="200",
uri="/consumers/{consumerId}"`. Preserve templated URI aggregation, and inspect
the live label set before applying dashboard filters. The repository does not
contain a measured Boot 2.0 metric/tag export: the 2.7 comparison is not evidence
that every Boot 2.0 tag or series is unchanged. In particular do not assume
exception/error labels alone capture handled application errors.

Review deployment JVM options before switching runtimes. The repo currently only
sets `JAVA_OPTS=-Xmx192m` in Compose; no PermGen/CMS flags were removed by the
cut-over. Do not carry external Java 8 PermGen (`-XX:PermSize`,
`-XX:MaxPermSize`) or CMS (`-XX:+UseConcMarkSweepGC`) options into Java 21:
these collectors/options are no longer supported. Revalidate any other external
`-XX` tuning against the new JVM instead of copying it blindly. No new heap
sizing is claimed here; AB-571 owns container configuration.

MySQL is now 8.4 LTS with Connector/J 8.4.0. Flyway Gradle plugin and its
`flyway-mysql` module are both 10.22.0. Flyway runs as a separate Gradle operation,
not automatically at app startup. The development JDBC URL adds
`allowPublicKeyRetrieval=true` and retains `useSSL=false`; these are local
credentials/settings, not production TLS guidance.

## Fresh-environment commands (verified 2026-11-05)

On this Ubuntu environment both default `java` and existing `JAVA_HOME` select
21.0.12.1. `env -u JAVA_HOME java -version` also selects 21: no new JAVA_HOME
export was needed. Elsewhere align JAVA_HOME and PATH with JDK 21 before Gradle.
Flyway 10 requires a Gradle JVM of at least 17; current Boot/build/toolchain
requirements make JDK 21 the supported choice for this branch.

```bash
sudo systemctl stop mysql  # only if the host service occupies 3306
docker compose up -d --build mysql
export DOCKER_HOST_IP=localhost
./gradlew waitForMySql :ftgo-flyway:flywayMigrate :ftgo-flyway:flywayValidate
./gradlew -x :ftgo-end-to-end-tests:test build
java -jar ftgo-application/build/libs/ftgo-application.jar --server.port=8081
# Another terminal, same repository:
export DOCKER_HOST_IP=localhost
curl -sS -o /dev/null -w '%{http_code} %{redirect_url}\n' http://localhost:8081/swagger-ui.html
curl -fsS http://localhost:8081/v3/api-docs
./run-end-to-end-tests.sh
```

Migration/validation: BUILD SUCCESSFUL. Build: 65 tests, zero failures/errors;
e2e: 2 tests, zero failures/errors. Swagger returns 302 to
`http://localhost:8081/swagger-ui/index.html`; OpenAPI responds successfully.
Port 8081 avoids the environment's nginx on 8080.

For disposable development data only, back up anything needed and run
`docker compose down -v`, then repeat database startup/migration above.
This deletes project volumes; never use it for rollback or mount MySQL 5.7
physical data directly into MySQL 8.4. See [mysql-flyway.md](mysql-flyway.md)
for logical export/import and staged MySQL upgrade considerations.

## Rollback

Redeploy the last Java 8 application image, leaving the MySQL 8.4 database and its
volume intact. The pre-cut-over integration tip is
`dfa0f14fa881e37cda871481d1652101d48b352e` (Merge pull request #396, Boot 2.7.18,
Java 8 application bytecode, already using Flyway 10). This is an **application
rollback**, not a MySQL engine downgrade or a return to the original Boot 2.0 tip.
Take normal production backups and stop incoming writes while switching images.

`git diff dfa0f14f HEAD -- ftgo-flyway/src/main/resources/db/migration` is empty:
V1 and V2 are byte-identical. Full `flyway_schema_history` rows were identical
before rollback, after rollback and after roll-forward (including timestamps and
execution times). Checksums are V1 `712113782`, V2 `-206675981`, success=1.
There is no cut-over schema change and **no data rollback is needed** for this
application rollback. Do not run repair, baseline, reverse SQL or delete history.
AB-569 separately tested actual Flyway 6-created history with Flyway 10 and
confirmed the same checksums; it did not test a physical MySQL downgrade.

### Rehearsed on 2026-11-05

The commands below used the same running Compose MySQL container and anonymous
volume throughout; neither was recreated. Stop the host jar first to free 8081.
Java 21 ran in Temurin without changing the AB-571-owned Dockerfile or Compose.

```bash
git worktree add /home/ubuntu/repos/ftgo-ab572-java8 dfa0f14f
# In that worktree, with the default JDK 21 Gradle JVM:
./gradlew :ftgo-application:assemble :ftgo-flyway:flywayValidate --info
docker build -t ftgo-ab572-java8 ftgo-application
# Back in the current Java 21 worktree:
docker run -d --name ftgo-ab572-java21 --network ftgo-monolith_default \
  -p 8081:8080 -v "$PWD/ftgo-application/build/libs:/app:ro" \
  -e 'SPRING_DATASOURCE_URL=jdbc:mysql://mysql/ftgo?useSSL=false&allowPublicKeyRetrieval=true' \
  eclipse-temurin:21-jre java -jar /app/ftgo-application.jar
curl --retry 30 --retry-all-errors --retry-delay 1 -fsS http://localhost:8081/actuator/health
curl -fsS -H 'Content-Type: application/json' \
  -d '{"name":{"firstName":"Rollback21","lastName":"AB572"}}' http://localhost:8081/consumers
docker compose exec -T mysql mysql -uroot -prootpassword \
  -e 'SELECT * FROM ftgo.flyway_schema_history' > /home/ubuntu/ab572-history-before.txt
docker stop ftgo-ab572-java21
docker run -d --name ftgo-ab572-java8 --network ftgo-monolith_default -p 8081:8080 \
  -e 'SPRING_DATASOURCE_URL=jdbc:mysql://mysql/ftgo?useSSL=false&allowPublicKeyRetrieval=true' \
  ftgo-ab572-java8
curl --retry 30 --retry-all-errors --retry-delay 1 -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8081/consumers/5
curl -fsS -H 'Content-Type: application/json' \
  -d '{"name":{"firstName":"Rollback8","lastName":"AB572"}}' http://localhost:8081/consumers
# In the Java 8 worktree, Gradle still on JDK 21:
DOCKER_HOST_IP=localhost ./gradlew :ftgo-flyway:flywayValidate
# Back in the Java 21 worktree:
docker compose exec -T mysql mysql -uroot -prootpassword \
  -e 'SELECT * FROM ftgo.flyway_schema_history' > /home/ubuntu/ab572-history-after.txt
diff -u /home/ubuntu/ab572-history-before.txt /home/ubuntu/ab572-history-after.txt
docker stop ftgo-ab572-java8
docker start ftgo-ab572-java21
curl --retry 30 --retry-all-errors --retry-delay 1 -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8081/consumers/5
curl -fsS http://localhost:8081/consumers/6
docker compose exec -T mysql mysql -uroot -prootpassword \
  -e 'SELECT * FROM ftgo.flyway_schema_history' > /home/ubuntu/ab572-history-forward.txt
diff -u /home/ubuntu/ab572-history-before.txt /home/ubuntu/ab572-history-forward.txt
```

Results: Java 21 created consumer 5; Java 8 health was UP, read its name
`Rollback21 AB572`, and created consumer 6. Roll-forward health was UP and both
names were readable. Both history diffs were empty; old-worktree Flyway validation
was BUILD SUCCESSFUL. GET responses have an existing `consumerId:0` field even
when fetching IDs 5/6; the names demonstrate persisted data readability, not
correctness of that response ID field. No fix to that unrelated behavior is made.

**Build caveat:** actually running the old wrapper with
`JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH ./gradlew :ftgo-application:assemble`
failed with `UnsupportedClassVersionError: org/flywaydb/gradle/FlywayPlugin ...
class file version 61.0 ... only recognizes ... 52.0`.
AB-569 already introduced Flyway 10 before this rollback tip. Building with the
default JDK 21 Gradle JVM succeeded; `--info` confirmed
`Compiling with toolchain '/usr/lib/jvm/java-8-openjdk-amd64'`.
The resulting jar ran in the original openjdk:8u171-jre-alpine Dockerfile image.
Do not describe this as a successful Gradle-on-JDK-8 build.

Known non-fatal warnings: Flyway 10.22 says MySQL 8.4 is newer than its tested
8.1 ceiling (documented in AB-569); Compose ignores its obsolete `version` key;
Gradle reports deprecated features for Gradle 9. These do not indicate missing
environment setup. Initial curls before container startup completed reset the
connection; wait for health before sending writes.
