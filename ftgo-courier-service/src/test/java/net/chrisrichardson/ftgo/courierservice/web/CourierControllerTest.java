package net.chrisrichardson.ftgo.courierservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourierControllerTest {

  private MockMvc mockMvc;
  private CourierService courierService;

  @Before
  public void setUp() {
    courierService = mock(CourierService.class);
    CourierController controller = new CourierController(courierService);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(converter)
            .build();
  }

  @Test
  public void shouldCreateCourier() throws Exception {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    Courier courier = new Courier(new PersonName("John", "Doe"), address);
    org.springframework.test.util.ReflectionTestUtils.setField(courier, "id", 1L);

    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    mockMvc.perform(post("/couriers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  public void shouldGetCourier() throws Exception {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Courier courier = new Courier(new PersonName("John", "Doe"), address);
    org.springframework.test.util.ReflectionTestUtils.setField(courier, "id", 1L);

    when(courierService.findCourierById(1L)).thenReturn(courier);

    mockMvc.perform(get("/couriers/1"))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldUpdateAvailability() throws Exception {
    doNothing().when(courierService).updateAvailability(1L, true);

    mockMvc.perform(post("/couriers/1/availability")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"available\":true}"))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldUpdateLocation() throws Exception {
    doNothing().when(courierService).updateLocation(1L, 40.7128, -74.0060);

    mockMvc.perform(post("/couriers/1/location")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"latitude\":40.7128,\"longitude\":-74.006}"))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldGetWorkload() throws Exception {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    Courier courier = new Courier(new PersonName("John", "Doe"), address);
    org.springframework.test.util.ReflectionTestUtils.setField(courier, "id", 1L);
    courier.noteAvailable();
    courier.updateLocation(37.8044, -122.2712);

    when(courierService.findCourierById(1L)).thenReturn(courier);

    mockMvc.perform(get("/couriers/1/workload"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courierId").value(1))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.activeDeliveries").value(0));
  }
}
