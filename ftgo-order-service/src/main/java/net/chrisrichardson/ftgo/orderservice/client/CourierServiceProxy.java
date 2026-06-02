package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.courierservice.api.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.Courier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HTTP client proxy for the extracted courier microservice.
 *
 * <p>Replaces the in-process {@code CourierRepository.findAllAvailable()} read that
 * {@code OrderService} used during delivery scheduling. It calls the courier service's REST surface
 * and reconstructs {@link Courier} instances (the shared {@code ftgo-domain} entity) so the existing
 * in-process {@code CourierAssignmentStrategy} keeps working unchanged.
 *
 * <p>Errors are mapped to domain exceptions: HTTP 404 -&gt; {@link CourierNotFoundException}; other
 * HTTP errors and connectivity failures are wrapped in {@link CourierServiceUnavailableException}.
 */
public class CourierServiceProxy {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public CourierServiceProxy(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public List<Courier> findAllAvailable() {
    String url = baseUrl + "/couriers/available";
    logger.debug("Fetching available couriers from courier service: {}", url);
    try {
      ResponseEntity<Courier[]> response = restTemplate.getForEntity(url, Courier[].class);
      Courier[] body = response.getBody();
      List<Courier> couriers = body == null ? Collections.emptyList() : Arrays.asList(body);
      logger.debug("Courier service returned {} available courier(s)", couriers.size());
      return couriers;
    } catch (ResourceAccessException e) {
      logger.error("Courier service unreachable at {}: {}", url, e.getMessage());
      throw new CourierServiceUnavailableException("Courier service unreachable at " + url, e);
    } catch (HttpStatusCodeException e) {
      logger.error("Courier service returned {} for {}", e.getStatusCode(), url);
      throw new CourierServiceUnavailableException(
              "Courier service returned " + e.getStatusCode() + " for " + url, e);
    }
  }

  public Courier findCourierById(long courierId) {
    String url = baseUrl + "/couriers/" + courierId;
    logger.debug("Fetching courier {} from courier service: {}", courierId, url);
    try {
      return restTemplate.getForObject(url, Courier.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("Courier {} not found in courier service", courierId);
        throw new CourierNotFoundException(courierId);
      }
      logger.error("Courier service returned {} for {}", e.getStatusCode(), url);
      throw new CourierServiceUnavailableException(
              "Courier service returned " + e.getStatusCode() + " for " + url, e);
    } catch (ResourceAccessException e) {
      logger.error("Courier service unreachable at {}: {}", url, e.getMessage());
      throw new CourierServiceUnavailableException("Courier service unreachable at " + url, e);
    } catch (HttpStatusCodeException e) {
      logger.error("Courier service returned {} for {}", e.getStatusCode(), url);
      throw new CourierServiceUnavailableException(
              "Courier service returned " + e.getStatusCode() + " for " + url, e);
    }
  }
}
