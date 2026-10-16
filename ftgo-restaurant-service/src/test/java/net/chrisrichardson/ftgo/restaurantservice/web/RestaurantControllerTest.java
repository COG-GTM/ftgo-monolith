package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class RestaurantControllerTest {

  private static final String AJANTA_JSON = "{\"name\":\"Ajanta\"," +
          "\"address\":{\"street1\":\"1 Main Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94611\"}," +
          "\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]}}";

  @Mock
  private RestaurantService restaurantService;

  @InjectMocks
  private RestaurantController restaurantController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper().registerModule(new MoneyModule());
    mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  private static Restaurant restaurant(Long id, String name) {
    return new Restaurant(id, name, new RestaurantMenu(Collections.emptyList()));
  }

  @Test
  public void shouldCreateRestaurant() throws Exception {
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant(99L, "Ajanta"));

    mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(AJANTA_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(99)));

    ArgumentCaptor<CreateRestaurantRequest> captor = ArgumentCaptor.forClass(CreateRestaurantRequest.class);
    verify(restaurantService).create(captor.capture());
    CreateRestaurantRequest request = captor.getValue();
    assertThat(request.getName()).isEqualTo("Ajanta");
    assertThat(request.getAddress().getCity()).isEqualTo("Oakland");
    assertThat(request.getAddress().getZip()).isEqualTo("94611");
    assertThat(request.getMenu().getMenuItemDTOs())
            .containsExactly(new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")));
  }

  @Test
  public void shouldRejectMalformedCreateRequestWithBadRequest() throws Exception {
    mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content("{not json"))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(restaurantService);
  }

  @Test
  public void shouldRejectNumericMenuItemPriceWithBadRequest() throws Exception {
    String numericPrice = AJANTA_JSON.replace("\"price\":\"12.34\"", "\"price\":12.34");

    mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(numericPrice))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(restaurantService);
  }

  @Test
  public void shouldRejectCreateRequestWithoutBody() throws Exception {
    mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(restaurantService);
  }

  @Test
  public void createWhenServiceReturnsRestaurantWithoutIdCurrentlyThrowsNullPointerException() {
    // Suspected bug: CreateRestaurantResponse takes a primitive long, so a null Restaurant id is auto-unboxed
    // and the request fails with an unhandled NullPointerException instead of a meaningful error response.
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant(null, "Ajanta"));

    assertThatThrownBy(() ->
            mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(AJANTA_JSON)))
            .isInstanceOf(NestedServletException.class)
            .hasRootCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldGetRestaurant() throws Exception {
    when(restaurantService.findById(1L)).thenReturn(Optional.of(restaurant(1L, "Ajanta")));

    mockMvc.perform(get("/restaurants/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.address").doesNotExist())
            .andExpect(jsonPath("$.menuItems").doesNotExist())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Ajanta")));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() throws Exception {
    when(restaurantService.findById(2L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/restaurants/2"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

    verify(restaurantService).findById(2L);
  }

  @Test
  public void shouldReturnBadRequestForNonNumericRestaurantId() throws Exception {
    mockMvc.perform(get("/restaurants/abc"))
            .andExpect(status().isBadRequest());

    verify(restaurantService, org.mockito.Mockito.never()).findById(anyLong());
  }

  @Test
  public void shouldRejectUnsupportedMethodOnRestaurant() throws Exception {
    mockMvc.perform(put("/restaurants/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isMethodNotAllowed());

    verifyZeroInteractions(restaurantService);
  }

  @Test
  public void createResponseJsonShouldRoundTripIntoCreateRestaurantResponse() throws Exception {
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant(123L, "Ajanta"));

    String body = mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(AJANTA_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    CreateRestaurantResponse response = objectMapper.readValue(body, CreateRestaurantResponse.class);
    assertThat(response.getId()).isEqualTo(123L);
  }

  @Test
  public void getResponseJsonShouldRoundTripIntoGetRestaurantResponse() throws Exception {
    when(restaurantService.findById(5L)).thenReturn(Optional.of(restaurant(5L, "Mumbai Express")));

    String body = mockMvc.perform(get("/restaurants/5"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    GetRestaurantResponse response = objectMapper.readValue(body, GetRestaurantResponse.class);
    assertThat(response.getId()).isEqualTo(5L);
    assertThat(response.getName()).isEqualTo("Mumbai Express");
  }

  @Test
  public void getShouldReturnNullNameWhenRestaurantHasNoName() throws Exception {
    when(restaurantService.findById(6L)).thenReturn(Optional.of(restaurant(6L, null)));

    String body = mockMvc.perform(get("/restaurants/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(6)))
            .andReturn().getResponse().getContentAsString();

    GetRestaurantResponse response = objectMapper.readValue(body, GetRestaurantResponse.class);
    assertThat(response.getId()).isEqualTo(6L);
    assertThat(response.getName()).isNull();
  }
}
