package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.DispatcherServlet;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerListTest {
  private OrderRepository repository;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(OrderRepository.class);
    OrderController controller = new OrderController(mock(OrderService.class), repository);
    mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    ReflectionTestUtils.setField(controller, "handlerExceptionResolver",
            ((DispatcherServlet) ReflectionTestUtils.getField(mvc, "servlet"))
                    .getWebApplicationContext().getBean("handlerExceptionResolver"));
  }

  @Test
  public void requiresNumericConsumerId() throws Exception {
    bad("/orders", "consumerId is required");
    bad("/orders?consumerId=abc", "consumerId must be a valid integer");
    bad("/orders?consumerId=", "consumerId must be a valid integer");
  }

  @Test
  public void preservesUnpagedArray() throws Exception {
    mvc.perform(get("/orders?consumerId=1")).andExpect(status().isOk())
            .andExpect(content().json("[]"));
    verify(repository).findAllByConsumerId(1L);
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void filtersCaseInsensitively() throws Exception {
    for (String state : new String[]{"APPROVED", "approved"}) {
      mvc.perform(get("/orders?consumerId=1&state=" + state)).andExpect(status().isOk())
              .andExpect(content().json("[]"));
    }
    verify(repository, times(2)).findAllByConsumerIdAndOrderStateOrderByIdAsc(1L, OrderState.APPROVED, Pageable.unpaged());
    bad("/orders?consumerId=1&state=bogus", "state must be a valid OrderState name");
  }

  @Test
  public void pagesInRepositoryAndDefaultsMissingHalf() throws Exception {
    mvc.perform(get("/orders?consumerId=1&page=1&size=2")).andExpect(status().isOk());
    verify(repository).findAllByConsumerIdOrderByIdAsc(1L, PageRequest.of(1, 2));
    mvc.perform(get("/orders?consumerId=1&page=1")).andExpect(status().isOk());
    verify(repository).findAllByConsumerIdOrderByIdAsc(1L, PageRequest.of(1, 20));
    mvc.perform(get("/orders?consumerId=1&size=100&state=approved")).andExpect(status().isOk());
    verify(repository).findAllByConsumerIdAndOrderStateOrderByIdAsc(1L, OrderState.APPROVED, PageRequest.of(0, 100));
  }

  @Test
  public void rejectsInvalidPaging() throws Exception {
    bad("/orders?consumerId=1&size=0", "size must be between 1 and 100");
    bad("/orders?consumerId=1&size=101", "size must be between 1 and 100");
    bad("/orders?consumerId=1&page=-1", "page must be greater than or equal to 0");
    bad("/orders?consumerId=1&page=abc", "page must be a valid integer");
    bad("/orders?consumerId=1&size=abc", "size must be a valid integer");
    verifyZeroInteractions(repository);
  }

  @Test
  public void leavesOtherEndpointsAlone() throws Exception {
    mvc.perform(get("/orders/abc")).andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }

  private void bad(String url, String message) throws Exception {
    mvc.perform(get(url)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(message))
            .andExpect(jsonPath("$.path").value("/orders"))
            .andExpect(jsonPath("$.timestamp").exists());
  }
}
