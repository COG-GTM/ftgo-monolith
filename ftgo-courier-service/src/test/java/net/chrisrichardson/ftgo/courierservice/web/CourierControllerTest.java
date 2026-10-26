package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.domain.CourierAccessTokens;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierControllerTest {

  private static final String DISPATCHER_KEY = "dispatcher-secret";
  private static final String COURIER_TOKEN = "courier-1-token";

  private CourierRepository courierRepository;
  private CourierService courierService;
  private Courier courier;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository, new CourierAccessTokens());
    courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"),
            CourierAccessTokens.hash(COURIER_TOKEN));
    ReflectionTestUtils.setField(courier, "id", 1L);
    when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
    when(courierRepository.findByAccessTokenHash(anyString())).thenReturn(Optional.empty());
    when(courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(COURIER_TOKEN))).thenReturn(Optional.of(courier));
  }

  private CourierController controller(String dispatcherKey) {
    return new CourierController(courierService, new CourierAuthorizer(courierService, dispatcherKey));
  }

  @Test
  public void shouldRejectAnonymousReads() {
    given().standaloneSetup(controller(DISPATCHER_KEY)).
            when().get("/couriers/1").
            then().statusCode(401);

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            when().get("/couriers/1/workload").
            then().statusCode(401);
  }

  @Test
  public void shouldRejectAnonymousWrites() {
    given().standaloneSetup(controller(DISPATCHER_KEY)).
            body(new CourierLocationUpdate(1.0, 2.0)).contentType("application/json").
            when().post("/couriers/1/location").
            then().statusCode(401);

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            body(new CourierAvailability(true)).contentType("application/json").
            when().post("/couriers/1/availability").
            then().statusCode(401);

    verify(courierRepository, never()).findById(1L);
  }

  @Test
  public void shouldRejectInvalidToken() {
    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer not-a-real-token").
            when().get("/couriers/1").
            then().statusCode(401);
  }

  @Test
  public void shouldRejectTokenOfAnotherCourier() {
    Courier other = new Courier(new PersonName("Jane", "Roe"), null, CourierAccessTokens.hash("other-token"));
    ReflectionTestUtils.setField(other, "id", 2L);
    when(courierRepository.findByAccessTokenHash(CourierAccessTokens.hash("other-token"))).thenReturn(Optional.of(other));

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer other-token").
            body(new CourierLocationUpdate(1.0, 2.0)).contentType("application/json").
            when().post("/couriers/1/location").
            then().statusCode(403);

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer other-token").
            when().get("/couriers/1/workload").
            then().statusCode(403);
  }

  @Test
  public void shouldAllowCourierToAccessOwnRecord() {
    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer " + COURIER_TOKEN).
            body(new CourierLocationUpdate(37.8, -122.2)).contentType("application/json").
            when().post("/couriers/1/location").
            then().statusCode(200);

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer " + COURIER_TOKEN).
            when().get("/couriers/1/workload").
            then().statusCode(200).
            body("currentLatitude", equalTo(37.8f));

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header("Authorization", "Bearer " + COURIER_TOKEN).
            when().get("/couriers/1").
            then().statusCode(200).
            body("id", equalTo(1)).
            body("$", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasKey("accessTokenHash")));
  }

  @Test
  public void shouldAllowDispatcherToAccessAnyCourier() {
    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header(CourierAuthorizer.DISPATCHER_API_KEY_HEADER, DISPATCHER_KEY).
            body(new CourierAvailability(true)).contentType("application/json").
            when().post("/couriers/1/availability").
            then().statusCode(200);

    given().standaloneSetup(controller(DISPATCHER_KEY)).
            header(CourierAuthorizer.DISPATCHER_API_KEY_HEADER, "wrong-key").
            when().get("/couriers/1").
            then().statusCode(401);
  }

  @Test
  public void shouldIgnoreDispatcherHeaderWhenNoKeyConfigured() {
    given().standaloneSetup(controller("")).
            header(CourierAuthorizer.DISPATCHER_API_KEY_HEADER, "").
            when().get("/couriers/1").
            then().statusCode(401);
  }
}
