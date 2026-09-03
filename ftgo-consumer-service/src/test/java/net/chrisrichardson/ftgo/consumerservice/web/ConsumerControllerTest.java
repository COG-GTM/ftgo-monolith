package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConsumerControllerTest {

  private static final String OPERATOR_KEY = "operator-secret";

  private ConsumerService consumerService;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    consumerService = mock(ConsumerService.class);
    when(consumerService.findById(1L)).thenReturn(Optional.of(new Consumer(new PersonName("Ada", "Lovelace"))));
    mockMvc = MockMvcBuilders.standaloneSetup(
            new ConsumerController(consumerService, new OperatorApiKeyAuthorizer(OPERATOR_KEY))).build();
  }

  @Test
  public void shouldRejectLookupWithoutOperatorKey() throws Exception {
    mockMvc.perform(get("/consumers/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", "Bearer"))
            .andExpect(jsonPath("$").doesNotExist());
  }

  @Test
  public void shouldRejectLookupWithWrongOperatorKey() throws Exception {
    mockMvc.perform(get("/consumers/1").header("Authorization", "Bearer wrong"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void shouldReturnConsumerForOperator() throws Exception {
    mockMvc.perform(get("/consumers/1").header("Authorization", "Bearer " + OPERATOR_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.firstName").value("Ada"))
            .andExpect(jsonPath("$.name.lastName").value("Lovelace"));
  }

  @Test
  public void shouldAcceptLowercaseBearerScheme() throws Exception {
    mockMvc.perform(get("/consumers/1").header("Authorization", "bearer " + OPERATOR_KEY))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldReturnNotFoundForOperatorWhenConsumerMissing() throws Exception {
    when(consumerService.findById(2L)).thenReturn(Optional.empty());
    mockMvc.perform(get("/consumers/2").header("Authorization", "Bearer " + OPERATOR_KEY))
            .andExpect(status().isNotFound());
  }

  @Test
  public void shouldFailClosedWhenNoOperatorKeyConfigured() throws Exception {
    MockMvc unconfigured = MockMvcBuilders.standaloneSetup(
            new ConsumerController(consumerService, new OperatorApiKeyAuthorizer(""))).build();
    unconfigured.perform(get("/consumers/1").header("Authorization", "Bearer "))
            .andExpect(status().isUnauthorized());
  }
}
