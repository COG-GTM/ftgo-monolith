package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierCredentialsRepository;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CourierController.class)
@Import({CourierController.class, CourierSecurityConfiguration.class,
        FtgoUserDetailsService.class, CourierAccessPolicy.class})
@TestPropertySource(properties = "ftgo.security.operator.password=")
public class CourierSecurityWithoutOperatorTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private CourierService courierService;
  @MockBean
  private CourierCredentialsRepository credentialsRepository;

  @Test
  public void shouldNotCreateOperatorWhenPasswordIsBlank() throws Exception {
    mockMvc.perform(get("/couriers/1").with(httpBasic("operator", "test-operator-password")))
            .andExpect(status().isUnauthorized());
    verifyZeroInteractions(courierService);
  }
}
