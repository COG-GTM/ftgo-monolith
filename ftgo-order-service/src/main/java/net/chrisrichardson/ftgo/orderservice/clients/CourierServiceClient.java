package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.courierservice.api.ScheduleDeliveryRequest;
import net.chrisrichardson.ftgo.courierservice.api.ScheduleDeliveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

public class CourierServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(CourierServiceClient.class);

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate,
                              @Value("${services.courier.url}") String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public long scheduleDelivery(long orderId, Address pickupAddress, Address deliveryAddress, LocalDateTime readyBy) {
    String url = courierServiceUrl + "/deliveries/schedule";
    ScheduleDeliveryRequest request = new ScheduleDeliveryRequest(orderId, pickupAddress, deliveryAddress, readyBy);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ScheduleDeliveryRequest> entity = new HttpEntity<>(request, headers);
    ResponseEntity<ScheduleDeliveryResponse> response = restTemplate.postForEntity(url, entity, ScheduleDeliveryResponse.class);
    if (response.getBody() == null) {
      throw new RuntimeException("No response from courier service");
    }
    return response.getBody().getCourierId();
  }

}
