package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerTest {

  private MockMvc mockMvc;
  private CourierService courierService;
  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    CourierController controller = new CourierController(courierService);
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter).build();
  }

  @Test
  public void shouldCreateCourier() throws Exception {
    PersonName name = new PersonName("John", "Doe");
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94607");
    Courier courier = new Courier(name, address);
    ReflectionTestUtils.setField(courier, "id", 1L);

    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    String json = objectMapper.writeValueAsString(new CreateCourierRequest(name, address));

    mockMvc.perform(post("/couriers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  public void shouldGetCourier() throws Exception {
    Courier courier = new Courier(new PersonName("Jane", "Smith"), null);
    ReflectionTestUtils.setField(courier, "id", 1L);
    when(courierService.findCourierById(1L)).thenReturn(courier);

    mockMvc.perform(get("/couriers/1"))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldUpdateAvailability() throws Exception {
    String json = objectMapper.writeValueAsString(new CourierAvailability(true));

    mockMvc.perform(post("/couriers/1/availability")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk());

    verify(courierService).updateAvailability(1L, true);
  }

  @Test
  public void shouldUpdateLocation() throws Exception {
    String json = objectMapper.writeValueAsString(new CourierLocationUpdate(37.8044, -122.2712));

    mockMvc.perform(post("/couriers/1/location")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk());

    verify(courierService).updateLocation(1L, 37.8044, -122.2712);
  }

  @Test
  public void shouldGetWorkload() throws Exception {
    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 St", null, "City", "CA", "94000", 37.0, -122.0));
    courier.noteAvailable();
    ReflectionTestUtils.setField(courier, "id", 1L);
    when(courierService.findCourierById(1L)).thenReturn(courier);

    mockMvc.perform(get("/couriers/1/workload"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courierId").value(1))
            .andExpect(jsonPath("$.available").value(true));
  }
}
