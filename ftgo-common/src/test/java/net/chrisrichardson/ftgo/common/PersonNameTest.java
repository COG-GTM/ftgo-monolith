package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonNameTest {

  @Test
  public void constructorStoresNames() {
    PersonName name = new PersonName("John", "Doe");

    assertEquals("John", name.getFirstName());
    assertEquals("Doe", name.getLastName());
  }

  @Test
  public void jacksonRoundTripUsesPrivateNoArgConstructor() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    PersonName name = mapper.readValue("{\"firstName\":\"John\",\"lastName\":\"Doe\"}", PersonName.class);
    String json = mapper.writeValueAsString(name);

    assertEquals("John", name.getFirstName());
    assertEquals("Doe", name.getLastName());
    assertEquals("{\"firstName\":\"John\",\"lastName\":\"Doe\"}", json);
  }
}
