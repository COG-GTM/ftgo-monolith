package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.*;

public class CourierTest {

  @Test
  public void shouldCreateCourierWithNameAndAddress() {
    PersonName name = new PersonName("Bob", "Smith");
    Address address = new Address("1 Main St", "", "Springfield", "IL", "62701");
    Courier courier = new Courier(name, address);

    assertNotNull(courier);
  }

  @Test
  public void shouldNoteAvailable() {
    Courier courier = new Courier(new PersonName("Bob", "Smith"),
        new Address("1 Main St", "", "Springfield", "IL", "62701"));
    courier.noteAvailable();
    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldNoteUnavailable() {
    Courier courier = new Courier(new PersonName("Bob", "Smith"),
        new Address("1 Main St", "", "Springfield", "IL", "62701"));
    courier.noteAvailable();
    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }
}
