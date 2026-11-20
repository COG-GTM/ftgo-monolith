package net.chrisrichardson.ftgo.courierservice;

import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CourierServiceContractTest {
  private CourierRepository repository;
  private CourierService service;

  @Before
  public void setUp() {
    repository = mock(CourierRepository.class);
    service = new CourierService(repository);
    when(repository.findById(999999L)).thenReturn(Optional.empty());
  }

  @Test(expected = CourierNotFoundException.class)
  public void unknownCourier() { service.findCourierById(999999); }

  @Test(expected = CourierNotFoundException.class)
  public void unknownResponse() { service.getCourier(999999); }

  @Test(expected = CourierNotFoundException.class)
  public void unknownAvailable() { service.updateAvailability(999999, true); }

  @Test(expected = CourierNotFoundException.class)
  public void unknownUnavailable() { service.updateAvailability(999999, false); }

  @Test(expected = CourierNotFoundException.class)
  public void unknownLocation() { service.updateLocation(999999, 0, 0); }

  @Test
  public void invalidCoordinates() {
    for (double latitude : new double[]{-91, 200, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertInvalid(latitude, 0, "Latitude must be between -90 and 90");
    }
    for (double longitude : new double[]{-181, 181, Double.NaN, Double.NEGATIVE_INFINITY}) {
      assertInvalid(0, longitude, "Longitude must be between -180 and 180");
    }
    verifyZeroInteractions(repository);
  }

  private void assertInvalid(double latitude, double longitude, String message) {
    try {
      service.updateLocation(1, latitude, longitude);
      fail("Expected invalid coordinates");
    } catch (IllegalArgumentException ex) {
      assertEquals(message, ex.getMessage());
    }
  }

  @Test
  public void validCoordinatesIncludingBoundaries() {
    Courier courier = new Courier();
    when(repository.findById(1L)).thenReturn(Optional.of(courier));
    for (double[] coordinates : new double[][]{{-90, -180}, {90, 180}, {37.8, -122.4}}) {
      service.updateLocation(1, coordinates[0], coordinates[1]);
      assertEquals(coordinates[0], courier.getCurrentLatitude(), 0);
      assertEquals(coordinates[1], courier.getCurrentLongitude(), 0);
      assertNotNull(courier.getLastLocationUpdate());
    }
  }
}
