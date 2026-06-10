package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

  @Mock
  private CourierRepository courierRepository;

  private CourierService courierService;

  @BeforeEach
  void setUp() {
    courierService = new CourierService(courierRepository);
  }

  @Test
  void shouldCreateCourier() {
    PersonName name = new PersonName("John", "Doe");
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    when(courierRepository.save(any(Courier.class))).thenAnswer(inv -> inv.getArgument(0));

    Courier courier = courierService.createCourier(name, address);

    assertThat(courier).isNotNull();
    assertThat(courier.getName().getFirstName()).isEqualTo("John");
    verify(courierRepository).save(any(Courier.class));
  }

  @Test
  void shouldUpdateAvailabilityToAvailable() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, true);

    assertThat(courier.isAvailable()).isTrue();
  }

  @Test
  void shouldUpdateAvailabilityToUnavailable() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    courier.noteAvailable();
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(1L, false);

    assertThat(courier.isAvailable()).isFalse();
  }

  @Test
  void shouldFindCourierById() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    Courier found = courierService.findCourierById(1L);

    assertThat(found).isEqualTo(courier);
  }

  @Test
  void shouldUpdateLocation() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1L, 37.8044, -122.2712);

    assertThat(courier.getCurrentLatitude()).isEqualTo(37.8044);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-122.2712);
  }

  @Test
  void shouldThrowWhenUpdatingLocationOfNonExistentCourier() {
    when(courierRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courierService.updateLocation(999L, 37.8044, -122.2712))
            .isInstanceOf(CourierNotFoundException.class);
  }
}
