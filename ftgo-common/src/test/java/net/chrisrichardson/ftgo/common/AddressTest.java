package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AddressTest {

  @Test
  public void shouldConstructWithoutCoordinates() {
    Address address = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94612");
    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Apt 2", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94612", address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void shouldConstructWithCoordinates() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8, -122.27);
    assertEquals(Double.valueOf(37.8), address.getLatitude());
    assertEquals(Double.valueOf(-122.27), address.getLongitude());
  }

  @Test
  public void shouldAllowUpdatingFields() {
    Address address = new Address();
    address.setStreet1("2 Elm St");
    address.setStreet2("Suite 5");
    address.setCity("Berkeley");
    address.setState("CA");
    address.setZip("94704");
    address.setLatitude(37.87);
    address.setLongitude(-122.27);

    assertEquals("2 Elm St", address.getStreet1());
    assertEquals("Suite 5", address.getStreet2());
    assertEquals("Berkeley", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94704", address.getZip());
    assertEquals(Double.valueOf(37.87), address.getLatitude());
    assertEquals(Double.valueOf(-122.27), address.getLongitude());
  }
}
