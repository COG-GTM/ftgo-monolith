package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @After
  public void clearMdc() {
    MDC.clear();
  }

  @Test
  public void orderNotFoundMessageCurrentlyHasNoSeparatorBeforeId() throws Exception {
    // Suspected bug: OrderNotFoundException message concatenates "Order not found" + id with no separator
    assertError(get("/test/order"), 404, "Not Found", "Order not found42", "/test/order");
  }

  @Test
  public void restaurantNotFoundReturnsNotFound() throws Exception {
    assertError(get("/test/restaurant"), 404, "Not Found",
            "Restaurant not found with id 7", "/test/restaurant");
  }

  @Test
  public void courierNotFoundReturnsNotFound() throws Exception {
    assertError(get("/test/courier"), 404, "Not Found", "Courier not found: 5", "/test/courier");
  }

  @Test
  public void unsupportedStateTransitionReturnsConflict() throws Exception {
    assertError(get("/test/state"), 409, "State Transition Error",
            "current state: " + TimeUnit.SECONDS.name(), "/test/state");
  }

  @Test
  public void noCourierAvailableReturnsServiceUnavailable() throws Exception {
    assertError(get("/test/no-courier"), 503, "No Courier Available",
            "No courier available for assignment", "/test/no-courier");
  }

  @Test
  public void illegalArgumentReturnsBadRequest() throws Exception {
    assertError(get("/test/illegal-argument"), 400, "Bad Request", "bad qty",
            "/test/illegal-argument");
  }

  @Test
  public void numberFormatExceptionUsesBadRequestHandler() throws Exception {
    assertError(get("/test/number-format"), 400, "Bad Request", "x", "/test/number-format");
  }

  @Test
  public void unexpectedRuntimeExceptionHidesInternalMessage() throws Exception {
    mockMvc.perform(get("/test/runtime"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.path").value("/test/runtime"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(content().string(not(containsString("secret internal detail"))));
  }

  @Test
  public void checkedExceptionReturnsInternalServerError() throws Exception {
    assertError(get("/test/checked"), 500, "Internal Server Error",
            "An unexpected error occurred", "/test/checked");
  }

  @Test
  public void correlationIdIsIncludedWhenPresentInMdc() throws Exception {
    MDC.put("correlationId", "corr-123");

    mockMvc.perform(get("/test/order"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.correlationId").value("corr-123"));
  }

  @Test
  public void correlationIdIsNullWhenAbsentFromMdc() throws Exception {
    mockMvc.perform(get("/test/order"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.correlationId").value(nullValue()));
  }

  @Test
  public void malformedJsonBodyCurrentlyReturns500() throws Exception {
    // Suspected bug: catch-all Exception handler converts Spring MVC HttpMessageNotReadableException (should be 400) into 500
    assertError(post("/test/body")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{not json"),
            500, "Internal Server Error", "An unexpected error occurred", "/test/body");
  }

  @Test
  public void missingRequiredParameterCurrentlyReturns500() throws Exception {
    // Suspected bug: catch-all Exception handler converts Spring MVC MissingServletRequestParameterException (should be 400) into 500
    assertError(get("/test/param"), 500, "Internal Server Error",
            "An unexpected error occurred", "/test/param");
  }

  @Test
  public void invalidIntegerParameterCurrentlyReturns500() throws Exception {
    // Suspected bug: catch-all Exception handler converts Spring MVC MethodArgumentTypeMismatchException (should be 400) into 500
    assertError(get("/test/param").param("qty", "abc"), 500, "Internal Server Error",
            "An unexpected error occurred", "/test/param");
  }

  @Test
  public void deleteOnGetOnlyEndpointCurrentlyReturns500() throws Exception {
    // Suspected bug: catch-all Exception handler converts Spring MVC HttpRequestMethodNotSupportedException (should be 405) into 500
    assertError(delete("/test/order"), 500, "Internal Server Error",
            "An unexpected error occurred", "/test/order");
  }

  private void assertError(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                           int expectedStatus, String expectedError, String expectedMessage,
                           String expectedPath) throws Exception {
    mockMvc.perform(request)
            .andExpect(status().is(expectedStatus))
            .andExpect(jsonPath("$.status").value(expectedStatus))
            .andExpect(jsonPath("$.error").value(expectedError))
            .andExpect(jsonPath("$.message").value(expectedMessage))
            .andExpect(jsonPath("$.path").value(expectedPath))
            .andExpect(jsonPath("$.timestamp").exists());
  }

  @RestController
  public static class ThrowingController {

    @GetMapping("/test/order")
    public void order() {
      throw new OrderNotFoundException(42L);
    }

    @GetMapping("/test/restaurant")
    public void restaurant() {
      throw new RestaurantNotFoundException(7);
    }

    @GetMapping("/test/courier")
    public void courier() {
      throw new CourierNotFoundException(5);
    }

    @GetMapping("/test/state")
    public void state() {
      throw new UnsupportedStateTransitionException(TimeUnit.SECONDS);
    }

    @GetMapping("/test/no-courier")
    public void noCourier() {
      throw new NoCourierAvailableException();
    }

    @GetMapping("/test/illegal-argument")
    public void illegalArgument() {
      throw new IllegalArgumentException("bad qty");
    }

    @GetMapping("/test/number-format")
    public void numberFormat() {
      throw new NumberFormatException("x");
    }

    @GetMapping("/test/runtime")
    public void runtime() {
      throw new RuntimeException("secret internal detail");
    }

    @GetMapping("/test/checked")
    public void checked() throws Exception {
      throw new IOException("checked failure");
    }

    @PostMapping("/test/body")
    public void body(@RequestBody Map<String, Object> body) {
    }

    @GetMapping("/test/param")
    public int parameter(@RequestParam int qty) {
      return qty;
    }
  }
}
