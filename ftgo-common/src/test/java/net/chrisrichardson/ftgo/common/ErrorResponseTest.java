package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ErrorResponseTest {

  @Test
  public void constructorStoresFieldsAndSetsTimestamp() {
    LocalDateTime before = LocalDateTime.now();
    ErrorResponse response = new ErrorResponse(404, "Not Found", "missing", "/orders/1", "abc-123");
    LocalDateTime after = LocalDateTime.now();

    assertEquals(404, response.getStatus());
    assertEquals("Not Found", response.getError());
    assertEquals("missing", response.getMessage());
    assertEquals("/orders/1", response.getPath());
    assertEquals("abc-123", response.getCorrelationId());
    assertTrue(!response.getTimestamp().isBefore(before));
    assertTrue(!response.getTimestamp().isAfter(after));
  }

  @Test
  public void noArgConstructorLeavesDefaults() {
    ErrorResponse response = new ErrorResponse();

    assertNull(response.getTimestamp());
    assertEquals(0, response.getStatus());
  }

  @Test
  public void serializesFieldsAndTimestampAsIsoString() throws Exception {
    ObjectMapper mapper = initializedMapper();
    ErrorResponse response = new ErrorResponse(404, "Not Found", "missing", "/orders/1", "abc-123");

    String json = mapper.writeValueAsString(response);
    assertTrue(json.contains("\"status\":404"));
    assertTrue(json.contains("\"error\":\"Not Found\""));
    assertTrue(json.contains("\"message\":\"missing\""));
    assertTrue(json.contains("\"path\":\"/orders/1\""));
    assertTrue(json.contains("\"correlationId\":\"abc-123\""));
    JsonNode ts = mapper.readTree(json).get("timestamp");
    assertTrue(ts.isTextual());
    assertEquals(response.getTimestamp(), LocalDateTime.parse(ts.asText()));
  }

  @Test
  public void serializesTimestampInIsoLocalDateTimeFormat() throws Exception {
    ObjectMapper mapper = initializedMapper();
    ErrorResponse response = new ErrorResponse(404, "Not Found", "missing", "/orders/1", "abc-123");

    ReflectionTestUtils.setField(response, "timestamp",
            LocalDateTime.of(2024, 1, 2, 3, 4, 5, 120_000_000));
    assertEquals("2024-01-02T03:04:05.12",
            mapper.readTree(mapper.writeValueAsString(response)).get("timestamp").asText());

    ReflectionTestUtils.setField(response, "timestamp", LocalDateTime.of(2024, 1, 2, 3, 4));
    assertEquals("2024-01-02T03:04:00",
            mapper.readTree(mapper.writeValueAsString(response)).get("timestamp").asText());
  }

  @Test
  public void jsonRoundTripPreservesAllFieldsWithoutSetters() throws Exception {
    ObjectMapper mapper = initializedMapper();
    ErrorResponse original = new ErrorResponse(404, "Not Found", "missing", "/orders/1", "abc-123");

    ErrorResponse restored = mapper.readValue(mapper.writeValueAsString(original), ErrorResponse.class);

    assertEquals(original.getStatus(), restored.getStatus());
    assertEquals(original.getError(), restored.getError());
    assertEquals(original.getMessage(), restored.getMessage());
    assertEquals(original.getPath(), restored.getPath());
    assertEquals(original.getCorrelationId(), restored.getCorrelationId());
    assertEquals(original.getTimestamp(), restored.getTimestamp());
  }

  private ObjectMapper initializedMapper() {
    ObjectMapper mapper = new ObjectMapper();
    CommonJsonMapperInitializer initializer = new CommonJsonMapperInitializer();
    ReflectionTestUtils.setField(initializer, "objectMapper", mapper);
    initializer.initialize();
    return mapper;
  }
}
