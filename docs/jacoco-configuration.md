# JaCoCo Configuration

This document describes the JaCoCo (Java Code Coverage) configuration strategy for the FTGO project, including coverage thresholds, report formats, and CI integration.

---

## Overview

[JaCoCo](https://www.jacoco.org/jacoco/) is the code coverage tool for the FTGO project. It instruments Java bytecode to measure which lines, branches, and methods are exercised by tests.

| Property | Value |
|----------|-------|
| Tool | JaCoCo 0.8.7 |
| Compatibility | Java 8+ |
| Gradle Plugin | `jacoco` (built-in) |
| Report Formats | HTML, XML, CSV |

---

## Gradle Plugin Configuration

JaCoCo is configured through the Gradle `jacoco` plugin. The following configuration should be applied in each module's `build.gradle` (or via a convention plugin):

### Basic Setup

```groovy
apply plugin: 'jacoco'

jacoco {
    toolVersion = "0.8.7"
}

// Configure the JaCoCo agent for test execution
test {
    jacoco {
        enabled = true
        // Exclude test infrastructure classes from coverage
        excludes = ['**/test/**', '**/config/**']
    }
}
```

### Report Generation

```groovy
jacocoTestReport {
    dependsOn test  // Ensure tests run before report generation

    reports {
        xml.enabled = true     // For CI tools (SonarQube, Codecov)
        html.enabled = true    // For human review
        csv.enabled = false    // Not needed
    }

    // Exclude generated code and infrastructure from reports
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/config/**',
                '**/dto/**',
                '**/*Application.class',
                '**/*Configuration.class'
            ])
        }))
    }
}
```

### Coverage Verification (Enforcement)

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                // Overall instruction coverage
                minimum = 0.60  // 60% minimum for existing code
            }
        }

        rule {
            // Stricter threshold for new service modules
            element = 'CLASS'
            includes = ['net.chrisrichardson.ftgo.*.service.*']
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.80  // 80% for service classes
            }
        }

        rule {
            // Branch coverage for business logic
            element = 'CLASS'
            includes = ['net.chrisrichardson.ftgo.*.domain.*']
            limit {
                counter = 'BRANCH'
                value = 'COVEREDRATIO'
                minimum = 0.70  // 70% branch coverage for domain
            }
        }
    }
}

// Wire coverage check into the build lifecycle
check.dependsOn jacocoTestCoverageVerification
```

> **Note**: Coverage verification thresholds should be applied incrementally. Start with lower thresholds for existing monolith modules and enforce higher thresholds for new microservice code.

---

## Coverage Thresholds

### Target Coverage Levels

| Category | Line Coverage | Branch Coverage | Enforcement |
|----------|--------------|----------------|-------------|
| New microservice code | >= 80% | >= 70% | Build failure |
| Existing monolith code | >= 60% | >= 50% | Warning |
| Domain entities | >= 80% | >= 70% | Build failure |
| Controllers | >= 70% | >= 60% | Warning |
| DTOs / Value objects | N/A | N/A | Excluded |
| Configuration classes | N/A | N/A | Excluded |

### Ratcheting Strategy

Coverage thresholds should be ratcheted upward over time:

1. **Phase 1 (Current)**: Establish baselines for existing code
2. **Phase 2**: Enforce 60% minimum for all modules
3. **Phase 3**: Enforce 80% for new microservice code
4. **Phase 4**: Gradually increase monolith coverage as code is migrated

---

## Report Formats

### HTML Reports

Interactive HTML reports are generated under:

```
<module>/build/reports/jacoco/test/html/index.html
```

HTML reports provide:
- Package-level coverage summary
- Class-level coverage breakdown
- Source-level line highlighting (green = covered, red = missed, yellow = partially covered)
- Method-level coverage metrics

### XML Reports

Machine-readable XML reports for CI tools:

```
<module>/build/reports/jacoco/test/jacocoTestReport.xml
```

Used by:
- SonarQube for coverage visualization
- Codecov / Coveralls for PR coverage comments
- Custom scripts for trend analysis

### Binary Execution Data

JaCoCo stores raw execution data in:

```
<module>/build/jacoco/test.exec
```

This binary format can be:
- Merged across modules for aggregate reports
- Used to generate reports offline
- Compared between runs for coverage diff

---

## Exclusion Patterns

### Classes Excluded from Coverage

The following patterns should be excluded from coverage measurement as they add noise without value:

```groovy
def jacocoExclusions = [
    // Spring Boot auto-configuration
    '**/*Application.class',
    '**/*Configuration.class',
    '**/*Config.class',

    // DTOs and API models (data holders, no logic)
    '**/dto/**',
    '**/api/**/*Request.class',
    '**/api/**/*Response.class',

    // JPA entities (getter/setter heavy, tested via integration)
    // Note: Domain entities WITH business logic should NOT be excluded

    // Generated code
    '**/generated/**',

    // Test infrastructure
    '**/test/**',
    '**/*Mother.class',
    '**/*TestData.class'
]
```

### Rationale

| Exclusion | Reason |
|-----------|--------|
| `*Application.class` | Spring Boot main class; just starts the context |
| `*Configuration.class` | Bean wiring; validated by integration tests |
| `**/dto/**` | Pure data carriers; no meaningful logic to cover |
| `**/api/**Request.class` | Request DTOs with only getters/setters |
| `**/*Mother.class` | Test data factories; not production code |

---

## Aggregate Coverage Reports

### Multi-Module Aggregation

For a project-wide coverage view, create an aggregate report task:

```groovy
// In root build.gradle or a dedicated reporting module
task jacocoRootReport(type: JacocoReport) {
    description = 'Generates aggregate JaCoCo coverage report'

    // Collect execution data from all subprojects
    executionData fileTree(dir: '.', includes: ['**/build/jacoco/*.exec'])

    // Collect source and class directories
    sourceDirectories.setFrom(subprojects.collect {
        it.sourceSets.main.allJava.srcDirs
    })
    classDirectories.setFrom(subprojects.collect {
        it.sourceSets.main.output
    })

    reports {
        xml.enabled = true
        html.enabled = true
    }
}
```

### Integration Test Coverage

JaCoCo can also measure coverage during integration tests:

```groovy
task jacocoIntegrationTestReport(type: JacocoReport) {
    executionData integrationTest
    sourceSets sourceSets.main

    reports {
        xml.enabled = true
        html.enabled = true
    }
}
```

---

## CI Integration

### Workflow Integration

JaCoCo reports are generated as part of the test workflows. The coverage data can be uploaded as artifacts:

```yaml
# Example CI step for coverage report upload
- name: Generate coverage report
  run: ./gradlew jacocoTestReport

- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: coverage-report-${{ matrix.service }}
    path: |
      **/build/reports/jacoco/
    retention-days: 14
```

### Coverage in PR Checks

Future integration with coverage tools can add PR-level coverage feedback:

| Tool | Integration | What it Shows |
|------|------------|---------------|
| Codecov | `codecov/codecov-action` | Coverage diff on PR, line annotations |
| SonarQube | `sonarqube-scan-action` | Coverage + quality metrics |
| JaCoCo Report | `madrapps/jacoco-report` | Coverage summary as PR comment |

### Coverage Enforcement in CI

Coverage verification can be added as a build step:

```yaml
- name: Check coverage thresholds
  run: |
    ./gradlew jacocoTestCoverageVerification \
      -x :ftgo-end-to-end-tests-common:test \
      -x :ftgo-end-to-end-tests:test
```

If any module falls below its configured threshold, the build fails.

---

## Tool Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| JaCoCo | 0.8.7 | Last version with full Java 8 support |
| Gradle Plugin | Built-in `jacoco` | No additional plugin needed |
| Java | 8 | JaCoCo 0.8.7 supports Java 8-17 |
| IntelliJ IDEA | Any | Reads `.exec` files natively |
| SonarQube | 7.9+ | Imports JaCoCo XML reports |

> **Important**: JaCoCo 0.8.7 is recommended for Java 8 projects. Newer versions (0.8.8+) require Java 11+ to run the JaCoCo agent itself.

---

## Local Development

### Generating Coverage Reports Locally

```bash
# Run tests with coverage and generate report
./gradlew test jacocoTestReport \
  -x :ftgo-end-to-end-tests-common:test \
  -x :ftgo-end-to-end-tests:test \
  -x :ftgo-application:test

# Open the report
open ftgo-order-service/build/reports/jacoco/test/html/index.html
```

### IDE Integration

Most Java IDEs support running tests with JaCoCo coverage:

- **IntelliJ IDEA**: Run > Run with Coverage (uses built-in JaCoCo)
- **Eclipse**: Install EclEmma plugin for JaCoCo integration
- **VS Code**: Use Java Test Runner extension with coverage support

---

## References

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Gradle JaCoCo Plugin](https://docs.gradle.org/4.10.2/userguide/jacoco_plugin.html)
- [Testing Strategy](testing-strategy.md) — Overall test approach
- [Test Reporting](test-reporting.md) — Report formats and CI artifacts
- [Quality Gates](quality-gates.md) — Coverage thresholds in quality gate context
