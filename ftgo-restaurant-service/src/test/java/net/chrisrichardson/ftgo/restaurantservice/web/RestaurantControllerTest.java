package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    restaurantService = mock(RestaurantService.class);
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", restaurantService);

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter).build();
  }

  @Test
  public void shouldCreateRestaurant() throws Exception {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94607");
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    Restaurant restaurant = new Restaurant("Ajanta", address, new RestaurantMenu(Collections.singletonList(item)));
    ReflectionTestUtils.setField(restaurant, "id", 1L);

    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant);

    net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO menuItemDTO =
            new net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO("1", "Chicken", new Money("10.00"));
    net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO menuDTO =
            new net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO(Collections.singletonList(menuItemDTO));
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menuDTO);

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  public void shouldGetRestaurant() throws Exception {
    Restaurant restaurant = new Restaurant(1L, "Ajanta", new RestaurantMenu(Collections.emptyList()));
    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant));

    mockMvc.perform(get("/restaurants/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ajanta"));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() throws Exception {
    when(restaurantService.findById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/restaurants/99"))
            .andExpect(status().isNotFound());
  }
}
