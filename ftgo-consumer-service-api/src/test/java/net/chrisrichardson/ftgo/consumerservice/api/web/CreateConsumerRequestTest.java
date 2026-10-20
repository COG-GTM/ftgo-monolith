package net.chrisrichardson.ftgo.consumerservice.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CreateConsumerRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void constructorAndSetterStoreName() {
    PersonName john = new PersonName("John", "Doe");
    CreateConsumerRequest request = new CreateConsumerRequest(john);
    assertSame(john, request.getName());

    PersonName jane = new PersonName("Jane", "Roe");
    request.setName(jane);
    assertSame(jane, request.getName());
  }

  @Test
  public void serializesNameAsNestedObject() throws Exception {
    String json = objectMapper.writeValueAsString(new CreateConsumerRequest(new PersonName("John", "Doe")));

    assertEquals("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}", json);
  }

  @Test
  public void deserializesFromJsonUsingPrivateNoArgConstructor() throws Exception {
    CreateConsumerRequest request = objectMapper.readValue(
            "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"}}", CreateConsumerRequest.class);

    assertEquals("John", request.getName().getFirstName());
    assertEquals("Doe", request.getName().getLastName());
  }
}
