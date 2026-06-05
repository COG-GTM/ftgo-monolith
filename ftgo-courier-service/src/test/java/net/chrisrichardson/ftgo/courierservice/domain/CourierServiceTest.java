package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    Address address = new Address("1 Main St", null, "NYC", "NY", "10001");
    Courier courier = new Courier(name, address);
    when(courierRepository.save(any(Courier.class))).thenReturn(courier);

    Courier result = courierService.createCourier(name, address);
    assertThat(result.getName().getFirstName()).isEqualTo("John");
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(new PersonName("A", "B"), new Address("1", null, "C", "S", "Z"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);
    assertThat(courier.isAvailable()).isTrue();
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(new PersonName("A", "B"), new Address("1", null, "C", "S", "Z"));
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);
    assertThat(courier.isAvailable()).isFalse();
  }

  @Test
  public void shouldFindCourierById() {
    Courier courier = new Courier(new PersonName("X", "Y"), new Address("1", null, "C", "S", "Z"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    Courier result = courierService.findCourierById(1L);
    assertThat(result).isEqualTo(courier);
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier(new PersonName("A", "B"), new Address("1", null, "C", "S", "Z"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1L, 40.7, -74.0);
    assertThat(courier.getCurrentLatitude()).isEqualTo(40.7);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-74.0);
  }

  @Test
  public void shouldThrowWhenUpdatingLocationForNonExistentCourier() {
    when(courierRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courierService.updateLocation(999L, 40.7, -74.0))
            .isInstanceOf(CourierNotFoundException.class);
  }
}
