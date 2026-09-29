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

    @GetMapping("/throw/order-not-found")
    public void orderNotFound() {
      throw new OrderNotFoundException(99L);
    }

    @GetMapping("/throw/restaurant-not-found")
    public void restaurantNotFound() {
      throw new RestaurantNotFoundException(7L);
    }

    @GetMapping("/throw/courier-not-found")
    public void courierNotFound() {
      throw new CourierNotFoundException(3L);
    }

    @GetMapping("/throw/unsupported-state-transition")
    public void unsupportedStateTransition() {
      throw new UnsupportedStateTransitionException(OrderState.DELIVERED);
    }

    @GetMapping("/throw/no-courier-available")
    public void noCourierAvailable() {
      throw new NoCourierAvailableException();
    }

    @GetMapping("/throw/illegal-argument")
    public void illegalArgument() {
      throw new IllegalArgumentException("bad input");
    }

    @GetMapping("/throw/unhandled")
    public void unhandled() {
      throw new IllegalStateException("secret internal detail");
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
  public void tearDown() {
    MDC.clear();
  }

  private void assertErrorResponse(String path, int status, String error, String message) throws Exception {
    ResultActions result = mockMvc.perform(get(path));
    result.andExpect(status().is(status))
            .andExpect(jsonPath("$.status").value(status))
            .andExpect(jsonPath("$.error").value(error))
            .andExpect(jsonPath("$.message").value(message))
            .andExpect(jsonPath("$.path").value(path))
            .andExpect(jsonPath("$.correlationId").value(CORRELATION_ID))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()));
  }

  @Test
  public void shouldReturn404ForOrderNotFound() throws Exception {
    assertErrorResponse("/throw/order-not-found", 404, "Not Found", "Order not found99");
  }

  @Test
  public void shouldReturn404ForRestaurantNotFound() throws Exception {
    assertErrorResponse("/throw/restaurant-not-found", 404, "Not Found", "Restaurant not found with id 7");
  }

  @Test
  public void shouldReturn404ForCourierNotFound() throws Exception {
    assertErrorResponse("/throw/courier-not-found", 404, "Not Found", "Courier not found: 3");
  }

  @Test
  public void shouldReturn409ForUnsupportedStateTransition() throws Exception {
    assertErrorResponse("/throw/unsupported-state-transition", 409, "State Transition Error", "current state: DELIVERED");
  }

  @Test
  public void shouldReturn503ForNoCourierAvailable() throws Exception {
    assertErrorResponse("/throw/no-courier-available", 503, "No Courier Available", "No courier available for assignment");
  }

  @Test
  public void shouldReturn400ForIllegalArgument() throws Exception {
    assertErrorResponse("/throw/illegal-argument", 400, "Bad Request", "bad input");
  }

  @Test
  public void shouldReturn500ForUnhandledException() throws Exception {
    assertErrorResponse("/throw/unhandled", 500, "Internal Server Error", "An unexpected error occurred");
  }
}
