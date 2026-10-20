package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CourierLocationUpdateTest {

  private static final double DELTA = 1e-9;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void defaultConstructorHasZeroCoordinates() {
    CourierLocationUpdate update = new CourierLocationUpdate();
    assertEquals(0.0, update.getLatitude(), DELTA);
    assertEquals(0.0, update.getLongitude(), DELTA);
  }

  @Test
  public void constructorSetsCoordinates() {
    CourierLocationUpdate update = new CourierLocationUpdate(37.7749, -122.4194);
    assertEquals(37.7749, update.getLatitude(), DELTA);
    assertEquals(-122.4194, update.getLongitude(), DELTA);
  }

  @Test
  public void settersReplaceCoordinates() {
    CourierLocationUpdate update = new CourierLocationUpdate(1.0, 2.0);
    update.setLatitude(-33.8688);
    update.setLongitude(151.2093);
    assertEquals(-33.8688, update.getLatitude(), DELTA);
    assertEquals(151.2093, update.getLongitude(), DELTA);
  }

  @Test
  public void serializesCoordinates() throws Exception {
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new CourierLocationUpdate(40.7128, -74.006)));
    assertEquals(2, json.size());
    assertEquals(40.7128, json.get("latitude").asDouble(), DELTA);
    assertEquals(-74.006, json.get("longitude").asDouble(), DELTA);
  }

  @Test
  public void deserializesCoordinates() throws Exception {
    CourierLocationUpdate update = objectMapper.readValue("{\"latitude\":51.5074,\"longitude\":-0.1278}", CourierLocationUpdate.class);
    assertEquals(51.5074, update.getLatitude(), DELTA);
    assertEquals(-0.1278, update.getLongitude(), DELTA);
  }
}
