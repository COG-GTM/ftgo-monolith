package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonNameTest {

  @Test
  public void shouldExposeConstructorValues() {
    PersonName name = new PersonName("Ada", "Lovelace");
    assertEquals("Ada", name.getFirstName());
    assertEquals("Lovelace", name.getLastName());
  }

  @Test
  public void shouldAllowUpdatingNames() {
    PersonName name = new PersonName("Ada", "Lovelace");
    name.setFirstName("Grace");
    name.setLastName("Hopper");
    assertEquals("Grace", name.getFirstName());
    assertEquals("Hopper", name.getLastName());
  }
}
