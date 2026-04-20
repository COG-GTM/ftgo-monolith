package net.chrisrichardson.ftgo.orderservice.domain.client;

import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class RestaurantServiceClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate, String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public RestaurantDTO findById(long restaurantId) {
    try {
      ResponseEntity<RestaurantDTO> response = restTemplate.getForEntity(
              restaurantServiceUrl + "/restaurants/{id}",
              RestaurantDTO.class,
              restaurantId);

      if (response.getBody() == null) {
        throw new RestaurantNotFoundException(restaurantId);
      }

      return response.getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new RestaurantNotFoundException(restaurantId);
      }
      logger.error("Error calling Restaurant Service for restaurant {}: {}", restaurantId, e.getMessage());
      throw new RuntimeException("Failed to fetch restaurant from Restaurant Service", e);
    } catch (Exception e) {
      logger.error("Error calling Restaurant Service for restaurant {}: {}", restaurantId, e.getMessage());
      throw new RuntimeException("Failed to fetch restaurant from Restaurant Service", e);
    }
  }
}
