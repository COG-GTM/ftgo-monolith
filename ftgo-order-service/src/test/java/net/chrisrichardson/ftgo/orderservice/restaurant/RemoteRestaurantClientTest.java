package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_MENU_ITEM_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_PRICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RemoteRestaurantClientTest {

  private static final String BASE_URL = "http://ftgo-restaurant-service:8082";
  private static final String AJANTA_URL = BASE_URL + "/restaurants/" + AJANTA_ID;

  private static final String GET_RESTAURANT_RESPONSE_JSON =
          "{\"id\":" + AJANTA_ID + ",\"name\":\"" + AJANTA_RESTAURANT_NAME + "\"," +
          "\"address\":{\"street1\":\"1 High Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\",\"latitude\":37.79,\"longitude\":-122.19}," +
          "\"menu\":{\"menuItemDTOs\":[{\"id\":\"" + CHICKEN_VINDALOO_MENU_ITEM_ID + "\",\"name\":\"" + CHICKEN_VINDALOO + "\",\"price\":\"12.34\"}]}}";

  private RestTemplate restTemplate;
  private MockRestServiceServer restaurantService;
  private RemoteRestaurantClient restaurantClient;

  @Before
  public void setUp() {
    restTemplate = RestaurantClientConfiguration.makeRestTemplate(5000, 5000);
    restaurantService = MockRestServiceServer.bindTo(restTemplate).build();
    restaurantClient = new RemoteRestaurantClient(restTemplate, BASE_URL);
  }

  @Test
  public void shouldTranslateRestaurantResponseIntoTheOrderDomainModel() {
    restaurantService.expect(requestTo(AJANTA_URL))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andRespond(withSuccess(GET_RESTAURANT_RESPONSE_JSON, MediaType.APPLICATION_JSON));

    Restaurant restaurant = restaurantClient.findRestaurant(AJANTA_ID).get();

    assertEquals(Long.valueOf(AJANTA_ID), restaurant.getId());
    assertEquals(AJANTA_RESTAURANT_NAME, restaurant.getName());
    assertEquals("Oakland", restaurant.getAddress().getCity());

    MenuItem chickenVindaloo = restaurant.findMenuItem(CHICKEN_VINDALOO_MENU_ITEM_ID).get();
    assertEquals(CHICKEN_VINDALOO, chickenVindaloo.getName());
    assertEquals(CHICKEN_VINDALOO_PRICE, chickenVindaloo.getPrice());

    restaurantService.verify();
  }

  @Test
  public void shouldTranslateNotFoundIntoAnEmptyResult() {
    restaurantService.expect(requestTo(AJANTA_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<Restaurant> restaurant = restaurantClient.findRestaurant(AJANTA_ID);

    assertFalse(restaurant.isPresent());
    restaurantService.verify();
  }

  @Test
  public void shouldTranslateServerErrorIntoServiceUnavailable() {
    restaurantService.expect(requestTo(AJANTA_URL))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    try {
      restaurantClient.findRestaurant(AJANTA_ID);
      fail("Expected RestaurantServiceUnavailableException");
    } catch (RestaurantServiceUnavailableException expected) {
      // expected
    }

    restaurantService.verify();
  }
}
