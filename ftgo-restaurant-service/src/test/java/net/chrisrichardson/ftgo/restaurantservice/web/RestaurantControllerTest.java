package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

  @Mock
  private RestaurantService restaurantService;

  @Mock
  private Restaurant mockRestaurant;

  @InjectMocks
  private RestaurantController controller;

  @Test
  void shouldCreateRestaurant() {
    when(mockRestaurant.getId()).thenReturn(1L);
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(mockRestaurant);

    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    MenuItemDTO menuItem = new MenuItemDTO("1", "Chicken", new Money("10.00"));
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(menuItem));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menu);

    CreateRestaurantResponse response = controller.create(request);

    assertThat(response.getId()).isEqualTo(1L);
  }

  @Test
  void shouldGetRestaurant() {
    when(mockRestaurant.getId()).thenReturn(1L);
    when(mockRestaurant.getName()).thenReturn("Ajanta");
    when(restaurantService.findById(1L)).thenReturn(Optional.of(mockRestaurant));

    ResponseEntity<GetRestaurantResponse> response = controller.get(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("Ajanta");
    assertThat(response.getBody().getId()).isEqualTo(1L);
  }

  @Test
  void shouldReturn404WhenRestaurantNotFound() {
    when(restaurantService.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetRestaurantResponse> response = controller.get(999L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
