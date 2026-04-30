package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CourierServiceTest {

  private CourierRepository courierRepository;
  private CourierService courierService;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
  }

  @Test
  public void shouldCreateCourier() {
    PersonName name = new PersonName("John", "Doe");
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94607");

    when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Courier result = courierService.createCourier(name, address);
    assertNotNull(result);
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);
    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier();
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1L, 37.8044, -122.2712);
    assertEquals(37.8044, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-122.2712, courier.getCurrentLongitude(), 0.0001);
  }

  @Test(expected = CourierNotFoundException.class)
  public void shouldThrowWhenCourierNotFoundOnLocationUpdate() {
    when(courierRepository.findById(99L)).thenReturn(Optional.empty());
    courierService.updateLocation(99L, 37.8, -122.2);
  }
}
