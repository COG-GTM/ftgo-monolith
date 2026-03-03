## Description

<!-- Provide a brief description of the changes in this PR. -->

**Related Issue:** <!-- Link to Jira/GitHub issue, e.g., EM-XX -->

## Type of Change

<!-- Check all that apply -->

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Refactoring (no functional changes)
- [ ] Documentation update
- [ ] CI/CD or build configuration change
- [ ] Dependency update

## Changes Made

<!-- List the key changes made in this PR -->

-
-
-

## Service(s) Affected

<!-- Check all services affected by this change -->

- [ ] ftgo-order-service
- [ ] ftgo-consumer-service
- [ ] ftgo-restaurant-service
- [ ] ftgo-courier-service
- [ ] ftgo-domain (shared domain)
- [ ] ftgo-common (shared utilities)
- [ ] shared/ libraries
- [ ] build-logic/ (convention plugins)
- [ ] CI/CD workflows
- [ ] Documentation only

## Testing

<!-- Describe the testing performed -->

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] E2E tests added/updated
- [ ] Manual testing performed
- [ ] No tests needed (explain why)

**Test Commands Run:**
```bash
# e.g., ./gradlew :ftgo-order-service:test
```

## Quality Checklist

<!-- All items must be checked before merge -->

### Code Quality
- [ ] Code follows the [FTGO coding standards](CONTRIBUTING.md)
- [ ] No new compiler warnings introduced
- [ ] Static analysis passes (Checkstyle, SpotBugs, PMD)
- [ ] Code coverage meets minimum threshold (70%)

### Design & Architecture
- [ ] Changes respect service boundaries (no cross-service imports)
- [ ] API changes are backward compatible (or documented as breaking)
- [ ] Database schema changes include migration scripts
- [ ] No secrets or credentials in code

### Documentation
- [ ] Public APIs are documented (Javadoc / OpenAPI)
- [ ] README or docs updated if behavior changed
- [ ] ADR created for significant architectural decisions

### Deployment
- [ ] Changes are backward compatible with existing deployments
- [ ] Configuration changes documented
- [ ] Feature flags used for risky changes (if applicable)

## Screenshots / API Examples

<!-- If applicable, add screenshots or API request/response examples -->

## Reviewer Notes

<!-- Any additional context for reviewers -->

---

> **Reminder**: All CI quality gates must pass before merging. See [CONTRIBUTING.md](CONTRIBUTING.md) for the full code review guidelines.
