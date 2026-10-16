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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantServiceTest {

  private static final Address AJANTA_ADDRESS = new Address("1 Main Street", "Unit 99", "Oakland", "CA", "94611");

  @Mock
  private RestaurantRepository restaurantRepository;

  @InjectMocks
  private RestaurantService restaurantService;

  private CreateRestaurantRequest ajantaRequest() {
    return new CreateRestaurantRequest("Ajanta", AJANTA_ADDRESS, new RestaurantMenuDTO(Arrays.asList(
            new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")),
            new MenuItemDTO("2", "Lamb Korma", new Money("15.50")))));
  }

  @Test
  public void shouldSaveRestaurantWithNameAddressAndMappedMenuItems() {
    Restaurant result = restaurantService.create(ajantaRequest());

    ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
    verify(restaurantRepository).save(captor.capture());
    Restaurant saved = captor.getValue();

    assertThat(saved).isSameAs(result);
    assertThat(saved.getName()).isEqualTo("Ajanta");
    assertThat(saved.getAddress()).isSameAs(AJANTA_ADDRESS);
    assertThat(saved.getId()).isNull();
    assertThat(saved.findMenuItem("1")).contains(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")));
    assertThat(saved.findMenuItem("2")).contains(new MenuItem("2", "Lamb Korma", new Money("15.50")));
    assertThat(saved.findMenuItem("3")).isEmpty();
  }

  @Test
  public void shouldCreateRestaurantWithNoMenuItemsWhenMenuIsEmpty() {
    CreateRestaurantRequest request = new CreateRestaurantRequest("Empty Kitchen", AJANTA_ADDRESS,
            new RestaurantMenuDTO(Collections.emptyList()));

    Restaurant result = restaurantService.create(request);

    verify(restaurantRepository).save(result);
    assertThat(result.getName()).isEqualTo("Empty Kitchen");
    assertThat(result.findMenuItem("1")).isEmpty();
  }

  @Test
  public void shouldFailWithoutSavingWhenMenuIsMissing() {
    CreateRestaurantRequest request = new CreateRestaurantRequest("No Menu", AJANTA_ADDRESS, null);

    assertThatThrownBy(() -> restaurantService.create(request)).isInstanceOf(NullPointerException.class);
    verifyZeroInteractions(restaurantRepository);
  }

  @Test
  public void shouldFailWithoutSavingWhenMenuItemListIsMissing() {
    CreateRestaurantRequest request = new CreateRestaurantRequest("No Items", AJANTA_ADDRESS, new RestaurantMenuDTO(null));

    assertThatThrownBy(() -> restaurantService.create(request)).isInstanceOf(NullPointerException.class);
    verify(restaurantRepository, never()).save(any());
  }

  @Test
  public void shouldPropagateRepositoryFailureOnCreate() {
    when(restaurantRepository.save(any(Restaurant.class))).thenThrow(new IllegalStateException("db down"));

    assertThatThrownBy(() -> restaurantService.create(ajantaRequest()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("db down");
  }

  @Test
  public void createWhenRepositoryReturnsDifferentInstanceCurrentlyReturnsUnsavedInstanceWithNullId() {
    // Suspected bug: create() ignores the instance returned by CrudRepository.save(), whose contract says
    // the returned instance should be used for further operations (it may differ, e.g. after a merge).
    Restaurant persisted = new Restaurant(42L, "Ajanta", new RestaurantMenu(Collections.emptyList()));
    when(restaurantRepository.save(any(Restaurant.class))).thenReturn(persisted);

    Restaurant result = restaurantService.create(ajantaRequest());

    assertThat(result).isNotSameAs(persisted);
    assertThat(result.getId()).isNull();
  }

  @Test
  public void shouldFindRestaurantById() {
    Restaurant restaurant = new Restaurant(7L, "Ajanta", new RestaurantMenu(Collections.emptyList()));
    when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));

    assertThat(restaurantService.findById(7L)).containsSame(restaurant);
  }

  @Test
  public void shouldReturnEmptyWhenRestaurantNotFound() {
    when(restaurantRepository.findById(8L)).thenReturn(Optional.empty());

    assertThat(restaurantService.findById(8L)).isEmpty();
    verify(restaurantRepository).findById(8L);
  }
}
