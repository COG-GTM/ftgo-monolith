package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddressTest {

  @Test
  public void shouldCreateWithoutLatLng() {
    Address address = new Address("123 Main St", "Apt 4", "Oakland", "CA", "94612");

    assertEquals("123 Main St", address.getStreet1());
    assertEquals("Apt 4", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94612", address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void shouldCreateWithLatLng() {
    Address address = new Address("123 Main St", "Apt 4", "Oakland", "CA", "94612", 37.8044, -122.2712);

    assertEquals("123 Main St", address.getStreet1());
    assertEquals("Apt 4", address.getStreet2());
    assertEquals("Oakland", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94612", address.getZip());
    assertEquals(Double.valueOf(37.8044), address.getLatitude());
    assertEquals(Double.valueOf(-122.2712), address.getLongitude());
  }

  @Test
  public void shouldAllowSettingFields() {
    Address address = new Address();
    address.setStreet1("456 Oak Ave");
    address.setStreet2("Suite 100");
    address.setCity("San Francisco");
    address.setState("CA");
    address.setZip("94102");
    address.setLatitude(37.7749);
    address.setLongitude(-122.4194);

    assertEquals("456 Oak Ave", address.getStreet1());
    assertEquals("Suite 100", address.getStreet2());
    assertEquals("San Francisco", address.getCity());
    assertEquals("CA", address.getState());
    assertEquals("94102", address.getZip());
    assertEquals(Double.valueOf(37.7749), address.getLatitude());
    assertEquals(Double.valueOf(-122.4194), address.getLongitude());
  }
}
