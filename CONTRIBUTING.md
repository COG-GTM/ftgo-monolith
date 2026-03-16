# Contributing to FTGO Monolith

This document establishes code review guidelines, contribution standards, and quality expectations for the FTGO monolith and its ongoing microservices migration.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Branch Strategy](#branch-strategy)
- [Coding Standards](#coding-standards)
- [Code Review Guidelines](#code-review-guidelines)
- [Pull Request Process](#pull-request-process)
- [Static Analysis and Quality Gates](#static-analysis-and-quality-gates)
- [Testing Requirements](#testing-requirements)
- [Migration-Specific Guidelines](#migration-specific-guidelines)
- [Documentation Standards](#documentation-standards)

---

## Getting Started

### Prerequisites

- **Java 8** (JDK 1.8)
- **Gradle 4.10.2** (included via Gradle Wrapper)
- **Docker** and **Docker Compose** for running infrastructure services
- **Git** with configured user name and email

### Building the Project

```bash
# Compile all modules
./gradlew compileJava -x :ftgo-end-to-end-tests-common:compileJava -x :ftgo-end-to-end-tests:compileJava

# Run tests
./gradlew test -x :ftgo-end-to-end-tests-common:test -x :ftgo-end-to-end-tests:test -x :ftgo-application:test

# Start infrastructure and run application
./start-infrastructure-services.sh
./build-and-run.sh
```

---

## Development Workflow

1. **Pick up a task** from the Jira board (EM-project).
2. **Create a feature branch** from the appropriate base branch.
3. **Implement changes** following the coding standards below.
4. **Run static analysis** and fix any violations.
5. **Write/update tests** to cover your changes.
6. **Open a Pull Request** using the PR template.
7. **Address review feedback** promptly.
8. **Merge** once approved and CI passes.

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready monolith code |
| `feat/microservices-migration` | Integration branch for migration work |
| `feat/EM-XX-description` | Feature branches for individual tasks |
| `fix/EM-XX-description` | Bug fix branches |
| `chore/EM-XX-description` | Maintenance and configuration tasks |

### Branch Rules

- Always branch from `feat/microservices-migration` for migration tasks.
- Keep branches focused on a single task or feature.
- Rebase or merge from the base branch regularly to avoid conflicts.
- Delete branches after merging.

---

## Coding Standards

### Java Style

This project follows a modified [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with the following project-specific adjustments:

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 150 characters maximum
- **Encoding**: UTF-8
- **Line endings**: LF (Unix-style)

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Packages | `lowercase.dotted` | `net.chrisrichardson.ftgo.orderservice` |
| Classes | `UpperCamelCase` | `OrderService`, `CreateOrderRequest` |
| Methods | `lowerCamelCase` | `createOrder()`, `validateConsumer()` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_ORDER_ITEMS`, `DEFAULT_TIMEOUT` |
| Variables | `lowerCamelCase` | `orderTotal`, `restaurantId` |
| Type parameters | Single uppercase | `T`, `E`, `K` |

### Spring Boot Conventions

- Use constructor injection over field injection.
- Annotate configuration classes with `@Configuration`.
- Use `@RestController` for REST endpoints, `@Service` for business logic.
- Define beans explicitly in configuration classes when possible.
- Use `@Value` or `@ConfigurationProperties` for externalized configuration.

### Domain-Driven Design (DDD) Patterns

- **Entities**: Mutable objects with identity (`Order`, `Consumer`).
- **Value Objects**: Immutable objects without identity (`Money`, `Address`).
- **Services**: Stateless operations that don't belong to entities.
- **Repositories**: Data access abstraction (Spring Data JPA).
- **DTOs**: Data transfer between layers (request/response objects).

### Import Ordering

Imports should be organized in the following groups, separated by blank lines:

1. `java.*`
2. `javax.*`
3. `org.*`
4. `com.*`
5. `net.chrisrichardson.*`

Static imports should be placed at the top.

---

## Code Review Guidelines

### Reviewer Responsibilities

Every pull request requires at least **one approving review** before merging. Reviewers should evaluate:

#### 1. Correctness
- Does the code do what it claims to do?
- Are edge cases handled?
- Are error conditions properly managed?
- Is the business logic correct per the requirements?

#### 2. Design and Architecture
- Does the change follow existing architectural patterns?
- Are service boundaries respected (especially during migration)?
- Is the change in the right module/package?
- Are dependencies flowing in the correct direction?

#### 3. Code Quality
- Does the code follow project naming conventions?
- Is the code readable and self-documenting?
- Are methods and classes appropriately sized?
- Is there unnecessary duplication?
- Are magic numbers and strings extracted as constants?

#### 4. Testing
- Are there sufficient unit tests for new logic?
- Are edge cases covered in tests?
- Do tests follow the Arrange-Act-Assert pattern?
- Are test names descriptive of the behavior being tested?

#### 5. Security
- Are inputs validated?
- Are SQL queries parameterized (no string concatenation)?
- Are sensitive data fields properly handled?
- Are authentication/authorization checks in place?

#### 6. Performance
- Are there potential N+1 query issues?
- Are database queries efficient?
- Is caching used where appropriate?
- Are there potential memory leaks?

#### 7. Migration Readiness
- Does this change move us toward clean service boundaries?
- Are shared dependencies minimized?
- Is the database schema compatible with future service extraction?
- Are API contracts well-defined and versioned?

### Review Etiquette

- **Be constructive**: Suggest improvements, don't just point out problems.
- **Be specific**: Reference line numbers and provide code examples.
- **Be timely**: Complete reviews within 1 business day.
- **Use labels**: Mark comments as `nit`, `question`, `suggestion`, or `blocker`.
- **Acknowledge good work**: Positive feedback is valuable too.

### Comment Prefixes

| Prefix | Meaning |
|--------|---------|
| `blocker:` | Must be fixed before merge |
| `suggestion:` | Recommended improvement, not required |
| `nit:` | Minor style or preference issue |
| `question:` | Seeking clarification |
| `praise:` | Highlighting good code |

---

## Pull Request Process

### Before Opening a PR

1. Ensure your branch is up to date with the base branch.
2. Run the full build: `./gradlew compileJava -x :ftgo-end-to-end-tests-common:compileJava -x :ftgo-end-to-end-tests:compileJava`
3. Run tests: `./gradlew test -x :ftgo-end-to-end-tests-common:test -x :ftgo-end-to-end-tests:test -x :ftgo-application:test`
4. Run static analysis tools (Checkstyle, SpotBugs, PMD).
5. Self-review your changes.
6. Fill out the PR template completely.

### PR Size Guidelines

- **Ideal**: Under 400 lines of changes.
- **Maximum**: 800 lines (consider splitting larger changes).
- **Exception**: Generated code, configuration files, and migrations.

### Merge Requirements

- At least 1 approving review.
- All CI checks passing.
- No unresolved blocker comments.
- PR template completed.
- Branch is up to date with the target branch.

---

## Static Analysis and Quality Gates

### Tools

The project uses three static analysis tools. Configuration files are in the `config/` directory:

| Tool | Configuration | Purpose |
|------|--------------|---------|
| **Checkstyle** | `config/checkstyle/checkstyle.xml` | Code style enforcement |
| **SpotBugs** | `config/spotbugs/exclude-filter.xml` | Bug pattern detection |
| **PMD** | `config/pmd/ruleset.xml` | Code quality rules |

### Quality Gate Thresholds

| Metric | Threshold | Action |
|--------|-----------|--------|
| Checkstyle violations | 0 errors | Build fails |
| SpotBugs bugs (High) | 0 | Build fails |
| SpotBugs bugs (Medium) | 5 max per module | Warning |
| PMD violations (Priority 1-2) | 0 | Build fails |
| PMD violations (Priority 3) | 10 max per module | Warning |
| Test coverage (new code) | 80% minimum | Warning |
| Test coverage (overall) | 60% minimum | Informational |

### Running Locally

Once static analysis plugins are integrated into the build (see `docs/quality-gates.md`), you will be able to run:

```bash
# Run Checkstyle
./gradlew checkstyleMain

# Run SpotBugs
./gradlew spotbugsMain

# Run PMD
./gradlew pmdMain

# Run all quality checks
./gradlew check
```

> **Note**: Plugin integration into `build.gradle` will be handled in a separate task. The configuration files in `config/` are ready for use once plugins are applied.

---

## Testing Requirements

### Unit Tests

- All new business logic must have unit tests.
- Use JUnit 4 (project standard) with Mockito for mocking.
- Follow the Arrange-Act-Assert (AAA) pattern.
- Test method naming: `methodName_condition_expectedResult()`.

### Integration Tests

- Test database interactions with an embedded or containerized database.
- Test REST endpoints with `MockMvc` or `TestRestTemplate`.
- Use `@SpringBootTest` sparingly (prefer sliced tests like `@WebMvcTest`).

### Test Organization

```
src/
  main/java/     # Production code
  test/java/     # Unit and integration tests
    ...Service/
      domain/
        OrderTest.java
        OrderServiceTest.java
      web/
        OrderControllerTest.java
```

---

## Migration-Specific Guidelines

### Service Extraction Rules

1. **One service per PR**: Extract one service at a time.
2. **API-first**: Define the service API contract before implementation.
3. **Shared nothing**: Minimize shared code between services.
4. **Database per service**: Each service should own its data.
5. **Backward compatible**: Changes must not break the monolith.

### Shared Library Guidelines

- Shared libraries (`shared-libraries/`) contain code used by multiple services.
- Keep shared libraries minimal and focused.
- Version shared libraries for independent release.
- Avoid business logic in shared libraries.

### Package Structure for New Services

```
services/<service-name>/
  src/main/java/net/chrisrichardson/ftgo/<service>/
    domain/          # Domain entities and services
    web/             # REST controllers
    configuration/   # Spring configuration
    repository/      # Data access
  src/main/resources/
    application.yml  # Service configuration
  src/test/java/
    ...              # Tests mirroring main structure
  build.gradle
  Dockerfile
```

---

## Documentation Standards

### When to Document

- New services or modules.
- Architecture decisions (use ADRs in `docs/adr/`).
- API changes or new endpoints.
- Configuration changes.
- Non-obvious design decisions in code comments.

### Documentation Locations

| Type | Location |
|------|----------|
| Architecture Decision Records | `docs/adr/` |
| API documentation | Swagger annotations in code |
| Service documentation | `docs/` |
| Configuration docs | `docs/` |
| Code comments | Inline in source files |

### ADR Format

Use the standard ADR format:
- **Title**: Short descriptive title
- **Status**: Proposed / Accepted / Deprecated / Superseded
- **Context**: Why this decision is needed
- **Decision**: What was decided
- **Consequences**: Impact of the decision

---

## Questions?

If you have questions about these guidelines or the contribution process, please reach out to the team leads listed in the `CODEOWNERS` file.
