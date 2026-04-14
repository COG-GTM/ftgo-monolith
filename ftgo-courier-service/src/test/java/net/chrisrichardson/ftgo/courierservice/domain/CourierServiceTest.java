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
    PersonName name = new PersonName("Bob", "Smith");
    Address address = new Address("1 Main St", "", "Springfield", "IL", "62701");
    Courier courier = new Courier(name, address);

    when(courierRepository.save(any(Courier.class))).thenReturn(courier);

    Courier result = courierService.createCourier(name, address);

    assertNotNull(result);
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(new PersonName("Bob", "Smith"),
        new Address("1 Main St", "", "Springfield", "IL", "62701"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);

    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(new PersonName("Bob", "Smith"),
        new Address("1 Main St", "", "Springfield", "IL", "62701"));
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);

    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldFindCourierById() {
    Courier courier = new Courier(new PersonName("Bob", "Smith"),
        new Address("1 Main St", "", "Springfield", "IL", "62701"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    Courier result = courierService.findCourierById(1L);

    assertNotNull(result);
  }
}
