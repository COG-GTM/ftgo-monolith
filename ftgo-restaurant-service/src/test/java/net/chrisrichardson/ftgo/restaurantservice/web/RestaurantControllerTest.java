package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RestaurantControllerTest {

  @Mock
  private RestaurantService restaurantService;

  @InjectMocks
  private RestaurantController controller;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateRestaurant() {
    Restaurant restaurant = new Restaurant("Italian",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Pizza", new Money("12.00")))));
    restaurant.setId(1L);
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant);

    RestaurantMenuDTO menuDTO = new RestaurantMenuDTO(Arrays.asList(
            new MenuItemDTO("m1", "Pizza", new Money("12.00"))
    ));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Italian", new Address("1", null, "C", "S", "Z"), menuDTO);

    CreateRestaurantResponse response = controller.create(request);
    assertThat(response.getId()).isEqualTo(1L);
  }

  @Test
  public void shouldGetRestaurantReturns200() {
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Item", new Money("10.00")))));
    restaurant.setId(1L);
    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant));

    ResponseEntity<GetRestaurantResponse> response = controller.get(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getName()).isEqualTo("TestR");
  }

  @Test
  public void shouldGetRestaurantReturns404WhenNotFound() {
    when(restaurantService.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetRestaurantResponse> response = controller.get(999L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
