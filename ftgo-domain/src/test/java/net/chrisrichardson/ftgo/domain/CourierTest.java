package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CourierTest {

  @Test
  public void rejectsInvalidCoordinatesWithoutChangingLocation() {
    Courier courier = new Courier(null, null);
    courier.updateLocation(12.5, 34.5);

    double[][] invalidCoordinates = {
            {Double.NaN, 0},
            {0, Double.NaN},
            {Double.POSITIVE_INFINITY, 0},
            {Double.NEGATIVE_INFINITY, 0},
            {0, Double.POSITIVE_INFINITY},
            {0, Double.NEGATIVE_INFINITY},
            {1e300, 0},
            {90.0001, 0},
            {-90.0001, 0},
            {0, 180.5},
            {0, -180.5}
    };

    for (double[] coordinates : invalidCoordinates) {
      assertInvalidLocation(courier, coordinates[0], coordinates[1]);
    }
  }

  @Test
  public void acceptsCoordinateBoundaries() {
    Courier courier = new Courier(null, null);
    courier.updateLocation(90, 180);
    assertEquals(Double.valueOf(90), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(180), courier.getCurrentLongitude());

    courier.updateLocation(-90, -180);
    assertEquals(Double.valueOf(-90), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-180), courier.getCurrentLongitude());
  }

  private void assertInvalidLocation(Courier courier, double latitude, double longitude) {
    Double originalLatitude = courier.getCurrentLatitude();
    Double originalLongitude = courier.getCurrentLongitude();
    Object originalUpdateTime = courier.getLastLocationUpdate();
    try {
      courier.updateLocation(latitude, longitude);
      fail("Expected invalid coordinates to be rejected");
    } catch (IllegalArgumentException expected) {
      assertNotNull(expected.getMessage());
    }
    assertEquals(originalLatitude, courier.getCurrentLatitude());
    assertEquals(originalLongitude, courier.getCurrentLongitude());
    assertEquals(originalUpdateTime, courier.getLastLocationUpdate());
  }
}
