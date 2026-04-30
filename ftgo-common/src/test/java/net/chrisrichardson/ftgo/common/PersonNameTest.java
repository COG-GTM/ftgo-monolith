package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class PersonNameTest {

  @Test
  public void shouldStoreFirstAndLastName() {
    PersonName name = new PersonName("John", "Doe");
    assertEquals("John", name.getFirstName());
    assertEquals("Doe", name.getLastName());
  }

  @Test
  public void shouldSerializeAndDeserialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    PersonName original = new PersonName("Jane", "Smith");

    String json = mapper.writeValueAsString(original);
    PersonName deserialized = mapper.readValue(json, PersonName.class);

    assertEquals(original.getFirstName(), deserialized.getFirstName());
    assertEquals(original.getLastName(), deserialized.getLastName());
  }
}
