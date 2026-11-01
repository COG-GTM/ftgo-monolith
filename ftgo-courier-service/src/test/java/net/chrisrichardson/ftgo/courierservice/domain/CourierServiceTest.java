package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CourierServiceTest {

  private CourierService courierService;
  private CourierRepository courierRepository;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
  }

  @Test
  public void shouldCreateCourier() {
    PersonName name = new PersonName("Jane", "Smith");
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);

    Courier result = courierService.createCourier(name, address);

    assertNotNull(result);
    assertEquals("Jane", result.getName().getFirstName());
    assertEquals("Smith", result.getName().getLastName());
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldFindCourierById() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    Courier result = courierService.findCourierById(1L);

    assertNotNull(result);
    assertEquals("Jane", result.getName().getFirstName());
    verify(courierRepository).findById(1L);
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);

    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);

    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1L, 40.7128, -74.0060);

    assertEquals(Double.valueOf(40.7128), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-74.0060), courier.getCurrentLongitude());
  }

  @Test(expected = CourierNotFoundException.class)
  public void shouldThrowWhenUpdatingLocationForNonExistentCourier() {
    when(courierRepository.findById(999L)).thenReturn(Optional.empty());

    courierService.updateLocation(999L, 40.7128, -74.0060);
  }
}
