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
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);

    when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Courier courier = courierService.createCourier(name, address);

    assertNotNull(courier);
    assertEquals("John", courier.getName().getFirstName());
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);

    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);

    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1L, 40.7128, -74.0060);

    assertEquals(40.7128, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-74.0060, courier.getCurrentLongitude(), 0.0001);
  }

  @Test(expected = CourierNotFoundException.class)
  public void shouldThrowWhenCourierNotFoundOnLocationUpdate() {
    when(courierRepository.findById(999L)).thenReturn(Optional.empty());

    courierService.updateLocation(999L, 40.7128, -74.0060);
  }
}
