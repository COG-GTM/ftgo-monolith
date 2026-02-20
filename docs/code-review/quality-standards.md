# FTGO Quality Standards

## Overview

This document defines the quality standards enforced across the FTGO project through automated tooling and manual review. All code merged into `feat/microservices-migration-v2` or `main` must meet these standards.

## Static Analysis Tools

### Checkstyle

Checkstyle enforces consistent Java code style across the project.

- **Configuration**: `config/checkstyle/checkstyle.xml`
- **Suppressions**: `config/checkstyle/checkstyle-suppressions.xml`
- **Severity**: Violations are treated as errors and block the build

#### Key Rules Enforced

| Category | Rule | Standard |
|---|---|---|
| Naming | Class names | `UpperCamelCase` |
| Naming | Method/variable names | `lowerCamelCase` |
| Naming | Constants | `UPPER_SNAKE_CASE` |
| Naming | Package names | `lowercase`, no underscores |
| Formatting | Line length | 150 characters max |
| Formatting | Indentation | 4 spaces, no tabs |
| Imports | Unused imports | Not allowed |
| Imports | Star imports | Not allowed (threshold: 999) |
| Design | Method length | 50 lines max |
| Design | Parameter count | 7 max |
| Design | File length | 500 lines max |

### SpotBugs

SpotBugs detects potential bugs through bytecode analysis.

- **Configuration**: `config/spotbugs/spotbugs-exclude.xml`
- **Effort**: `max` (most thorough analysis)
- **Report level**: `medium` (medium and high confidence bugs)

#### Bug Categories Checked

| Category | Description |
|---|---|
| Correctness | Likely bugs (null dereferences, infinite loops) |
| Bad Practice | Violations of recommended coding practice |
| Performance | Inefficient code patterns |
| Multithreading | Concurrency issues |
| Security | Potential security vulnerabilities |

## Quality Gate Thresholds

The CI quality gate (`quality-gate.yml`) enforces the following:

| Check | Requirement |
|---|---|
| Checkstyle | Zero violations |
| SpotBugs | Zero high-priority bugs |
| Unit Tests | All tests pass |
| Test Reports | Uploaded as artifacts |

## Running Checks Locally

### Full Quality Check

```bash
./gradlew check
```

This runs Checkstyle, SpotBugs, and tests for all modules.

### Individual Checks

```bash
# Checkstyle only
./gradlew checkstyleMain checkstyleTest

# SpotBugs only
./gradlew spotbugsMain

# Tests only
./gradlew test

# Specific module
./gradlew :services:order-service:check
./gradlew :libs:ftgo-common:checkstyleMain
```

### Viewing Reports

After running checks, reports are generated at:

| Tool | Report Location |
|---|---|
| Checkstyle | `<module>/build/reports/checkstyle/` |
| SpotBugs | `<module>/build/reports/spotbugs/` |
| Tests | `<module>/build/reports/tests/` |

## Pre-commit Hooks

A pre-commit hook configuration is provided to catch issues before they reach CI.

### Setup

```bash
# Install pre-commit (requires Python)
pip install pre-commit

# Install hooks
pre-commit install
```

### What Hooks Check

- Trailing whitespace removal
- End-of-file newline
- YAML syntax validation
- Large file prevention (max 500KB)
- Merge conflict marker detection
- Secret detection (credentials, API keys)
- Checkstyle on staged Java files

## Suppressing Rules

### Checkstyle Suppressions

Add entries to `config/checkstyle/checkstyle-suppressions.xml` for legitimate exceptions:

```xml
<suppress files=".*Test\.java" checks="MagicNumber"/>
```

### SpotBugs Suppressions

Add entries to `config/spotbugs/spotbugs-exclude.xml`:

```xml
<Match>
    <Class name="~.*\.dto\..*"/>
    <Bug pattern="EI_EXPOSE_REP"/>
</Match>
```

Or use `@SuppressFBWarnings` annotation in code (requires `spotbugs-annotations` dependency):

```java
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "DTO intentionally exposes mutable state")
```

## Continuous Improvement

Quality standards will evolve as the project matures. Proposed changes to rules should be:

1. Discussed in a PR modifying the relevant configuration
2. Reviewed by at least 2 team members
3. Applied consistently (no grandfathering of existing violations without a suppression plan)
