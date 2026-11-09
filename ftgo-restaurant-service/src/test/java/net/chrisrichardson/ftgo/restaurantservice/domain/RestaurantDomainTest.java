package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestaurantDomainTest {

  private static final Address ADDRESS = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8, -122.27);
  private static final Money PRICE = new Money("12.34");

  private RestaurantRepository repository;
  private RestaurantService service;

  @Before
  public void setUp() {
    repository = mock(RestaurantRepository.class);
    service = new RestaurantService(repository);
  }

  private Restaurant ajanta() {
    Restaurant r = new Restaurant("Ajanta", ADDRESS, new RestaurantMenu(Arrays.asList(
            new MenuItem("1", "Chicken Vindaloo", PRICE),
            new MenuItem("2", "Naan", new Money("2.50")))));
    r.setId(7L);
    return r;
  }

  @Test
  public void shouldFindMenuItemById() {
    Restaurant r = ajanta();
    assertEquals("Naan", r.findMenuItem("2").get().getName());
    assertFalse(r.findMenuItem("3").isPresent());
  }

  @Test
  public void shouldCreateRestaurantFromRequest() {
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", ADDRESS,
            new RestaurantMenuDTO(Collections.singletonList(new MenuItemDTO("1", "Chicken Vindaloo", PRICE))));

    Restaurant created = service.create(request);

    verify(repository).save(any(Restaurant.class));
    assertEquals("Ajanta", created.getName());
    assertSame(ADDRESS, created.getAddress());
    assertEquals(PRICE, created.findMenuItem("1").get().getPrice());
  }

  @Test
  public void shouldCreateRestaurantWithEmptyMenuWhenMenuMissing() {
    Restaurant created = service.create(new CreateRestaurantRequest("Empty", ADDRESS, null));
    assertTrue(created.getMenuItems().isEmpty());
  }

  @Test
  public void shouldMapRestaurantToApiResponse() {
    GetRestaurantResponse response = RestaurantResponses.toGetRestaurantResponse(ajanta());

    assertEquals(Long.valueOf(7L), response.getId());
    assertEquals("Ajanta", response.getName());
    assertSame(ADDRESS, response.getAddress());
    assertEquals(2, response.getMenu().getMenuItemDTOs().size());
    assertEquals(PRICE, response.findMenuItem("1").get().getPrice());
  }

  @Test
  public void inProcessClientShouldServeContractFromLocalService() {
    when(repository.findById(7L)).thenReturn(Optional.of(ajanta()));
    when(repository.findById(8L)).thenReturn(Optional.empty());
    InProcessRestaurantServiceClient client = new InProcessRestaurantServiceClient(service);

    assertEquals("Ajanta", client.findRestaurantById(7L).get().getName());
    assertFalse(client.findRestaurantById(8L).isPresent());
  }
}
