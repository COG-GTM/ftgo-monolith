package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

  @Mock
  private OrderService orderService;

  @Test
  void shouldConstruct() {
    TicketController controller = new TicketController(orderService);
    assertThat(controller).isNotNull();
  }
}
