package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantServiceTest {

  @InjectMocks
  private RestaurantService restaurantService;

  @Mock
  private RestaurantRepository restaurantRepository;

  @Test
  public void shouldCreateRestaurant() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    MenuItemDTO menuItem = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(menuItem));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menu);

    Restaurant result = restaurantService.create(request);

    assertNotNull(result);
    assertEquals("Ajanta", result.getName());
    assertEquals(address, result.getAddress());
    verify(restaurantRepository).save(any(Restaurant.class));
  }

  @Test
  public void shouldCreateRestaurantWithCorrectMenuItems() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    MenuItemDTO menuItem = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(menuItem));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menu);

    Restaurant result = restaurantService.create(request);

    assertTrue(result.findMenuItem("1").isPresent());
    assertEquals("Chicken Vindaloo", result.findMenuItem("1").get().getName());
    assertEquals(new Money("12.34"), result.findMenuItem("1").get().getPrice());
  }

  @Test
  public void shouldFindRestaurantById() {
    Restaurant restaurant = new Restaurant(1L, "Ajanta",
            new net.chrisrichardson.ftgo.domain.RestaurantMenu(Collections.emptyList()));
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    Optional<Restaurant> result = restaurantService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals("Ajanta", result.get().getName());
    verify(restaurantRepository).findById(1L);
  }

  @Test
  public void shouldReturnEmptyWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Restaurant> result = restaurantService.findById(999L);

    assertFalse(result.isPresent());
    verify(restaurantRepository).findById(999L);
  }
}
