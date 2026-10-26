package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TicketControllerTest {

  @Test
  public void shouldCreateTicketController() {
    OrderService orderService = mock(OrderService.class);
    TicketController controller = new TicketController(orderService);
    assertThat(controller).isNotNull();
  }
}
