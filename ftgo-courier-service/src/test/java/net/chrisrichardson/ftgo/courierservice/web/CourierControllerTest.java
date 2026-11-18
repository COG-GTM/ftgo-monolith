package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerTest {
  @Test
  public void availabilitySerializesAndRoundTrips() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(new CourierAvailability(true));
    assertEquals("{\"available\":true}", json);
    assertEquals(Boolean.TRUE, mapper.readValue(json, CourierAvailability.class).isAvailable());
  }

  @Test
  public void locationSerializesAndRoundTrips() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(new CourierLocationUpdate(37.8, -122.2));
    assertEquals("{\"latitude\":37.8,\"longitude\":-122.2}", json);
    CourierLocationUpdate location = mapper.readValue(json, CourierLocationUpdate.class);
    assertEquals(Double.valueOf(37.8), location.getLatitude());
    assertEquals(Double.valueOf(-122.2), location.getLongitude());
  }

  private CourierRepository repository;
  private Courier courier;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(CourierRepository.class);
    courier = new Courier(new PersonName("Ada", "Lovelace"),
            new Address("1 Main", null, "Oakland", "CA", "94612", 37.8, -122.2));
    org.springframework.test.util.ReflectionTestUtils.setField(courier, "id", 1L);
    when(repository.findById(1L)).thenReturn(Optional.of(courier));
    when(repository.findById(999999L)).thenReturn(Optional.empty());
    mvc = MockMvcBuilders.standaloneSetup(new CourierController(new CourierService(repository)))
            .setControllerAdvice(new TestErrorHandler()).build();
  }

  @RestControllerAdvice
  public static class TestErrorHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void badRequest() {}
    @ExceptionHandler(CourierNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}
  }

  @Test
  public void unknownCouriersReturn404() throws Exception {
    mvc.perform(get("/couriers/999999")).andExpect(status().isNotFound());
    mvc.perform(get("/couriers/999999/workload")).andExpect(status().isNotFound());
    for (boolean available : new boolean[]{true, false})
      mvc.perform(post("/couriers/999999/availability").contentType("application/json")
              .content("{\"available\":" + available + "}")).andExpect(status().isNotFound());
  }

  @Test
  public void rejectsMissingNullAndOutOfRangeCoordinates() throws Exception {
    for (String body : new String[]{"{}", "{\"latitude\":0}", "{\"longitude\":0}",
            "{\"latitude\":null,\"longitude\":0}", "{\"latitude\":0,\"longitude\":null}",
            "{\"latitude\":999,\"longitude\":0}", "{\"latitude\":-91,\"longitude\":0}",
            "{\"latitude\":0,\"longitude\":181}", "{\"latitude\":0,\"longitude\":-181}"})
      mvc.perform(post("/couriers/1/location").contentType("application/json").content(body))
              .andExpect(status().isBadRequest());
    assertNull(courier.getLastLocationUpdate());
  }

  @Test
  public void acceptsCoordinatesIncludingBoundaries() throws Exception {
    for (String body : new String[]{"{\"latitude\":-90,\"longitude\":-180}",
            "{\"latitude\":90,\"longitude\":180}", "{\"latitude\":0,\"longitude\":0}"})
      mvc.perform(post("/couriers/1/location").contentType("application/json").content(body))
              .andExpect(status().isOk()).andExpect(content().string(""));
    assertEquals(Double.valueOf(0), courier.getCurrentLatitude());
    assertNotNull(courier.getLastLocationUpdate());
    mvc.perform(post("/couriers/999999/location").contentType("application/json")
            .content("{\"latitude\":0,\"longitude\":0}")).andExpect(status().isNotFound());
  }

  @Test
  public void availabilityRequiresValueAndAcceptsBothValues() throws Exception {
    for (String body : new String[]{"{}", "{\"available\":null}"})
      mvc.perform(post("/couriers/1/availability").contentType("application/json").content(body))
              .andExpect(status().isBadRequest());
    for (boolean available : new boolean[]{true, false}) {
      mvc.perform(post("/couriers/1/availability").contentType("application/json")
              .content("{\"available\":" + available + "}"))
              .andExpect(status().isOk()).andExpect(content().string(""));
      assertEquals(available, courier.isAvailable());
    }
  }

  @Test
  public void creationRequiresNameAndPreservesResponse() throws Exception {
    for (String body : new String[]{"{}", "{\"name\":null}"})
      mvc.perform(post("/couriers").contentType("application/json").content(body))
              .andExpect(status().isBadRequest());
    doAnswer(invocation -> {
      Courier saved = invocation.getArgument(0);
      org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 42L);
      return saved;
    }).when(repository).save(any(Courier.class));
    mvc.perform(post("/couriers").contentType("application/json")
            .content("{\"name\":{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}}"))
            .andExpect(status().isOk()).andExpect(content().json("{\"id\":42}"));
  }

  @Test
  public void existingCourierAndWorkloadKeepFields() throws Exception {
    mvc.perform(get("/couriers/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.plan.actions").isEmpty())
            .andExpect(jsonPath("$.name.firstName").value("Ada"))
            .andExpect(jsonPath("$.address.latitude").value(37.8))
            .andExpect(jsonPath("$.activeDeliveryCount").value(0));
    mvc.perform(get("/couriers/1/workload")).andExpect(status().isOk())
            .andExpect(jsonPath("$.activeDeliveries").value(0))
            .andExpect(jsonPath("$.available").value(false));
  }

  @Test
  public void responseExactlyMatchesEntitySerializationWithAndWithoutPlanActions() throws Exception {
    ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
    assertEquals(mapper.valueToTree(courier), mapper.valueToTree(new CourierResponse(courier)));
    courier.addAction(new Action(ActionType.PICKUP, null, null));
    courier.addAction(new Action(ActionType.DROPOFF, null, LocalDateTime.of(2026, 1, 2, 3, 4, 5)));
    courier.updateLocation(12, 34);
    courier.noteAvailable();
    assertEquals(mapper.valueToTree(courier), mapper.valueToTree(new CourierResponse(courier)));
    assertEquals(1, new CourierResponse(courier).getActiveDeliveryCount());
  }
}
