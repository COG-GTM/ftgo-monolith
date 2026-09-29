package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AddressTest {

  private static final String STREET1 = "1 Main Street";
  private static final String STREET2 = "Unit 99";
  private static final String CITY = "Oakland";
  private static final String STATE = "CA";
  private static final String ZIP = "94611";
  private static final Double LATITUDE = 37.8044;
  private static final Double LONGITUDE = -122.2712;

  @Test
  public void shouldCreateWithoutLatLng() {
    Address address = new Address(STREET1, STREET2, CITY, STATE, ZIP);

    assertEquals(STREET1, address.getStreet1());
    assertEquals(STREET2, address.getStreet2());
    assertEquals(CITY, address.getCity());
    assertEquals(STATE, address.getState());
    assertEquals(ZIP, address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void shouldCreateWithLatLng() {
    Address address = new Address(STREET1, STREET2, CITY, STATE, ZIP, LATITUDE, LONGITUDE);

    assertEquals(STREET1, address.getStreet1());
    assertEquals(STREET2, address.getStreet2());
    assertEquals(CITY, address.getCity());
    assertEquals(STATE, address.getState());
    assertEquals(ZIP, address.getZip());
    assertEquals(LATITUDE, address.getLatitude());
    assertEquals(LONGITUDE, address.getLongitude());
  }

  @Test
  public void shouldAllowSettingFields() {
    Address address = new Address();

    address.setStreet1(STREET1);
    address.setStreet2(STREET2);
    address.setCity(CITY);
    address.setState(STATE);
    address.setZip(ZIP);
    address.setLatitude(LATITUDE);
    address.setLongitude(LONGITUDE);

    assertEquals(STREET1, address.getStreet1());
    assertEquals(STREET2, address.getStreet2());
    assertEquals(CITY, address.getCity());
    assertEquals(STATE, address.getState());
    assertEquals(ZIP, address.getZip());
    assertEquals(LATITUDE, address.getLatitude());
    assertEquals(LONGITUDE, address.getLongitude());
  }
}
