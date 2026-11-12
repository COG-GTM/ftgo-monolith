package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierAccessTokens;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerTest {

  private static final String OPERATOR_TOKEN = "operator-secret";
  private static final String COURIER_A_TOKEN = "courier-a-secret";
  private static final String COURIER_B_TOKEN = "courier-b-secret";

  private CourierRepository courierRepository;
  private CourierService courierService;
  private Courier courierA;
  private Courier courierB;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    courierRepository = mock(CourierRepository.class);
    courierService = new CourierService(courierRepository);
    courierA = courier(1L, COURIER_A_TOKEN);
    courierB = courier(2L, COURIER_B_TOKEN);

    when(courierRepository.findById(1L)).thenReturn(Optional.of(courierA));
    when(courierRepository.findById(2L)).thenReturn(Optional.of(courierB));
    when(courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(COURIER_A_TOKEN)))
            .thenReturn(Optional.of(courierA));
    when(courierRepository.findByAccessTokenHash(CourierAccessTokens.hash(COURIER_B_TOKEN)))
            .thenReturn(Optional.of(courierB));

    CourierRequestAuthenticator authenticator = new CourierRequestAuthenticator(courierService, OPERATOR_TOKEN);
    mockMvc = MockMvcBuilders.standaloneSetup(new CourierController(courierService, authenticator))
            .setControllerAdvice(new CourierWebExceptionHandler())
            .build();
  }

  @Test
  public void shouldRequireAuthenticationForAllWriteRoutes() throws Exception {
    assertUnauthorized(post("/couriers/1/availability", null, "{\"available\":true}"));
    assertUnauthorized(post("/couriers/1/location", null, "{\"latitude\":37.77,\"longitude\":-122.41}"));
    assertUnauthorized(post("/couriers", null, createCourierBody()));
  }

  @Test
  public void shouldRejectUnknownBearerToken() throws Exception {
    assertUnauthorized(post("/couriers/1/availability", "unknown-token", "{\"available\":true}"));
  }

  @Test
  public void shouldAuthenticateBeforeValidatingLocation() throws Exception {
    assertUnauthorized(post("/couriers/1/location", null, "{\"latitude\":500,\"longitude\":-122.41}"));
  }

  @Test
  public void shouldDenyAnotherCouriersAvailabilityAndLocation() throws Exception {
    post("/couriers/1/availability", COURIER_B_TOKEN, "{\"available\":true}")
            .andExpect(status().isForbidden());
    post("/couriers/1/location", COURIER_B_TOKEN, "{\"latitude\":37.77,\"longitude\":-122.41}")
            .andExpect(status().isForbidden());
    assertFalse(courierA.isAvailable());
    assertFalse(courierA.hasLocation());
  }

  @Test
  public void shouldDenyCourierTokenOnCreate() throws Exception {
    post("/couriers", COURIER_A_TOKEN, createCourierBody()).andExpect(status().isForbidden());
    verify(courierRepository, never()).save(any(Courier.class));
  }

  @Test
  public void shouldAllowCourierToUpdateOwnAvailabilityAndLocation() throws Exception {
    post("/couriers/1/availability", COURIER_A_TOKEN, "{\"available\":true}")
            .andExpect(status().isOk());
    post("/couriers/1/location", COURIER_A_TOKEN, "{\"latitude\":37.77,\"longitude\":-122.41}")
            .andExpect(status().isOk());

    assertTrue(courierA.isAvailable());
    assertEquals(Double.valueOf(37.77), courierA.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.41), courierA.getCurrentLongitude());
  }

  @Test
  public void shouldAcceptLowercaseBearerScheme() throws Exception {
    postWithAuthorization("/couriers/1/availability", "bearer " + COURIER_A_TOKEN, "{\"available\":true}")
            .andExpect(status().isOk());

    assertTrue(courierA.isAvailable());
  }

  @Test
  public void shouldAllowOperatorToUpdateAnyCourier() throws Exception {
    post("/couriers/1/availability", OPERATOR_TOKEN, "{\"available\":true}")
            .andExpect(status().isOk());
    post("/couriers/1/location", OPERATOR_TOKEN, "{\"latitude\":37.77,\"longitude\":-122.41}")
            .andExpect(status().isOk());

    assertTrue(courierA.isAvailable());
    assertEquals(Double.valueOf(37.77), courierA.getCurrentLatitude());
  }

  @Test
  public void shouldRejectInvalidLocationWithoutChangingExistingLocation() throws Exception {
    courierA.updateLocation(37.0, -122.0);

    post("/couriers/1/location", COURIER_A_TOKEN, "{\"latitude\":500,\"longitude\":-122.41}")
            .andExpect(status().isBadRequest());

    assertEquals(Double.valueOf(37.0), courierA.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.0), courierA.getCurrentLongitude());
  }

  @Test
  public void shouldCreateCourierForOperatorAndReturnItsAccessToken() throws Exception {
    when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> {
      Courier courier = (Courier) invocation.getArguments()[0];
      ReflectionTestUtils.setField(courier, "id", 3L);
      return courier;
    });

    String response = post("/couriers", OPERATOR_TOKEN, createCourierBody())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andReturn().getResponse().getContentAsString();

    String returnedToken = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(response).get("accessToken").asText();
    org.mockito.ArgumentCaptor<Courier> savedCourier = org.mockito.ArgumentCaptor.forClass(Courier.class);
    verify(courierRepository).save(savedCourier.capture());
    assertTrue(savedCourier.getValue().hasAccessTokenHash(CourierAccessTokens.hash(returnedToken)));
  }

  private void assertUnauthorized(org.springframework.test.web.servlet.ResultActions result) throws Exception {
    result.andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
  }

  private org.springframework.test.web.servlet.ResultActions post(String path, String token, String body)
          throws Exception {
    return postWithAuthorization(path, token == null ? null : "Bearer " + token, body);
  }

  private org.springframework.test.web.servlet.ResultActions postWithAuthorization(
          String path, String authorization, String body) throws Exception {
    MockHttpServletRequestBuilder request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    if (authorization != null) {
      request.header(HttpHeaders.AUTHORIZATION, authorization);
    }
    return mockMvc.perform(request);
  }

  private Courier courier(long id, String token) {
    Courier courier = new Courier(new PersonName("Courier", Long.toString(id)),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    ReflectionTestUtils.setField(courier, "id", id);
    courier.assignAccessTokenHash(CourierAccessTokens.hash(token));
    return courier;
  }

  private String createCourierBody() {
    return "{\"name\":{\"firstName\":\"New\",\"lastName\":\"Courier\"}," +
            "\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"}}";
  }
}
