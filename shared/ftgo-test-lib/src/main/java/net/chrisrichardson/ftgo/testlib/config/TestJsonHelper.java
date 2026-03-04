package net.chrisrichardson.ftgo.testlib.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * JSON serialization/deserialization helper for tests.
 *
 * <p>Provides a pre-configured {@link ObjectMapper} with Java 8 date/time
 * support and utility methods for converting between objects and JSON.
 *
 * <p>Usage:
 * <pre>{@code
 * String json = TestJsonHelper.toJson(OrderBuilder.anOrder().build());
 * Map<String, Object> map = TestJsonHelper.fromJson(json);
 * }</pre>
 */
public final class TestJsonHelper {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private TestJsonHelper() {
        // Utility class
    }

    /**
     * Returns the shared ObjectMapper instance.
     *
     * @return the pre-configured ObjectMapper
     */
    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param object the object to serialize
     * @return JSON string representation
     * @throws RuntimeException if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Serializes an object to a pretty-printed JSON string.
     *
     * @param object the object to serialize
     * @return pretty-printed JSON string
     * @throws RuntimeException if serialization fails
     */
    public static String toPrettyJson(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to a Map.
     *
     * @param json the JSON string
     * @return deserialized Map
     * @throws RuntimeException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to a specific type.
     *
     * @param json  the JSON string
     * @param clazz the target type
     * @param <T>   the type parameter
     * @return deserialized object
     * @throws RuntimeException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }
}
