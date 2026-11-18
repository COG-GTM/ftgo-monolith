package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RestaurantControllerTest {

  private RestaurantRepository restaurantRepository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    restaurantRepository = mock(RestaurantRepository.class);
    RestaurantService restaurantService = new RestaurantService();
    ReflectionTestUtils.setField(restaurantService, "restaurantRepository", restaurantRepository);
    EntityManager entityManager = mock(EntityManager.class);
    TypedQuery<MenuItem> query = mock(TypedQuery.class);
    ReflectionTestUtils.setField(restaurantService, "entityManager", entityManager);
    when(entityManager.createQuery(any(String.class), eq(MenuItem.class))).thenReturn(query);
    when(query.setParameter("restaurantId", 1L)).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.singletonList(new MenuItem("1", "Curry", new Money("12.50"))));
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", restaurantService);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new MoneyModule());
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new BadRequestAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
  }

  @Test
  public void shouldFindRestaurantWithAddressAndMenuItems() throws Exception {
    Restaurant restaurant = new Restaurant("Test restaurant", new Address("1 Main", "", "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Curry", new Money("12.50")))));
    restaurant.setId(1L);
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    mockMvc.perform(get("/restaurants/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Test restaurant")))
            .andExpect(jsonPath("$.address.street1", is("1 Main")))
            .andExpect(jsonPath("$.address.city", is("Oakland")))
            .andExpect(jsonPath("$.menuItems[0].id", is("1")))
            .andExpect(jsonPath("$.menuItems[0].name", is("Curry")))
            .andExpect(jsonPath("$.menuItems[0].price", is("12.50")));
  }

  @Test
  public void shouldReturnNotFoundForUnknownRestaurant() throws Exception {
    when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());
    mockMvc.perform(get("/restaurants/99")).andExpect(status().isNotFound());
  }

  @Test
  public void shouldCreateRestaurantWithExistingPayloadShape() throws Exception {
    doAnswer(invocation -> {
      Restaurant restaurant = invocation.getArgument(0);
      restaurant.setId(1L);
      return restaurant;
    }).when(restaurantRepository).save(any(Restaurant.class));
    mockMvc.perform(post("/restaurants").contentType("application/json")
            .content(payload("Test restaurant", menu("\"12.50\""))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)));
    verify(restaurantRepository).save(any(Restaurant.class));
  }

  @Test
  public void shouldRejectBlankName() throws Exception {
    assertInvalid(payload("   ", menu("\"12.50\"")));
    assertInvalid(payload("", menu("\"12.50\"")));
    assertInvalid("{\"menu\":" + menu("\"12.50\"") + "}");
  }

  @Test
  public void shouldRejectMissingMenu() throws Exception {
    assertInvalid("{\"name\":\"Test restaurant\"}");
    assertInvalid(payload("Test restaurant", "null"));
  }

  @Test
  public void shouldRejectEmptyMenu() throws Exception {
    assertInvalid(payload("Test restaurant", "{\"menuItemDTOs\":[]}"));
    assertInvalid(payload("Test restaurant", "{}"));
  }

  @Test
  public void shouldRejectNullPrice() throws Exception {
    assertInvalid(payload("Test restaurant", menu("null")));
  }

  @Test
  public void shouldRejectZeroPrice() throws Exception {
    assertInvalid(payload("Test restaurant", menu("\"0\"")));
  }

  @Test
  public void shouldRejectNegativePrice() throws Exception {
    assertInvalid(payload("Test restaurant", menu("\"-0.01\"")));
  }

  private void assertInvalid(String payload) throws Exception {
    mockMvc.perform(post("/restaurants").contentType("application/json").content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
    verify(restaurantRepository, never()).save(any(Restaurant.class));
  }

  private String payload(String name, String menu) {
    return "{\"name\":\"" + name + "\",\"address\":{\"street1\":\"1 Main\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"},\"menu\":" + menu + "}";
  }

  private String menu(String price) {
    return "{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Curry\",\"price\":" + price + "}]}";
  }

  @ControllerAdvice
  public static class BadRequestAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgument() {
    }
  }
}
