# FTGO Testing Strategy

## Test Pyramid

| Level | Scope | Speed | Tools |
|-------|-------|-------|-------|
| Unit | Single class/method | Fast (<1s) | JUnit 5, Mockito |
| Integration | Service + DB/dependencies | Medium (5-30s) | Spring Boot Test, Testcontainers |
| E2E | Full system via API | Slow (1-5min) | REST Assured, Docker Compose |

## Unit Tests

- Located in `src/test/java` within each module
- Use `@ExtendWith(MockitoExtension.class)` for mocking
- Test business logic in isolation
- Naming: `<Class>Test.java` (e.g., `OrderServiceTest.java`)
- Run: `./gradlew test`

## Integration Tests

- Use `@SpringBootTest` with test database
- Suffix: `*IntegrationTest.java`
- Use MySQL service container in CI
- Test repository queries, service interactions, REST endpoints

## E2E Tests

- Located in `ftgo-end-to-end-tests/`
- Require full Docker Compose environment
- Test complete user workflows across services
- Run separately from unit/integration tests

## CI Pipeline

The testing pipeline (`.github/workflows/testing-pipeline.yml`) runs:

1. **Unit Tests** — parallel per module, fast feedback
2. **Integration Tests** — with MySQL service container, after unit tests pass
3. **Test Summary** — aggregates results, fails pipeline if any tests fail

## Coverage

- Use JaCoCo for code coverage
- Minimum thresholds: 70% line coverage per service
- Coverage reports uploaded as CI artifacts
