# Quality Gates Documentation

This document describes the static analysis tools, quality gate configurations, and enforcement strategy for the FTGO monolith and microservices migration project.

## Overview

Quality gates ensure that code meets minimum quality standards before being merged. The FTGO project uses three static analysis tools in combination with code review practices to maintain code quality throughout the microservices migration.

| Tool | Purpose | Configuration |
|------|---------|---------------|
| [Checkstyle](https://checkstyle.org/) | Code style and formatting enforcement | `config/checkstyle/checkstyle.xml` |
| [SpotBugs](https://spotbugs.github.io/) | Static bug pattern detection | `config/spotbugs/exclude-filter.xml` |
| [PMD](https://pmd.github.io/) | Code quality and best practices | `config/pmd/ruleset.xml` |

---

## Checkstyle

### Purpose

Checkstyle enforces consistent code style across the project. It checks naming conventions, import ordering, whitespace rules, and structural patterns.

### Key Rules

| Category | Rule | Setting |
|----------|------|---------|
| **Naming** | Package names | `^[a-z]+(\.[a-z][a-z0-9]*)*$` |
| **Naming** | Type names | `^[A-Z][a-zA-Z0-9]*$` (UpperCamelCase) |
| **Naming** | Method names | `^[a-z][a-zA-Z0-9]*$` (lowerCamelCase) |
| **Naming** | Constants | `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` (UPPER_SNAKE_CASE) |
| **Imports** | Star imports | Prohibited (static imports allowed) |
| **Imports** | Order | java, javax, org, com, net.chrisrichardson |
| **Formatting** | Indentation | 4 spaces (no tabs) |
| **Formatting** | Line length | 150 characters maximum |
| **Formatting** | File length | 500 lines maximum (warning) |
| **Coding** | Missing `@Override` | Error |
| **Coding** | Equals/hashCode | Must be defined together |
| **Coding** | Switch default | Required |
| **Coding** | Empty blocks | Must contain comment |

### Exclusions

- File-level Javadoc is informational only (not enforced as error).
- `@Override` annotation check is enabled to catch inheritance bugs.
- Utility class constructor check is a warning (not error).

### Running Checkstyle

```bash
# Once plugin is integrated into build.gradle:
./gradlew checkstyleMain checkstyleTest

# Or with standalone CLI:
java -jar checkstyle-*.jar -c config/checkstyle/checkstyle.xml src/
```

---

## SpotBugs

### Purpose

SpotBugs performs static analysis to detect potential bugs in Java bytecode. It identifies patterns like null pointer dereferences, resource leaks, concurrency issues, and security vulnerabilities.

### Exclusion Categories

The `exclude-filter.xml` defines exclusions for known false positives in the FTGO codebase:

| Category | Reason |
|----------|--------|
| **Spring Framework** | `@Autowired` fields appear unread; `@Value` fields appear unwritten |
| **JPA/Hibernate** | Entity fields are managed by the ORM, not direct code |
| **DTOs** | Request/Response objects are serialized/deserialized, not directly accessed |
| **Test Classes** | Test data factories and assertion patterns trigger false positives |
| **Value Objects** | `Money`, `Address` etc. have intentional comparison patterns |
| **Generated Code** | Lombok, MapStruct generated code is excluded |

### Bug Priority Levels

| Priority | Action | Threshold |
|----------|--------|-----------|
| High (P1) | Build failure | 0 bugs allowed |
| Medium (P2) | Warning | 5 max per module |
| Low (P3) | Informational | Tracked but not enforced |

### Running SpotBugs

```bash
# Once plugin is integrated into build.gradle:
./gradlew spotbugsMain

# View HTML report:
open build/reports/spotbugs/main.html
```

---

## PMD

### Purpose

PMD analyzes Java source code for potential problems including unused variables, empty catch blocks, unnecessary object creation, and overly complex code.

### Rule Categories

| Category | Included | Notable Exclusions |
|----------|----------|-------------------|
| **Best Practices** | Yes | `GuardLogStatement`, `JUnit*` assertion rules |
| **Code Style** | Yes | `ShortVariable`, `LongVariable`, `OnlyOneReturn` |
| **Design** | Yes (with limits) | `LawOfDemeter`, `DataClass`, `GodClass` |
| **Error Prone** | Yes | `BeanMembersShouldSerialize`, `DataflowAnomalyAnalysis` |
| **Multithreading** | Yes | `UseConcurrentHashMap`, `DoNotUseThreads` |
| **Performance** | Yes | `AvoidInstantiatingObjectsInLoops` |
| **Security** | Yes (all rules) | None |

### Complexity Thresholds

| Metric | Threshold |
|--------|-----------|
| Cyclomatic Complexity (method) | 15 |
| Cyclomatic Complexity (class) | 80 |
| NPath Complexity | 200 |
| Maximum methods per class | 25 |
| Maximum fields per class | 20 |

### PMD Priority Levels

| Priority | Action | Threshold |
|----------|--------|-----------|
| Priority 1-2 (High) | Build failure | 0 violations |
| Priority 3 (Medium) | Warning | 10 max per module |
| Priority 4-5 (Low) | Informational | Tracked only |

### Running PMD

```bash
# Once plugin is integrated into build.gradle:
./gradlew pmdMain

# View HTML report:
open build/reports/pmd/main.html
```

---

## Quality Gate Summary

### Build-Breaking Gates (Must Pass)

| Gate | Tool | Condition |
|------|------|-----------|
| No Checkstyle errors | Checkstyle | 0 errors |
| No high-priority bugs | SpotBugs | 0 P1 bugs |
| No critical PMD violations | PMD | 0 Priority 1-2 violations |
| Compilation succeeds | Gradle | Clean compile |
| Unit tests pass | JUnit | All tests green |

### Warning Gates (Should Pass)

| Gate | Tool | Condition |
|------|------|-----------|
| SpotBugs medium bugs | SpotBugs | <= 5 per module |
| PMD medium violations | PMD | <= 10 per module |
| Checkstyle warnings | Checkstyle | Trending downward |
| Test coverage (new code) | JaCoCo | >= 80% |

### Informational Gates (Tracked)

| Gate | Tool | Condition |
|------|------|-----------|
| Overall test coverage | JaCoCo | >= 60% target |
| SpotBugs low bugs | SpotBugs | Tracked |
| PMD low violations | PMD | Tracked |
| Code duplication | CPD | Tracked |

---

## CI Pipeline Integration

Quality gates are enforced in the CI pipeline. The pipeline configuration is managed separately in `.github/workflows/`.

### Pipeline Stages

```
compile -> checkstyle -> spotbugs -> pmd -> test -> coverage-report
```

Each stage must pass before the next one runs. Failures block the PR from merging.

### Local Pre-Commit Workflow

Developers should run quality checks locally before pushing:

```bash
# Full quality check (once plugins are integrated)
./gradlew check

# Quick compile check
./gradlew compileJava -x :ftgo-end-to-end-tests-common:compileJava -x :ftgo-end-to-end-tests:compileJava

# Run tests
./gradlew test -x :ftgo-end-to-end-tests-common:test -x :ftgo-end-to-end-tests:test -x :ftgo-application:test
```

---

## Suppressing Violations

### When to Suppress

Suppressions should be rare and well-justified. Valid reasons include:

- **False positives**: The tool incorrectly flags correct code.
- **Legacy code**: Existing code that will be refactored during migration.
- **Framework patterns**: Spring/JPA patterns that tools don't understand.

### How to Suppress

#### Checkstyle

```java
@SuppressWarnings("checkstyle:MethodLength")
public void complexMigrationMethod() {
    // Justified: This method orchestrates a multi-step migration
}
```

Or use `// CHECKSTYLE:OFF` / `// CHECKSTYLE:ON` for blocks (use sparingly).

#### SpotBugs

```java
@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Field is used by Spring DI")
private SomeService someService;
```

#### PMD

```java
@SuppressWarnings("PMD.TooManyMethods")
public class LargeServiceClass {
    // Justified: Service aggregates multiple business operations pre-extraction
}
```

Or use `// NOPMD` at end of line with justification comment.

### Suppression Review

All suppressions should be:
1. Accompanied by a justification comment.
2. Reviewed during code review.
3. Tracked for removal during migration cleanup phases.

---

## Tool Versions

| Tool | Recommended Version | Compatibility |
|------|-------------------|---------------|
| Checkstyle | 8.45.1 | Java 8+ |
| SpotBugs | 4.2.3 | Java 8+ |
| PMD | 6.55.0 | Java 8+ |
| JaCoCo | 0.8.7 | Java 8+ |

> **Note**: Version compatibility is important since the project uses Java 8. Newer versions of these tools may require Java 11+.

---

## References

- [Checkstyle Documentation](https://checkstyle.org/)
- [SpotBugs Documentation](https://spotbugs.readthedocs.io/)
- [PMD Documentation](https://pmd.github.io/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Code review guidelines and contribution process
- [EditorConfig](.editorconfig) - Editor formatting configuration
