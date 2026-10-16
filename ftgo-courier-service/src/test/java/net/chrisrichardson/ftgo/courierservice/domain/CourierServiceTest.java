package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CourierServiceTest {

  @Test
  public void createCourierSavesAndReturnsCourierWithAddressLocation() {
    CourierRepository repository = mock(CourierRepository.class);
    CourierService service = new CourierService(repository);
    PersonName name = new PersonName("Ada", "Lovelace");
    Address address = new Address("1 Main St", null, "London", "London", "N1", 51.5, -0.12);
    ArgumentCaptor<Courier> courierCaptor = ArgumentCaptor.forClass(Courier.class);

    Courier courier = service.createCourier(name, address);

    verify(repository).save(courierCaptor.capture());
    Courier savedCourier = courierCaptor.getValue();
    assertSame(savedCourier, courier);
    assertSame(name, savedCourier.getName());
    assertSame(address, savedCourier.getAddress());
    assertEquals(Double.valueOf(51.5), savedCourier.getCurrentLatitude());
    assertEquals(Double.valueOf(-0.12), savedCourier.getCurrentLongitude());
  }

  @Test
  public void updateAvailabilityMarksCourierAvailable() {
    CourierRepository repository = mock(CourierRepository.class);
    Courier courier = new Courier(new PersonName("Ada", "Lovelace"), null);
    when(repository.findById(1L)).thenReturn(Optional.of(courier));
    CourierService service = new CourierService(repository);

    service.updateAvailability(1L, true);

    assertTrue(courier.isAvailable());
    verify(repository).findById(1L);
  }

  @Test
  public void updateAvailabilityMarksAvailableCourierUnavailable() {
    CourierRepository repository = mock(CourierRepository.class);
    Courier courier = new Courier(new PersonName("Ada", "Lovelace"), null);
    courier.noteAvailable();
    when(repository.findById(2L)).thenReturn(Optional.of(courier));
    CourierService service = new CourierService(repository);

    service.updateAvailability(2L, false);

    assertFalse(courier.isAvailable());
    verify(repository).findById(2L);
  }

  @Test
  public void findCourierByIdReturnsRepositoryCourier() {
    CourierRepository repository = mock(CourierRepository.class);
    Courier courier = new Courier(new PersonName("Ada", "Lovelace"), null);
    when(repository.findById(3L)).thenReturn(Optional.of(courier));
    CourierService service = new CourierService(repository);

    assertSame(courier, service.findCourierById(3L));
  }

  @Test
  public void updateLocationUpdatesCoordinatesAndTimestamp() {
    CourierRepository repository = mock(CourierRepository.class);
    Courier courier = new Courier(new PersonName("Ada", "Lovelace"), null);
    when(repository.findById(4L)).thenReturn(Optional.of(courier));
    CourierService service = new CourierService(repository);

    service.updateLocation(4L, 40.1, -73.9);

    assertEquals(Double.valueOf(40.1), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-73.9), courier.getCurrentLongitude());
    assertNotNull(courier.getLastLocationUpdate());
  }

  @Test
  public void updateLocationForUnknownCourierThrowsCourierNotFoundException() {
    CourierRepository repository = mock(CourierRepository.class);
    when(repository.findById(99L)).thenReturn(Optional.empty());
    CourierService service = new CourierService(repository);

    assertThatThrownBy(() -> service.updateLocation(99L, 40.1, -73.9))
            .isInstanceOf(CourierNotFoundException.class)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Courier not found: 99");
    verify(repository).findById(99L);
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void findCourierByIdForUnknownCourierCurrentlyThrowsNoSuchElementException() {
    // Suspected bug: uses Optional.get() so missing couriers throw NoSuchElementException instead of CourierNotFoundException.
    CourierRepository repository = mock(CourierRepository.class);
    when(repository.findById(99L)).thenReturn(Optional.empty());
    CourierService service = new CourierService(repository);

    assertThatThrownBy(() -> service.findCourierById(99L))
            .isExactlyInstanceOf(NoSuchElementException.class)
            .isNotInstanceOf(CourierNotFoundException.class);
    verify(repository).findById(99L);
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void updateAvailabilityForUnknownCourierCurrentlyThrowsNoSuchElementException() {
    // Suspected bug: noteAvailable/noteUnavailable use Optional.get() so missing couriers throw NoSuchElementException instead of CourierNotFoundException.
    CourierRepository repository = mock(CourierRepository.class);
    when(repository.findById(99L)).thenReturn(Optional.empty());
    CourierService service = new CourierService(repository);

    assertThatThrownBy(() -> service.updateAvailability(99L, true))
            .isExactlyInstanceOf(NoSuchElementException.class);
    assertThatThrownBy(() -> service.updateAvailability(99L, false))
            .isExactlyInstanceOf(NoSuchElementException.class);
    verify(repository, times(2)).findById(99L);
    verifyNoMoreInteractions(repository);
  }
}
