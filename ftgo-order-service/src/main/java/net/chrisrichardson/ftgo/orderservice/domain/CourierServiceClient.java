package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.courierservice.api.CourierActionDTO;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CourierServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(CourierServiceClient.class);

  private RestTemplate restTemplate;
  private String courierServiceUrl;

  public CourierServiceClient() {
  }

  public CourierServiceClient(RestTemplate restTemplate, String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public CourierAssignmentResponse assignCourier(CourierAssignmentRequest request) {
    String url = courierServiceUrl + "/couriers/assignments";
    logger.info("Calling courier service for assignment: orderId={}", request.getOrderId());
    return restTemplate.postForObject(url, request, CourierAssignmentResponse.class);
  }

  public List<CourierActionDTO> getActionsForOrder(long courierId, long orderId) {
    String url = courierServiceUrl + "/couriers/" + courierId + "/actions?orderId=" + orderId;
    logger.info("Fetching courier actions: courierId={}, orderId={}", courierId, orderId);
    CourierActionDTO[] actions = restTemplate.getForObject(url, CourierActionDTO[].class);
    return actions != null ? Arrays.asList(actions) : Collections.emptyList();
  }
}
