package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CourierWorkloadResponse;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CourierControllerTest {

  private CourierService courierService;
  private CourierController controller;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    controller = new CourierController(courierService);
  }

  @Test
  public void shouldCreateCourier() {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(1L);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    CreateCourierRequest request = new CreateCourierRequest();
    request.setName(new PersonName("J", "D"));
    request.setAddress(new Address("1", null, "C", "S", "Z"));

    ResponseEntity<CreateCourierResponse> response = controller.create(request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getId()).isEqualTo(1L);
  }

  @Test
  public void shouldUpdateAvailability() {
    CourierAvailability availability = new CourierAvailability();
    availability.setAvailable(true);

    ResponseEntity<String> response = controller.updateCourierLocation(1L, availability);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(courierService).updateAvailability(1L, true);
  }

  @Test
  public void shouldGetCourier() {
    Courier courier = new Courier(new PersonName("J", "D"), new Address("1", null, "C", "S", "Z"));
    when(courierService.findCourierById(1L)).thenReturn(courier);

    ResponseEntity<Courier> response = controller.get(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(courier);
  }

  @Test
  public void shouldUpdateLocation() {
    CourierLocationUpdate locationUpdate = new CourierLocationUpdate();
    locationUpdate.setLatitude(40.7);
    locationUpdate.setLongitude(-74.0);

    ResponseEntity<String> response = controller.updateLocation(1L, locationUpdate);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(courierService).updateLocation(1L, 40.7, -74.0);
  }

  @Test
  public void shouldGetWorkload() {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(1L);
    when(courier.getActiveDeliveryCount()).thenReturn(2);
    when(courier.isAvailable()).thenReturn(true);
    when(courier.getCurrentLatitude()).thenReturn(40.7);
    when(courier.getCurrentLongitude()).thenReturn(-74.0);
    when(courier.getLastLocationUpdate()).thenReturn(null);
    when(courierService.findCourierById(1L)).thenReturn(courier);

    ResponseEntity<CourierWorkloadResponse> response = controller.getWorkload(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().isAvailable()).isTrue();
    assertThat(response.getBody().getActiveDeliveries()).isEqualTo(2);
  }
}
