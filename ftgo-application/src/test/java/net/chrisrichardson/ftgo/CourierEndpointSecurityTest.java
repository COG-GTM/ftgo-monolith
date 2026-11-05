package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.web.CourierController;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Plan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = CourierEndpointSecurityTest.TestConfiguration.class,
        properties = {
                "ftgo.security.operator.username=operator",
                "ftgo.security.operator.password=e2e-operator-password"
        })
@AutoConfigureMockMvc
public class CourierEndpointSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CourierService courierService;

  @Test
  public void courierDetailsRequireAuthenticationWithoutCallingService() throws Exception {
    mockMvc.perform(get("/couriers/1"))
            .andExpect(status().isUnauthorized());

    verifyZeroInteractions(courierService);
  }

  @Test
  public void courierWorkloadRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/couriers/1/workload"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void wrongPasswordIsUnauthorized() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("operator", "wrong-password")))
            .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "COURIER")
  public void courierRoleCannotReadDetailsOrWorkload() throws Exception {
    mockMvc.perform(get("/couriers/1"))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/couriers/1/workload"))
            .andExpect(status().isForbidden());
  }

  @Test
  public void operatorReceivesCourierDetailsWithoutOrderData() throws Exception {
    Courier courier = mock(Courier.class);
    Plan plan = new Plan();
    plan.add(Action.makePickup(null));
    when(courier.getId()).thenReturn(1L);
    when(courier.getName()).thenReturn(new PersonName("Taylor", "Courier"));
    when(courier.getAddress()).thenReturn(new Address("1 Main Street", null, "Oakland", "CA", "94619"));
    when(courier.isAvailable()).thenReturn(true);
    when(courier.getActiveDeliveryCount()).thenReturn(1);
    when(courier.getPlan()).thenReturn(plan);
    when(courierService.findCourierById(1)).thenReturn(courier);

    mockMvc.perform(get("/couriers/1").with(httpBasic("operator", "e2e-operator-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courierId").value(1))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.plan.actions[0].type").value("PICKUP"))
            .andExpect(jsonPath("$.plan.actions[0].order").doesNotExist());
  }

  @Test
  public void missingCourierReturnsNotFound() throws Exception {
    when(courierService.findCourierById(999)).thenThrow(new CourierNotFoundException(999));

    mockMvc.perform(get("/couriers/999").with(httpBasic("operator", "e2e-operator-password")))
            .andExpect(status().isNotFound());
  }

  @Test
  public void postCourierRemainsPublicWithoutCsrfToken() throws Exception {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(1L);
    when(courierService.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);

    mockMvc.perform(post("/couriers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":{\"firstName\":\"Taylor\",\"lastName\":\"Courier\"},"
                            + "\"address\":{\"street1\":\"1 Main Street\",\"city\":\"Oakland\","
                            + "\"state\":\"CA\",\"zip\":\"94619\"}}"))
            .andExpect(status().isOk());
  }

  @Configuration
  @EnableAutoConfiguration(exclude = {
          DataSourceAutoConfiguration.class,
          HibernateJpaAutoConfiguration.class,
          JpaRepositoriesAutoConfiguration.class
  })
  @Import({CourierController.class, SecurityConfiguration.class, GlobalExceptionHandler.class})
  public static class TestConfiguration {
  }
}
