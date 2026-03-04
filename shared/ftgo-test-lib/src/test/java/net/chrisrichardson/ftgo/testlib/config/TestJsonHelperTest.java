package net.chrisrichardson.ftgo.testlib.config;

import net.chrisrichardson.ftgo.testlib.builders.OrderBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link TestJsonHelper}.
 */
@DisplayName("TestJsonHelper")
class TestJsonHelperTest {

    @Test
    @DisplayName("should serialize map to JSON string")
    void shouldSerializeMapToJson() {
        Map<String, Object> order = OrderBuilder.anOrder().build();

        String json = TestJsonHelper.toJson(order);

        assertThat(json).isNotNull();
        assertThat(json).contains("orderId");
        assertThat(json).contains("APPROVAL_PENDING");
    }

    @Test
    @DisplayName("should deserialize JSON string to map")
    void shouldDeserializeJsonToMap() {
        String json = "{\"orderId\":1,\"state\":\"APPROVED\"}";

        Map<String, Object> map = TestJsonHelper.fromJson(json);

        assertThat(map).containsEntry("state", "APPROVED");
        assertThat(map).containsEntry("orderId", 1);
    }

    @Test
    @DisplayName("should round-trip through JSON")
    void shouldRoundTripThroughJson() {
        Map<String, Object> original = OrderBuilder.anOrder().build();

        String json = TestJsonHelper.toJson(original);
        Map<String, Object> deserialized = TestJsonHelper.fromJson(json);

        assertThat(deserialized).containsKey("orderId");
        assertThat(deserialized).containsKey("state");
    }

    @Test
    @DisplayName("should produce pretty-printed JSON")
    void shouldProducePrettyPrintedJson() {
        Map<String, Object> order = OrderBuilder.anOrder().build();

        String json = TestJsonHelper.toPrettyJson(order);

        assertThat(json).contains("\n");
    }

    @Test
    @DisplayName("should throw RuntimeException for invalid JSON")
    void shouldThrowForInvalidJson() {
        assertThatThrownBy(() -> TestJsonHelper.fromJson("not-valid-json"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to deserialize JSON");
    }
}
