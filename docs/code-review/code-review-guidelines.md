# FTGO Code Review Guidelines

## Purpose

This document defines code review standards for the FTGO microservices migration project. All pull requests targeting `feat/microservices-migration-v2` or `main` must undergo peer review before merging.

## Review Process

### 1. Before Submitting a PR

- Ensure all local quality checks pass (`./gradlew check`)
- Run Checkstyle: `./gradlew checkstyleMain checkstyleTest`
- Run SpotBugs: `./gradlew spotbugsMain`
- Write or update unit tests for changed code
- Keep PRs focused: one logical change per PR
- Update relevant documentation if behavior changes

### 2. PR Requirements

| Requirement | Description |
|---|---|
| **Title** | Clear, descriptive summary using conventional commit format |
| **Description** | What changed, why, and how to test |
| **Size** | Prefer < 400 lines changed; split larger changes |
| **Tests** | New/modified code must include tests |
| **CI** | All quality gates must pass |

### 3. Reviewer Checklist

#### Correctness
- [ ] Does the code do what the PR description says?
- [ ] Are edge cases handled?
- [ ] Are error conditions handled gracefully?
- [ ] Is null-safety considered (use `Optional` where appropriate)?

#### Design
- [ ] Does the change follow FTGO's layered architecture (Controller -> Service -> Repository)?
- [ ] Are domain boundaries respected between services?
- [ ] Does the code follow SOLID principles?
- [ ] Is the code in the correct module (service vs. shared library)?

#### Code Quality
- [ ] Does the code follow FTGO naming conventions?
- [ ] Are methods and classes appropriately sized?
- [ ] Is there unnecessary duplication?
- [ ] Are magic numbers/strings extracted as constants?

#### Testing
- [ ] Are unit tests meaningful (not just coverage padding)?
- [ ] Are integration tests included for API/repository changes?
- [ ] Do tests follow Arrange-Act-Assert pattern?

#### Security
- [ ] No secrets or credentials in code
- [ ] Input validation on all external-facing endpoints
- [ ] Proper authentication/authorization checks
- [ ] No SQL injection vulnerabilities (use parameterized queries)

#### Performance
- [ ] No N+1 query issues in JPA repositories
- [ ] Appropriate use of `@Transactional` scope
- [ ] No unnecessary eager loading of relationships
- [ ] Pagination used for list endpoints

## Review Etiquette

### For Authors
- Respond to all comments before requesting re-review
- Use "Resolved" only after addressing feedback
- Don't take feedback personally; it improves the codebase

### For Reviewers
- Be constructive and specific; suggest alternatives
- Distinguish between blocking issues and nitpicks (prefix with `nit:`)
- Approve with comments if only minor issues remain
- Review within 1 business day of being assigned

## Approval Requirements

| Branch Target | Required Approvals | CI Must Pass |
|---|---|---|
| `feat/microservices-migration-v2` | 1 | Yes |
| `main` | 2 | Yes |

## Conventional Commit Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `docs`: Documentation only
- `test`: Adding or correcting tests
- `chore`: Build process or auxiliary tool changes

### Scopes
Use the module name: `order-service`, `consumer-service`, `ftgo-common`, `ftgo-domain`, etc.

## File Organization Standards

```
services/<service-name>/
  src/main/java/com/ftgo/<service>/
    config/          # Spring configuration classes
    controller/      # REST controllers
    domain/          # Domain entities and value objects
    repository/      # JPA repositories
    service/         # Business logic
    dto/             # Data transfer objects
    exception/       # Custom exceptions
    mapper/          # MapStruct mappers
  src/test/java/com/ftgo/<service>/
    controller/      # Controller tests
    service/         # Service tests
    repository/      # Repository tests
    integration/     # Integration tests
```
