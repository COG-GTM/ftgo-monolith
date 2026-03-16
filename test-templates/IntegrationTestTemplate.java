package net.chrisrichardson.ftgo.<servicename>;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration Test Template - Spring Boot Test with JUnit 4
 *
 * This template demonstrates integration tests that verify component
 * interactions with real dependencies (database, Spring context).
 *
 * Conventions:
 *   - Test class name: {ClassUnderTest}IntegrationTest
 *   - Location: src/integration-test/java/{package}/
 *     (uses IntegrationTestsPlugin source set from buildSrc)
 *   - Gradle task: integrationTest
 *   - Spring context is loaded; real database is used
 *
 * Source set structure (configured by IntegrationTestsPlugin):
 *   src/
 *     main/java/              # Production code
 *     test/java/              # Unit tests
 *     integration-test/
 *       java/                 # Integration test classes (this template)
 *       resources/            # Test-specific config (e.g., application-test.properties)
 *
 * Prerequisites:
 *   - MySQL must be running (via Docker or CI service container)
 *   - Flyway migrations applied for schema setup
 *
 * JUnit 5 Migration Note:
 *   When migrating to JUnit 5, replace:
 *     - @RunWith(SpringRunner.class) -> @ExtendWith(SpringExtension.class)
 *       or simply remove (Spring Boot 2.1+ auto-detects JUnit 5)
 *     - @Test (org.junit.Test) -> @Test (org.junit.jupiter.api.Test)
 *     - Assert.* -> Assertions.*
 *   See: https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = ServiceNameIntegrationTest.TestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Transactional  // Rolls back after each test for isolation
public class ServiceNameIntegrationTest {

    /**
     * Minimal Spring configuration for the integration test.
     * Import only the service configuration(s) needed for the test.
     *
     * Pattern from FtgoApplicationTest:
     *   @Import({ConsumerServiceConfiguration.class, OrderServiceConfiguration.class, ...})
     */
    @Configuration
    @EnableAutoConfiguration
    @ComponentScan
    @Import({/* ServiceNameConfiguration.class */})
    public static class TestConfig {
    }

    // --- Inject real beans from the Spring context ---

    @Autowired
    private SomeRepository someRepository;

    @Autowired
    private SomeService someService;

    @Autowired
    private EntityManager entityManager;

    // ---------------------------------------------------------------
    // Repository tests - verify JPA queries against real database
    // ---------------------------------------------------------------

    @Test
    public void shouldPersistAndRetrieveEntity() {
        // Arrange
        SomeEntity entity = new SomeEntity("Integration Test Entity");

        // Act - persist via repository
        SomeEntity saved = someRepository.save(entity);
        entityManager.flush();  // Force SQL execution
        entityManager.clear();  // Clear persistence context to force fresh load

        // Assert - retrieve and verify
        Optional<SomeEntity> found = someRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Integration Test Entity", found.get().getName());
    }

    @Test
    public void shouldFindEntitiesByCriteria() {
        // Arrange - create test data
        someRepository.save(new SomeEntity("Alpha"));
        someRepository.save(new SomeEntity("Beta"));
        entityManager.flush();
        entityManager.clear();

        // Act
        // List<SomeEntity> results = someRepository.findByNameContaining("Alpha");

        // Assert
        // assertEquals(1, results.size());
        // assertEquals("Alpha", results.get(0).getName());
    }

    // ---------------------------------------------------------------
    // Service-layer tests - verify business logic with real DB
    // ---------------------------------------------------------------

    @Test
    public void shouldCreateEntityThroughService() {
        // Act
        // SomeEntity result = someService.create("Service Test Entity");

        // Assert
        // assertNotNull(result);
        // assertNotNull(result.getId());
        // assertEquals("Service Test Entity", result.getName());
    }

    // ---------------------------------------------------------------
    // Transaction boundary tests
    // ---------------------------------------------------------------

    @Test
    public void shouldRollbackOnFailure() {
        // Arrange
        long countBefore = someRepository.count();

        // Act - attempt operation that should fail
        try {
            someService.createWithValidation(null);
        } catch (Exception ignored) {
            // expected
        }

        // Assert - count should be unchanged due to rollback
        assertEquals(countBefore, someRepository.count());
    }

    // ---------------------------------------------------------------
    // Spring context loading test
    // ---------------------------------------------------------------

    @Test
    public void shouldLoadApplicationContext() {
        // If this test passes, the Spring context loaded successfully.
        // This verifies bean wiring, configuration, and auto-configuration.
        assertNotNull(someRepository);
        assertNotNull(someService);
    }

    // ---------------------------------------------------------------
    // Flyway migration verification
    // ---------------------------------------------------------------

    @Test
    public void shouldHaveAppliedMigrations() {
        // Verify that the expected database tables exist after Flyway migration.
        // Long count = (Long) entityManager
        //     .createNativeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'SOME_TABLE'")
        //     .getSingleResult();
        // assertEquals(Long.valueOf(1), count);
    }
}
