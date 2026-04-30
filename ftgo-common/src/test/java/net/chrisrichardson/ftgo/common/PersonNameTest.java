package net.chrisrichardson.ftgo.common;

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
  public void shouldAllowSettingFirstAndLastName() {
    PersonName name = new PersonName("John", "Doe");
    name.setFirstName("Jane");
    name.setLastName("Smith");

    assertEquals("Jane", name.getFirstName());
    assertEquals("Smith", name.getLastName());
  }
}
