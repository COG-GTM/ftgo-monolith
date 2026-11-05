package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = CourierEndpointSecurityTest.TestConfiguration.class,
        properties = {
                "ftgo.security.operator.username=operator",
                "ftgo.security.operator.password="
        })
@AutoConfigureMockMvc
public class CourierEndpointSecurityBlankPasswordTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CourierService courierService;

  @Test
  public void operatorCannotAuthenticateWhenPasswordIsBlank() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("operator", "e2e-operator-password")))
            .andExpect(status().isUnauthorized());
  }
}
