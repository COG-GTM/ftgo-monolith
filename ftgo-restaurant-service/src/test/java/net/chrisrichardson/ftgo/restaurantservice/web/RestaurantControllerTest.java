package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.restaurantservice.domain.MenuItem;
import net.chrisrichardson.ftgo.restaurantservice.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestaurantControllerTest {

  private static final long RESTAURANT_ID = 42L;
  private static final String RESTAURANT_NAME = "Ajanta";
  private static final String MENU_ITEM_ID = "1";
  private static final String MENU_ITEM_NAME = "Chicken Vindaloo";
  private static final Money MENU_ITEM_PRICE = new Money("12.34");

  private RestaurantService restaurantService;
  private RestaurantController restaurantController;

  @Before
  public void setUp() {
    restaurantService = mock(RestaurantService.class);
    restaurantController = new RestaurantController();
    ReflectionTestUtils.setField(restaurantController, "restaurantService", restaurantService);
  }

  @Test
  public void shouldReturnRestaurantWithMenuItems() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Restaurant restaurant = new Restaurant(RESTAURANT_ID, RESTAURANT_NAME,
            new RestaurantMenu(Collections.singletonList(
                    new MenuItem(MENU_ITEM_ID, MENU_ITEM_NAME, MENU_ITEM_PRICE))));
    ReflectionTestUtils.setField(restaurant, "address", address);

    when(restaurantService.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/{id}", RESTAURANT_ID).
    then().
            statusCode(200).
            body("id", equalTo((int) RESTAURANT_ID)).
            body("name", equalTo(RESTAURANT_NAME)).
            body("address.street1", equalTo("1 Main St")).
            body("menuItems[0].id", equalTo(MENU_ITEM_ID)).
            body("menuItems[0].name", equalTo(MENU_ITEM_NAME)).
            body("menuItems[0].price", equalTo(MENU_ITEM_PRICE.asString()));
  }

  @Test
  public void shouldReturn404WhenRestaurantMissing() {
    when(restaurantService.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/{id}", RESTAURANT_ID).
    then().
            statusCode(404);
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }
}
