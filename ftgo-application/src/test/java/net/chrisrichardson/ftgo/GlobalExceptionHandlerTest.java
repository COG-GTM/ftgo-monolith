package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

  private static final String CORRELATION_ID = "test-correlation-id";

  private MockMvc mockMvc;

  @RestController
  static class ThrowingController {

    @GetMapping("/throw/{kind}")
    public String throwException(@PathVariable("kind") String kind) throws Exception {
      switch (kind) {
        case "order-not-found":
          throw new OrderNotFoundException(1L);
        case "restaurant-not-found":
          throw new RestaurantNotFoundException(2L);
        case "courier-not-found":
          throw new CourierNotFoundException(3L);
        case "state-transition":
          throw new UnsupportedStateTransitionException(OrderState.DELIVERED);
        case "no-courier":
          throw new NoCourierAvailableException();
        case "illegal-argument":
          throw new IllegalArgumentException("quantity must be positive");
        default:
          throw new Exception("secret internal detail");
      }
    }
  }

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    MDC.put("correlationId", CORRELATION_ID);
  }

  @After
  public void clearMdc() {
    MDC.clear();
  }

  private ResultActions perform(String kind, int expectedStatus, String expectedError) throws Exception {
    String path = "/throw/" + kind;
    return mockMvc.perform(get(path))
            .andExpect(status().is(expectedStatus))
            .andExpect(jsonPath("$.status").value(expectedStatus))
            .andExpect(jsonPath("$.error").value(expectedError))
            .andExpect(jsonPath("$.path").value(path))
            .andExpect(jsonPath("$.correlationId").value(CORRELATION_ID))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()));
  }

  @Test
  public void shouldReturn404ForOrderNotFound() throws Exception {
    perform("order-not-found", 404, "Not Found")
            .andExpect(jsonPath("$.message").value("Order not found1"));
  }

  @Test
  public void shouldReturn404ForRestaurantNotFound() throws Exception {
    perform("restaurant-not-found", 404, "Not Found")
            .andExpect(jsonPath("$.message").value("Restaurant not found with id 2"));
  }

  @Test
  public void shouldReturn404ForCourierNotFound() throws Exception {
    perform("courier-not-found", 404, "Not Found")
            .andExpect(jsonPath("$.message").value("Courier not found: 3"));
  }

  @Test
  public void shouldReturn409ForUnsupportedStateTransition() throws Exception {
    perform("state-transition", 409, "State Transition Error")
            .andExpect(jsonPath("$.message").value("current state: DELIVERED"));
  }

  @Test
  public void shouldReturn503ForNoCourierAvailable() throws Exception {
    perform("no-courier", 503, "No Courier Available")
            .andExpect(jsonPath("$.message").value("No courier available for assignment"));
  }

  @Test
  public void shouldReturn400ForIllegalArgument() throws Exception {
    perform("illegal-argument", 400, "Bad Request")
            .andExpect(jsonPath("$.message").value("quantity must be positive"));
  }

  @Test
  public void shouldReturn500ForUnhandledException() throws Exception {
    perform("unexpected", 500, "Internal Server Error")
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
