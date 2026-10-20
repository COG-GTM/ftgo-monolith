package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CreateCourierRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void defaultConstructorHasNoNameOrAddress() {
    CreateCourierRequest request = new CreateCourierRequest();
    assertNull(request.getName());
    assertNull(request.getAddress());
  }

  @Test
  public void constructorSetsNameAndAddress() {
    PersonName name = new PersonName("Jane", "Doe");
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    CreateCourierRequest request = new CreateCourierRequest(name, address);
    assertSame(name, request.getName());
    assertSame(address, request.getAddress());
  }

  @Test
  public void settersReplaceNameAndAddress() {
    CreateCourierRequest request = new CreateCourierRequest(new PersonName("A", "B"), new Address());
    PersonName name = new PersonName("John", "Smith");
    Address address = new Address("2 Oak Ave", "Apt 3", "Berkeley", "CA", "94704");
    request.setName(name);
    request.setAddress(address);
    assertSame(name, request.getName());
    assertSame(address, request.getAddress());
  }

  @Test
  public void serializesNestedNameAndAddress() throws Exception {
    CreateCourierRequest request = new CreateCourierRequest(new PersonName("Jane", "Doe"),
            new Address("1 Main St", "Suite 2", "Oakland", "CA", "94612", 37.8, -122.27));
    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(request));
    assertEquals("Jane", json.at("/name/firstName").asText());
    assertEquals("Doe", json.at("/name/lastName").asText());
    assertEquals("1 Main St", json.at("/address/street1").asText());
    assertEquals("Suite 2", json.at("/address/street2").asText());
    assertEquals("Oakland", json.at("/address/city").asText());
    assertEquals("CA", json.at("/address/state").asText());
    assertEquals("94612", json.at("/address/zip").asText());
    assertEquals(37.8, json.at("/address/latitude").asDouble(), 1e-9);
    assertEquals(-122.27, json.at("/address/longitude").asDouble(), 1e-9);
  }

  @Test
  public void deserializesNestedNameAndAddress() throws Exception {
    String json = "{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"},"
            + "\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"}}";
    CreateCourierRequest request = objectMapper.readValue(json, CreateCourierRequest.class);
    assertEquals("Jane", request.getName().getFirstName());
    assertEquals("Doe", request.getName().getLastName());
    assertEquals("1 Main St", request.getAddress().getStreet1());
    assertNull(request.getAddress().getStreet2());
    assertEquals("Oakland", request.getAddress().getCity());
    assertEquals("CA", request.getAddress().getState());
    assertEquals("94612", request.getAddress().getZip());
  }
}
