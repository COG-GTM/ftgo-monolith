package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierServiceTest {

  private static final long COURIER_ID = 201L;
  private static final PersonName COURIER_NAME = new PersonName("Jane", "Rider");
  private static final Address COURIER_ADDRESS =
          new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
  private static final double DELTA = 1e-9;

  private CourierRepository courierRepository;
  private CourierService courierService;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
  }

  @Test
  public void shouldCreateCourier() {
    Courier courier = courierService.createCourier(COURIER_NAME, COURIER_ADDRESS);

    verify(courierRepository).save(courier);
    assertSame(COURIER_NAME, courier.getName());
    assertSame(COURIER_ADDRESS, courier.getAddress());
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(COURIER_NAME, COURIER_ADDRESS);
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(COURIER_ID, true);

    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(COURIER_NAME, COURIER_ADDRESS);
    courier.noteAvailable();
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(COURIER_ID, false);

    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier(COURIER_NAME, COURIER_ADDRESS);
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
    LocalDateTime before = LocalDateTime.now();

    courierService.updateLocation(COURIER_ID, 37.9000, -122.4000);

    assertEquals(37.9000, courier.getCurrentLatitude(), DELTA);
    assertEquals(-122.4000, courier.getCurrentLongitude(), DELTA);
    assertNotNull(courier.getLastLocationUpdate());
    assertFalse(courier.getLastLocationUpdate().isBefore(before));
  }

  @Test
  public void shouldThrowWhenCourierNotFoundOnLocationUpdate() {
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.empty());

    try {
      courierService.updateLocation(COURIER_ID, 37.9000, -122.4000);
      fail("Expected CourierNotFoundException");
    } catch (CourierNotFoundException e) {
      assertEquals("Courier not found: " + COURIER_ID, e.getMessage());
    }
  }
}
