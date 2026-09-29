package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourierControllerTest {

  private static final long COURIER_ID = 42L;
  private static final PersonName COURIER_NAME = new PersonName("Jane", "Rider");
  private static final Address COURIER_ADDRESS =
          new Address("1 Main Street", null, "Oakland", "CA", "94611", 37.8, -122.2);

  private CourierService courierService;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new CourierController(courierService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build()))
            .build();
  }

  @Test
  public void shouldCreateCourier() throws Exception {
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(makeCourier());

    mockMvc.perform(post("/couriers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                    "\"name\": {\"firstName\": \"Jane\", \"lastName\": \"Rider\"}," +
                    "\"address\": {\"street1\": \"1 Main Street\", \"city\": \"Oakland\", \"state\": \"CA\", \"zip\": \"94611\"," +
                    "\"latitude\": 37.8, \"longitude\": -122.2}" +
                    "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(equalTo((int) COURIER_ID)));

    ArgumentCaptor<PersonName> nameCaptor = ArgumentCaptor.forClass(PersonName.class);
    ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
    verify(courierService).createCourier(nameCaptor.capture(), addressCaptor.capture());
    assertEquals("Jane", nameCaptor.getValue().getFirstName());
    assertEquals("Rider", nameCaptor.getValue().getLastName());
    assertEquals("Oakland", addressCaptor.getValue().getCity());
    assertEquals(Double.valueOf(37.8), addressCaptor.getValue().getLatitude());
    assertEquals(Double.valueOf(-122.2), addressCaptor.getValue().getLongitude());
  }

  @Test
  public void shouldGetCourier() throws Exception {
    Courier courier = makeCourier();
    courier.noteAvailable();
    when(courierService.findCourierById(COURIER_ID)).thenReturn(courier);

    mockMvc.perform(get("/couriers/" + COURIER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(equalTo((int) COURIER_ID)))
            .andExpect(jsonPath("$.name.firstName").value("Jane"))
            .andExpect(jsonPath("$.name.lastName").value("Rider"))
            .andExpect(jsonPath("$.address.city").value("Oakland"))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.currentLatitude").value(37.8))
            .andExpect(jsonPath("$.currentLongitude").value(-122.2));
  }

  @Test
  public void shouldUpdateAvailability() throws Exception {
    mockMvc.perform(post("/couriers/" + COURIER_ID + "/availability")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"available\": true}"))
            .andExpect(status().isOk());

    verify(courierService).updateAvailability(COURIER_ID, true);
  }

  @Test
  public void shouldUpdateAvailabilityToUnavailable() throws Exception {
    mockMvc.perform(post("/couriers/" + COURIER_ID + "/availability")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"available\": false}"))
            .andExpect(status().isOk());

    verify(courierService).updateAvailability(COURIER_ID, false);
  }

  @Test
  public void shouldUpdateLocation() throws Exception {
    mockMvc.perform(post("/couriers/" + COURIER_ID + "/location")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": 37.7749, \"longitude\": -122.4194}"))
            .andExpect(status().isOk());

    verify(courierService).updateLocation(COURIER_ID, 37.7749, -122.4194);
  }

  @Test
  public void shouldGetWorkload() throws Exception {
    Courier courier = makeCourier();
    courier.noteAvailable();
    Order firstOrder = mock(Order.class);
    Order secondOrder = mock(Order.class);
    courier.addAction(Action.makePickup(firstOrder));
    courier.addAction(Action.makeDropoff(firstOrder, LocalDateTime.of(2026, 1, 2, 4, 0)));
    courier.addAction(Action.makePickup(secondOrder));
    ReflectionTestUtils.setField(courier, "lastLocationUpdate", LocalDateTime.of(2026, 1, 2, 3, 4, 5));
    when(courierService.findCourierById(COURIER_ID)).thenReturn(courier);

    mockMvc.perform(get("/couriers/" + COURIER_ID + "/workload"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courierId").value(equalTo((int) COURIER_ID)))
            .andExpect(jsonPath("$.activeDeliveries").value(2))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.currentLatitude").value(37.8))
            .andExpect(jsonPath("$.currentLongitude").value(-122.2))
            .andExpect(jsonPath("$.lastLocationUpdate").value("2026-01-02T03:04:05"));
  }

  private Courier makeCourier() {
    Courier courier = new Courier(COURIER_NAME, COURIER_ADDRESS);
    ReflectionTestUtils.setField(courier, "id", COURIER_ID);
    return courier;
  }
}
