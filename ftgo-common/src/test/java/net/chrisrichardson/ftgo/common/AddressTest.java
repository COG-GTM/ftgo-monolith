package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddressTest {

  @Test
  public void shouldCreateWithoutLatLng() {
    Address address = new Address("1 Main St", "Suite 100", "Oakland", "CA", "94612");

    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Suite 100", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94612", address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void shouldCreateWithLatLng() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);

    assertEquals(37.8044, address.getLatitude(), 0.0001);
    assertEquals(-122.2712, address.getLongitude(), 0.0001);
  }

  @Test
  public void shouldAllowSettingFields() {
    Address address = new Address();
    address.setStreet1("2 Oak St");
    address.setStreet2("Apt 3");
    address.setCity("San Francisco");
    address.setState("CA");
    address.setZip("94102");
    address.setLatitude(37.7749);
    address.setLongitude(-122.4194);

    assertEquals("2 Oak St", address.getStreet1());
    assertEquals("Apt 3", address.getStreet2());
    assertEquals("San Francisco", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94102", address.getZip());
    assertEquals(37.7749, address.getLatitude(), 0.0001);
    assertEquals(-122.4194, address.getLongitude(), 0.0001);
  }
}
