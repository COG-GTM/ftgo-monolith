package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.courierservice.api.CourierResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CourierServiceClient {

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate,
                              @Value("${courier.service.url:http://localhost:8083}") String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public List<Long> findAllAvailable() {
    String url = courierServiceUrl + "/couriers/available";
    try {
      ResponseEntity<List<CourierResponse>> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<CourierResponse>>() {}
      );
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return response.getBody().stream()
            .map(CourierResponse::getId)
            .collect(Collectors.toList());
      }
    } catch (HttpClientErrorException e) {
      return Collections.emptyList();
    }
    return Collections.emptyList();
  }
}
