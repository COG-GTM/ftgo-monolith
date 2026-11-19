package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestaurantControllerTest {

  private RestaurantRepository repository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    repository = mock(RestaurantRepository.class);
    RestaurantService service = new RestaurantService();
    ReflectionTestUtils.setField(service, "restaurantRepository", repository);
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", service);
    ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule());
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
            .build();
  }

  @Test
  public void shouldReturnNumericIdForPopulatedEmptyAndPartialMenus() throws Exception {
    when(repository.save(any(Restaurant.class))).thenAnswer(invocation -> {
      Restaurant restaurant = invocation.getArgument(0);
      restaurant.setId(42L);
      return restaurant;
    });

    for (String items : Arrays.asList("[{\"id\":\"tea\",\"name\":\"Tea\",\"price\":\"3.50\"}]", "[]", "[{}, {\"id\":\"legacy\"}]")) {
      mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                      .content("{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":" + items + "}}"))
              .andExpect(status().isOk())
              .andExpect(content().json("{\"id\":42}", true));
    }
  }

  @Test
  public void shouldRejectNullMenusListsAndElementsBeforePersistence() throws Exception {
    List<String> brokenRequests = Arrays.asList(
            "{\"name\":\"Cafe\"}",
            "{\"name\":\"Cafe\",\"menu\":null}",
            "{\"name\":\"Cafe\",\"menu\":{}}",
            "{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":null}}",
            "{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":[{}, null]}}");

    for (String request : brokenRequests) {
      try {
        mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(request));
        fail("Expected the IllegalArgumentException mapped to 400 by the app advice: " + request);
      } catch (NestedServletException ex) {
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
      }
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void shouldOmitMenuWithoutExplicitOptIn() throws Exception {
    Restaurant restaurant = mock(Restaurant.class);
    when(restaurant.getId()).thenReturn(42L);
    when(restaurant.getName()).thenReturn("Cafe");
    when(restaurant.streamMenuItems()).thenThrow(new AssertionError("Menu should remain unloaded"));
    when(repository.findById(42L)).thenReturn(Optional.of(restaurant));

    for (String query : Arrays.asList("", "?includeMenu=false", "?includeMenu=on")) {
      mockMvc.perform(get("/restaurants/42" + query))
              .andExpect(status().isOk())
              .andExpect(content().json("{\"id\":42,\"name\":\"Cafe\"}", true));
    }
  }

  @Test
  public void shouldReturnEmptyMenuOnOptIn() throws Exception {
    givenRestaurantWithMenu(Collections.emptyList());

    mockMvc.perform(get("/restaurants/42").param("includeMenu", "true"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"id\":42,\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":[]}}", true));
  }

  @Test
  public void shouldReturnStringPricesAndSkipNullLegacyEntries() throws Exception {
    givenRestaurantWithMenu(Arrays.asList(
            new MenuItem("tea", "Tea", new Money("3.50")),
            null,
            new MenuItem(null, "Legacy", null)));

    mockMvc.perform(get("/restaurants/42").param("includeMenu", "true"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"id\":42,\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":["
                    + "{\"id\":\"tea\",\"name\":\"Tea\",\"price\":\"3.50\"},"
                    + "{\"id\":null,\"name\":\"Legacy\",\"price\":null}]}}", true));
  }

  @Test
  public void shouldKeepEmptyNotFoundWithOrWithoutOptIn() throws Exception {
    when(repository.findById(42L)).thenReturn(Optional.empty());

    for (String query : Arrays.asList("", "?includeMenu=true")) {
      mockMvc.perform(get("/restaurants/42" + query))
              .andExpect(status().isNotFound())
              .andExpect(content().string(""));
    }
  }

  private void givenRestaurantWithMenu(List<MenuItem> items) {
    Restaurant restaurant = new Restaurant(42L, "Cafe", new RestaurantMenu(items));
    when(repository.findById(42L)).thenReturn(Optional.of(restaurant));
  }
}
