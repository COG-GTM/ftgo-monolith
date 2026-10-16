package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.AJANTA_ID;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.CHICKEN_VINDALOO_ID;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.CHICKEN_VINDALOO_PRICE;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.SAMOSA_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test (JUnit 5 + Mockito) of {@link RestaurantService}: the request DTO must be translated into
 * the {@link Restaurant} aggregate that is handed to the repository, and lookups must delegate to it.
 */
public class RestaurantServiceTest {

  private RestaurantRepository restaurantRepository;
  private RestaurantService restaurantService;

  @BeforeEach
  public void setUp() {
    restaurantRepository = mock(RestaurantRepository.class);

    // RestaurantService is field-injected, so the mock repository is wired in reflectively.
    restaurantService = new RestaurantService();
    ReflectionTestUtils.setField(restaurantService, "restaurantRepository", restaurantRepository);
  }

  @Test
  public void shouldCreateRestaurantFromRequest() {
    Restaurant created = restaurantService.create(RestaurantTestData.ajantaCreateRequest());

    // The very same aggregate that was saved is returned to the caller (the controller reads its id).
    ArgumentCaptor<Restaurant> saved = ArgumentCaptor.forClass(Restaurant.class);
    verify(restaurantRepository).save(saved.capture());
    assertSame(saved.getValue(), created);

    // Name, address and every menu item (including the Money price) are copied from the DTOs.
    assertEquals(RestaurantTestData.AJANTA_NAME, created.getName());
    assertEquals(RestaurantTestData.AJANTA_ADDRESS.getStreet1(), created.getAddress().getStreet1());
    Optional<MenuItem> vindaloo = created.findMenuItem(CHICKEN_VINDALOO_ID);
    assertTrue(vindaloo.isPresent());
    assertEquals(CHICKEN_VINDALOO_PRICE, vindaloo.get().getPrice());
    assertTrue(created.findMenuItem(SAMOSA_ID).isPresent());
    assertFalse(created.findMenuItem("no-such-item").isPresent());
  }

  @Test
  public void shouldFindRestaurantById() {
    Restaurant ajanta = RestaurantTestData.ajantaRestaurant();
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(ajanta));

    assertSame(ajanta, restaurantService.findById(AJANTA_ID).orElseThrow());
  }

  @Test
  public void shouldReturnEmptyForUnknownRestaurant() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.empty());

    assertFalse(restaurantService.findById(AJANTA_ID).isPresent());
  }
}
