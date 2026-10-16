package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Collections;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CourierControllerTest {

  @Test
  public void createCourierReturnsIdAndPassesDeserializedNameAndAddressToService() {
    CourierService courierService = mock(CourierService.class);
    Courier courier = courierWithId(101L);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    given().
            standaloneSetup(new CourierController(courierService)).
            contentType("application/json").
            body("{\"name\":{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"},"
                    + "\"address\":{\"street1\":\"1 Main St\",\"city\":\"London\","
                    + "\"state\":\"London\",\"zip\":\"N1\",\"latitude\":51.5,\"longitude\":-0.12}}").
    when().
            post("/couriers").
    then().
            statusCode(200).
            body("id", equalTo(101));

    ArgumentCaptor<PersonName> nameCaptor = ArgumentCaptor.forClass(PersonName.class);
    ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
    verify(courierService).createCourier(nameCaptor.capture(), addressCaptor.capture());
    assertEquals("Ada", nameCaptor.getValue().getFirstName());
    assertEquals("Lovelace", nameCaptor.getValue().getLastName());
    assertEquals("1 Main St", addressCaptor.getValue().getStreet1());
    assertEquals(Double.valueOf(51.5), addressCaptor.getValue().getLatitude());
    assertEquals(Double.valueOf(-0.12), addressCaptor.getValue().getLongitude());
  }

  @Test
  public void updateAvailabilityWithTrueReturnsOkAndUpdatesService() {
    CourierService courierService = mock(CourierService.class);

    given().
            standaloneSetup(new CourierController(courierService)).
            contentType("application/json").
            body("{\"available\":true}").
    when().
            post("/couriers/12/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(12L, true);
  }

  @Test
  public void updateAvailabilityWithFalseReturnsOkAndUpdatesService() {
    CourierService courierService = mock(CourierService.class);

    given().
            standaloneSetup(new CourierController(courierService)).
            contentType("application/json").
            body("{\"available\":false}").
    when().
            post("/couriers/12/availability").
    then().
            statusCode(200);

    verify(courierService).updateAvailability(12L, false);
  }

  @Test
  public void getCourierReturnsIdAndAvailability() {
    CourierService courierService = mock(CourierService.class);
    Courier courier = courierWithId(21L);
    courier.noteAvailable();
    when(courierService.findCourierById(21L)).thenReturn(courier);

    given().
            standaloneSetup(new CourierController(courierService)).
    when().
            get("/couriers/21").
    then().
            statusCode(200).
            body("id", equalTo(21)).
            body("available", equalTo(true));
  }

  @Test
  public void updateLocationReturnsOkAndUpdatesService() {
    CourierService courierService = mock(CourierService.class);

    given().
            standaloneSetup(new CourierController(courierService)).
            contentType("application/json").
            body("{\"latitude\":40.1,\"longitude\":-73.9}").
    when().
            post("/couriers/31/location").
    then().
            statusCode(200);

    verify(courierService).updateLocation(31L, 40.1, -73.9);
  }

  @Test
  public void getWorkloadReturnsCourierLocationAndActiveDeliveryCount() {
    CourierService courierService = mock(CourierService.class);
    Courier courier = courierWithId(41L);
    courier.noteAvailable();
    courier.updateLocation(40.1, -73.9);
    Order order = new Order(1L, null, Collections.emptyList());
    order.setId(501L);
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, null));
    when(courierService.findCourierById(41L)).thenReturn(courier);

    given().
            standaloneSetup(new CourierController(courierService)).
    when().
            get("/couriers/41/workload").
    then().
            statusCode(200).
            body("courierId", equalTo(41)).
            body("activeDeliveries", equalTo(1)).
            body("available", equalTo(true)).
            body("currentLatitude", equalTo(40.1f)).
            body("currentLongitude", equalTo(-73.9f)).
            body("lastLocationUpdate", notNullValue());
  }

  @Test
  public void updateLocationForUnknownCourierCurrentlyPropagatesExceptionInsteadOf404() throws Exception {
    CourierService courierService = mock(CourierService.class);
    doThrow(new CourierNotFoundException(99L)).when(courierService).updateLocation(99L, 40.1, -73.9);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CourierController(courierService)).build();

    // Suspected bug: the controller does not map an unknown courier to a 404 response.
    try {
      mockMvc.perform(post("/couriers/99/location")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"latitude\":40.1,\"longitude\":-73.9}"));
      throw new AssertionError("Expected the unhandled exception to propagate");
    } catch (NestedServletException e) {
      assertTrue(e.getCause() instanceof CourierNotFoundException);
      assertEquals("Courier not found: 99", e.getCause().getMessage());
    }
  }

  @Test
  public void nonNumericCourierIdReturnsBadRequestWithoutCallingService() {
    CourierService courierService = mock(CourierService.class);

    given().
            standaloneSetup(new CourierController(courierService)).
            contentType("application/json").
            body("{\"available\":true}").
    when().
            post("/couriers/abc/availability").
    then().
            statusCode(400);

    verify(courierService, never()).updateAvailability(anyLong(), anyBoolean());
    verifyZeroInteractions(courierService);
  }

  private Courier courierWithId(long id) {
    Courier courier = new Courier(new PersonName("Ada", "Lovelace"),
            new Address("1 Main St", null, "London", "London", "N1"));
    ReflectionTestUtils.setField(courier, "id", id);
    return courier;
  }
}
