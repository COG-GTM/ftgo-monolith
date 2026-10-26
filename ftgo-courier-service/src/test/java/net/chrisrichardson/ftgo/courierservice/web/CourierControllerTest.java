package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CourierRegistration;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierControllerTest {

  private static final long COURIER_ID = 1L;
  private static final String COURIER_TOKEN = "courier-1-token";
  private static final String OTHER_COURIER_TOKEN = "courier-2-token";

  private CourierService courierService;
  private Courier courier;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    courier = new Courier(new PersonName("John", "Doe"), new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    when(courierService.findCourierById(COURIER_ID)).thenReturn(courier);
    when(courierService.authenticate(anyLong(), anyString())).thenReturn(false);
    when(courierService.authenticate(COURIER_ID, COURIER_TOKEN)).thenReturn(true);
  }

  @Test
  public void shouldRejectAnonymousAvailabilityUpdate() {
    given().
            standaloneSetup(configure()).
            contentType("application/json").
            body(new CourierAvailability(true)).
    when().
            post("/couriers/1/availability").
    then().
            statusCode(401).
            header("WWW-Authenticate", equalTo("Bearer"));

    verify(courierService, never()).updateAvailability(anyLong(), anyBoolean());
  }

  @Test
  public void shouldRejectAvailabilityUpdateWithAnotherCouriersToken() {
    given().
            standaloneSetup(configure()).
            header("Authorization", "Bearer " + OTHER_COURIER_TOKEN).
            contentType("application/json").
            body(new CourierAvailability(true)).
    when().
            post("/couriers/1/availability").
    then().
            statusCode(401);

    verify(courierService, never()).updateAvailability(anyLong(), anyBoolean());
  }

  @Test
  public void shouldUpdateAvailabilityWithOwnToken() {
    given().
            standaloneSetup(configure()).
            header("Authorization", "Bearer " + COURIER_TOKEN).
            contentType("application/json").
            body(new CourierAvailability(true)).
    when().
            post("/couriers/1/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(COURIER_ID, true);
  }

  @Test
  public void shouldRejectAnonymousLocationUpdate() {
    given().
            standaloneSetup(configure()).
            contentType("application/json").
            body(new CourierLocationUpdate(37.8, -122.2)).
    when().
            post("/couriers/1/location").
    then().
            statusCode(401);

    verify(courierService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
  }

  @Test
  public void shouldRejectAnonymousGet() {
    given().
            standaloneSetup(configure()).
    when().
            get("/couriers/1").
    then().
            statusCode(401);

    verify(courierService, never()).findCourierById(anyLong());
  }

  @Test
  public void shouldGetWithOwnToken() {
    given().
            standaloneSetup(configure()).
            header("Authorization", "Bearer " + COURIER_TOKEN).
    when().
            get("/couriers/1").
    then().
            statusCode(200).
            body("name.firstName", equalTo("John"));
  }

  @Test
  public void shouldRejectAnonymousWorkload() {
    given().
            standaloneSetup(configure()).
    when().
            get("/couriers/1/workload").
    then().
            statusCode(401);

    verify(courierService, never()).findCourierById(anyLong());
  }

  @Test
  public void shouldReturnAccessTokenOnCreate() {
    CreateCourierRequest request = new CreateCourierRequest(new PersonName("John", "Doe"), new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"));
    Courier created = mock(Courier.class);
    when(created.getId()).thenReturn(COURIER_ID);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(new CourierRegistration(created, COURIER_TOKEN));

    given().
            standaloneSetup(configure()).
            contentType("application/json").
            body(request).
    when().
            post("/couriers").
    then().
            statusCode(200).
            body("id", equalTo((int) COURIER_ID)).
            body("accessToken", equalTo(COURIER_TOKEN));
  }

  private StandaloneMockMvcBuilder configure() {
    return MockMvcBuilders.standaloneSetup(new CourierController(courierService))
            .addMappedInterceptors(CourierAuthenticationInterceptor.PATH_PATTERNS, new CourierAuthenticationInterceptor(courierService));
  }
}
