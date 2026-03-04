package net.chrisrichardson.ftgo.testlib.builders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConsumerBuilder}.
 */
@DisplayName("ConsumerBuilder")
class ConsumerBuilderTest {

    @Test
    @DisplayName("should create consumer with defaults")
    void shouldCreateConsumerWithDefaults() {
        Map<String, Object> consumer = ConsumerBuilder.aConsumer().build();

        assertThat(consumer).containsKeys("consumerId", "firstName", "lastName",
                "email", "phoneNumber", "deliveryAddress", "createdAt");
        assertThat(consumer.get("firstName")).isEqualTo("John");
        assertThat(consumer.get("lastName")).isEqualTo("Doe");
    }

    @Test
    @DisplayName("should override defaults with custom values")
    void shouldOverrideDefaults() {
        Map<String, Object> consumer = ConsumerBuilder.aConsumer()
                .withFirstName("Jane")
                .withEmail("jane@example.com")
                .build();

        assertThat(consumer.get("firstName")).isEqualTo("Jane");
        assertThat(consumer.get("email")).isEqualTo("jane@example.com");
    }
}
