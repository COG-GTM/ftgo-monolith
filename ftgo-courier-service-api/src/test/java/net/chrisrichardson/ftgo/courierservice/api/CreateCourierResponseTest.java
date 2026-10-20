package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateCourierResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void defaultConstructorHasZeroId() {
    assertEquals(0L, new CreateCourierResponse().getId());
  }

  @Test
  public void constructorSetsId() {
    assertEquals(42L, new CreateCourierResponse(42L).getId());
  }

  @Test
  public void setterReplacesId() {
    CreateCourierResponse response = new CreateCourierResponse(1L);
    response.setId(99L);
    assertEquals(99L, response.getId());
  }

  @Test
  public void serializesId() throws Exception {
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new CreateCourierResponse(7L)));
    assertEquals(1, json.size());
    assertEquals(7L, json.get("id").asLong());
  }

  @Test
  public void deserializesId() throws Exception {
    assertEquals(123L, objectMapper.readValue("{\"id\":123}", CreateCourierResponse.class).getId());
  }
}
