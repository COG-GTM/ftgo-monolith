# FTGO Test Templates

Reusable test template files for the FTGO microservices migration. Each template follows the conventions and patterns already established in the codebase.

## Templates

| Template | Purpose | Based On |
|----------|---------|----------|
| [UnitTestTemplate.java](UnitTestTemplate.java) | Domain logic unit tests with Mockito | `MoneyTest`, `MoneySerializationTest` |
| [IntegrationTestTemplate.java](IntegrationTestTemplate.java) | Spring Boot integration tests with real DB | `FtgoApplicationTest`, `IntegrationTestsPlugin` |
| [RestApiTestTemplate.java](RestApiTestTemplate.java) | REST API tests with Rest-Assured MockMvc | `OrderControllerTest` |
| [ContractTestTemplate.java](ContractTestTemplate.java) | Consumer-driven contract test documentation | Spring Cloud Contract / Pact |

## How to Use

1. Copy the relevant template into your service module's test directory.
2. Replace placeholder names (`SomeService`, `SomeEntity`, `<servicename>`) with actual class names.
3. Update the package declaration to match your service.
4. Remove the documentation comments once you understand the patterns.

### Directory Mapping

| Template | Target Location |
|----------|----------------|
| Unit tests | `services/<name>/src/test/java/net/chrisrichardson/ftgo/<service>/` |
| Integration tests | `services/<name>/src/integration-test/java/net/chrisrichardson/ftgo/<service>/` |
| REST API tests | `services/<name>/src/test/java/net/chrisrichardson/ftgo/<service>/web/` |
| Contract tests | `services/<name>/src/test/java/net/chrisrichardson/ftgo/<service>/contract/` |

## Framework Versions

| Dependency | Version | Notes |
|------------|---------|-------|
| JUnit | 4.12 | Current standard; JUnit 5 migration notes included in templates |
| Mockito | 2.x | Used for service/repository mocking |
| Rest-Assured | 3.x | MockMvc module for standalone controller tests |
| Spring Boot Test | 2.0.3.RELEASE | `@SpringBootTest` for integration tests |
| Hamcrest | 1.3 | Matcher library for assertions |

## Related Documentation

- [Testing Strategy](../docs/testing-strategy.md) - Test pyramid, CI workflows, execution strategy
- [Microservice Testing Guide](../docs/microservice-testing-guide.md) - Comprehensive testing guide for the migration
- [Test Reporting](../docs/test-reporting.md) - Report formats and CI artifact management
- [Quality Gates](../docs/quality-gates.md) - Static analysis and enforcement thresholds
