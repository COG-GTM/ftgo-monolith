package net.chrisrichardson.ftgo.courierservice.domain;

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
  public void invalidLatitudeIsRejectedBeforeRepositoryLookup() {
    assertInvalidLocation(500, 0);
    assertInvalidLocation(-91, 0);
    assertInvalidLocation(Double.NaN, 0);
    assertInvalidLocation(Double.POSITIVE_INFINITY, 0);
  }

  @Test
  public void invalidLongitudeIsRejectedBeforeRepositoryLookup() {
    assertInvalidLocation(0, 181);
    assertInvalidLocation(0, -181);
    assertInvalidLocation(0, Double.NaN);
    assertInvalidLocation(0, Double.NEGATIVE_INFINITY);
  }

  @Test
  public void inclusiveCoordinateBoundsAreAccepted() {
    Courier courier = new Courier(null, null);
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

    courierService.updateLocation(1, 90, 180);
    assertThat(courier.getCurrentLatitude()).isEqualTo(90);
    assertThat(courier.getCurrentLongitude()).isEqualTo(180);

    courierService.updateLocation(1, -90, -180);
    assertThat(courier.getCurrentLatitude()).isEqualTo(-90);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-180);
    verify(courierRepository, times(2)).findById(1L);
  }

  @Test
  public void missingCourierUsesCourierNotFoundException() {
    when(courierRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courierService.findCourierById(99))
            .isInstanceOf(CourierNotFoundException.class);
    assertThatThrownBy(() -> courierService.noteAvailable(99))
            .isInstanceOf(CourierNotFoundException.class);
    assertThatThrownBy(() -> courierService.noteUnavailable(99))
            .isInstanceOf(CourierNotFoundException.class);
  }

  private void assertInvalidLocation(double latitude, double longitude) {
    assertThatThrownBy(() -> courierService.updateLocation(1, latitude, longitude))
            .isInstanceOf(IllegalArgumentException.class);
    verifyZeroInteractions(courierRepository);
  }
}
