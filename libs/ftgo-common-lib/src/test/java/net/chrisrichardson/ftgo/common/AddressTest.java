package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

  @Test
  void shouldCreateAddress() {
    Address address = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94612");
    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Apt 2", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94612", address.getZip());
  }

  @Test
  void shouldBeEqual() {
    Address a1 = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94612");
    Address a2 = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94612");
    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenDifferent() {
    Address a1 = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94612");
    Address a2 = new Address("2 Main St", "Apt 2", "Oakland", "CA", "94612");
    assertNotEquals(a1, a2);
  }
}
