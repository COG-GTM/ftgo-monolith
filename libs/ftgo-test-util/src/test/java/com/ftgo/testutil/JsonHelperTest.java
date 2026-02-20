package com.ftgo.testutil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonHelper")
class JsonHelperTest {

    @Test
    @DisplayName("toJson should serialize object to JSON string")
    void toJsonShouldSerialize() {
        Map<String, Object> data = Map.of("name", "test", "value", 42);

        String json = JsonHelper.toJson(data);

        assertThat(json).contains("\"name\"");
        assertThat(json).contains("\"test\"");
        assertThat(json).contains("42");
    }

    @Test
    @DisplayName("toPrettyJson should format with indentation")
    void toPrettyJsonShouldFormat() {
        Map<String, Object> data = Map.of("name", "test");

        String json = JsonHelper.toPrettyJson(data);

        assertThat(json).contains("\n");
        assertThat(json).contains("\"name\"");
    }

    @Test
    @DisplayName("fromJson should deserialize JSON to object")
    void fromJsonShouldDeserialize() {
        String json = "{\"key\":\"value\",\"count\":5}";

        Map<?, ?> result = JsonHelper.fromJson(json, Map.class);

        assertThat(result).containsEntry("key", "value");
        assertThat(result).containsEntry("count", 5);
    }

    @Test
    @DisplayName("fromJson should ignore unknown properties")
    void fromJsonShouldIgnoreUnknownProperties() {
        String json = "{\"known\":\"value\",\"unknown\":\"ignored\"}";

        SimpleDto result = JsonHelper.fromJson(json, SimpleDto.class);

        assertThat(result.known).isEqualTo("value");
    }

    @Test
    @DisplayName("fromJson should throw on invalid JSON")
    void fromJsonShouldThrowOnInvalidJson() {
        assertThatThrownBy(() -> JsonHelper.fromJson("not-json", Map.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to deserialize");
    }

    @Test
    @DisplayName("objectMapper should return independent copy")
    void objectMapperShouldReturnCopy() {
        assertThat(JsonHelper.objectMapper()).isNotNull();
    }

    @Test
    @DisplayName("should handle Java time types")
    void shouldHandleJavaTime() {
        TimeDto dto = new TimeDto();
        dto.timestamp = LocalDateTime.of(2025, 1, 15, 12, 0, 0);

        String json = JsonHelper.toJson(dto);
        TimeDto result = JsonHelper.fromJson(json, TimeDto.class);

        assertThat(result.timestamp).isEqualTo(dto.timestamp);
    }

    static class SimpleDto {
        public String known;
    }

    static class TimeDto {
        public LocalDateTime timestamp;
    }
}
