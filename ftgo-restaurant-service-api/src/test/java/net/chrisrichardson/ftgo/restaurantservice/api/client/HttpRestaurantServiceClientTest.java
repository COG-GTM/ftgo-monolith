package net.chrisrichardson.ftgo.restaurantservice.api.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.web.CorrelationIdFilter;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class HttpRestaurantServiceClientTest {

  private static final String RESTAURANT_JSON = "{\"id\":7,\"name\":\"Ajanta\","
          + "\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\",\"latitude\":37.8,\"longitude\":-122.27},"
          + "\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]},"
          + "\"someFutureField\":true}";

  private RestTemplate restTemplate;
  private MockRestServiceServer server;
  private HttpRestaurantServiceClient client;

  @Before
  public void setUp() {
    restTemplate = RestaurantServiceClientConfiguration.restaurantServiceRestTemplate(1000);
    client = new HttpRestaurantServiceClient(restTemplate, "http://restaurant-service/");
    server = MockRestServiceServer.bindTo(restTemplate).build();
  }

  @After
  public void tearDown() {
    MDC.clear();
  }

  @Test
  public void shouldMapRestaurantResponseAndPropagateCorrelationId() {
    MDC.put(CorrelationIdFilter.MDC_KEY, "corr-42");
    server.expect(once(), requestTo("http://restaurant-service/restaurants/7"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(CorrelationIdFilter.HEADER, "corr-42"))
            .andRespond(withSuccess(RESTAURANT_JSON, MediaType.APPLICATION_JSON));

    GetRestaurantResponse restaurant = client.findRestaurantById(7).get();

    server.verify();
    assertEquals("Ajanta", restaurant.getName());
    assertEquals(Double.valueOf(37.8), restaurant.getAddress().getLatitude());
    assertEquals(new Money("12.34"), restaurant.findMenuItem("1").get().getPrice());
    assertFalse(restaurant.findMenuItem("nope").isPresent());
  }

  @Test
  public void shouldReturnEmptyWhenRestaurantNotFound() {
    server.expect(requestTo("http://restaurant-service/restaurants/99")).andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertEquals(Optional.empty(), client.findRestaurantById(99));
  }

  @Test(expected = RestaurantServiceUnavailableException.class)
  public void shouldWrapServerErrors() {
    server.expect(requestTo("http://restaurant-service/restaurants/1")).andRespond(withServerError());

    client.findRestaurantById(1);
  }

  @Test
  public void shouldWrapConnectionFailures() {
    server.expect(requestTo("http://restaurant-service/restaurants/1")).andRespond(request -> {
      throw new ResourceAccessException("Connection refused");
    });

    try {
      client.findRestaurantById(1);
      fail("expected RestaurantServiceUnavailableException");
    } catch (RestaurantServiceUnavailableException e) {
      assertTrue(e.getCause() instanceof ResourceAccessException);
    }
  }
}
