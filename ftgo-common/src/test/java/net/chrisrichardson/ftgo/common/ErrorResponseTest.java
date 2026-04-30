package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorResponseTest {

  @Test
  public void shouldPopulateAllFields() {
    ErrorResponse error = new ErrorResponse(404, "Not Found", "Order not found", "/orders/1", "abc-123");

    assertEquals(404, error.getStatus());
    assertEquals("Not Found", error.getError());
    assertEquals("Order not found", error.getMessage());
    assertEquals("/orders/1", error.getPath());
    assertEquals("abc-123", error.getCorrelationId());
  }

  @Test
  public void shouldSetTimestampAutomatically() {
    ErrorResponse error = new ErrorResponse(500, "Internal Server Error", "Something went wrong", "/api/test", "xyz-456");

    assertNotNull(error.getTimestamp());
  }
}
