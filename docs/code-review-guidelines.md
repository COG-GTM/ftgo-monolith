# Code Review Guidelines

## Overview

This document provides detailed code review guidelines for the FTGO project.
For a quick-start guide, see [CONTRIBUTING.md](../CONTRIBUTING.md).

## Quality Gate Configuration

### Static Analysis Tools

The FTGO project uses the following static analysis tools, configured via the
`ftgo.quality-gate-conventions` Gradle convention plugin:

| Tool       | Version  | Purpose                          | Config Location                       |
|------------|----------|----------------------------------|---------------------------------------|
| Checkstyle | 8.45.1   | Code style enforcement           | `config/checkstyle/checkstyle.xml`    |
| PMD        | 6.55.0   | Static analysis (bugs/smells)    | `config/pmd/pmd-ruleset.xml`          |
| FindBugs   | 3.0.1    | Bytecode-level bug detection     | `config/spotbugs/spotbugs-exclude.xml`|
| JaCoCo     | 0.8.8    | Code coverage measurement        | Convention plugin (inline config)     |

> **Note:** When the project upgrades to Gradle 7.5+ and Java 11+, the tools
> will be upgraded: Checkstyle 10.x, PMD 7.x, SpotBugs 4.x, JaCoCo 0.8.12.

### Applying Quality Gates

To apply quality gates to a module, add the convention plugin in `build.gradle`:

```groovy
apply plugin: 'ftgo.quality-gate-conventions'
```

This plugin automatically applies Checkstyle, PMD, FindBugs, and JaCoCo with
standardized configuration. It also registers a `qualityGate` task that runs
all checks in sequence.

### Coverage Thresholds

| Metric           | Minimum Threshold |
|------------------|-------------------|
| Line coverage    | 70%               |
| Branch coverage  | 60%               |

### Running Quality Checks

```bash
# All quality checks at once
./gradlew qualityGate

# Individual tools
./gradlew checkstyleMain    # Checkstyle
./gradlew pmdMain           # PMD
./gradlew findbugsMain      # FindBugs
./gradlew jacocoTestReport  # Coverage report
./gradlew jacocoTestCoverageVerification  # Coverage enforcement
```

### Report Locations

After running quality checks, reports are available at:

| Tool       | Report Path                                  |
|------------|----------------------------------------------|
| Checkstyle | `build/reports/checkstyle/main.html`         |
| PMD        | `build/reports/pmd/main.html`                |
| FindBugs   | `build/reports/findbugs/main.html`           |
| JaCoCo     | `build/reports/jacoco/test/html/index.html`  |

## Checkstyle Rules Summary

The Checkstyle configuration (`config/checkstyle/checkstyle.xml`) enforces:

### Naming Conventions
- **Packages**: `all.lowercase.dotted`
- **Types**: `UpperCamelCase`
- **Members/Parameters/Variables**: `lowerCamelCase`
- **Methods**: `lowerCamelCase` (underscores allowed for test methods)
- **Constants**: `UPPER_SNAKE_CASE` (exception: `log`/`logger`)

### Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Braces**: Required for all control structures (K&R style)
- **Imports**: No star imports; no unused or redundant imports
- **Empty lines**: Between major elements; no multiple empty lines

### Coding
- `equals()` and `hashCode()` must be overridden together
- Switch statements must have default case
- No fall-through in switch without comment
- Boolean expressions/returns should be simplified

### Suppressions

Certain checks are relaxed for test code and generated code. See
`config/checkstyle/suppressions.xml` for details.

## PMD Rules Summary

The PMD configuration (`config/pmd/pmd-ruleset.xml`) checks for:

### Best Practices
- Unused variables, fields, methods, and parameters
- Parameter reassignment
- Missing `@Override` annotations
- Proper stack trace preservation

### Design
- Cyclomatic complexity (max 15 per method)
- NPath complexity (max 200)
- Coupling between objects (max 25)
- Collapsible if statements

### Error-Prone Patterns
- Empty catch/if/while/try/finally blocks
- Null check issues
- Equals/hashCode contract violations
- Unconditional if statements

### Performance
- Unnecessary object creation (String, BigInteger)
- Inefficient string operations

## FindBugs Rules Summary

The FindBugs exclusion filter (`config/spotbugs/spotbugs-exclude.xml`) excludes:

- Generated code and generated sources
- Lombok-generated code in DTO/API/event/command packages
- Test classes (unit and integration)
- Spring configuration classes (for unread field warnings)
- Serialization warnings for Spring-managed beans

## CI Pipeline Integration

Quality gates are enforced in the CI pipeline via the `ci-quality-gate.yml`
GitHub Actions workflow. This workflow:

1. Runs Checkstyle on all shared libraries and services
2. Runs PMD on all shared libraries and services
3. Runs FindBugs on all shared libraries and services
4. Generates JaCoCo coverage reports
5. Verifies coverage thresholds
6. Uploads all reports as build artifacts

**Quality gate failures will be visible in the PR checks.** All quality checks
must pass before a PR can be merged.

## CODEOWNERS

The `.github/CODEOWNERS` file automatically assigns reviewers based on the
files changed in a PR:

| Path Pattern                | Assigned Team               |
|-----------------------------|-----------------------------|
| `services/ftgo-order-*`    | `@COG-GTM/ftgo-order-team`  |
| `services/ftgo-consumer-*` | `@COG-GTM/ftgo-consumer-team` |
| `services/ftgo-restaurant-*`| `@COG-GTM/ftgo-restaurant-team` |
| `services/ftgo-courier-*`  | `@COG-GTM/ftgo-courier-team` |
| `shared/*`                  | `@COG-GTM/ftgo-platform`    |
| `buildSrc/`, `.github/`    | `@COG-GTM/ftgo-platform`    |
| Everything else             | `@COG-GTM/ftgo-maintainers` |

## Upgrading Quality Tools

When upgrading to Gradle 7.5+ and Java 11+:

1. Update `FtgoQualityGatePlugin.groovy`:
   - Replace `findbugs` plugin with `com.github.spotbugs`
   - Add SpotBugs Gradle plugin dependency to `buildSrc/build.gradle`
   - Update tool versions (Checkstyle 10.x, PMD 7.x, SpotBugs 4.x)
   - Change report properties from `.enabled` to `.required`
2. Update `config/pmd/pmd-ruleset.xml` for PMD 7.x rule format changes
3. Rename `config/spotbugs/spotbugs-exclude.xml` namespace if needed
4. Update CI workflow tool references
