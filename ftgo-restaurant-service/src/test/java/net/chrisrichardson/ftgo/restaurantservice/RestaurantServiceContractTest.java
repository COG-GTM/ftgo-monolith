package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.web.CorrelationIdFilter;
import net.chrisrichardson.ftgo.restaurantservice.api.client.HttpRestaurantServiceClient;
import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClient;
import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClientConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.api.web.CreateRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Boots the restaurant service exactly as {@code RestaurantServiceMain} does (own config, own Flyway schema) and
 * verifies the published HTTP contract using the consumer-side {@link HttpRestaurantServiceClient}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestaurantServiceConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.name=restaurant-service",
                "spring.datasource.url=jdbc:h2:mem:restaurant;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        })
public class RestaurantServiceContractTest {

  @Rule
  public OutputCapture output = new OutputCapture();

  @LocalServerPort
  private int port;

  private final RestTemplate restTemplate = RestaurantServiceClientConfiguration.restaurantServiceRestTemplate(5000);

  @After
  public void clearMdc() {
    MDC.clear();
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private long createAjanta(String correlationId) {
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8, -122.27),
            new RestaurantMenuDTO(Collections.singletonList(new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")))));
    HttpHeaders headers = new HttpHeaders();
    headers.set(CorrelationIdFilter.HEADER, correlationId);
    ResponseEntity<CreateRestaurantResponse> response = restTemplate.exchange(baseUrl() + "/restaurants", HttpMethod.POST,
            new HttpEntity<>(request, headers), CreateRestaurantResponse.class);
    assertEquals(correlationId, response.getHeaders().getFirst(CorrelationIdFilter.HEADER));
    return response.getBody().getId();
  }

  @Test
  public void clientShouldReadRestaurantCreatedThroughTheApi() {
    long id = createAjanta("contract-create-1");

    MDC.put(CorrelationIdFilter.MDC_KEY, "contract-read-1");
    RestaurantServiceClient client = new HttpRestaurantServiceClient(
            RestaurantServiceClientConfiguration.restaurantServiceRestTemplate(5000), baseUrl());
    GetRestaurantResponse restaurant = client.findRestaurantById(id).get();

    assertEquals(Long.valueOf(id), restaurant.getId());
    assertEquals("Ajanta", restaurant.getName());
    assertEquals("Oakland", restaurant.getAddress().getCity());
    assertEquals(Double.valueOf(37.8), restaurant.getAddress().getLatitude());
    MenuItemDTO item = restaurant.findMenuItem("1").get();
    assertEquals("Chicken Vindaloo", item.getName());
    assertEquals(new Money("12.34"), item.getPrice());
  }

  @Test
  public void clientShouldReturnEmptyForUnknownRestaurant() {
    RestaurantServiceClient client = new HttpRestaurantServiceClient(
            RestaurantServiceClientConfiguration.restaurantServiceRestTemplate(5000), baseUrl());
    assertFalse(client.findRestaurantById(987654L).isPresent());
  }

  @Test
  public void shouldWriteStructuredJsonLogsWithCorrelationId() {
    createAjanta("contract-log-1");

    String logLine = java.util.Arrays.stream(output.toString().split("\\r?\\n")).filter(l -> l.contains("Created restaurant"))
            .findFirst().orElseThrow(() -> new AssertionError("no 'Created restaurant' log line"));
    assertTrue(logLine, logLine.startsWith("{"));
    assertTrue(logLine, logLine.contains("\"correlationId\":\"contract-log-1\""));
    assertTrue(logLine, logLine.contains("\"service\":\"ftgo-restaurant-service\""));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldExposeActuatorHealthMetricsAndSharedSwaggerDocs() {
    Map<String, Object> health = restTemplate.getForObject(baseUrl() + "/actuator/health", Map.class);
    assertEquals("UP", health.get("status"));

    Map<String, Object> metrics = restTemplate.getForObject(baseUrl() + "/actuator/metrics", Map.class);
    assertTrue(metrics.get("names").toString().contains("jvm.memory.used"));

    Map<String, Object> apiDocs = restTemplate.getForObject(baseUrl() + "/v2/api-docs", Map.class);
    assertTrue(((Map<String, Object>) apiDocs.get("paths")).containsKey("/restaurants/{restaurantId}"));
  }
}
