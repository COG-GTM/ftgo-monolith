package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

public class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private MockHttpServletRequest request;

  @Before
  public void setUp() {
    handler = new GlobalExceptionHandler();
    request = new MockHttpServletRequest();
    request.setRequestURI("/api/test");
  }

  @Test
  public void shouldReturn404ForOrderNotFound() {
    OrderNotFoundException ex = new OrderNotFoundException(42L);

    ResponseEntity<ErrorResponse> response = handler.handleOrderNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
    assertEquals("Not Found", response.getBody().getError());
    assertEquals("/api/test", response.getBody().getPath());
  }

  @Test
  public void shouldReturn404ForRestaurantNotFound() {
    RestaurantNotFoundException ex = new RestaurantNotFoundException(99L);

    ResponseEntity<ErrorResponse> response = handler.handleRestaurantNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn404ForCourierNotFound() {
    CourierNotFoundException ex = new CourierNotFoundException(55L);

    ResponseEntity<ErrorResponse> response = handler.handleCourierNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn409ForUnsupportedStateTransition() {
    UnsupportedStateTransitionException ex = new UnsupportedStateTransitionException(OrderState.APPROVED);

    ResponseEntity<ErrorResponse> response = handler.handleUnsupportedStateTransition(ex, request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals(409, response.getBody().getStatus());
    assertEquals("State Transition Error", response.getBody().getError());
  }

  @Test
  public void shouldReturn503ForNoCourierAvailable() {
    NoCourierAvailableException ex = new NoCourierAvailableException();

    ResponseEntity<ErrorResponse> response = handler.handleNoCourierAvailable(ex, request);

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals(503, response.getBody().getStatus());
    assertEquals("No Courier Available", response.getBody().getError());
  }

  @Test
  public void shouldReturn400ForIllegalArgument() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

    ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().getStatus());
    assertEquals("Bad Request", response.getBody().getError());
  }

  @Test
  public void shouldReturn500ForUnhandledException() {
    Exception ex = new RuntimeException("Unexpected error");

    ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(500, response.getBody().getStatus());
    assertEquals("An unexpected error occurred", response.getBody().getMessage());
  }
}
