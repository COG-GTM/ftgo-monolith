package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.NestedServletException;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerTest {

  private CourierRepository repository;
  private CourierService service;
  private Courier courier;
  private MockMvc mvc;
  private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Before
  public void setUp() {
    repository = mock(CourierRepository.class);
    service = new CourierService(repository);
    courier = new Courier(new PersonName("Test", "Courier"),
            new Address("1 Main St", "", "Oakland", "CA", "94612"));
    ReflectionTestUtils.setField(courier, "id", 1L);
    when(repository.findById(1L)).thenReturn(Optional.of(courier));
    when(repository.findById(999L)).thenReturn(Optional.empty());
    mvc = MockMvcBuilders.standaloneSetup(new CourierController(service)).build();
  }

  @Test
  public void shouldPreserveEntityJsonExceptPlan() throws Exception {
    assertResponseMatchesEntity();
    courier.noteAvailable();
    courier.updateLocation(37.8, -122.2);
    assertResponseMatchesEntity();
  }

  private void assertResponseMatchesEntity() throws Exception {
    JsonNode entity = mapper.readTree(mapper.writeValueAsString(courier));
    JsonNode response = mapper.readTree(mvc.perform(get("/couriers/1"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertFalse(response.has("plan"));
    Iterator<String> fields = entity.fieldNames();
    while (fields.hasNext()) {
      String field = fields.next();
      if (!field.equals("plan")) {
        assertTrue(field, response.has(field));
        assertEquals(field, entity.get(field), response.get(field));
      }
    }
  }

  @Test
  public void shouldThrowCourierNotFoundOnAllByIdEndpoints() throws Exception {
    assertFailure(get("/couriers/999"), CourierNotFoundException.class);
    assertFailure(get("/couriers/999/workload"), CourierNotFoundException.class);
    assertFailure(post("/couriers/999/location").contentType("application/json")
            .content("{\"latitude\":0,\"longitude\":0}"), CourierNotFoundException.class);
    for (boolean available : new boolean[]{true, false}) {
      assertFailure(post("/couriers/999/availability").contentType("application/json")
              .content("{\"available\":" + available + "}"), CourierNotFoundException.class);
    }
  }

  @Test
  public void shouldRejectInvalidAndMissingCoordinatesWithoutMutation() throws Exception {
    String[] invalid = {"91,0", "-91,0", "0,181", "0,-181", "null,0", "0,null", "null,null"};
    for (String pair : invalid) {
      String[] coordinates = pair.split(",");
      assertFailure(post("/couriers/1/location").contentType("application/json")
              .content("{\"latitude\":" + coordinates[0] + ",\"longitude\":" + coordinates[1] + "}"),
              IllegalArgumentException.class);
    }
    assertFailure(post("/couriers/1/location").contentType("application/json").content("{}"),
            IllegalArgumentException.class);
    assertNull(courier.getCurrentLatitude());
    assertNull(courier.getCurrentLongitude());
    assertNull(courier.getLastLocationUpdate());
    verify(repository, never()).findById(anyLong());
  }

  @Test
  public void shouldAcceptValidCoordinatesIncludingBoundaries() throws Exception {
    for (double[] coordinates : new double[][]{{37.8, -122.2}, {-90, -180}, {90, 180}, {0, 0}}) {
      mvc.perform(post("/couriers/1/location").contentType("application/json")
              .content("{\"latitude\":" + coordinates[0] + ",\"longitude\":" + coordinates[1] + "}"))
              .andExpect(status().isOk());
      assertEquals(coordinates[0], courier.getCurrentLatitude(), 0);
      assertEquals(coordinates[1], courier.getCurrentLongitude(), 0);
      assertNotNull(courier.getLastLocationUpdate());
    }
  }

  @Test
  public void shouldPreserveSuccessfulAvailabilityAndWorkload() throws Exception {
    mvc.perform(post("/couriers/1/availability").contentType("application/json")
            .content("{\"available\":true}")).andExpect(status().isOk());
    mvc.perform(get("/couriers/1/workload")).andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.activeDeliveries").value(0));
  }

  private void assertFailure(RequestBuilder request, Class<? extends Exception> type) throws Exception {
    try {
      mvc.perform(request);
      fail("Expected " + type.getSimpleName());
    } catch (NestedServletException ex) {
      assertTrue(ex.toString(), type.isInstance(ex.getCause()));
    }
  }
}
