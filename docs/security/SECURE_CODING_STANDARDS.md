# Secure coding standards

These rules apply to every change in this repository. The `semgrep` and `snyk` CircleCI jobs enforce the automated ones.

## Enforced in CI

| Check | Tool | Fails the build when |
|---|---|---|
| Static analysis (SAST) | Semgrep `p/java`, `p/javascript`, `p/secrets`, `p/dockerfile` | any finding of severity `ERROR` |
| Dependency vulnerabilities (SCA) | Snyk Open Source (`snyk test --all-sub-projects`) | any `critical` issue not ignored in `.snyk` (runs on `master` only, with `SNYK_TOKEN` from the `security-scanning` CircleCI context; the context must exist, even empty, and the token stays out of project-level variables so branch builds can't read it. PRs are covered by the Snyk GitHub checks) |

Run them locally before opening a PR:

```sh
semgrep scan --config p/java --config p/javascript --config p/secrets --config p/dockerfile --severity ERROR --error .
snyk test --all-sub-projects --policy-path=.snyk --severity-threshold=critical
```

## Dependencies

- Patched minimum versions for vulnerable libraries live in one place: `securityBomProperties`, `securityGroupVersions` and `securityVersions` in the root `build.gradle`. They apply to every subproject, including those using `io.spring.dependency-management`.
- Never lower a version in those maps or remove an entry without a security review. When a scan reports a new issue, raise the floor there instead of pinning versions in individual modules.
- Do not add libraries that are end-of-life or have no maintained release line (for example `commons-lang:commons-lang`, `dom4j:dom4j`, `mysql:mysql-connector-java`; use `commons-lang3`, `org.dom4j:dom4j`, `com.mysql:mysql-connector-j`).

## Input handling (Java)

- Validate every request parameter, path variable and header that reaches a query, a log line or a response header. Bound numeric ranges and string lengths; reject with `IllegalArgumentException` (mapped to HTTP 400 by `GlobalExceptionHandler`).
- Use Spring Data derived queries or `@Query` with bound parameters. Never build JPQL/SQL by string concatenation.
- Never write raw client-supplied values (such as `X-Correlation-ID`) to logs or headers without validating them against an allow-list pattern.
- Error responses must not include stack traces or exception messages from unexpected errors; `GlobalExceptionHandler` returns a generic message for those.

## Output handling (dashboard JavaScript)

- Every value that comes from the API or from user input and is interpolated into `innerHTML` must go through `escapeHtml()`. Prefer `textContent` when no markup is needed.
- Do not use `eval`, `new Function` or inline event handler strings built from data.

## Secrets and configuration

- No credentials in source. Configuration reads secrets from environment variables (`FTGO_DB_USERNAME`, `FTGO_DB_PASSWORD`, `SENTRY_DSN`); the defaults in `application.properties` are for the local Docker MySQL only.
- Telemetry must not send PII by default (`SENTRY_SEND_DEFAULT_PII` defaults to `false`).

## Containers

- Images use a maintained base image and run as a non-root user.

## Known gaps

- The API has no authentication or authorization. Adding it changes the application's auth boundary and needs ARB review.
- Spring Boot 2.0 / Spring Framework 5.0 / Tomcat 8.5 are end-of-life. Nine critical Snyk issues are only fixed by a Spring Boot major upgrade (Tomcat 9+, Spring 5.2+ or later); they are ignored in `.snyk` until 2027-02-22 so new criticals still fail CI. Tomcat stays at 8.5.42 because Spring Boot 2.0 deadlocks on startup with 8.5.43+.
- The Spring Boot upgrade (2.7 is the last line supporting Java 8; 3.x needs Java 17) also needs a Gradle upgrade and is an ARB-reviewed change of its own.
