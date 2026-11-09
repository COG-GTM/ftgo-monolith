package net.chrisrichardson.ftgo.restaurantservice.api.client;

import net.chrisrichardson.ftgo.common.web.CorrelationIdFilter;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class HttpRestaurantServiceClient implements RestaurantServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(HttpRestaurantServiceClient.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public HttpRestaurantServiceClient(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.restTemplate.getInterceptors().add((request, body, execution) -> {
      String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
      if (correlationId != null)
        request.getHeaders().set(CorrelationIdFilter.HEADER, correlationId);
      return execution.execute(request, body);
    });
  }

  @Override
  public Optional<GetRestaurantResponse> findRestaurantById(long restaurantId) {
    try {
      return Optional.ofNullable(restTemplate.getForObject(baseUrl + "/restaurants/{restaurantId}",
              GetRestaurantResponse.class, restaurantId));
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND)
        return Optional.empty();
      logger.warn("Restaurant service returned {} for restaurant {}", e.getStatusCode(), restaurantId);
      throw new RestaurantServiceUnavailableException("Restaurant service returned " + e.getStatusCode(), e);
    } catch (ResourceAccessException e) {
      logger.warn("Restaurant service unreachable at {}: {}", baseUrl, e.getMessage());
      throw new RestaurantServiceUnavailableException("Restaurant service unreachable", e);
    }
  }
}
