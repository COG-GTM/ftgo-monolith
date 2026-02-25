package com.ftgo.template.repository;

// =============================================================================
// TEMPLATE: Repository Integration Test with Testcontainers
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "Example" with your entity.
//
// This template demonstrates:
//   - @SpringBootTest with Testcontainers MySQL
//   - Repository CRUD testing against a real database
//   - Custom query testing
//   - @Transactional for test isolation (auto-rollback)
//   - Testing pagination and sorting
//   - Testing entity relationships
//
// Prerequisites:
//   - Flyway migrations in src/main/resources/db/migration/
//   - application-integration-test.properties in src/integration-test/resources/
//   - Testcontainers dependencies in build.gradle
// =============================================================================

import com.ftgo.template.config.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the repository layer.
 *
 * <p>Tests run against a real MySQL database provisioned by Testcontainers.
 * Flyway migrations are applied before tests execute, so the schema matches
 * what will be used in production.
 *
 * <p>Each test is wrapped in a transaction that rolls back after execution,
 * ensuring test isolation without manual cleanup.
 *
 * <p><b>Location:</b> {@code src/integration-test/java/com/ftgo/{service}/repository/}
 */
// TODO: Uncomment when you have a Spring Boot application class
// @SpringBootTest
// @ActiveProfiles("integration-test")
// @Transactional
@DisplayName("ExampleRepository Integration Tests")
class ExampleRepositoryIntegrationTest extends AbstractMySqlIntegrationTest {

    // TODO: Inject your repository
    // @Autowired
    // private ExampleRepository exampleRepository;

    // -------------------------------------------------------------------------
    // Basic CRUD operations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist entity and generate ID")
        void shouldPersistEntity() {
            // Arrange
            // var entity = new ExampleEntity("Test Entity", "Description");

            // Act
            // var saved = exampleRepository.save(entity);

            // Assert
            // assertThat(saved.getId()).isNotNull();
            // assertThat(saved.getName()).isEqualTo("Test Entity");
            // assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update existing entity")
        void shouldUpdateEntity() {
            // Arrange
            // var entity = exampleRepository.save(new ExampleEntity("Original", "Desc"));

            // Act
            // entity.setName("Updated");
            // var updated = exampleRepository.save(entity);

            // Assert
            // assertThat(updated.getName()).isEqualTo("Updated");
            // assertThat(updated.getId()).isEqualTo(entity.getId());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should find entity by ID")
        void shouldFindById() {
            // Arrange
            // var entity = exampleRepository.save(new ExampleEntity("Test", "Desc"));

            // Act
            // var found = exampleRepository.findById(entity.getId());

            // Assert
            // assertThat(found).isPresent();
            // assertThat(found.get().getName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            // Act
            // var found = exampleRepository.findById(999999L);

            // Assert
            // assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete entity")
        void shouldDeleteEntity() {
            // Arrange
            // var entity = exampleRepository.save(new ExampleEntity("ToDelete", "Desc"));
            // var id = entity.getId();

            // Act
            // exampleRepository.delete(entity);

            // Assert
            // assertThat(exampleRepository.findById(id)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Custom query tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("custom queries")
    class CustomQueries {

        @Test
        @DisplayName("should find entities by name")
        void shouldFindByName() {
            // Arrange: create multiple entities
            // exampleRepository.save(new ExampleEntity("Alpha", "Desc"));
            // exampleRepository.save(new ExampleEntity("Beta", "Desc"));
            // exampleRepository.save(new ExampleEntity("Alpha", "Other"));

            // Act: use custom query method
            // var results = exampleRepository.findByName("Alpha");

            // Assert
            // assertThat(results).hasSize(2);
            // assertThat(results).allMatch(e -> e.getName().equals("Alpha"));
        }

        @Test
        @DisplayName("should find entities by status")
        void shouldFindByStatus() {
            // Arrange
            // var active = exampleRepository.save(new ExampleEntity("Active1", "Desc"));
            // var inactive = exampleRepository.save(new ExampleEntity("Inactive1", "Desc"));
            // inactive.deactivate();
            // exampleRepository.save(inactive);

            // Act
            // var results = exampleRepository.findByStatus(Status.ACTIVE);

            // Assert
            // assertThat(results).hasSize(1);
            // assertThat(results.get(0).getName()).isEqualTo("Active1");
        }
    }

    // -------------------------------------------------------------------------
    // Pagination and sorting tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("pagination")
    class Pagination {

        @Test
        @DisplayName("should return paginated results")
        void shouldReturnPaginatedResults() {
            // Arrange: create 10 entities
            // for (int i = 0; i < 10; i++) {
            //     exampleRepository.save(new ExampleEntity("Entity " + i, "Desc"));
            // }

            // Act: request page 0 with size 3
            // var page = exampleRepository.findAll(PageRequest.of(0, 3, Sort.by("name")));

            // Assert
            // assertThat(page.getContent()).hasSize(3);
            // assertThat(page.getTotalElements()).isEqualTo(10);
            // assertThat(page.getTotalPages()).isEqualTo(4);
        }
    }

    // -------------------------------------------------------------------------
    // Flyway migration verification
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("schema validation")
    class SchemaValidation {

        @Test
        @DisplayName("should have applied all Flyway migrations")
        void shouldHaveAppliedMigrations() {
            // This test implicitly validates that Flyway migrations ran successfully.
            // If any migration fails, the Spring context will not start, and all
            // tests in this class will fail.
            //
            // For explicit migration testing, you can query the flyway_schema_history table:
            // @Autowired
            // private JdbcTemplate jdbcTemplate;
            //
            // int migrationCount = jdbcTemplate.queryForObject(
            //     "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
            //     Integer.class);
            // assertThat(migrationCount).isGreaterThan(0);
        }
    }
}
