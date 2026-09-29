package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestaurantControllerTest {

  private static final long RESTAURANT_ID = 7L;
  private static final String RESTAURANT_NAME = "Ajanta";

  private RestaurantService restaurantService;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    restaurantService = mock(RestaurantService.class);
    RestaurantController restaurantController = new RestaurantController();
    ReflectionTestUtils.setField(restaurantController, "restaurantService", restaurantService);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  public void shouldCreateRestaurant() throws Exception {
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(makeRestaurant());

    mockMvc.perform(post("/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"name\": \"Ajanta\"," +
                    "\"address\": {\"street1\": \"1 Main Street\", \"city\": \"Oakland\", \"state\": \"CA\", \"zip\": \"94611\"}," +
                    "\"menu\": {\"menuItemDTOs\": [{\"id\": \"1\", \"name\": \"Chicken Vindaloo\", \"price\": \"12.34\"}]}" +
                    "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(equalTo((int) RESTAURANT_ID)));

    ArgumentCaptor<CreateRestaurantRequest> requestCaptor = ArgumentCaptor.forClass(CreateRestaurantRequest.class);
    verify(restaurantService).create(requestCaptor.capture());
    CreateRestaurantRequest request = requestCaptor.getValue();
    assertEquals(RESTAURANT_NAME, request.getName());
    assertEquals("Oakland", request.getAddress().getCity());
    assertEquals(Collections.singletonList(new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"))),
            request.getMenu().getMenuItemDTOs());
  }

  @Test
  public void shouldGetRestaurant() throws Exception {
    when(restaurantService.findById(RESTAURANT_ID)).thenReturn(Optional.of(makeRestaurant()));

    mockMvc.perform(get("/restaurants/" + RESTAURANT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(equalTo((int) RESTAURANT_ID)))
            .andExpect(jsonPath("$.name").value(RESTAURANT_NAME));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() throws Exception {
    when(restaurantService.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

    mockMvc.perform(get("/restaurants/" + RESTAURANT_ID))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  private Restaurant makeRestaurant() {
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(
            new MenuItem("1", "Chicken Vindaloo", new Money("12.34"))));
    return new Restaurant(RESTAURANT_ID, RESTAURANT_NAME, menu);
  }
}
