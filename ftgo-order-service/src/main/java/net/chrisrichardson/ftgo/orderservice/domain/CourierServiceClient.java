package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
public class CourierServiceClient {

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate, @Value("${services.courier.url}") String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public CourierAssignmentResponse assignCourier(Long orderId, Address restaurantAddress, Address deliveryAddress, LocalDateTime readyBy) {
    CourierAssignmentRequest request = new CourierAssignmentRequest(orderId, restaurantAddress, deliveryAddress, readyBy);
    return restTemplate.postForObject(courierServiceUrl + "/couriers/assign", request, CourierAssignmentResponse.class);
  }
}
