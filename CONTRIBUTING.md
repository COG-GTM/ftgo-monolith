# Contributing to FTGO

Thank you for contributing to the FTGO project! This guide outlines our code review
process, coding standards, and quality expectations.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Style & Formatting](#code-style--formatting)
- [Static Analysis & Quality Gates](#static-analysis--quality-gates)
- [Code Review Guidelines](#code-review-guidelines)
- [Testing Standards](#testing-standards)
- [Commit Message Conventions](#commit-message-conventions)
- [Pull Request Process](#pull-request-process)

## Getting Started

### Prerequisites

- **Java 8+** (JDK)
- **Gradle 4.10.x** (wrapper included)
- **Docker & Docker Compose** (for integration tests)
- **Git**

### Building the Project

```bash
# Build all modules
./gradlew clean build

# Build a specific service
./gradlew :services-ftgo-order-service:build

# Run tests
./gradlew test

# Run quality gate checks
./gradlew qualityGate
```

## Development Workflow

1. **Create a branch** from `feat/microservices-migration-v4` (or the current development branch)
   ```bash
   git checkout -b feature/EM-XX-short-description
   ```

2. **Make your changes** following the coding standards below

3. **Run quality checks locally** before pushing
   ```bash
   ./gradlew clean build test qualityGate
   ```

4. **Push your branch** and create a Pull Request

5. **Address review feedback** and ensure CI passes

6. **Merge** after approval (squash merge preferred)

### Branch Naming Convention

| Type       | Pattern                              | Example                                |
|------------|--------------------------------------|----------------------------------------|
| Feature    | `feature/EM-XX-short-description`    | `feature/EM-47-quality-gates`          |
| Bug fix    | `fix/EM-XX-short-description`        | `fix/EM-52-null-pointer-order`         |
| Hotfix     | `hotfix/EM-XX-short-description`     | `hotfix/EM-60-critical-auth-bypass`    |
| Chore      | `chore/EM-XX-short-description`      | `chore/EM-55-upgrade-spring-boot`      |
| Docs       | `docs/short-description`             | `docs/update-api-standards`            |

## Code Style & Formatting

### EditorConfig

The project includes an `.editorconfig` file that configures:
- **Indentation**: 4 spaces for Java/Groovy, 2 spaces for YAML/JSON/XML
- **Charset**: UTF-8
- **Line endings**: LF (Unix-style)
- **Max line length**: 120 characters for Java
- **Trailing whitespace**: Trimmed (except in Markdown)

Ensure your IDE/editor supports EditorConfig (most do natively or via plugins).

### Java Code Style

We follow a style based on [Google Java Style](https://google.github.io/styleguide/javaguide.html)
with the following customizations:

- **Indentation**: 4 spaces (not 2 as in default Google style)
- **Max line length**: 120 characters
- **Braces**: Required for all control structures (K&R style)
- **Imports**: No star imports; unused imports must be removed
- **Naming**:
  - Classes: `UpperCamelCase`
  - Methods/variables: `lowerCamelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `all.lowercase`

### Checkstyle

Checkstyle enforces code style automatically. Configuration: `config/checkstyle/checkstyle.xml`

```bash
# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# View reports
open build/reports/checkstyle/main.html
```

## Static Analysis & Quality Gates

### Overview

| Tool       | Purpose                        | Config File                          |
|------------|--------------------------------|--------------------------------------|
| Checkstyle | Code style enforcement         | `config/checkstyle/checkstyle.xml`   |
| PMD        | Static analysis (bugs/smells)  | `config/pmd/pmd-ruleset.xml`         |
| FindBugs   | Bytecode bug detection         | `config/spotbugs/spotbugs-exclude.xml`|
| JaCoCo     | Code coverage (70% minimum)    | Convention plugin configuration      |

### Running Quality Checks

```bash
# Run all quality gates at once
./gradlew qualityGate

# Run individual checks
./gradlew checkstyleMain    # Code style
./gradlew pmdMain           # Static analysis
./gradlew findbugsMain      # Bug detection
./gradlew jacocoTestReport  # Generate coverage report
./gradlew jacocoTestCoverageVerification  # Verify coverage thresholds
```

### Coverage Requirements

- **Line coverage**: Minimum 70%
- **Branch coverage**: Minimum 60%

Coverage reports are generated at `build/reports/jacoco/test/html/index.html`.

### Applying Quality Gates to a Module

Add the quality gate convention plugin to your module's `build.gradle`:

```groovy
apply plugin: 'ftgo.quality-gate-conventions'
```

This automatically configures Checkstyle, PMD, FindBugs, and JaCoCo for the module.

## Code Review Guidelines

### For Authors

1. **Keep PRs small and focused** - One logical change per PR (< 400 lines preferred)
2. **Write a clear description** - Use the PR template; explain *why*, not just *what*
3. **Self-review before requesting** - Review your own diff before assigning reviewers
4. **Link related issues** - Reference JIRA tickets (e.g., EM-47)
5. **Add tests** - Every behavior change should have corresponding tests
6. **Respond promptly** - Address review feedback within 1 business day

### For Reviewers

1. **Review within 1 business day** of being assigned
2. **Be constructive** - Suggest improvements, don't just point out problems
3. **Focus on**:
   - **Correctness**: Does the code do what it claims?
   - **Design**: Is the approach appropriate? Are abstractions right?
   - **Security**: Any vulnerabilities? Proper input validation?
   - **Performance**: Any obvious bottlenecks? N+1 queries?
   - **Maintainability**: Is the code readable? Well-documented?
   - **Testing**: Are edge cases covered? Are tests meaningful?
4. **Use conventional comments** for clarity:
   - `nit:` - Minor style suggestion (non-blocking)
   - `suggestion:` - Recommended improvement (non-blocking)
   - `issue:` - Must be addressed before merge (blocking)
   - `question:` - Clarification needed
   - `praise:` - Positive feedback for good patterns
5. **Approve when satisfied** - Don't block on nits

### Review Requirements

- **Minimum 1 approval** required before merge
- **CODEOWNERS** auto-assigns reviewers based on changed files
- **All CI checks must pass** before merge
- **Quality gate violations block merge**

## Testing Standards

### Test Structure

```
src/
  main/java/          # Production code
  test/java/          # Unit tests
  integration-test/   # Integration tests
    java/
    resources/
```

### Test Naming Convention

```java
// Unit tests: {ClassName}Test
class OrderServiceTest { ... }

// Integration tests: {ClassName}IT
class OrderServiceIT { ... }

// Test methods: should_{expectedBehavior}_when_{condition}
@Test
void should_createOrder_when_validRequest() { ... }

@Test
void should_throwException_when_invalidConsumerId() { ... }
```

### Test Requirements

- **Unit tests** for all business logic
- **Integration tests** for API endpoints and database operations
- **Use JUnit 5** (Jupiter) as the test framework
- **Use Mockito** for mocking dependencies
- **Use AssertJ** for fluent assertions
- **Use Rest-Assured** for REST API testing
- **Target 70%+ line coverage** for all modules

## Commit Message Conventions

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

<body - optional>

<footer - optional>
```

### Types

| Type       | Description                                       |
|------------|---------------------------------------------------|
| `feat`     | New feature                                       |
| `fix`      | Bug fix                                           |
| `docs`     | Documentation only                                |
| `style`    | Code style (formatting, no logic change)          |
| `refactor` | Code refactoring (no feature/fix)                 |
| `test`     | Adding or updating tests                          |
| `chore`    | Build, CI, tooling changes                        |
| `perf`     | Performance improvement                           |

### Scope

Use the module name as scope: `order-service`, `consumer-service`, `ftgo-common`, etc.

### Examples

```
feat(order-service): add order cancellation endpoint

fix(consumer-service): handle null consumer email gracefully

chore(ci): add JaCoCo coverage enforcement to quality gate

docs: update CONTRIBUTING.md with review guidelines
```

## Pull Request Process

1. **Fill out the PR template** completely
2. **Ensure CI passes** - All quality gates and tests must pass
3. **Request review** from appropriate team (auto-assigned via CODEOWNERS)
4. **Address all blocking feedback** before requesting re-review
5. **Squash merge** into the target branch
6. **Delete the feature branch** after merge

### PR Size Guidelines

| Size       | Lines Changed | Review Time |
|------------|---------------|-------------|
| Small      | < 100         | < 30 min    |
| Medium     | 100-400       | 30-60 min   |
| Large      | 400-1000      | 1-2 hours   |
| Extra Large| > 1000        | Split into smaller PRs |

### When to Split PRs

- Refactoring + new feature -> Separate PRs
- Multiple unrelated changes -> Separate PRs
- Large migrations -> Incremental PRs with feature flags

## Questions?

If you have questions about contributing, please reach out to the platform team
or open a discussion in the repository.
