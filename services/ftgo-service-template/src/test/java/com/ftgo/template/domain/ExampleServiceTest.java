package com.ftgo.template.domain;

// =============================================================================
// TEMPLATE: Unit Test for Domain Service (with Mockito)
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "ExampleService" with your service.
//
// This template demonstrates:
//   - Mockito @Mock and @InjectMocks annotations
//   - MockitoExtension for JUnit 5
//   - Stubbing with when().thenReturn()
//   - Verification with verify()
//   - ArgumentCaptor for capturing method arguments
//   - Testing exception scenarios
//
// When to mock vs. use real dependencies:
//   - MOCK: Repositories, external API clients, message publishers
//   - REAL: Domain entities, value objects, utility classes
// =============================================================================

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the domain service layer.
 *
 * <p>Service tests mock all repository and external dependencies using Mockito.
 * Only the service's business logic orchestration is tested.
 *
 * <p><b>Pattern:</b> {@code @ExtendWith(MockitoExtension.class)} replaces
 * JUnit 4's {@code @RunWith(MockitoJUnitRunner.class)}.
 *
 * <p><b>Location:</b> {@code src/test/java/com/ftgo/{service}/domain/}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExampleService")
class ExampleServiceTest {

    // -------------------------------------------------------------------------
    // Mocked dependencies — injected into the service under test
    // -------------------------------------------------------------------------

    // TODO: Replace with your actual repository interfaces
    // @Mock
    // private ExampleRepository exampleRepository;

    // @Mock
    // private AnotherServiceClient anotherServiceClient;

    // TODO: Replace with your actual service class
    // @InjectMocks
    // private ExampleService exampleService;

    // Captures arguments passed to mocked methods for detailed assertions
    // @Captor
    // private ArgumentCaptor<ExampleEntity> entityCaptor;

    // -------------------------------------------------------------------------
    // Happy path tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create entity and persist it")
        void shouldCreateEntity() {
            // Arrange: stub the repository to return a saved entity
            // var request = new CreateExampleRequest("name", "description");
            // when(exampleRepository.save(any(ExampleEntity.class)))
            //     .thenAnswer(invocation -> {
            //         ExampleEntity saved = invocation.getArgument(0);
            //         saved.setId(42L);
            //         return saved;
            //     });

            // Act
            // ExampleEntity result = exampleService.create(request);

            // Assert: verify the result
            // assertThat(result.getId()).isEqualTo(42L);
            // assertThat(result.getName()).isEqualTo("name");

            // Assert: verify the repository was called with correct arguments
            // verify(exampleRepository).save(entityCaptor.capture());
            // ExampleEntity capturedEntity = entityCaptor.getValue();
            // assertThat(capturedEntity.getName()).isEqualTo("name");
            // assertThat(capturedEntity.getDescription()).isEqualTo("description");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return entity when found")
        void shouldReturnEntityWhenFound() {
            // Arrange
            // var entity = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("Test")
            //     .build();
            // when(exampleRepository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            // ExampleEntity result = exampleService.findById(1L);

            // Assert
            // assertThat(result).isNotNull();
            // assertThat(result.getId()).isEqualTo(1L);
            // assertThat(result.getName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("should throw when entity not found")
        void shouldThrowWhenEntityNotFound() {
            // Arrange
            // when(exampleRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            // assertThatThrownBy(() -> exampleService.findById(999L))
            //     .isInstanceOf(EntityNotFoundException.class)
            //     .hasMessageContaining("999");

            // Verify no further interactions
            // verify(exampleRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // Business logic orchestration tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update entity fields and persist")
        void shouldUpdateEntity() {
            // Arrange
            // var existing = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("Old Name")
            //     .build();
            // when(exampleRepository.findById(1L)).thenReturn(Optional.of(existing));
            // when(exampleRepository.save(any(ExampleEntity.class)))
            //     .thenAnswer(inv -> inv.getArgument(0));

            // Act
            // var request = new UpdateExampleRequest("New Name", "New Description");
            // ExampleEntity result = exampleService.update(1L, request);

            // Assert
            // assertThat(result.getName()).isEqualTo("New Name");
            // verify(exampleRepository).save(existing);
        }

        @Test
        @DisplayName("should not persist when no changes detected")
        void shouldNotPersistWhenNoChanges() {
            // Arrange: return entity with same values as update request
            // var existing = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("Same Name")
            //     .build();
            // when(exampleRepository.findById(1L)).thenReturn(Optional.of(existing));

            // Act
            // var request = new UpdateExampleRequest("Same Name", null);
            // exampleService.update(1L, request);

            // Assert: save should not be called if nothing changed
            // verify(exampleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete existing entity")
        void shouldDeleteExistingEntity() {
            // Arrange
            // var entity = ExampleEntityTestBuilder.anEntity().withId(1L).build();
            // when(exampleRepository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            // exampleService.delete(1L);

            // Assert
            // verify(exampleRepository).delete(entity);
        }

        @Test
        @DisplayName("should throw when deleting non-existent entity")
        void shouldThrowWhenDeletingNonExistent() {
            // Arrange
            // when(exampleRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            // assertThatThrownBy(() -> exampleService.delete(999L))
            //     .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Inter-service communication tests (when using service clients)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("cross-service operations")
    class CrossServiceOperations {

        @Test
        @DisplayName("should call external service and handle response")
        void shouldCallExternalService() {
            // Arrange: stub the external service client
            // when(anotherServiceClient.getData(anyLong()))
            //     .thenReturn(new ExternalData("result"));

            // Act
            // var result = exampleService.processWithExternalData(1L);

            // Assert
            // assertThat(result).isNotNull();
            // verify(anotherServiceClient).getData(1L);
        }

        @Test
        @DisplayName("should handle external service failure gracefully")
        void shouldHandleExternalServiceFailure() {
            // Arrange: stub the external service to throw
            // when(anotherServiceClient.getData(anyLong()))
            //     .thenThrow(new ServiceUnavailableException("Service down"));

            // Act & Assert
            // assertThatThrownBy(() -> exampleService.processWithExternalData(1L))
            //     .isInstanceOf(ServiceUnavailableException.class);
        }
    }
}
