# Contributing to FTGO Monolith

Thank you for contributing to the FTGO project! This guide establishes code review
guidelines, coding standards, and quality expectations for all contributors.

## Table of Contents

- [Getting Started](#getting-started)
- [Branch Strategy](#branch-strategy)
- [Coding Standards](#coding-standards)
- [Code Review Process](#code-review-process)
- [Code Review Checklist](#code-review-checklist)
- [Static Analysis & Quality Gates](#static-analysis--quality-gates)
- [Testing Requirements](#testing-requirements)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Commit Message Convention](#commit-message-convention)
- [Architecture Guidelines](#architecture-guidelines)

---

## Getting Started

### Prerequisites

- **Java 17** (required for new microservices under `services/` and `shared/`)
- **Java 8** (required for legacy monolith modules at repo root)
- **Gradle 8.7+** (wrapper included)
- **Docker** and **Docker Compose** (for local development)

### Building the Project

```bash
# Compile all modules (excluding end-to-end tests)
./gradlew compileJava \
  -x :ftgo-end-to-end-tests-common:compileJava \
  -x :ftgo-end-to-end-tests:compileJava

# Run unit tests
./gradlew test \
  -x :ftgo-end-to-end-tests-common:test \
  -x :ftgo-end-to-end-tests:test \
  -x :ftgo-application:test
```

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `master` / `main` | Production-ready code |
| `feat/microservices-migration` | Active migration work |
| `feat/<ticket-id>-<description>` | Feature branches |
| `fix/<ticket-id>-<description>` | Bug fix branches |
| `chore/<description>` | Maintenance tasks |

### Rules

- Always branch from `feat/microservices-migration` for migration work.
- Never push directly to `master`, `main`, or `feat/microservices-migration`.
- Keep branches short-lived (< 1 week when possible).
- Rebase or merge upstream changes regularly to avoid large conflicts.

---

## Coding Standards

### Java Style Guide

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
with the following project-specific conventions:

| Rule | Value |
|------|-------|
| Indentation | 2 spaces (Java), 4 spaces (Gradle/XML) |
| Max line length | 120 characters |
| Max method length | 60 lines (warning) |
| Max parameters | 7 (warning) |
| Imports | No wildcards, no unused imports |
| Naming | Standard Java conventions (camelCase, PascalCase, UPPER_SNAKE_CASE) |

### Code Formatting

- An `.editorconfig` file is provided in the repository root. Configure your IDE
  to use it.
- **IntelliJ IDEA**: Install the "Google Java Format" plugin and enable it in
  `Settings > google-java-format`.
- **VS Code**: Install the "EditorConfig for VS Code" extension.

### Package Structure

New microservices must follow this package layout:

```
com.ftgo.<service>/
  +-- config/          # Spring configuration classes
  +-- controller/      # REST controllers
  +-- domain/          # Domain entities and value objects
  +-- repository/      # Data access (JPA repositories)
  +-- service/         # Business logic services
  +-- dto/             # Data transfer objects
  +-- exception/       # Custom exceptions
  +-- mapper/          # Entity-DTO mappers
  +-- metrics/         # Custom metrics
```

### Javadoc Requirements

- All **public classes** must have a class-level Javadoc comment.
- All **public methods** on service and controller classes must be documented.
- Use `@param`, `@return`, and `@throws` tags as appropriate.
- Internal/private methods: document when the logic is non-obvious.

---

## Code Review Process

### Workflow

1. **Author** creates a feature branch and implements changes.
2. **Author** ensures all quality gates pass locally before opening a PR.
3. **Author** opens a PR using the PR template and fills in all sections.
4. **CODEOWNERS** are automatically assigned as reviewers.
5. **Reviewers** provide feedback within **1 business day**.
6. **Author** addresses all feedback and re-requests review.
7. **Reviewer** approves when all criteria are met.
8. **Author** merges after CI passes and required approvals are obtained.

### Review Expectations

#### For Authors

- Keep PRs small and focused (< 400 lines of diff when possible).
- Write a clear PR description explaining *what* and *why*.
- Self-review your own PR before requesting others.
- Respond to review comments promptly (within 1 business day).
- Do not merge your own PR without at least one approval.

#### For Reviewers

- Review within **1 business day** of being assigned.
- Be constructive and specific in feedback.
- Distinguish between blocking issues and suggestions:
  - **Blocking**: Use "Request Changes" for issues that must be fixed.
  - **Suggestion**: Prefix with "nit:" or "suggestion:" for non-blocking items.
- Approve only when you are confident the code is production-ready.
- If unsure, request a second reviewer.

### Review Priority

| Priority | Response Time | Examples |
|----------|--------------|---------|
| Critical (hotfix) | 2 hours | Production outages, security patches |
| High | 4 hours | Blocking other team members |
| Normal | 1 business day | Feature work, refactoring |
| Low | 2 business days | Documentation, minor cleanup |

---

## Code Review Checklist

Use this checklist when reviewing pull requests:

### Correctness
- [ ] Logic is correct and handles edge cases
- [ ] Error handling is appropriate (no swallowed exceptions)
- [ ] Null safety is considered
- [ ] Thread safety for shared state
- [ ] Resource cleanup (try-with-resources, close connections)

### Design
- [ ] Changes respect service boundaries
- [ ] No circular dependencies introduced
- [ ] Single Responsibility Principle followed
- [ ] DRY - no unnecessary duplication
- [ ] Appropriate use of design patterns

### Security
- [ ] No secrets or credentials in code
- [ ] Input validation on all external inputs
- [ ] SQL injection prevention (parameterized queries / JPA)
- [ ] Authentication/authorization checks where needed
- [ ] Sensitive data not logged

### Performance
- [ ] No N+1 query problems
- [ ] Appropriate use of database indexes
- [ ] No unnecessary object creation in loops
- [ ] Pagination for list endpoints
- [ ] Caching considered where appropriate

### Testing
- [ ] Unit tests cover new/changed logic
- [ ] Edge cases tested
- [ ] Test names clearly describe the scenario
- [ ] Tests are deterministic (no flaky tests)
- [ ] Integration tests for external dependencies

### Documentation
- [ ] Public APIs documented
- [ ] Complex logic explained with comments
- [ ] README/docs updated for behavior changes
- [ ] API changes reflected in OpenAPI specs

---

## Static Analysis & Quality Gates

All PRs must pass the following automated quality gates before merging:

### Tools

| Tool | Purpose | Config Location |
|------|---------|----------------|
| **Checkstyle** | Code style enforcement | `build-logic/src/main/resources/config/checkstyle/checkstyle.xml` |
| **SpotBugs** | Static bug detection | `build-logic/src/main/resources/config/spotbugs/exclusion-filter.xml` |
| **PMD** | Code quality rules | `build-logic/src/main/resources/config/pmd/ruleset.xml` |
| **JaCoCo** | Code coverage (min 70%) | Configured in `ftgo.quality-conventions` plugin |

### Running Quality Checks Locally

```bash
# Run all quality checks (for new microservices using convention plugins)
./gradlew check

# Run individual tools
./gradlew checkstyleMain checkstyleTest
./gradlew spotbugsMain
./gradlew pmdMain pmdTest
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

### Suppressing False Positives

In rare cases, you may need to suppress a warning. Always document the reason:

```java
// Checkstyle
@SuppressWarnings("checkstyle:MagicNumber")

// SpotBugs
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "DTO is immutable in practice")

// PMD
@SuppressWarnings("PMD.TooManyMethods") // Service facade aggregates domain operations
```

### Coverage Thresholds

| Metric | Minimum | Target |
|--------|---------|--------|
| Overall instruction coverage | **70%** | 80%+ |
| Branch coverage | Recommended | 70%+ |

Classes excluded from coverage:
- Configuration classes (`*Configuration`, `*Config`)
- Application entry points (`*Application`, `*Main`)
- DTOs and request/response objects
- Generated code

---

## Testing Requirements

### Test Pyramid

| Level | Location | Minimum Coverage | Run Command |
|-------|----------|-----------------|-------------|
| Unit Tests | `src/test/java/` | Required for all services | `./gradlew test` |
| Integration Tests | `src/integration-test/java/` | Required for repositories and external integrations | `./gradlew integrationTest` |
| E2E Tests | `ftgo-end-to-end-tests/` | Required for critical user flows | `./run-end-to-end-tests.sh` |

### Test Naming Convention

```java
@Test
void shouldCreateOrder_whenValidInput() { ... }

@Test
void shouldThrowException_whenConsumerNotFound() { ... }

@Test
void shouldReturnEmptyList_whenNoOrdersExist() { ... }
```

Pattern: `should<ExpectedBehavior>_when<Condition>`

---

## Pull Request Guidelines

### PR Size

- **Small** (< 200 lines): Ideal, quick to review.
- **Medium** (200-400 lines): Acceptable for features.
- **Large** (400+ lines): Split into smaller PRs if possible.
  - If unavoidable, provide extra context in the PR description.

### PR Title Convention

```
<type>(<scope>): <description>

Examples:
feat(order-service): add order cancellation endpoint
fix(consumer-service): handle null consumer name validation
refactor(domain): extract Money value object to shared module
docs(api): update OpenAPI spec for restaurant endpoints
chore(ci): add SpotBugs quality gate to CI pipeline
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no behavior change) |
| `docs` | Documentation only |
| `test` | Adding or updating tests |
| `chore` | Build, CI, tooling changes |
| `perf` | Performance improvements |

---

## Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Rules

- **Subject line**: Max 72 characters, imperative mood ("add" not "added").
- **Body**: Explain *what* and *why*, not *how*. Wrap at 72 characters.
- **Footer**: Reference issue keys (e.g., `Refs: EM-47`).

---

## Architecture Guidelines

### Service Boundaries

- Each microservice owns its data and exposes it only via APIs.
- No direct database access across service boundaries.
- Shared domain concepts live in `shared/ftgo-domain`.
- Service-to-service communication uses REST APIs (synchronous) or events (asynchronous).

### API Design

- Follow the [API Standards](docs/api-standards.md) document.
- Use RESTful conventions with proper HTTP methods and status codes.
- Version APIs using URL path versioning (`/api/v1/...`).
- Document all endpoints with OpenAPI 3.0 annotations.

### Database Changes

- All schema changes must use Flyway migrations.
- Migration scripts are in `ftgo-flyway/`.
- See the [Database Migration Runbook](docs/database-migration-runbook.md).

---

## Questions?

- Check the [docs/](docs/) directory for detailed guides.
- Open a GitHub Discussion for architecture questions.
- Reach out to `@COG-GTM/ftgo-maintainers` for urgent issues.
