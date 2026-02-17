package net.chrisrichardson.ftgo.SERVICENAME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test template for FTGO service classes.
 * Replace SERVICENAME, ServiceClass, and EntityClass with actual names.
 *
 * Naming convention: should_<expected>_when_<condition>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceClass Unit Tests")
class ServiceClassTest {

    @Mock
    private EntityRepository entityRepository;

    @InjectMocks
    private ServiceClass serviceClass;

    @Nested
    @DisplayName("Create operations")
    class CreateTests {

        @Test
        @DisplayName("should create entity when valid request provided")
        void should_createEntity_when_validRequest() {
            // Given
            var request = new CreateEntityRequest(/* valid params */);
            when(entityRepository.save(any())).thenReturn(new EntityClass());

            // When
            var result = serviceClass.create(request);

            // Then
            assertThat(result).isNotNull();
            verify(entityRepository).save(any());
        }

        @Test
        @DisplayName("should throw exception when duplicate entity")
        void should_throwException_when_duplicateEntity() {
            // Given
            var request = new CreateEntityRequest(/* duplicate params */);
            when(entityRepository.existsById(any())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> serviceClass.create(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Read operations")
    class ReadTests {

        @Test
        @DisplayName("should return entity when found by id")
        void should_returnEntity_when_foundById() {
            // Given
            var entity = new EntityClass();
            when(entityRepository.findById(1L)).thenReturn(Optional.of(entity));

            // When
            var result = serviceClass.findById(1L);

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should throw not found when entity does not exist")
        void should_throwNotFound_when_entityDoesNotExist() {
            // Given
            when(entityRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> serviceClass.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
