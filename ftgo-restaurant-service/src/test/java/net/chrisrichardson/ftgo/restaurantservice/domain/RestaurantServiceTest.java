package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RestaurantServiceTest {

  @Mock
  private RestaurantRepository restaurantRepository;

  @InjectMocks
  private RestaurantService restaurantService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateRestaurant() {
    RestaurantMenuDTO menuDTO = new RestaurantMenuDTO(Arrays.asList(
            new MenuItemDTO("m1", "Pizza", new Money("12.00"))
    ));
    Address address = new Address("100 Broadway", null, "NYC", "NY", "10001");
    CreateRestaurantRequest request = new CreateRestaurantRequest("Italian Place", address, menuDTO);

    when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> {
      Restaurant r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });

    Restaurant result = restaurantService.create(request);
    assertThat(result.getName()).isEqualTo("Italian Place");
    verify(restaurantRepository).save(any(Restaurant.class));
  }

  @Test
  public void shouldFindById() {
    Restaurant restaurant = new Restaurant("Test",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Item", new Money("10.00")))));
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    Optional<Restaurant> result = restaurantService.findById(1L);
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Test");
  }

  @Test
  public void shouldReturnEmptyWhenNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Restaurant> result = restaurantService.findById(999L);
    assertThat(result).isEmpty();
  }
}
