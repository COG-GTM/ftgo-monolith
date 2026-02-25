# Contributing to FTGO Platform

Thank you for contributing to the FTGO Platform microservices migration project. This guide establishes code review guidelines, quality standards, and development workflows to ensure consistent, high-quality code across all microservices.

## Table of Contents

- [Development Setup](#development-setup)
- [Code Style & Formatting](#code-style--formatting)
- [Static Analysis Tools](#static-analysis-tools)
- [Code Review Guidelines](#code-review-guidelines)
- [Pull Request Process](#pull-request-process)
- [Quality Gates](#quality-gates)
- [Branch Strategy](#branch-strategy)

---

## Development Setup

### Prerequisites

- **JDK 17** (Temurin recommended)
- **Gradle 8.5+** (use the included `gradlew` wrapper)
- **Docker & Docker Compose** (for local infrastructure)
- An IDE with EditorConfig support (IntelliJ IDEA, VS Code, Eclipse)

### Building the Project

```bash
# Full build (excluding known broken legacy modules)
./gradlew clean build \
  -x :ftgo-end-to-end-tests-common:compileJava \
  -x :ftgo-end-to-end-tests:compileJava \
  -x :ftgo-end-to-end-tests-common:build \
  -x :ftgo-end-to-end-tests:build \
  -x :ftgo-application:compileTestJava \
  -x :ftgo-application:test

# Build a specific service
./gradlew :services:ftgo-order-service:build

# Run tests for a specific module
./gradlew :shared:ftgo-common:test
```

### Running Quality Checks Locally

```bash
# Run all quality checks for a module
./gradlew :services:ftgo-order-service:qualityCheck

# Individual checks
./gradlew checkstyleMain      # Checkstyle style enforcement
./gradlew pmdMain             # PMD code quality
./gradlew jacocoTestReport    # JaCoCo coverage report
```

---

## Code Style & Formatting

### EditorConfig

The project uses `.editorconfig` for basic formatting rules. Ensure your IDE supports EditorConfig:

| Setting | Value |
|---------|-------|
| Charset | UTF-8 |
| Line endings | LF |
| Indent style | Spaces |
| Indent size | 4 (Java, Groovy), 2 (YAML, XML, JSON) |
| Max line length | 120 characters |
| Trailing whitespace | Trimmed |
| Final newline | Required |

### Java Conventions

- **Package naming**: `com.ftgo.<context>.<layer>` (e.g., `com.ftgo.order.service`)
- **Class naming**: PascalCase (e.g., `OrderService`, `CreateOrderRequest`)
- **Method naming**: camelCase (e.g., `createOrder`, `findByCustomerId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **No star imports**: Use explicit imports only
- **Annotations**: Place on separate lines above the declaration
- **Braces**: Required for all control structures (if, else, for, while)

### Convention Plugins

New microservices should apply the convention plugins from `buildSrc/`:

```groovy
plugins {
    id 'org.springframework.boot'
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.code-quality-conventions'
}
```

---

## Static Analysis Tools

### Checkstyle

Enforces consistent code style based on a customized Google Java Style configuration.

- **Config**: `buildSrc/src/main/resources/config/checkstyle/checkstyle.xml`
- **Suppressions**: `buildSrc/src/main/resources/config/checkstyle/suppressions.xml`
- **Reports**: `build/reports/checkstyle/`

### PMD

Detects common programming flaws: unused variables, empty catch blocks, unnecessary object creation.

- **Ruleset**: `buildSrc/src/main/resources/config/pmd/pmd-ruleset.xml`
- **Reports**: `build/reports/pmd/`

### SpotBugs

Finds potential bugs through bytecode analysis.

- **Exclusions**: `buildSrc/src/main/resources/config/spotbugs/spotbugs-exclude.xml`
- **Reports**: `build/reports/spotbugs/`

### JaCoCo (Code Coverage)

Measures test code coverage with the following thresholds:

| Metric | Minimum |
|--------|---------|
| Line coverage | 70% |
| Branch coverage | 50% |

Coverage exclusions:
- Configuration classes (`*Config.class`, `*Configuration.class`)
- Application entry points (`*Application.class`)
- Generated code

### SonarQube

Quality gate criteria enforced in CI:

| Metric | Threshold |
|--------|-----------|
| Code coverage | > 70% |
| New bugs | 0 |
| New vulnerabilities | 0 |
| New code smells | Acceptable (A rating) |
| Duplicated lines | < 3% |

---

## Code Review Guidelines

### Review Checklist

Every code review should evaluate the following areas:

#### Correctness
- [ ] Code does what it claims to do
- [ ] Edge cases are handled
- [ ] Error handling is comprehensive and appropriate
- [ ] No null pointer risks (use `Optional` where appropriate)
- [ ] Concurrent access is handled correctly (if applicable)

#### Security
- [ ] No hardcoded secrets, API keys, or passwords
- [ ] Input validation is performed on all external inputs
- [ ] SQL injection prevention (parameterized queries / JPA)
- [ ] Authentication and authorization checks are in place
- [ ] Sensitive data is not logged

#### Performance
- [ ] No N+1 query issues (check JPA relationships)
- [ ] Database queries are optimized (indexes, projections)
- [ ] No unnecessary object creation in hot paths
- [ ] Pagination used for list endpoints
- [ ] Caching considered where appropriate

#### Readability
- [ ] Code is self-documenting (meaningful names)
- [ ] Complex logic has explanatory comments
- [ ] Methods are focused (single responsibility)
- [ ] No dead code or commented-out code
- [ ] Consistent naming conventions followed

#### Testing
- [ ] Unit tests cover the happy path and error cases
- [ ] Integration tests for API endpoints and database interactions
- [ ] Test names clearly describe the scenario
- [ ] No test interdependencies
- [ ] Mocks are used appropriately (not excessively)

#### Architecture
- [ ] Follows the layered architecture (controller > service > repository)
- [ ] DTOs used for API boundaries (not exposing entities)
- [ ] Shared code placed in appropriate `shared/` module
- [ ] No circular dependencies between modules
- [ ] API contracts are backward-compatible

### Required Reviewers Policy

| Change Type | Required Reviewers | Approval Count |
|------------|-------------------|----------------|
| Shared libraries (`shared/`) | Platform team | 2 |
| Service API contracts | Platform team + consuming service owners | 2 |
| CI/CD changes (`.github/`, `buildSrc/`) | Platform team | 1 |
| Individual service code | Service team + 1 platform member | 1 |
| Documentation only | Any team member | 1 |
| Database migrations | Platform team + DBA | 2 |

### Time-to-Review SLA

| Priority | Target Review Time | Escalation |
|----------|-------------------|------------|
| Critical (hotfix) | 2 hours | Notify team lead directly |
| High (blocking) | 4 hours | Post in team channel |
| Normal | 1 business day | Standard PR notification |
| Low (docs, refactor) | 2 business days | Standard PR notification |

### Review Etiquette

- **Be constructive**: Suggest improvements, don't just criticize
- **Explain why**: Link to documentation or examples when suggesting changes
- **Use conventional comments**: Prefix with `nit:`, `suggestion:`, `question:`, `issue:`, or `blocker:`
- **Approve with comments**: If changes are minor, approve and note them as optional improvements
- **Don't block on style**: If it passes Checkstyle and PMD, style is acceptable

---

## Pull Request Process

### Before Opening a PR

1. **Create a feature branch** from `feat/microservices-migration-v3`
2. **Run the full build locally** to ensure it passes
3. **Run quality checks**: `./gradlew qualityCheck`
4. **Write meaningful commit messages** following conventional commits:
   - `feat:` new feature
   - `fix:` bug fix
   - `refactor:` code change that neither fixes a bug nor adds a feature
   - `docs:` documentation only
   - `test:` adding or updating tests
   - `chore:` maintenance tasks (build, CI, dependencies)

### PR Requirements

1. **Fill out the PR template** completely
2. **Link the related issue** (e.g., EM-47)
3. **Keep PRs focused**: One logical change per PR
4. **Ensure CI passes**: All quality gates must be green
5. **Respond to review feedback** within 1 business day

### Merge Policy

- **Squash merge** for feature branches (clean history)
- **Merge commit** for release branches
- **Delete branch** after merge
- PRs require at least **1 approval** (2 for shared libraries)
- All CI checks must pass before merge

---

## Quality Gates

Quality gates are enforced automatically in the CI pipeline. A PR cannot be merged if any gate fails.

### CI Quality Gate Checks

| Check | Tool | Threshold | Blocks Merge |
|-------|------|-----------|-------------|
| Code style | Checkstyle | 0 errors, 0 warnings | Yes |
| Code quality | PMD | 0 priority 1-3 violations | Yes |
| Bug detection | SpotBugs | 0 high/medium bugs | Yes |
| Code coverage | JaCoCo | 70% line, 50% branch | Yes |
| Compilation | Gradle | No errors | Yes |
| Unit tests | JUnit 5 | All pass | Yes |

### SonarQube Quality Gate (CI)

| Metric | Condition |
|--------|-----------|
| Coverage on new code | > 70% |
| New bugs | 0 |
| New vulnerabilities | 0 |
| New security hotspots reviewed | 100% |
| Duplicated lines on new code | < 3% |

---

## Branch Strategy

```
main (production)
  └── feat/microservices-migration-v3 (migration base)
        ├── feature/EM-XX-description (feature branches)
        ├── fix/EM-XX-description (bug fixes)
        └── chore/EM-XX-description (maintenance)
```

- **main**: Production-ready code
- **feat/microservices-migration-v3**: Integration branch for migration work
- **feature/**: New functionality
- **fix/**: Bug fixes
- **chore/**: Build, CI, documentation changes

---

## Getting Help

- **Architecture questions**: Check `docs/` directory or ask in the platform team channel
- **Build issues**: See `gradle/CONVENTIONS.md` for convention plugin documentation
- **API standards**: See `docs/api-standards.md`
- **CI pipeline issues**: Check `.github/workflows/` for pipeline configuration
