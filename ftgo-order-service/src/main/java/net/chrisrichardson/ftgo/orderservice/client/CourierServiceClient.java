package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class CourierServiceClient {

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate,
                               @Value("${courier.service.url}") String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public List<Courier> findAllAvailableCouriers() {
    String url = courierServiceUrl + "/couriers/available";
    
    try {
      ResponseEntity<List<Courier>> response = restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Courier>>() {}
      );
      
      return response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch available couriers", e);
    }
  }
}
