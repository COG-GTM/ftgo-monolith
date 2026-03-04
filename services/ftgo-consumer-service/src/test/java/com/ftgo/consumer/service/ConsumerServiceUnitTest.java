package com.ftgo.consumer.service;

import net.chrisrichardson.ftgo.testlib.builders.ConsumerBuilder;
import net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example unit test for the Consumer bounded context.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>ConsumerBuilder usage for test data creation</li>
 *   <li>Custom assertions for address validation</li>
 *   <li>Nested test organization</li>
 * </ul>
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Consumer Service - Unit Tests")
class ConsumerServiceUnitTest {

    @Nested
    @DisplayName("Consumer Registration")
    class ConsumerRegistration {

        @Test
        @DisplayName("should create consumer with default values")
        void shouldCreateConsumerWithDefaults() {
            Map<String, Object> consumer = ConsumerBuilder.aConsumer().build();

            assertThat(consumer.get("firstName")).isEqualTo("John");
            assertThat(consumer.get("lastName")).isEqualTo("Doe");
            assertThat(consumer.get("email")).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("should create consumer with custom values")
        void shouldCreateConsumerWithCustomValues() {
            Map<String, Object> consumer = ConsumerBuilder.aConsumer()
                    .withFirstName("Jane")
                    .withLastName("Smith")
                    .withEmail("jane.smith@example.com")
                    .build();

            assertThat(consumer.get("firstName")).isEqualTo("Jane");
            assertThat(consumer.get("lastName")).isEqualTo("Smith");
            assertThat(consumer.get("email")).isEqualTo("jane.smith@example.com");
        }
    }

    @Nested
    @DisplayName("Consumer Address")
    class ConsumerAddress {

        @Test
        @DisplayName("should have valid delivery address")
        void shouldHaveValidDeliveryAddress() {
            Map<String, Object> consumer = ConsumerBuilder.aConsumer().build();

            FtgoAssertions.assertValidAddress(
                    (String) consumer.get("deliveryAddress"));
        }

        @Test
        @DisplayName("should allow custom delivery address")
        void shouldAllowCustomDeliveryAddress() {
            Map<String, Object> consumer = ConsumerBuilder.aConsumer()
                    .withDeliveryAddress("789 Oak Ave, Chicago, IL 60601")
                    .build();

            assertThat(consumer.get("deliveryAddress"))
                    .isEqualTo("789 Oak Ave, Chicago, IL 60601");
        }
    }
}
