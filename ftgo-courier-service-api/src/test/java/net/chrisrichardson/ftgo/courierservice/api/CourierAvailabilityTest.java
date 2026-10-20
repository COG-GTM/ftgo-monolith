package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CourierAvailabilityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void defaultConstructorIsUnavailable() {
    assertFalse(new CourierAvailability().isAvailable());
  }

  @Test
  public void constructorSetsAvailability() {
    assertTrue(new CourierAvailability(true).isAvailable());
    assertFalse(new CourierAvailability(false).isAvailable());
  }

  @Test
  public void setterTogglesAvailability() {
    CourierAvailability availability = new CourierAvailability(false);
    availability.setAvailable(true);
    assertTrue(availability.isAvailable());
    availability.setAvailable(false);
    assertFalse(availability.isAvailable());
  }

  @Test
  public void serializesAvailableFlag() throws Exception {
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new CourierAvailability(true)));
    assertEquals(1, json.size());
    assertTrue(json.get("available").asBoolean());
  }

  @Test
  public void deserializesAvailableFlag() throws Exception {
    assertTrue(objectMapper.readValue("{\"available\":true}", CourierAvailability.class).isAvailable());
    assertFalse(objectMapper.readValue("{\"available\":false}", CourierAvailability.class).isAvailable());
    assertFalse(objectMapper.readValue("{}", CourierAvailability.class).isAvailable());
  }
}
