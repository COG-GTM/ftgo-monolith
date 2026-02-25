# Code Quality Configuration

## Overview

The FTGO Platform uses a comprehensive suite of static analysis tools to maintain code quality across all microservices. These tools are configured as a Gradle convention plugin (`ftgo.code-quality-conventions`) and enforced via GitHub Actions CI pipelines.

## Architecture

```
buildSrc/
├── src/main/groovy/
│   └── ftgo.code-quality-conventions.gradle   # Convention plugin
└── src/main/resources/config/
    ├── checkstyle/
    │   ├── checkstyle.xml          # Style rules
    │   └── suppressions.xml        # Exclusions for legacy code
    ├── pmd/
    │   └── pmd-ruleset.xml         # Code quality rules
    └── spotbugs/
        └── spotbugs-exclude.xml    # Bug detection exclusions

.github/workflows/
└── ci-code-quality.yml             # CI quality gate workflow
```

## Tools

### Checkstyle (v10.14.0)

Style enforcement based on a customized Google Java Style.

**Key rules:**
- Naming conventions (packages, classes, methods, variables)
- Import management (no star imports, no unused imports)
- Code structure (braces, whitespace, modifiers)
- Size limits (500 lines per file, 80 lines per method, 8 parameters)

**Suppressed for:**
- Generated code (`**/generated/**`)
- Test files (relaxed star import and method length rules)

### PMD (v6.55.0)

Detects common programming mistakes and potential bugs.

**Rule categories:**
- **Best practices**: Unused fields, variables, parameters
- **Code style**: Unnecessary imports, redundant code
- **Design**: Cyclomatic complexity (max 15), NPath complexity (max 200)
- **Error prone**: Empty blocks, null check issues, exception handling
- **Performance**: String and BigInteger instantiation
- **Security**: Hardcoded crypto keys, insecure IVs

### SpotBugs

Bytecode analysis for potential bugs (configured in CI workflow).

**Excluded patterns:**
- Serialization warnings on JPA entities
- EI_EXPOSE_REP on DTOs and request/response objects
- Generated code

### JaCoCo (v0.8.11)

Code coverage measurement and enforcement.

**Thresholds:**
| Metric | Minimum |
|--------|---------|
| Line coverage | 70% |
| Branch coverage | 50% |

**Excluded from coverage:**
- Configuration classes
- Application entry points
- Generated code

## Usage

### Apply to a Module

Add to your module's `build.gradle`:

```groovy
plugins {
    id 'ftgo.code-quality-conventions'
}
```

### Run Quality Checks

```bash
# All quality checks
./gradlew :services:ftgo-order-service:qualityCheck

# Individual tools
./gradlew :services:ftgo-order-service:checkstyleMain
./gradlew :services:ftgo-order-service:pmdMain
./gradlew :services:ftgo-order-service:jacocoTestReport

# Coverage verification (fails build if below threshold)
./gradlew :services:ftgo-order-service:jacocoTestCoverageVerification
```

### View Reports

Reports are generated in each module's `build/reports/` directory:

- `build/reports/checkstyle/main.html`
- `build/reports/pmd/main.html`
- `build/reports/jacoco/test/html/index.html`

## CI Integration

The `ci-code-quality.yml` workflow runs on every PR targeting the migration branch:

1. **Checkstyle** - Style violations fail the build
2. **PMD** - Code quality issues fail the build
3. **SpotBugs** - Bug patterns fail the build
4. **JaCoCo** - Coverage below threshold fails the build
5. **SonarQube** - Quality gate evaluation (when configured)

Reports are uploaded as artifacts and available for download from the workflow run.

## SonarQube Integration

The CI pipeline includes SonarQube scanner configuration for deeper analysis:

```bash
# Triggered automatically in CI with:
./gradlew sonar \
  -Dsonar.host.url=$SONAR_HOST_URL \
  -Dsonar.token=$SONAR_TOKEN
```

Quality gate criteria:
- Coverage > 70% on new code
- 0 new bugs
- 0 new vulnerabilities
- < 3% duplicated lines on new code

## Customizing Rules

### Adding Checkstyle Suppressions

Edit `buildSrc/src/main/resources/config/checkstyle/suppressions.xml`:

```xml
<suppress checks="MethodLength" files="YourSpecificFile\.java"/>
```

### Adjusting PMD Rules

Edit `buildSrc/src/main/resources/config/pmd/pmd-ruleset.xml` to add or remove rules.

### Excluding from SpotBugs

Edit `buildSrc/src/main/resources/config/spotbugs/spotbugs-exclude.xml` to add exclusion patterns.
