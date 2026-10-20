package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CourierWorkloadResponseTest {

  private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 2, 3, 4, 5);

  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  public void defaultConstructorHasEmptyWorkloadAndNoLocation() {
    CourierWorkloadResponse response = new CourierWorkloadResponse();
    assertEquals(0L, response.getCourierId());
    assertEquals(0, response.getActiveDeliveries());
    assertFalse(response.isAvailable());
    assertNull(response.getCurrentLatitude());
    assertNull(response.getCurrentLongitude());
    assertNull(response.getLastLocationUpdate());
  }

  @Test
  public void constructorSetsAllFields() {
    CourierWorkloadResponse response = new CourierWorkloadResponse(5L, 3, true, 37.77, -122.42, FIXED_TIME);
    assertEquals(5L, response.getCourierId());
    assertEquals(3, response.getActiveDeliveries());
    assertTrue(response.isAvailable());
    assertEquals(Double.valueOf(37.77), response.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.42), response.getCurrentLongitude());
    assertEquals(FIXED_TIME, response.getLastLocationUpdate());
  }

  @Test
  public void constructorAcceptsUnknownLocation() {
    CourierWorkloadResponse response = new CourierWorkloadResponse(6L, 0, false, null, null, null);
    assertNull(response.getCurrentLatitude());
    assertNull(response.getCurrentLongitude());
    assertNull(response.getLastLocationUpdate());
  }

  @Test
  public void settersReplaceAllFields() {
    CourierWorkloadResponse response = new CourierWorkloadResponse(1L, 1, false, 1.0, 2.0, FIXED_TIME);
    LocalDateTime later = FIXED_TIME.plusMinutes(10);
    response.setCourierId(8L);
    response.setActiveDeliveries(4);
    response.setAvailable(true);
    response.setCurrentLatitude(40.71);
    response.setCurrentLongitude(-74.0);
    response.setLastLocationUpdate(later);
    assertEquals(8L, response.getCourierId());
    assertEquals(4, response.getActiveDeliveries());
    assertTrue(response.isAvailable());
    assertEquals(Double.valueOf(40.71), response.getCurrentLatitude());
    assertEquals(Double.valueOf(-74.0), response.getCurrentLongitude());
    assertEquals(later, response.getLastLocationUpdate());
  }

  @Test
  public void settersCanClearLocation() {
    CourierWorkloadResponse response = new CourierWorkloadResponse(1L, 1, true, 1.0, 2.0, FIXED_TIME);
    response.setCurrentLatitude(null);
    response.setCurrentLongitude(null);
    response.setLastLocationUpdate(null);
    assertNull(response.getCurrentLatitude());
    assertNull(response.getCurrentLongitude());
    assertNull(response.getLastLocationUpdate());
  }

  @Test
  public void serializesAllFieldsWithIsoTimestamp() throws Exception {
    CourierWorkloadResponse response = new CourierWorkloadResponse(5L, 3, true, 37.77, -122.42, FIXED_TIME);
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
    assertEquals(6, json.size());
    assertEquals(5L, json.get("courierId").asLong());
    assertEquals(3, json.get("activeDeliveries").asInt());
    assertTrue(json.get("available").asBoolean());
    assertEquals(37.77, json.get("currentLatitude").asDouble(), 1e-9);
    assertEquals(-122.42, json.get("currentLongitude").asDouble(), 1e-9);
    assertEquals("2026-01-02T03:04:05", json.get("lastLocationUpdate").asText());
  }

  @Test
  public void serializesUnknownLocationAsNulls() throws Exception {
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(
            new CourierWorkloadResponse(6L, 0, false, null, null, null)));
    assertTrue(json.get("currentLatitude").isNull());
    assertTrue(json.get("currentLongitude").isNull());
    assertTrue(json.get("lastLocationUpdate").isNull());
  }

  @Test
  public void deserializesAllFields() throws Exception {
    String json = "{\"courierId\":9,\"activeDeliveries\":2,\"available\":true,"
            + "\"currentLatitude\":51.5,\"currentLongitude\":-0.12,\"lastLocationUpdate\":\"2026-01-02T03:04:05\"}";
    CourierWorkloadResponse response = objectMapper.readValue(json, CourierWorkloadResponse.class);
    assertEquals(9L, response.getCourierId());
    assertEquals(2, response.getActiveDeliveries());
    assertTrue(response.isAvailable());
    assertEquals(Double.valueOf(51.5), response.getCurrentLatitude());
    assertEquals(Double.valueOf(-0.12), response.getCurrentLongitude());
    assertEquals(FIXED_TIME, response.getLastLocationUpdate());
  }
}
