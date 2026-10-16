package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AddressTest {

  @Test
  public void fiveArgumentConstructorLeavesCoordinatesUnset() {
    Address address = new Address("1 Main St", "Apt 2", "Boston", "MA", "02110");

    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Apt 2", address.getStreet2());
    assertEquals("Boston", address.getCity());
    assertEquals("MA", address.getState());
    assertEquals("02110", address.getZip());
    assertNull(address.getLatitude());
    assertNull(address.getLongitude());
  }

  @Test
  public void sevenArgumentConstructorStoresCoordinates() {
    Address address = new Address("1 Main St", "Apt 2", "Boston", "MA", "02110", 42.36, -71.06);

    assertEquals("1 Main St", address.getStreet1());
    assertEquals("Apt 2", address.getStreet2());
    assertEquals("Boston", address.getCity());
    assertEquals("MA", address.getState());
    assertEquals("02110", address.getZip());
    assertEquals(Double.valueOf(42.36), address.getLatitude());
    assertEquals(Double.valueOf(-71.06), address.getLongitude());
  }

  @Test
  public void jacksonRoundTripPreservesAllFields() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Address address = new Address("1 Main St", "Apt 2", "Boston", "MA", "02110", 42.36, -71.06);

    Address restored = mapper.readValue(mapper.writeValueAsString(address), Address.class);

    assertEquals(address.getStreet1(), restored.getStreet1());
    assertEquals(address.getStreet2(), restored.getStreet2());
    assertEquals(address.getCity(), restored.getCity());
    assertEquals(address.getState(), restored.getState());
    assertEquals(address.getZip(), restored.getZip());
    assertEquals(address.getLatitude(), restored.getLatitude());
    assertEquals(address.getLongitude(), restored.getLongitude());
  }
}
