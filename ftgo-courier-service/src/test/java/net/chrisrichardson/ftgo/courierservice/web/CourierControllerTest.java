package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourierControllerTest {

  private CourierService courierService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    objectMapper = Jackson2ObjectMapperBuilder.json().build();
    mockMvc = MockMvcBuilders.standaloneSetup(new CourierController(courierService))
            .setControllerAdvice(new NotFoundAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)).build();
  }

  @Test
  public void shouldPreserveFreshCourierJson() throws Exception {
    JsonNode response = assertDetailParity(freshCourier());
    assertTrue(response.get("currentLatitude").isNull());
    assertTrue(response.get("currentLongitude").isNull());
    assertTrue(response.get("lastLocationUpdate").isNull());
    assertEquals(0, response.get("plan").get("actions").size());
    assertFalse(response.get("available").asBoolean());
  }

  @Test
  public void shouldPreserveNullNameAndAddress() throws Exception {
    JsonNode response = assertDetailParity(new Courier(null, null));
    assertTrue(response.get("name").isNull());
    assertTrue(response.get("address").isNull());
  }

  @Test
  public void shouldPreservePopulatedCourierJsonWithoutOrders() throws Exception {
    Courier courier = populatedCourier();
    JsonNode response = assertDetailParity(courier);
    JsonNode actions = response.get("plan").get("actions");
    assertEquals("PICKUP", actions.get(0).get("type").asText());
    assertEquals("DROPOFF", actions.get(1).get("type").asText());
    assertEquals(1, response.get("activeDeliveryCount").asInt());
    for (JsonNode action : actions) {
      assertFalse(action.has("order"));
      assertEquals(2, action.size());
    }
  }

  @Test
  public void shouldPreserveNullPlan() throws Exception {
    Courier courier = freshCourier();
    ReflectionTestUtils.setField(courier, "plan", null);
    assertTrue(assertDetailParity(courier).get("plan").isNull());
  }

  @Test
  public void shouldPreserveNullActions() throws Exception {
    Courier courier = freshCourier();
    ReflectionTestUtils.setField(courier.getPlan(), "actions", null);
    assertTrue(assertDetailParity(courier).get("plan").get("actions").isNull());
  }

  @Test
  public void shouldReturn404ForUnknownDetailAndWorkload() throws Exception {
    when(courierService.findCourierById(99L)).thenThrow(new CourierNotFoundException(99L));
    mockMvc.perform(get("/couriers/99")).andExpect(status().isNotFound());
    mockMvc.perform(get("/couriers/99/workload")).andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn404ForUnknownAvailability() throws Exception {
    for (boolean available : new boolean[]{true, false}) {
      doThrow(new CourierNotFoundException(99L)).when(courierService).updateAvailability(99L, available);
      mockMvc.perform(post("/couriers/99/availability").contentType(MediaType.APPLICATION_JSON)
              .content("{\"available\":" + available + "}"))
              .andExpect(status().isNotFound());
    }
  }

  @Test
  public void shouldRejectMissingCourierInService() {
    CourierRepository repository = mock(CourierRepository.class);
    when(repository.findById(99L)).thenReturn(Optional.empty());
    CourierService service = new CourierService(repository);
    assertNotFound(() -> service.findCourierById(99L));
    assertNotFound(() -> service.updateAvailability(99L, true));
    assertNotFound(() -> service.updateAvailability(99L, false));
  }

  @Test
  public void shouldUpdateAvailabilityWithEmptySuccessBody() throws Exception {
    for (boolean available : new boolean[]{true, false}) {
      mockMvc.perform(post("/couriers/1/availability").contentType(MediaType.APPLICATION_JSON)
              .content("{\"available\":" + available + "}"))
              .andExpect(status().isOk()).andExpect(content().string(""));
      verify(courierService).updateAvailability(1L, available);
    }
  }

  @Test
  public void shouldReturnWorkload() throws Exception {
    Courier courier = populatedCourier();
    when(courierService.findCourierById(1L)).thenReturn(courier);
    String body = mockMvc.perform(get("/couriers/1/workload")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    JsonNode expected = objectMapper.readTree("{\"courierId\":1,\"activeDeliveries\":1,\"available\":true,"
            + "\"currentLatitude\":37.5,\"currentLongitude\":-122.5,\"lastLocationUpdate\":null}");
    ((com.fasterxml.jackson.databind.node.ObjectNode) expected).set("lastLocationUpdate",
            objectMapper.valueToTree(courier.getLastLocationUpdate()));
    assertEquals(expected, objectMapper.readTree(body));
  }

  private JsonNode assertDetailParity(Courier courier) throws Exception {
    ReflectionTestUtils.setField(courier, "id", 1L);
    when(courierService.findCourierById(1L)).thenReturn(courier);
    String body = mockMvc.perform(get("/couriers/1")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    JsonNode response = objectMapper.readTree(body);
    assertEquals(objectMapper.readTree(objectMapper.writeValueAsString(courier)), response);
    return response;
  }

  private Courier freshCourier() {
    Courier courier = new Courier(new PersonName("Alex", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    ReflectionTestUtils.setField(courier, "id", 1L);
    return courier;
  }

  private Courier populatedCourier() {
    Courier courier = freshCourier();
    courier.updateLocation(37.5, -122.5);
    courier.noteAvailable();
    Order order = mock(Order.class);
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.of(2026, 4, 2, 12, 30)));
    return courier;
  }

  private void assertNotFound(Runnable operation) {
    try {
      operation.run();
      fail("Expected CourierNotFoundException");
    } catch (CourierNotFoundException expected) {
      assertEquals("Courier not found: 99", expected.getMessage());
    }
  }

  @ControllerAdvice
  public static class NotFoundAdvice {
    @ExceptionHandler(CourierNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
  }
}
