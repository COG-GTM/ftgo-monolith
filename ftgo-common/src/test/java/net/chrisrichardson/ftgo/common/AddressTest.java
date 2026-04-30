package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddressTest {

  @Test
  public void shouldCreateWithoutLatLng() {
    Address address = new Address("1 Main St", "Apt 2", "Oakland", "CA", "94607");
    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Apt 2", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94607", address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void shouldCreateWithLatLng() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94607", 37.8044, -122.2712);
    assertEquals(37.8044, address.getLatitude(), 0.0001);
    assertEquals(-122.2712, address.getLongitude(), 0.0001);
  }

  @Test
  public void shouldAllowSettingFields() {
    Address address = new Address();
    address.setStreet1("100 Broadway");
    address.setCity("New York");
    address.setState("NY");
    address.setZip("10001");
    address.setLatitude(40.7128);
    address.setLongitude(-74.0060);

    assertEquals("100 Broadway", address.getStreet1());
    assertEquals("New York", address.getCity());
    assertEquals("NY", address.getState());
    assertEquals("10001", address.getZip());
    assertEquals(40.7128, address.getLatitude(), 0.0001);
  }
}
