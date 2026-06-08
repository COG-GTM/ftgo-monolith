package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that the Order service uses to talk to the extracted Restaurant
 * microservice. Replaces the previous direct {@code RestaurantRepository} call.
 * Translates HTTP status codes back into the domain exceptions callers expect.
 */
public class RestaurantServiceProxy {

  private static final Logger logger = LoggerFactory.getLogger(RestaurantServiceProxy.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestaurantServiceProxy(String baseUrl, RestTemplate restTemplate) {
    this.baseUrl = baseUrl;
    this.restTemplate = restTemplate;
  }

  public RestaurantDTO findRestaurant(long restaurantId) {
    String url = baseUrl + "/restaurants/" + restaurantId;
    logger.debug("Fetching restaurant {} from {}", restaurantId, url);
    try {
      return restTemplate.getForObject(url, RestaurantDTO.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new RestaurantNotFoundException(restaurantId);
      }
      logger.error("Error fetching restaurant {}: {}", restaurantId, e.getStatusCode(), e);
      throw new RestaurantServiceException("Error fetching restaurant " + restaurantId + ": " + e.getStatusCode(), e);
    } catch (ResourceAccessException e) {
      logger.error("Restaurant service unavailable when fetching restaurant {}", restaurantId, e);
      throw new RestaurantServiceException("Restaurant service unavailable for restaurant " + restaurantId, e);
    } catch (RestClientException e) {
      logger.error("Unexpected error fetching restaurant {}", restaurantId, e);
      throw new RestaurantServiceException("Unexpected error fetching restaurant " + restaurantId, e);
    }
  }
}
