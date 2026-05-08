package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

public class CourierServiceClient {

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate,
                              @Value("${services.courier-service.url:http://courier-service:8080}") String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public AssignCourierResponse assignCourier(long orderId, Address restaurantAddress, LocalDateTime readyBy) {
    String url = courierServiceUrl + "/couriers/assign";
    AssignCourierRequest request = new AssignCourierRequest(orderId, restaurantAddress, readyBy);
    ResponseEntity<AssignCourierResponse> response = restTemplate.postForEntity(url, request, AssignCourierResponse.class);
    return response.getBody();
  }
}
