package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RestaurantControllerTest {

  private MockMvc mockMvc;
  private RestaurantService restaurantService;
  private RestaurantController restaurantController;

  @Before
  public void setUp() {
    restaurantService = mock(RestaurantService.class);
    restaurantController = new RestaurantController();
    ReflectionTestUtils.setField(restaurantController, "restaurantService", restaurantService);
    mockMvc = MockMvcBuilders.standaloneSetup(restaurantController).build();
  }

  @Test
  public void shouldCreateRestaurant() throws Exception {
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    Restaurant restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.singletonList(item)));
    ReflectionTestUtils.setField(restaurant, "id", 1L);

    when(restaurantService.create(any())).thenReturn(restaurant);

    mockMvc.perform(post("/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Ajanta\",\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"},\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":{\"amount\":12.34}}]}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  public void shouldGetRestaurant() throws Exception {
    Restaurant restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    ReflectionTestUtils.setField(restaurant, "id", 1L);

    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant));

    mockMvc.perform(get("/restaurants/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Ajanta"));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() throws Exception {
    when(restaurantService.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/restaurants/999"))
            .andExpect(status().isNotFound());
  }
}
