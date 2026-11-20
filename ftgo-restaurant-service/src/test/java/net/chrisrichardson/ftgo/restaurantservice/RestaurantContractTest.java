package net.chrisrichardson.ftgo.restaurantservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.web.RestaurantController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RestaurantContractTest {
  private final ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule());
  private RestaurantService service;
  private RestaurantRepository repository;
  private EntityManager entityManager;
  private TypedQuery<Object[]> query;
  private MockMvc mvc;

  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    service = new RestaurantService();
    repository = mock(RestaurantRepository.class);
    entityManager = mock(EntityManager.class);
    query = mock(TypedQuery.class);
    ReflectionTestUtils.setField(service, "restaurantRepository", repository);
    ReflectionTestUtils.setField(service, "entityManager", entityManager);
    when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
    when(query.setParameter(eq("id"), any(Object.class))).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.emptyList());
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", service);
    mvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
  }

  private String request(String name, String items) {
    return "{\"name\":" + name + ",\"menu\":{\"menuItemDTOs\":" + items + "}}";
  }

  private void invalid(String json, String message) throws Exception {
    try {
      service.create(mapper.readValue(json, CreateRestaurantRequest.class));
      fail("Expected validation failure");
    } catch (IllegalArgumentException ex) {
      assertEquals(message, ex.getMessage());
    }
    // GlobalExceptionHandler belongs to ftgo-application, outside this module's classpath.
    try {
      mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(json));
      fail("Expected validation failure");
    } catch (NestedServletException ex) {
      assertTrue(ex.getCause() instanceof IllegalArgumentException);
      assertEquals(message, ex.getCause().getMessage());
    }
    verify(repository, never()).save(any(Restaurant.class));
  }

  @Test
  public void rejectsMissingAndEmptyMenus() throws Exception {
    for (String json : Arrays.asList("{\"name\":\"R\"}", "{\"name\":\"R\",\"menu\":null}",
            "{\"name\":\"R\",\"menu\":{}}", request("\"R\"", "[]"))) {
      invalid(json, "Restaurant menu must contain at least one item");
    }
  }

  @Test
  public void rejectsBlankRestaurantName() throws Exception {
    for (String name : Arrays.asList("null", "\"\"", "\"  \"")) {
      invalid(request(name, "[]"), "Restaurant name must not be blank");
    }
  }

  @Test
  public void rejectsInvalidItems() throws Exception {
    invalid(request("\"R\"", "[null]"), "Menu item must not be null");
    for (String id : Arrays.asList("", "\"id\":null,", "\"id\":\" \","))
      invalid(request("\"R\"", "[{" + id + "\"name\":\"Item\",\"price\":\"1\"}]"), "Menu item id must not be blank");
    for (String name : Arrays.asList("", "\"name\":null,", "\"name\":\" \","))
      invalid(request("\"R\"", "[{\"id\":\"1\"," + name + "\"price\":\"1\"}]"), "Menu item name must not be blank");
    invalid(request("\"R\"", "[{\"id\":\"1\",\"name\":\"Item\"}]"), "Menu item price is required");
    invalid(request("\"R\"", "[{\"id\":\"1\",\"name\":\"Item\",\"price\":null}]"), "Menu item price is required");
    invalid(request("\"R\"", "[{\"id\":\"1\",\"name\":\"Item\",\"price\":\"-0.01\"}]"), "Menu item price must not be negative");
    String item = "{\"id\":\"1\",\"name\":\"Item\",\"price\":\"1\"}";
    invalid(request("\"R\"", "[" + item + "," + item + "]"), "Menu item ids must be unique");
  }

  @Test
  public void validCreateReturns200AndId() throws Exception {
    when(repository.save(any(Restaurant.class))).thenAnswer(invocation -> {
      Restaurant restaurant = invocation.getArgument(0);
      restaurant.setId(42L);
      return restaurant;
    });
    String json = request("\"R\"", "[{\"id\":\"1\",\"name\":\"Item\",\"price\":\"0\"}]");
    assertEquals(Long.valueOf(42), service.create(mapper.readValue(json, CreateRestaurantRequest.class)).getId());
    mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk()).andExpect(content().json("{\"id\":42}", true));
  }

  @Test
  public void getReturnsAddressAndStringPricesIncludingLegacyNullId() throws Exception {
    Restaurant restaurant = new Restaurant("R", new Address("Street", null, "City", "State", "Zip"),
            new RestaurantMenu(Collections.emptyList()));
    restaurant.setId(42L);
    when(repository.findById(42L)).thenReturn(Optional.of(restaurant));
    when(query.getResultList()).thenReturn(Arrays.asList(
            new Object[]{"1", "Item", new BigDecimal("12.34")},
            new Object[]{null, null, null}, new Object[]{null, "Legacy", null}));
    List<MenuItemDTO> items = service.findMenuItems(42L);
    assertEquals(2, items.size());
    assertNull(items.get(1).getId());
    assertEquals("12.34", items.get(0).getPrice().asString());
    mvc.perform(get("/restaurants/42")).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42)).andExpect(jsonPath("$.name").value("R"))
            .andExpect(jsonPath("$.address.street1").value("Street"))
            .andExpect(jsonPath("$.menuItems.length()").value(2))
            .andExpect(jsonPath("$.menuItems[0].id").value("1"))
            .andExpect(jsonPath("$.menuItems[0].name").value("Item"))
            .andExpect(jsonPath("$.menuItems[0].price").value("12.34"));
    verify(entityManager, times(2)).createQuery(
            "select mi.id, mi.name, mi.price.amount from Restaurant r join r.menuItems mi where r.id = :id", Object[].class);
  }

  @Test
  public void getEmptyMenuAndUnknownRestaurant() throws Exception {
    Restaurant restaurant = new Restaurant(42L, "R", new RestaurantMenu(Collections.emptyList()));
    when(repository.findById(42L)).thenReturn(Optional.of(restaurant));
    assertTrue(service.findMenuItems(42L).isEmpty());
    mvc.perform(get("/restaurants/42")).andExpect(status().isOk()).andExpect(jsonPath("$.menuItems").isEmpty());
    when(repository.findById(99L)).thenReturn(Optional.empty());
    assertFalse(service.findById(99L).isPresent());
    mvc.perform(get("/restaurants/99")).andExpect(status().isNotFound());
    verify(query, never()).setParameter("id", 99L);
  }
}
