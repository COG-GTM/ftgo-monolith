package net.chrisrichardson.ftgo.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CourierTest {

  @Test
  public void shouldRejectLatitudeAboveRange() {
    assertInvalidLocation(500, 0);
  }

  @Test
  public void shouldRejectLatitudeBelowRange() {
    assertInvalidLocation(-91, 0);
  }

  @Test
  public void shouldRejectLongitudeAboveRange() {
    assertInvalidLocation(0, 181);
  }

  @Test
  public void shouldRejectNaN() {
    assertInvalidLocation(Double.NaN, 0);
  }

  @Test
  public void shouldRejectInfinity() {
    assertInvalidLocation(0, Double.POSITIVE_INFINITY);
  }

  @Test
  public void shouldAcceptCoordinateBoundaries() {
    Courier northEast = new Courier(new PersonName("A", "B"), null);
    northEast.updateLocation(90, 180);
    assertEquals(Double.valueOf(90), northEast.getCurrentLatitude());
    assertEquals(Double.valueOf(180), northEast.getCurrentLongitude());

    Courier southWest = new Courier(new PersonName("C", "D"), null);
    southWest.updateLocation(-90, -180);
    assertEquals(Double.valueOf(-90), southWest.getCurrentLatitude());
    assertEquals(Double.valueOf(-180), southWest.getCurrentLongitude());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectInvalidAddressLocationInConstructor() {
    new Courier(new PersonName("A", "B"),
            new Address("1 Main St", null, "Oakland", "CA", "94612", 91.0, 0.0));
  }

  @Test
  public void shouldNotSerializeAccessTokenHash() throws Exception {
    Courier courier = new Courier(new PersonName("A", "B"), null);
    String hash = "private-token-hash";
    courier.assignAccessTokenHash(hash);

    String json = new ObjectMapper().writeValueAsString(courier);

    assertFalse(json.contains(hash));
    assertFalse(json.contains("accessTokenHash"));
  }

  private void assertInvalidLocation(double latitude, double longitude) {
    Courier courier = new Courier(new PersonName("A", "B"), null);
    try {
      courier.updateLocation(latitude, longitude);
    } catch (IllegalArgumentException expected) {
      return;
    }
    throw new AssertionError("Expected invalid coordinates to be rejected");
  }
}
