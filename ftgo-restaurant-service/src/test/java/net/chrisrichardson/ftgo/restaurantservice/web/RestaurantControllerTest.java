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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestaurantControllerTest {
  private static final String ITEM = "{\"id\":\"soup\",\"name\":\"Soup\",\"price\":\"4.25\"}";
  private RestaurantRepository repository;
  private EntityManager entityManager;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(RestaurantRepository.class);
    entityManager = mock(EntityManager.class);
    RestaurantService service = new RestaurantService();
    ReflectionTestUtils.setField(service, "restaurantRepository", repository);
    ReflectionTestUtils.setField(service, "entityManager", entityManager);
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", service);
    mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(
            new MappingJackson2HttpMessageConverter(new ObjectMapper().registerModule(new MoneyModule()))).build();
  }

  @Test
  public void createsRestaurantWithUnchangedResponse() throws Exception {
    stubRestaurantSave();

    mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(createWithItem(ITEM)))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"id\":17}", true));
  }

  @Test
  public void createsRestaurantWithEmptyMenu() throws Exception {
    stubRestaurantSave();

    mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":[]}}"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"id\":17}", true));
  }

  @Test
  public void readsAddressAndMenuWithStringPrice() throws Exception {
    stubRestaurantRead(Collections.singletonList(new MenuItem("soup", "Soup", new Money("4.25"))));

    mvc.perform(get("/restaurants/17"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(17))
            .andExpect(jsonPath("$.name").value("Cafe"))
            .andExpect(jsonPath("$.address.street1").value("1 High Street"))
            .andExpect(jsonPath("$.address.city").value("Oakland"))
            .andExpect(jsonPath("$.menuItems[0].id").value("soup"))
            .andExpect(jsonPath("$.menuItems[0].name").value("Soup"))
            .andExpect(jsonPath("$.menuItems[0].price").isString())
            .andExpect(jsonPath("$.menuItems[0].price").value("4.25"));
  }

  @Test
  public void readsEmptyMenuAsAnArray() throws Exception {
    stubRestaurantRead(Collections.emptyList());

    mvc.perform(get("/restaurants/17"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.menuItems").isArray())
            .andExpect(jsonPath("$.menuItems").isEmpty());
  }

  @Test
  public void readsLegacyMenuWithoutFailingOnAnAllNullItem() throws Exception {
    stubRestaurantRead(Arrays.asList(null, new MenuItem("legacy", "No price", null)));

    mvc.perform(get("/restaurants/17"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"menuItems\":[{\"id\":\"legacy\",\"name\":\"No price\",\"price\":null}]}"));
  }

  @Test
  public void returnsNotFoundForMissingRestaurant() throws Exception {
    when(repository.findById(999999L)).thenReturn(Optional.empty());

    mvc.perform(get("/restaurants/999999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  @Test
  public void rejectsMissingOrBlankRestaurantName() throws Exception {
    assertInvalidCreate("{}");
    assertInvalidCreate("{\"menu\":{\"menuItemDTOs\":[" + ITEM + "]}}");
    assertInvalidCreate("{\"name\":\"  \",\"menu\":{\"menuItemDTOs\":[]}}");
  }

  @Test
  public void rejectsMissingMenuOrItemList() throws Exception {
    assertInvalidCreate("{\"name\":\"Cafe\"}");
    assertInvalidCreate("{\"name\":\"Cafe\",\"menu\":{}}");
    assertInvalidCreate("{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":null}}");
  }

  @Test
  public void rejectsIncompleteOrNullMenuItems() throws Exception {
    assertInvalidCreate(createWithItem("{\"name\":\"Soup\",\"price\":\"4.25\"}"));
    assertInvalidCreate(createWithItem("{\"id\":\"soup\",\"price\":\"4.25\"}"));
    assertInvalidCreate(createWithItem("{\"id\":\"soup\",\"name\":\"Soup\"}"));
    assertInvalidCreate(createWithItem("null"));
  }

  @Test
  public void rejectsNegativePriceButAcceptsZero() throws Exception {
    assertInvalidCreate(createWithItem(ITEM.replace("4.25", "-0.01")));
    stubRestaurantSave();

    mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                    .content(createWithItem(ITEM.replace("4.25", "0.00"))))
            .andExpect(status().isOk());
  }

  private void stubRestaurantSave() {
    when(repository.save(any(Restaurant.class))).thenAnswer(invocation -> {
      Restaurant restaurant = invocation.getArgument(0);
      restaurant.setId(17L);
      return restaurant;
    });
  }

  private void stubRestaurantRead(List<MenuItem> menuItems) {
    Restaurant restaurant = new Restaurant("Cafe", new Address("1 High Street", null, "Oakland", "CA", "94619"),
            new RestaurantMenu(menuItems));
    restaurant.setId(17L);
    when(repository.findById(17L)).thenReturn(Optional.of(restaurant));
    @SuppressWarnings("unchecked")
    TypedQuery<MenuItem> query = mock(TypedQuery.class);
    when(entityManager.createQuery(anyString(), eq(MenuItem.class))).thenReturn(query);
    when(query.setParameter("restaurantId", 17L)).thenReturn(query);
    when(query.getResultList()).thenReturn(menuItems);
  }

  private String createWithItem(String item) {
    return "{\"name\":\"Cafe\",\"menu\":{\"menuItemDTOs\":[" + item + "]}}";
  }

  private void assertInvalidCreate(String json) throws Exception {
    try {
      mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(json));
      fail("Expected an IllegalArgumentException for " + json);
    } catch (NestedServletException ex) {
      assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }
    verifyZeroInteractions(repository);
  }
}
