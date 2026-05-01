package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CourierServiceTest {

  private CourierRepository courierRepository;
  private CourierService courierService;

  @Before
  public void setUp() {
    courierRepository = Mockito.mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
  }

  @Test
  public void shouldCreateCourier() {
    PersonName name = new PersonName("John", "Doe");
    Address address = new Address("1 Scenic Drive", null, "Oakland", "CA", "94555");

    Courier savedCourier = new Courier(name, address);
    when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);

    Courier result = courierService.createCourier(name, address);

    assertNotNull(result);
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldUpdateAvailabilityToTrue() {
    long courierId = 1L;
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(courierId, true);

    assertTrue(courier.isAvailable());
    verify(courierRepository).findById(courierId);
  }

  @Test
  public void shouldUpdateAvailabilityToFalse() {
    long courierId = 1L;
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    courier.noteAvailable();
    when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(courierId, false);

    assertFalse(courier.isAvailable());
    verify(courierRepository).findById(courierId);
  }

  @Test
  public void shouldFindCourierById() {
    long courierId = 1L;
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

    Courier result = courierService.findCourierById(courierId);

    assertNotNull(result);
    assertEquals("John", result.getName().getFirstName());
    assertEquals("Doe", result.getName().getLastName());
    verify(courierRepository).findById(courierId);
  }
}
