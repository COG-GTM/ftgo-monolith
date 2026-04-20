package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantControllerTest {

  @Mock
  private RestaurantService restaurantService;

  @InjectMocks
  private RestaurantController restaurantController;

  @Test
  public void shouldCreateRestaurant() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Restaurant restaurant = new Restaurant("Ajanta", address,
            new RestaurantMenu(Collections.emptyList()));
    restaurant.setId(1L);
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant);

    given().
            standaloneSetup(configureControllers(restaurantController)).
            contentType("application/json").
            body("{\"name\": \"Ajanta\", \"address\": {\"street1\": \"1 Main St\", \"city\": \"Oakland\", \"state\": \"CA\", \"zip\": \"94612\"}, \"menu\": {\"menuItemDTOs\": [{\"id\": \"1\", \"name\": \"Chicken\", \"price\": \"12.34\"}]}}").
    when().
            post("/restaurants").
    then().
            statusCode(200).
            body("id", equalTo(1));
  }

  @Test
  public void shouldGetRestaurantById() {
    Restaurant restaurant = new Restaurant(1L, "Ajanta",
            new RestaurantMenu(Collections.emptyList()));
    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant));

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/1").
    then().
            statusCode(200).
            body("id", equalTo(1)).
            body("name", equalTo("Ajanta"));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() {
    when(restaurantService.findById(999L)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(restaurantController)).
    when().
            get("/restaurants/999").
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
