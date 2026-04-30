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
    request.setRequestURI("/orders/1");
  }

  @Test
  public void shouldReturn404ForOrderNotFound() {
    ResponseEntity<ErrorResponse> response = handler.handleOrderNotFound(
            new OrderNotFoundException(1L), request);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn404ForRestaurantNotFound() {
    ResponseEntity<ErrorResponse> response = handler.handleRestaurantNotFound(
            new RestaurantNotFoundException(1L), request);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn404ForCourierNotFound() {
    ResponseEntity<ErrorResponse> response = handler.handleCourierNotFound(
            new CourierNotFoundException(1L), request);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn409ForUnsupportedStateTransition() {
    ResponseEntity<ErrorResponse> response = handler.handleUnsupportedStateTransition(
            new UnsupportedStateTransitionException(OrderState.APPROVED), request);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals(409, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn503ForNoCourierAvailable() {
    ResponseEntity<ErrorResponse> response = handler.handleNoCourierAvailable(
            new NoCourierAvailableException(), request);
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals(503, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn400ForIllegalArgument() {
    ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
            new IllegalArgumentException("invalid parameter"), request);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().getStatus());
  }

  @Test
  public void shouldReturn500ForUnhandledException() {
    ResponseEntity<ErrorResponse> response = handler.handleGenericException(
            new RuntimeException("unexpected error"), request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(500, response.getBody().getStatus());
    assertEquals("An unexpected error occurred", response.getBody().getMessage());
  }
}
