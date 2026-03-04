package net.chrisrichardson.ftgo.testlib.templates;

import net.chrisrichardson.ftgo.testlib.config.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Template for JUnit 5 unit tests with Mockito.
 *
 * <p>This template demonstrates the recommended structure for FTGO unit tests:
 * <ul>
 *   <li>Extend {@link AbstractUnitTest} for Mockito and tag configuration</li>
 *   <li>Use {@code @Mock} for dependencies</li>
 *   <li>Use {@code @InjectMocks} for the system under test</li>
 *   <li>Use {@code @Nested} classes to group related tests</li>
 *   <li>Use {@code @DisplayName} for readable test names</li>
 *   <li>Use AssertJ for fluent assertions</li>
 *   <li>Use BDD-style Mockito (given/when/then)</li>
 * </ul>
 *
 * <h3>Copy this template and rename it for your service:</h3>
 * <pre>{@code
 * // Copy this file to:
 * // services/ftgo-order-service/src/test/java/com/ftgo/order/service/OrderServiceTest.java
 * //
 * // Then:
 * // 1. Replace the package declaration
 * // 2. Replace ExampleRepository/ExampleService with your actual classes
 * // 3. Add your test methods
 * }</pre>
 */
// @DisplayName("ExampleService")  // Uncomment and rename
class UnitTestTemplate extends AbstractUnitTest {

    // === Step 1: Declare mocks for dependencies ===
    // @Mock
    // private ExampleRepository exampleRepository;
    //
    // @Mock
    // private EventPublisher eventPublisher;

    // === Step 2: Declare the system under test ===
    // @InjectMocks
    // private ExampleService exampleService;

    // === Step 3: Optional setup method ===
    // @BeforeEach
    // void setUp() {
    //     // Additional setup if needed beyond @InjectMocks
    // }

    // === Step 4: Group tests with @Nested classes ===

    // @Nested
    // @DisplayName("create")
    // class Create {
    //
    //     @Test
    //     @DisplayName("should create entity with valid input")
    //     void shouldCreateEntityWithValidInput() {
    //         // Arrange (Given)
    //         given(exampleRepository.save(any()))
    //                 .willReturn(new ExampleEntity(1L, "test"));
    //
    //         // Act (When)
    //         ExampleEntity result = exampleService.create("test");
    //
    //         // Assert (Then)
    //         assertThat(result).isNotNull();
    //         assertThat(result.getName()).isEqualTo("test");
    //         verify(eventPublisher).publish(any());
    //     }
    //
    //     @Test
    //     @DisplayName("should throw exception for null input")
    //     void shouldThrowExceptionForNullInput() {
    //         assertThatThrownBy(() -> exampleService.create(null))
    //                 .isInstanceOf(IllegalArgumentException.class)
    //                 .hasMessageContaining("must not be null");
    //     }
    // }

    // @Nested
    // @DisplayName("findById")
    // class FindById {
    //
    //     @Test
    //     @DisplayName("should return entity when found")
    //     void shouldReturnEntityWhenFound() {
    //         given(exampleRepository.findById(1L))
    //                 .willReturn(Optional.of(new ExampleEntity(1L, "test")));
    //
    //         Optional<ExampleEntity> result = exampleService.findById(1L);
    //
    //         assertThat(result).isPresent();
    //         assertThat(result.get().getId()).isEqualTo(1L);
    //     }
    //
    //     @Test
    //     @DisplayName("should return empty when not found")
    //     void shouldReturnEmptyWhenNotFound() {
    //         given(exampleRepository.findById(999L))
    //                 .willReturn(Optional.empty());
    //
    //         Optional<ExampleEntity> result = exampleService.findById(999L);
    //
    //         assertThat(result).isEmpty();
    //     }
    // }
}
