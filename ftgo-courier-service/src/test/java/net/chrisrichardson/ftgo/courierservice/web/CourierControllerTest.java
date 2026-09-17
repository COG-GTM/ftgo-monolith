package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit 5 (Jupiter) MockMvc tests for {@link CourierController}.
 *
 * Uses io.rest-assured 5.x {@code spring-mock-mvc} in standalone mode, which drives the controller
 * through the Jakarta Servlet based Spring MVC 6 stack without starting a server or a database.
 * {@link CourierService} is a Mockito mock so only the web layer (routing, JSON binding, status codes)
 * is under test.
 */
public class CourierControllerTest {

  private static final long COURIER_ID = 7L;

  private CourierService courierService;
  private CourierController courierController;
  private Courier courier;

  // Build a courier with a persistent-looking id; the id is normally assigned by JPA on insert.
  @BeforeEach
  public void setUp() {
    courierService = mock(CourierService.class);
    courierController = new CourierController(courierService);
    courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94611", 37.8044, -122.2712));
    ReflectionTestUtils.setField(courier, "id", COURIER_ID);
  }

  // POST /couriers delegates to the service and echoes the new courier's id.
  @Test
  public void shouldCreateCourier() {
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            body(new CreateCourierRequest(courier.getName(), courier.getAddress())).
    when().
            post("/couriers").
    then().
            statusCode(200).
            body("id", equalTo((int) COURIER_ID));
  }

  // GET /couriers/{id} returns the courier entity serialized as JSON.
  @Test
  public void shouldGetCourier() {
    when(courierService.findCourierById(COURIER_ID)).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
    when().
            get("/couriers/" + COURIER_ID).
    then().
            statusCode(200).
            body("id", equalTo((int) COURIER_ID)).
            body("name.firstName", equalTo("Jane")).
            body("address.city", equalTo("Oakland"));
  }

  // POST /couriers/{id}/availability binds the JSON body and forwards the flag to the service.
  @Test
  public void shouldUpdateAvailability() {
    given().
            standaloneSetup(configureControllers(courierController)).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            body(new CourierAvailability(true)).
    when().
            post("/couriers/" + COURIER_ID + "/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(COURIER_ID, true);
  }

  // POST /couriers/{id}/location binds the coordinates and forwards them to the service.
  @Test
  public void shouldUpdateLocation() {
    given().
            standaloneSetup(configureControllers(courierController)).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            body(new CourierLocationUpdate(37.7749, -122.4194)).
    when().
            post("/couriers/" + COURIER_ID + "/location").
    then().
            statusCode(200);

    verify(courierService).updateLocation(COURIER_ID, 37.7749, -122.4194);
  }

  // GET /couriers/{id}/workload projects the courier into a CourierWorkloadResponse.
  @Test
  public void shouldGetWorkload() {
    courier.noteAvailable();
    when(courierService.findCourierById(COURIER_ID)).thenReturn(courier);

    given().
            standaloneSetup(configureControllers(courierController)).
    when().
            get("/couriers/" + COURIER_ID + "/workload").
    then().
            statusCode(200).
            body("courierId", equalTo((int) COURIER_ID)).
            body("activeDeliveries", equalTo(0)).
            body("available", equalTo(true)).
            body("currentLatitude", equalTo(37.8044f)).
            body("currentLongitude", equalTo(-122.2712f));
  }

  // Mirror the application's Jackson setup (Money + java.time modules, ISO-8601 dates) so the
  // standalone MockMvc serializes exactly like the running service.
  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }
}
