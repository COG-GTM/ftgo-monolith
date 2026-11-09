package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = CourierControllerSecurityTest.Config.class)
@TestPropertySource(properties = {
        "ftgo.security.users[0].username=dispatcher",
        "ftgo.security.users[0].password={noop}dispatcher-pw",
        "ftgo.security.users[0].roles=DISPATCHER",
        "ftgo.security.users[1].username=courier1",
        "ftgo.security.users[1].password={noop}courier1-pw",
        "ftgo.security.users[1].roles=COURIER",
        "ftgo.security.users[1].courier-id=1",
        "ftgo.security.users[2].username=courier2",
        "ftgo.security.users[2].password={noop}courier2-pw",
        "ftgo.security.users[2].roles=COURIER",
        "ftgo.security.users[2].courier-id=2"
})
public class CourierControllerSecurityTest {

  private static final String LOCATION = "{\"latitude\": 37.8, \"longitude\": -122.2}";
  private static final String AVAILABLE = "{\"available\": false}";

  @Configuration
  @EnableWebMvc
  @Import({CourierSecurityConfiguration.class, CourierController.class, CourierAccess.class})
  static class Config {
    @Bean
    public CourierService courierService() {
      return mock(CourierService.class);
    }
  }

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private CourierService courierService;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    reset(courierService);
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(1L);
    when(courier.getCurrentLatitude()).thenReturn(37.8);
    when(courier.getCurrentLongitude()).thenReturn(-122.2);
    when(courierService.findCourierById(anyLong())).thenReturn(courier);
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  public void anonymousCannotUpdateLocation() throws Exception {
    mockMvc.perform(post("/couriers/1/location").contentType(MediaType.APPLICATION_JSON).content(LOCATION))
            .andExpect(status().isUnauthorized());
    verify(courierService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
  }

  @Test
  public void anonymousCannotReadWorkload() throws Exception {
    mockMvc.perform(get("/couriers/1/workload")).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/couriers/1")).andExpect(status().isUnauthorized());
    verify(courierService, never()).findCourierById(anyLong());
  }

  @Test
  public void anonymousCannotChangeAvailabilityOrCreate() throws Exception {
    mockMvc.perform(post("/couriers/1/availability").contentType(MediaType.APPLICATION_JSON).content(AVAILABLE))
            .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/couriers").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    verifyZeroInteractions(courierService);
  }

  @Test
  public void wrongPasswordIsRejected() throws Exception {
    mockMvc.perform(get("/couriers/1/workload").with(httpBasic("courier1", "wrong")))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void courierCanUpdateOwnLocation() throws Exception {
    mockMvc.perform(post("/couriers/1/location").with(httpBasic("courier1", "courier1-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(LOCATION))
            .andExpect(status().isOk());
    verify(courierService).updateLocation(1L, 37.8, -122.2);
  }

  @Test
  public void courierCannotUpdateAnotherCouriersLocation() throws Exception {
    mockMvc.perform(post("/couriers/2/location").with(httpBasic("courier1", "courier1-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(LOCATION))
            .andExpect(status().isForbidden());
    verify(courierService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
  }

  @Test
  public void dispatcherCannotSpoofCourierLocation() throws Exception {
    mockMvc.perform(post("/couriers/1/location").with(httpBasic("dispatcher", "dispatcher-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(LOCATION))
            .andExpect(status().isForbidden());
    verify(courierService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
  }

  @Test
  public void courierCannotReadAnotherCouriersWorkloadOrDetails() throws Exception {
    mockMvc.perform(get("/couriers/1/workload").with(httpBasic("courier2", "courier2-pw")))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/couriers/1").with(httpBasic("courier2", "courier2-pw")))
            .andExpect(status().isForbidden());
    verify(courierService, never()).findCourierById(anyLong());
  }

  @Test
  public void courierCannotToggleAnotherCouriersAvailability() throws Exception {
    mockMvc.perform(post("/couriers/1/availability").with(httpBasic("courier2", "courier2-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(AVAILABLE))
            .andExpect(status().isForbidden());
    verify(courierService, never()).updateAvailability(anyLong(), anyBoolean());
  }

  @Test
  public void courierCannotCreateCouriers() throws Exception {
    mockMvc.perform(post("/couriers").with(httpBasic("courier1", "courier1-pw"))
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    verify(courierService, never()).createCourier(any(), any());
  }

  @Test
  public void courierCanReadOwnWorkloadAndToggleOwnAvailability() throws Exception {
    mockMvc.perform(get("/couriers/1/workload").with(httpBasic("courier1", "courier1-pw")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentLatitude").value(37.8));
    mockMvc.perform(post("/couriers/1/availability").with(httpBasic("courier1", "courier1-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(AVAILABLE))
            .andExpect(status().isOk());
    verify(courierService).updateAvailability(1L, false);
  }

  @Test
  public void dispatcherCanReadWorkloadAndManageAvailability() throws Exception {
    mockMvc.perform(get("/couriers/2/workload").with(httpBasic("dispatcher", "dispatcher-pw")))
            .andExpect(status().isOk());
    mockMvc.perform(post("/couriers/2/availability").with(httpBasic("dispatcher", "dispatcher-pw"))
            .contentType(MediaType.APPLICATION_JSON).content(AVAILABLE))
            .andExpect(status().isOk());
    verify(courierService).updateAvailability(2L, false);
  }
}
