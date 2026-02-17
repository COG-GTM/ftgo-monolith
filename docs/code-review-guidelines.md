# FTGO Code Review Guidelines

## Review Checklist

### Architecture
- [ ] Changes respect service boundaries (no cross-service direct DB access)
- [ ] New endpoints follow REST API standards (`docs/rest-api-standards.md`)
- [ ] Entity ownership respected (`docs/entity-ownership.md`)

### Code Quality
- [ ] No unused imports or dead code
- [ ] Methods are under 30 lines; classes under 300 lines
- [ ] Proper exception handling (no empty catch blocks)
- [ ] Logging uses SLF4J with structured fields
- [ ] No hardcoded secrets or credentials

### Testing
- [ ] Unit tests cover new business logic (minimum 70% coverage)
- [ ] Integration tests for new API endpoints
- [ ] Test naming follows `should_<expected>_when_<condition>` pattern

### Security
- [ ] Authentication/authorization checks on new endpoints
- [ ] Input validation on all request DTOs
- [ ] No SQL injection vulnerabilities (use parameterized queries)
- [ ] Sensitive data not logged

### Database
- [ ] Flyway migrations are additive (no destructive changes)
- [ ] Indexes added for frequently queried columns
- [ ] Migration naming follows `V<N>__<description>.sql` convention

### Documentation
- [ ] OpenAPI annotations on new/modified endpoints
- [ ] Javadoc on public interfaces and non-obvious methods
- [ ] README updated if configuration changes

## PR Requirements

- PRs must target `feat/microservices-migration` (not `master`) during migration
- One Jira task per PR — keep changes focused
- PR title format: `feat(<phase>): <JIRA-KEY> - <summary>`
- All CI checks must pass before merge
- At least one approval required

## Static Analysis

Checkstyle runs on every PR via the testing pipeline. Configuration at `config/checkstyle/checkstyle.xml`.

Key rules enforced:
- Line length: 120 characters max
- Naming conventions (camelCase methods, PascalCase classes)
- No wildcard imports
- Proper Javadoc on public methods
