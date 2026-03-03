# FTGO Code Review Guidelines

> Detailed code review guidelines for the FTGO microservices project.
> For a quick reference, see [CONTRIBUTING.md](../CONTRIBUTING.md).

## Purpose

Code reviews serve multiple purposes in the FTGO project:

1. **Quality Assurance**: Catch bugs, logic errors, and security issues before they reach production.
2. **Knowledge Sharing**: Spread understanding of the codebase across the team.
3. **Consistency**: Ensure all code follows the same standards and patterns.
4. **Mentoring**: Help team members grow through constructive feedback.

---

## Review Areas

### 1. Functional Correctness

| Check | Description |
|-------|-------------|
| Business logic | Does the code correctly implement the requirements? |
| Edge cases | Are boundary conditions handled? (empty lists, null values, max/min) |
| Error handling | Are exceptions caught and handled appropriately? |
| Data integrity | Are database transactions used correctly? |
| Concurrency | Is shared state properly synchronized? |

### 2. Code Quality

| Check | Description |
|-------|-------------|
| Readability | Is the code easy to understand without excessive comments? |
| Naming | Are variables, methods, and classes named clearly and consistently? |
| Complexity | Is the cyclomatic complexity within acceptable limits (< 15 per method)? |
| Duplication | Is there unnecessary code duplication? |
| SOLID principles | Does the code follow SOLID design principles? |

### 3. Security

| Check | Description |
|-------|-------------|
| Input validation | Are all external inputs validated and sanitized? |
| Authentication | Are endpoints properly secured? |
| Authorization | Are access controls enforced? |
| Secrets | Are no credentials, API keys, or tokens hardcoded? |
| Logging | Is sensitive data excluded from logs? |
| Dependencies | Are third-party libraries from trusted sources? |

### 4. Performance

| Check | Description |
|-------|-------------|
| Database queries | Are queries efficient? (no N+1, proper indexing) |
| Memory usage | Are large collections handled efficiently? (streaming, pagination) |
| Caching | Is caching used where appropriate? |
| Connection management | Are database/HTTP connections properly pooled? |
| Async operations | Are long-running tasks executed asynchronously? |

### 5. Testing

| Check | Description |
|-------|-------------|
| Coverage | Are new code paths covered by tests? |
| Test quality | Do tests verify behavior, not implementation? |
| Test isolation | Are tests independent and deterministic? |
| Test naming | Do test names describe the scenario clearly? |
| Edge cases | Are boundary conditions tested? |

### 6. API Design

| Check | Description |
|-------|-------------|
| REST conventions | Are HTTP methods and status codes used correctly? |
| Request/Response | Are DTOs properly structured and validated? |
| Error responses | Are errors returned in a consistent format? |
| Versioning | Are API changes backward compatible? |
| Documentation | Are OpenAPI annotations present and accurate? |

---

## Review Feedback Guidelines

### Constructive Feedback

**Good feedback:**
> The `findByCustomerId` query might cause performance issues when a customer has
> many orders. Consider adding pagination or a limit clause.

**Poor feedback:**
> This is wrong.

### Feedback Categories

Use prefixes to communicate the severity of your feedback:

| Prefix | Meaning | Action Required |
|--------|---------|----------------|
| (none) | Blocking issue | Must be fixed before merge |
| `nit:` | Minor style/formatting | Nice to have, won't block merge |
| `suggestion:` | Alternative approach | Author decides, won't block merge |
| `question:` | Clarification needed | May or may not block merge |
| `praise:` | Positive feedback | No action needed |

### Examples

```
nit: Consider renaming `data` to `orderDetails` for clarity.

suggestion: You could use a Stream here instead of the for-loop:
  return orders.stream().filter(Order::isActive).collect(toList());

question: Is this null check necessary? The repository should never return null
          when using Optional<>.

praise: Great use of the Builder pattern here - makes the test data setup very readable.
```

---

## Static Analysis Tools

### Checkstyle

**Purpose**: Enforces Java coding style conventions.

**Configuration**: `build-logic/src/main/resources/config/checkstyle/checkstyle.xml`

**Key rules enforced**:
- Google Java Style Guide naming conventions
- No wildcard imports
- Maximum line length: 120 characters
- No trailing whitespace
- Consistent whitespace around operators and braces

### SpotBugs

**Purpose**: Detects potential bugs through static analysis.

**Configuration**: `build-logic/src/main/resources/config/spotbugs/exclusion-filter.xml`

**Common issues detected**:
- Null pointer dereferences
- Resource leaks (unclosed streams, connections)
- Incorrect equals/hashCode implementations
- Thread safety violations
- Performance anti-patterns

### PMD

**Purpose**: Identifies code quality issues and enforces best practices.

**Configuration**: `build-logic/src/main/resources/config/pmd/ruleset.xml`

**Key rules enforced**:
- Unused variables, parameters, and private fields
- Empty catch/if/while blocks
- Overly complex methods (cognitive complexity > 25)
- Error-prone patterns (broken null checks, float loop indices)
- Best practice violations

### JaCoCo

**Purpose**: Measures code coverage and enforces minimum thresholds.

**Threshold**: 70% instruction coverage minimum.

**Excluded from coverage**:
- Configuration classes
- Application entry points
- DTOs and request/response objects
- Generated code

---

## CI Quality Gate

The CI pipeline runs all quality checks automatically on every PR:

```
PR Created/Updated
    |
    v
[Compile] --> [Checkstyle] --> [SpotBugs] --> [PMD] --> [Unit Tests] --> [JaCoCo Coverage]
    |                                                                          |
    v                                                                          v
[Quality Gate Decision]                                              [Coverage Report]
    |
    +-- All passed --> PR can be merged
    +-- Any failed --> PR is blocked
```

### Handling Quality Gate Failures

1. **Check the CI logs** for the specific failure.
2. **Fix locally** before pushing again.
3. **Suppress only if justified** (see CONTRIBUTING.md for suppression syntax).
4. **Ask for help** if you believe the rule is incorrect for your case.

---

## Service-Specific Review Considerations

### Order Service
- Verify order state machine transitions are correct.
- Check that order totals calculate correctly with Money value objects.
- Ensure optimistic locking on concurrent order modifications.

### Consumer Service
- Validate consumer data (names, addresses, payment info).
- Check authorization on consumer data access.
- Ensure consumer validation is called during order creation.

### Restaurant Service
- Verify menu item pricing consistency.
- Check restaurant availability/hours logic.
- Ensure menu updates don't break existing orders.

### Courier Service
- Validate geolocation data handling.
- Check courier assignment algorithms.
- Ensure availability status updates are atomic.

---

## Escalation Path

If there is a disagreement during code review:

1. **Discuss** in the PR comments.
2. **Seek a third opinion** from another team member.
3. **Escalate to tech lead** if consensus is not reached.
4. **Document the decision** as an ADR if it sets a precedent.

The goal is to ship quality code efficiently. Reviews should not become bottlenecks.
