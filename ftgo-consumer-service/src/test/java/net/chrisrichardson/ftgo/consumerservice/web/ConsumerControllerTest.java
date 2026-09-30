package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ConsumerControllerTest {

  private static final long CONSUMER_ID = 1L;

  @Configuration
  @Import({ConsumerController.class, ConsumerSecurityConfiguration.class})
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
    Consumer createdConsumer = mock(Consumer.class);
    when(createdConsumer.getId()).thenReturn(CONSUMER_ID);
    when(consumerService.create(any(PersonName.class))).thenReturn(createdConsumer);
  }

  @Test
  public void shouldRejectAnonymousGet() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("WWW-Authenticate"));
  }

  @Test
  public void shouldRejectInvalidCredentials() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(httpBasic("1", "wrong")))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldForbidOtherConsumer() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(user("2")))
            .andExpect(status().isForbidden());
  }

  @Test
  public void shouldAllowOwner() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(user("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName", equalTo("Ada")))
            .andExpect(jsonPath("$.name.lastName", equalTo("Lovelace")));
  }

  @Test
  public void shouldAllowAdmin() throws Exception {
    mockMvc.perform(get("/consumers/{id}", CONSUMER_ID).with(user("ops").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName", equalTo("Ada")));
  }

  @Test
  public void shouldAllowAnonymousCreate() throws Exception {
    mockMvc.perform(post("/consumers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId", equalTo((int) CONSUMER_ID)));
  }
}
