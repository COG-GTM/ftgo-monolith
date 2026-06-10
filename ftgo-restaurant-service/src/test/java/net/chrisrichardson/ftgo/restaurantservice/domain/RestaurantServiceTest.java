package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

  @Mock
  private RestaurantRepository restaurantRepository;

  @InjectMocks
  private RestaurantService restaurantService;

  @Test
  void shouldCreateRestaurant() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    MenuItemDTO menuItem = new MenuItemDTO("1", "Chicken", new Money("10.00"));
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(menuItem));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menu);

    Restaurant result = restaurantService.create(request);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Ajanta");
    verify(restaurantRepository).save(any(Restaurant.class));
  }

  @Test
  void shouldFindRestaurantById() {
    Restaurant restaurant = new Restaurant(1L, "Ajanta",
            new net.chrisrichardson.ftgo.domain.RestaurantMenu(Collections.emptyList()));
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    Optional<Restaurant> result = restaurantService.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Ajanta");
  }

  @Test
  void shouldReturnEmptyWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Restaurant> result = restaurantService.findById(999L);

    assertThat(result).isEmpty();
  }
}
