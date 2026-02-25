package com.ftgo.template.domain;

// =============================================================================
// TEMPLATE: Unit Test for Domain Entity
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "ExampleEntity" with your entity.
//
// This template demonstrates:
//   - JUnit 5 annotations (@Test, @DisplayName, @Nested, @BeforeEach)
//   - AssertJ fluent assertions
//   - Arrange-Act-Assert pattern
//   - Nested test classes for grouping
//   - Exception testing
//   - Parameterized tests
//
// Dependencies (provided by ftgo.testing-conventions plugin):
//   - org.junit.jupiter:junit-jupiter
//   - org.assertj:assertj-core
//   - org.mockito:mockito-junit-jupiter
// =============================================================================

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the domain entity.
 *
 * <p>Follows the testing pyramid: domain entity tests are pure unit tests
 * with no Spring context, no database, and no external dependencies.
 *
 * <p><b>Naming convention:</b> {@code should{ExpectedBehavior}[When{Condition}]}
 *
 * <p><b>Location:</b> {@code src/test/java/com/ftgo/{service}/domain/}
 */
@DisplayName("ExampleEntity")
class ExampleEntityTest {

    // -------------------------------------------------------------------------
    // Test fixtures — set up in @BeforeEach for isolation between tests
    // -------------------------------------------------------------------------

    // TODO: Replace with your actual entity
    // private ExampleEntity entity;

    @BeforeEach
    void setUp() {
        // Arrange: Create a fresh entity for each test using a test builder.
        // Prefer builders over constructors for readability:
        //   entity = ExampleEntityTestBuilder.anEntity().withDefaults().build();
        //
        // Or using the Object Mother pattern (legacy style):
        //   entity = ExampleEntityMother.DEFAULT_ENTITY;
    }

    // -------------------------------------------------------------------------
    // Nested test classes — group related tests for readability
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("should have correct initial state")
        void shouldHaveCorrectInitialState() {
            // Arrange
            // (done in @BeforeEach or inline)

            // Act
            // var entity = new ExampleEntity("name", "value");

            // Assert
            // assertThat(entity.getName()).isEqualTo("name");
            // assertThat(entity.getStatus()).isEqualTo(Status.ACTIVE);
            // assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should reject null required fields")
        void shouldRejectNullRequiredFields() {
            // assertThatThrownBy(() -> new ExampleEntity(null, "value"))
            //     .isInstanceOf(IllegalArgumentException.class)
            //     .hasMessageContaining("name must not be null");
        }
    }

    @Nested
    @DisplayName("state transitions")
    class StateTransitions {

        @Test
        @DisplayName("should transition from ACTIVE to INACTIVE")
        void shouldTransitionToInactive() {
            // Arrange: entity is in ACTIVE state (from setUp)

            // Act
            // entity.deactivate();

            // Assert
            // assertThat(entity.getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("should reject invalid state transition")
        void shouldRejectInvalidStateTransition() {
            // Arrange: entity is already inactive
            // entity.deactivate();

            // Act & Assert
            // assertThatThrownBy(() -> entity.deactivate())
            //     .isInstanceOf(UnsupportedStateTransitionException.class)
            //     .hasMessageContaining("Cannot transition from INACTIVE");
        }
    }

    @Nested
    @DisplayName("business logic")
    class BusinessLogic {

        @Test
        @DisplayName("should calculate derived value correctly")
        void shouldCalculateDerivedValue() {
            // Example: testing a calculation method
            // var entity = ExampleEntityTestBuilder.anEntity()
            //     .withPrice(new Money(10))
            //     .withQuantity(3)
            //     .build();
            //
            // assertThat(entity.getTotal()).isEqualTo(new Money(30));
        }
    }

    // -------------------------------------------------------------------------
    // Parameterized tests — data-driven testing for multiple inputs
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validation")
    class Validation {

        @ParameterizedTest(name = "should accept valid name: \"{0}\"")
        @ValueSource(strings = {"Alice", "Bob", "Charlie-Dave", "Eve O'Brien"})
        @DisplayName("should accept valid names")
        void shouldAcceptValidNames(String validName) {
            // var entity = new ExampleEntity(validName, "value");
            // assertThat(entity.getName()).isEqualTo(validName);
        }

        @ParameterizedTest(name = "should reject blank name: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("should reject blank names")
        void shouldRejectBlankNames(String invalidName) {
            // assertThatThrownBy(() -> new ExampleEntity(invalidName, "value"))
            //     .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest(name = "price={0}, qty={1} => total={2}")
        @CsvSource({
            "10, 1, 10",
            "10, 2, 20",
            "5,  3, 15",
            "0, 10,  0"
        })
        @DisplayName("should calculate total for various inputs")
        void shouldCalculateTotalForVariousInputs(int price, int quantity, int expectedTotal) {
            // var lineItem = new OrderLineItem("id", "item", new Money(price), quantity);
            // assertThat(lineItem.getTotal()).isEqualTo(new Money(expectedTotal));
        }
    }

    // -------------------------------------------------------------------------
    // Equality and serialization tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("should be equal to entity with same values")
        void shouldBeEqualToEntityWithSameValues() {
            // var entity1 = new ExampleEntity("name", "value");
            // var entity2 = new ExampleEntity("name", "value");
            // assertThat(entity1).isEqualTo(entity2);
            // assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        }

        @Test
        @DisplayName("should not be equal to entity with different values")
        void shouldNotBeEqualToDifferentEntity() {
            // var entity1 = new ExampleEntity("name1", "value");
            // var entity2 = new ExampleEntity("name2", "value");
            // assertThat(entity1).isNotEqualTo(entity2);
        }
    }
}
