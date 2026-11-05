package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerController;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerSecurityConfiguration;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ConsumerAuthorizationIntegrationTest {

  private static final long CONSUMER_ID = 1L;

  @Configuration
  @Import({ConsumerController.class, ConsumerSecurityConfiguration.class, GlobalExceptionHandler.class})
  public static class Config {
  }

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ConsumerService consumerService;

  @Before
  public void setUp() {
    Consumer consumer = new Consumer(new PersonName("Ada", "Lovelace"));
    when(consumerService.findById(CONSUMER_ID)).thenReturn(Optional.of(consumer));
  }

  @Test
  public void shouldForbidOtherConsumer() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(user("2")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status", equalTo(403)));
  }

  @Test
  public void shouldAllowOwner() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(user("1")))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldRejectAnonymous() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID))
            .andExpect(status().isUnauthorized());
  }
}
