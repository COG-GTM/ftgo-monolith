package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierServiceClientTest {

  private static final String BASE_URL = "http://localhost:8084";

  private RestTemplate restTemplate;
  private CourierServiceClient client;

  @Before
  public void setUp() {
    restTemplate = mock(RestTemplate.class);
    client = new CourierServiceClient(restTemplate, BASE_URL);
  }

  @Test
  public void shouldPostAssignRequestToCourierService() {
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(20);
    LocalDateTime eta = readyBy.plusMinutes(30);
    CourierAssignmentResponse expected = new CourierAssignmentResponse(99L, eta);

    when(restTemplate.postForObject(
            eq(BASE_URL + "/couriers/assign"),
            any(CourierAssignmentRequest.class),
            eq(CourierAssignmentResponse.class)))
            .thenReturn(expected);

    Address restaurantAddress = new Address("1 Main St", null, "Oakland", "CA", "94612");
    CourierAssignmentResponse response = client.assignCourier(42L, restaurantAddress, null, readyBy);

    assertEquals(Long.valueOf(99L), response.getCourierId());
    assertEquals(eta, response.getEstimatedDeliveryTime());

    verify(restTemplate).postForObject(
            eq(BASE_URL + "/couriers/assign"),
            any(CourierAssignmentRequest.class),
            eq(CourierAssignmentResponse.class));
  }
}
