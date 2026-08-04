package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Arrays;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestaurantControllerTest {

  private static final Address ADDRESS = new Address("1 High Street", null, "Oakland", "CA", "94619", 37.79, -122.19);

  private static final String CREATE_RESTAURANT_REQUEST_JSON =
          "{\"name\":\"My Restaurant\"," +
          "\"address\":{\"street1\":\"1 High Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\"}," +
          "\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]}}";

  private RestaurantService restaurantService;
  private RestaurantController restaurantController;

  @Before
  public void setUp() {
    restaurantService = mock(RestaurantService.class);
    restaurantController = new RestaurantController(restaurantService);
  }

  @Test
  public void shouldCreateRestaurant() {
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(makeRestaurant(1L));

    given().
            standaloneSetup(configureControllers(restaurantController)).
            body(CREATE_RESTAURANT_REQUEST_JSON).
            contentType("application/json").
    when().
            post("/restaurants").
    then().
            statusCode(200).
            body("id", equalTo(1));
  }

  @Test
  public void shouldFindRestaurant() {
    when(restaurantService.findById(1L)).thenReturn(Optional.of(makeRestaurant(1L)));

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/1").
    then().
            statusCode(200).
            body("id", equalTo(1)).
            body("name", equalTo("My Restaurant"));
  }

  @Test
  public void shouldNotFindUnknownRestaurant() {
    when(restaurantService.findById(1L)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/1").
    then().
            statusCode(404);
  }

  private Restaurant makeRestaurant(long id) {
    return new Restaurant(id, "My Restaurant", ADDRESS,
            new RestaurantMenu(Arrays.asList(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")))));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }
}
