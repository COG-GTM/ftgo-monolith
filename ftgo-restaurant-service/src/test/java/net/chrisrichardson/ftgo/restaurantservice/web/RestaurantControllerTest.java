package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.AJANTA_ID;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.AJANTA_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Standalone MockMvc test of {@link RestaurantController} (JUnit 5 + io.rest-assured 5 spring-mock-mvc,
 * replacing the JUnit 4 / com.jayway.restassured stack). The service layer is mocked, so this covers
 * only request mapping, JSON (de)serialization of the API DTOs and the HTTP status codes.
 */
public class RestaurantControllerTest {

  private RestaurantService restaurantService;

  // Jackson mapper with the FTGO Money module, mirroring what CommonConfiguration wires at runtime.
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MoneyModule());

  @BeforeEach
  public void setUp() {
    restaurantService = mock(RestaurantService.class);

    // The controller uses field injection, so the mock is set reflectively instead of via a constructor.
    RestaurantController controller = new RestaurantController();
    ReflectionTestUtils.setField(controller, "restaurantService", restaurantService);

    // Route every request through a MockMvc that (de)serializes with the Money-aware mapper.
    RestAssuredMockMvc.standaloneSetup(MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)));
  }

  // Standalone setup is static state in RestAssuredMockMvc; reset it so tests stay independent.
  @AfterEach
  public void tearDown() {
    RestAssuredMockMvc.reset();
  }

  @Test
  public void shouldCreateRestaurant() throws Exception {
    when(restaurantService.create(any(CreateRestaurantRequest.class))).thenReturn(RestaurantTestData.ajantaRestaurant());

    // POST the JSON form of the create request and expect the new restaurant's id back.
    given().
            contentType(ContentType.JSON).
            body(objectMapper.writeValueAsString(RestaurantTestData.ajantaCreateRequest())).
    when().
            post("/restaurants").
    then().
            statusCode(200).
            body("id", equalTo((int) AJANTA_ID));

    // The request body must have been deserialized faithfully (including the Money prices) before
    // reaching the service.
    ArgumentCaptor<CreateRestaurantRequest> captor = ArgumentCaptor.forClass(CreateRestaurantRequest.class);
    verify(restaurantService).create(captor.capture());
    CreateRestaurantRequest received = captor.getValue();
    assertEquals(AJANTA_NAME, received.getName());
    assertEquals(RestaurantTestData.AJANTA_ADDRESS.getZip(), received.getAddress().getZip());
    assertEquals(RestaurantTestData.ajantaCreateRequest().getMenu(), received.getMenu());
  }

  @Test
  public void shouldGetRestaurant() {
    when(restaurantService.findById(AJANTA_ID)).thenReturn(Optional.of(RestaurantTestData.ajantaRestaurant()));

    given().
    when().
            get("/restaurants/" + AJANTA_ID).
    then().
            statusCode(200).
            body("id", equalTo((int) AJANTA_ID)).
            body("name", equalTo(AJANTA_NAME));
  }

  @Test
  public void shouldReturn404WhenRestaurantNotFound() {
    when(restaurantService.findById(AJANTA_ID)).thenReturn(Optional.<Restaurant>empty());

    given().
    when().
            get("/restaurants/" + AJANTA_ID).
    then().
            statusCode(404);
  }
}
