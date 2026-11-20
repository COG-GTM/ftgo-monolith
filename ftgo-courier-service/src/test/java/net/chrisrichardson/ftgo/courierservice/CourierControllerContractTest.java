package net.chrisrichardson.ftgo.courierservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.web.CourierController;
import net.chrisrichardson.ftgo.courierservice.web.CourierResponse;
import net.chrisrichardson.ftgo.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerContractTest {
  private CourierService service;
  private MockMvc mvc;
  private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Before
  public void setUp() {
    CourierRepository repository = mock(CourierRepository.class);
    when(repository.findById(999999L)).thenReturn(Optional.empty());
    Courier courier = courier();
    when(repository.findById(1L)).thenReturn(Optional.of(courier));
    service = spy(new CourierService(repository));
    mvc = MockMvcBuilders.standaloneSetup(new CourierController(service))
            .setControllerAdvice(new TestExceptionAdvice()).build();
  }

  private Courier courier() {
    Courier courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("1 Main", null, "Oakland", "CA", "94619"));
    ReflectionTestUtils.setField(courier, "id", 1L);
    return courier;
  }

  @Test
  public void unknownCourierEndpointsReturn404() throws Exception {
    mvc.perform(get("/couriers/999999")).andExpect(status().isNotFound());
    mvc.perform(get("/couriers/999999/workload")).andExpect(status().isNotFound());
    mvc.perform(post("/couriers/999999/availability").contentType("application/json")
            .content("{\"available\":true}")).andExpect(status().isNotFound());
    mvc.perform(post("/couriers/999999/location").contentType("application/json")
            .content("{\"latitude\":0,\"longitude\":0}")).andExpect(status().isNotFound());
  }

  @Test
  public void locationValidation() throws Exception {
    mvc.perform(post("/couriers/1/location").contentType("application/json")
            .content("{\"latitude\":200,\"longitude\":0}"))
            .andExpect(status().isBadRequest()).andExpect(content().string("Latitude must be between -90 and 90"));
    mvc.perform(post("/couriers/1/location").contentType("application/json")
            .content("{\"latitude\":0,\"longitude\":-181}"))
            .andExpect(status().isBadRequest()).andExpect(content().string("Longitude must be between -180 and 180"));
    mvc.perform(post("/couriers/1/location").contentType("application/json")
            .content("{\"latitude\":37.8,\"longitude\":-122.4}")).andExpect(status().isOk());
  }

  @Test
  public void createAndAvailabilityStillReturn200() throws Exception {
    doReturn(courier()).when(service).createCourier(any(PersonName.class), any(Address.class));
    mvc.perform(post("/couriers").contentType("application/json")
            .content("{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"},\"address\":{\"street1\":\"1 Main\"}}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    mvc.perform(post("/couriers/1/availability").contentType("application/json")
            .content("{\"available\":true}")).andExpect(status().isOk());
    verify(service).updateAvailability(1, true);
    assertTrue(mapper.readValue("{\"available\":true}", CourierAvailability.class).isAvailable());
    assertEquals("{\"available\":true}", mapper.writeValueAsString(new CourierAvailability(true)));
  }

  @Test
  public void responsePreservesEntityFieldsAndActionFormats() throws Exception {
    Courier courier = courier();
    courier.noteAvailable();
    courier.updateLocation(37.8, -122.4);
    LocalDateTime time = LocalDateTime.of(2026, 1, 2, 12, 30);
    courier.addAction(new Action(ActionType.PICKUP, null, time));
    CourierResponse response = new CourierResponse(courier, Arrays.asList(
            new CourierResponse.ActionResponse(ActionType.PICKUP, time, 42L)));
    JsonNode original = mapper.valueToTree(courier);
    JsonNode actual = mapper.valueToTree(response);
    assertEquals(9, actual.size());
    original.fieldNames().forEachRemaining(field -> {
      if (!field.equals("plan")) assertEquals(original.get(field), actual.get(field));
    });
    assertEquals(original.at("/plan/actions/0/type"), actual.at("/plan/actions/0/type"));
    assertEquals(original.at("/plan/actions/0/time"), actual.at("/plan/actions/0/time"));
    doReturn(response).when(service).getCourier(1);
    mvc.perform(get("/couriers/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.plan.actions[0].type").value("PICKUP"))
            .andExpect(jsonPath("$.plan.actions[0].orderId").value(42))
            .andExpect(jsonPath("$.plan.actions[0].order").doesNotExist());
  }

  @Test
  public void emptyPlanAndNullableFields() throws Exception {
    Courier courier = courier();
    CourierResponse response = new CourierResponse(courier, Collections.emptyList());
    assertEquals(mapper.valueToTree(courier), mapper.valueToTree(response));
    doReturn(response).when(service).getCourier(1);
    mvc.perform(get("/couriers/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.plan.actions").isEmpty());
  }

  @ControllerAdvice
  public static class TestExceptionAdvice {
    @ExceptionHandler(CourierNotFoundException.class)
    public ResponseEntity<String> notFound(CourierNotFoundException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalid(IllegalArgumentException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
