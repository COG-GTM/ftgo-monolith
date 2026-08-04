package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestaurantServiceTest {

  private static final Address ADDRESS = new Address("1 High Street", null, "Oakland", "CA", "94619", 37.79, -122.19);

  private RestaurantRepository restaurantRepository;
  private RestaurantService restaurantService;

  @Before
  public void setUp() {
    restaurantRepository = mock(RestaurantRepository.class);
    restaurantService = new RestaurantService(restaurantRepository);
  }

  @Test
  public void shouldCreateRestaurantWithItsMenu() {
    CreateRestaurantRequest request = new CreateRestaurantRequest("My Restaurant", ADDRESS,
            new RestaurantMenuDTO(Arrays.asList(
                    new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")),
                    new MenuItemDTO("2", "Naan", new Money("2.50")))));

    Restaurant restaurant = restaurantService.create(request);

    verify(restaurantRepository).save(any(Restaurant.class));

    assertEquals("My Restaurant", restaurant.getName());
    assertEquals("Oakland", restaurant.getAddress().getCity());
    assertEquals(Double.valueOf(37.79), restaurant.getAddress().getLatitude());

    MenuItem chickenVindaloo = restaurant.findMenuItem("1").get();
    assertEquals("Chicken Vindaloo", chickenVindaloo.getName());
    assertEquals(new Money("12.34"), chickenVindaloo.getPrice());

    assertEquals("Naan", restaurant.findMenuItem("2").get().getName());
    assertFalse(restaurant.findMenuItem("99").isPresent());
  }

  @Test
  public void shouldFindRestaurantById() {
    Restaurant restaurant = new Restaurant("My Restaurant", ADDRESS,
            new RestaurantMenu(Arrays.asList(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")))));
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    assertSame(restaurant, restaurantService.findById(1L).get());
  }

  @Test
  public void shouldNotFindUnknownRestaurant() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

    assertFalse(restaurantService.findById(1L).isPresent());
  }
}
