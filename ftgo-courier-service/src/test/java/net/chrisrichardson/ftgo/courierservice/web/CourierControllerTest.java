package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.*;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierControllerTest {

  @Mock
  private CourierService courierService;

  @Mock
  private Courier mockCourier;

  private CourierController controller;

  @BeforeEach
  void setUp() {
    controller = new CourierController(courierService);
  }

  @Test
  void shouldCreateCourier() {
    when(mockCourier.getId()).thenReturn(1L);
    when(courierService.createCourier(any(), any())).thenReturn(mockCourier);

    CreateCourierRequest request = new CreateCourierRequest();
    request.setName(new PersonName("John", "Doe"));
    request.setAddress(new Address("1 Main St", null, "Oakland", "CA", "94612"));

    ResponseEntity<CreateCourierResponse> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getId()).isEqualTo(1L);
    verify(courierService).createCourier(any(), any());
  }

  @Test
  void shouldUpdateAvailability() {
    CourierAvailability availability = new CourierAvailability();
    availability.setAvailable(true);

    ResponseEntity<String> response = controller.updateCourierLocation(1L, availability);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(courierService).updateAvailability(1L, true);
  }

  @Test
  void shouldGetCourier() {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    when(courierService.findCourierById(1L)).thenReturn(courier);

    ResponseEntity<Courier> response = controller.get(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void shouldUpdateLocation() {
    CourierLocationUpdate update = new CourierLocationUpdate();
    update.setLatitude(37.8044);
    update.setLongitude(-122.2712);

    ResponseEntity<String> response = controller.updateLocation(1L, update);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(courierService).updateLocation(1L, 37.8044, -122.2712);
  }

  @Test
  void shouldGetWorkload() {
    when(mockCourier.getId()).thenReturn(1L);
    when(mockCourier.getActiveDeliveryCount()).thenReturn(2);
    when(mockCourier.isAvailable()).thenReturn(true);
    when(mockCourier.getCurrentLatitude()).thenReturn(37.8044);
    when(mockCourier.getCurrentLongitude()).thenReturn(-122.2712);
    when(mockCourier.getLastLocationUpdate()).thenReturn(null);
    when(courierService.findCourierById(1L)).thenReturn(mockCourier);

    ResponseEntity<CourierWorkloadResponse> response = controller.getWorkload(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isAvailable()).isTrue();
    assertThat(response.getBody().getActiveDeliveries()).isEqualTo(2);
  }
}
