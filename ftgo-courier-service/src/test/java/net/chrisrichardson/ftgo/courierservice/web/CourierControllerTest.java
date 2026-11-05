package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CreatedCourier;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CourierControllerTest {

  private CourierService courierService;
  private CourierAuthenticator courierAuthenticator;
  private Courier courier;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    when(courierService.findCourierIdByAccessToken(anyString())).thenReturn(Optional.empty());
    when(courierService.findCourierIdByAccessToken("courier-1-token")).thenReturn(Optional.of(1L));
    when(courierService.findCourierIdByAccessToken("courier-2-token")).thenReturn(Optional.of(2L));
    courierAuthenticator = new CourierAuthenticator(courierService, "test-operator-token");

    courier = mock(Courier.class);
    when(courier.getId()).thenReturn(3L);
    when(courierService.createCourier(any(PersonName.class), any(Address.class)))
            .thenReturn(new CreatedCourier(courier, "created-courier-token"));
  }

  @Test
  public void availabilityRequiresAuthenticationAndOnlyAllowsOwnerOrOperator() {
    MockMvcResponse response = post("/couriers/1/availability", new CourierAvailability(true), null);
    assertEquals(401, response.statusCode());
    assertEquals("Bearer realm=\"ftgo\"", response.getHeader("WWW-Authenticate"));
    verify(courierService, never()).updateAvailability(1L, true);

    assertEquals(401, post("/couriers/1/availability", new CourierAvailability(true), "Bearer bogus").statusCode());
    assertEquals(401, post("/couriers/1/availability", new CourierAvailability(true), "Basic xyz").statusCode());

    assertEquals(403, post("/couriers/1/availability", new CourierAvailability(true), "Bearer courier-2-token").statusCode());
    verify(courierService, never()).updateAvailability(1L, true);

    assertEquals(200, post("/couriers/1/availability", new CourierAvailability(true), "Bearer courier-1-token").statusCode());
    verify(courierService).updateAvailability(1L, true);

    assertEquals(200, post("/couriers/1/availability", new CourierAvailability(true), "Bearer test-operator-token").statusCode());
    verify(courierService, org.mockito.Mockito.times(2)).updateAvailability(1L, true);
  }

  @Test
  public void locationRequiresAuthenticationAndOnlyAllowsOwnerOrOperator() {
    CourierLocationUpdate location = new CourierLocationUpdate(12.5, 34.5);
    assertEquals(401, post("/couriers/1/location", location, null).statusCode());
    verify(courierService, never()).updateLocation(1L, 12.5, 34.5);

    assertEquals(403, post("/couriers/1/location", location, "Bearer courier-2-token").statusCode());
    verify(courierService, never()).updateLocation(1L, 12.5, 34.5);

    assertEquals(200, post("/couriers/1/location", location, "Bearer courier-1-token").statusCode());
    verify(courierService).updateLocation(1L, 12.5, 34.5);
  }

  @Test
  public void creatingCourierRequiresOperatorAndReturnsItsToken() {
    CreateCourierRequest request = new CreateCourierRequest(
            new PersonName("Jane", "Doe"),
            new Address("1 Main Street", null, "Oakland", "CA", "94619"));

    assertEquals(401, post("/couriers", request, null).statusCode());
    verify(courierService, never()).createCourier(any(PersonName.class), any(Address.class));

    assertEquals(403, post("/couriers", request, "Bearer courier-1-token").statusCode());
    verify(courierService, never()).createCourier(any(PersonName.class), any(Address.class));

    MockMvcResponse response = post("/couriers", request, "Bearer test-operator-token");
    assertEquals(200, response.statusCode());
    assertEquals(3, response.jsonPath().getInt("id"));
    assertEquals("created-courier-token", response.jsonPath().getString("accessToken"));
    verify(courierService).createCourier(any(PersonName.class), any(Address.class));
  }

  @Test
  public void operatorCredentialsAreRejectedWhenOperatorAccessIsDisabled() {
    CourierAuthenticator disabledAuthenticator = new CourierAuthenticator(courierService, "");
    CreateCourierRequest request = new CreateCourierRequest(
            new PersonName("Jane", "Doe"),
            new Address("1 Main Street", null, "Oakland", "CA", "94619"));

    assertEquals(401, post(
            "/couriers", request, "Bearer ", disabledAuthenticator).statusCode());
    assertEquals(401, post(
            "/couriers", request, "Bearer test-operator-token", disabledAuthenticator).statusCode());
    verify(courierService, never()).createCourier(any(PersonName.class), any(Address.class));
  }

  private MockMvcResponse post(String path, Object body, String authorization) {
    return post(path, body, authorization, courierAuthenticator);
  }

  private MockMvcResponse post(String path, Object body, String authorization, CourierAuthenticator authenticator) {
    CourierController controller = new CourierController(courierService, authenticator);
    MockMvcRequestSpecification request = given()
            .standaloneSetup(configureController(controller))
            .contentType("application/json")
            .body(body);
    if (authorization != null) {
      request.header("Authorization", authorization);
    }
    return request.when().post(path).then().extract().response();
  }

  private StandaloneMockMvcBuilder configureController(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return MockMvcBuilders.standaloneSetup(controllers)
            .setControllerAdvice(new CourierSecurityExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
  }
}
