package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PersonNameTest {

  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void shouldStoreFirstAndLastName() {
    PersonName name = new PersonName(FIRST_NAME, LAST_NAME);

    assertEquals(FIRST_NAME, name.getFirstName());
    assertEquals(LAST_NAME, name.getLastName());
  }

  @Test
  public void shouldSerializeAndDeserialize() throws IOException {
    PersonName name = new PersonName(FIRST_NAME, LAST_NAME);

    String json = objectMapper.writeValueAsString(name);
    assertEquals("{\"firstName\":\"John\",\"lastName\":\"Doe\"}", json);

    PersonName deserialized = objectMapper.readValue(json, PersonName.class);
    assertEquals(FIRST_NAME, deserialized.getFirstName());
    assertEquals(LAST_NAME, deserialized.getLastName());
  }
}
