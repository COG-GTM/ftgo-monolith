package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit 5 (Jupiter) unit tests for {@link CourierService}.
 *
 * The repository is a Mockito mock (Mockito ships with spring-boot-starter-test), so these tests run
 * without a Spring context or database and exercise only the service's orchestration logic.
 * Test cases follow section 3.3 of UNIT_TEST_PLAN.md.
 */
public class CourierServiceTest {

  private static final long COURIER_ID = 101L;

  private CourierRepository courierRepository;
  private CourierService courierService;
  private Courier courier;

  // Fresh mock, service and courier before every test so state never leaks between cases.
  @BeforeEach
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
    courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94611"));
  }

  // createCourier must build a Courier from the request data and persist it exactly once.
  @Test
  public void shouldCreateCourier() {
    PersonName name = new PersonName("John", "Smith");
    Address address = new Address("9 High St", null, "Berkeley", "CA", "94704");

    Courier created = courierService.createCourier(name, address);

    assertThat(created.getName()).isSameAs(name);
    assertThat(created.getAddress()).isSameAs(address);
    verify(courierRepository).save(created);
  }

  // updateAvailability(true) flips the courier to available.
  @Test
  public void shouldUpdateAvailabilityToAvailable() {
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(COURIER_ID, true);

    assertThat(courier.isAvailable()).isTrue();
  }

  // updateAvailability(false) flips a previously available courier back to unavailable.
  @Test
  public void shouldUpdateAvailabilityToUnavailable() {
    courier.noteAvailable();
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));

    courierService.updateAvailability(COURIER_ID, false);

    assertThat(courier.isAvailable()).isFalse();
  }

  // updateLocation stores the coordinates and stamps the time of the update.
  @Test
  public void shouldUpdateLocation() {
    when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));

    courierService.updateLocation(COURIER_ID, 37.8044, -122.2712);

    assertThat(courier.getCurrentLatitude()).isEqualTo(37.8044);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-122.2712);
    assertThat(courier.getLastLocationUpdate()).isNotNull();
  }

  // An unknown courier id on a location update surfaces as CourierNotFoundException.
  @Test
  public void shouldThrowWhenCourierNotFoundOnLocationUpdate() {
    when(courierRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courierService.updateLocation(COURIER_ID, 1.0, 2.0))
            .isInstanceOf(CourierNotFoundException.class)
            .hasMessageContaining(String.valueOf(COURIER_ID));
  }
}
