## Description

<!-- Provide a clear and concise description of the changes in this PR. -->
<!-- Link to the related issue/ticket: EM-XXX -->

**Related Issue:** EM-

### What changed and why?

<!-- Describe the problem and how this PR solves it. -->

---

## Type of Change

<!-- Check all that apply -->

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Refactoring (no functional changes)
- [ ] Documentation update
- [ ] Infrastructure / CI change
- [ ] Database migration

---

## Testing

### How has this been tested?

<!-- Describe the tests you ran to verify your changes. -->

- [ ] Unit tests pass (`./gradlew test`)
- [ ] Integration tests pass (`./gradlew integrationTest`)
- [ ] Manual testing performed (describe below)

### Test environment

- [ ] Local development
- [ ] CI pipeline
- [ ] Docker Compose stack

### Manual test steps (if applicable)

<!--
1. Step one
2. Step two
3. Expected result
-->

---

## Code Quality

- [ ] Static analysis passes (Checkstyle, PMD, SpotBugs)
- [ ] Code coverage meets minimum threshold (70%)
- [ ] No new compiler warnings introduced
- [ ] SonarQube quality gate passes

---

## Review Checklist

### Author checklist

- [ ] Code follows FTGO coding conventions (see `CONTRIBUTING.md`)
- [ ] Self-review of code completed
- [ ] Comments added for complex logic
- [ ] Documentation updated (if applicable)
- [ ] No hardcoded secrets, passwords, or API keys
- [ ] Database migrations are backward-compatible
- [ ] API changes are backward-compatible (or versioned)

### Reviewer checklist

- [ ] Code is correct and handles edge cases
- [ ] No security vulnerabilities introduced
- [ ] Performance impact considered
- [ ] Error handling is appropriate
- [ ] Logging is sufficient but not excessive
- [ ] Tests adequately cover the changes

---

## Breaking Changes

<!-- If this PR introduces breaking changes, describe them here and the migration path. -->

**Breaking changes:** None / Yes (describe below)

---

## Screenshots / Recordings

<!-- If applicable, add screenshots or recordings to demonstrate the changes. -->

---

## Deployment Notes

<!-- Any special deployment steps, environment variables, or configuration changes needed? -->

- [ ] No special deployment steps required
- [ ] Environment variables added/changed (list below)
- [ ] Database migration required
- [ ] Infrastructure changes required
