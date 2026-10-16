package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TicketControllerTest {

  // Suspected bug: TicketController declares no request mappings (dead controller).
  @Test
  public void ticketControllerCurrentlyExposesNoEndpoints() throws Exception {
    OrderService orderService = mock(OrderService.class);
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TicketController(orderService)).build();

    mockMvc.perform(post("/tickets/1/accept")).andExpect(status().isNotFound());
    mockMvc.perform(get("/tickets/1")).andExpect(status().isNotFound());

    verifyZeroInteractions(orderService);
  }
}
