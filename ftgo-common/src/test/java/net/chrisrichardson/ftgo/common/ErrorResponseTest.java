package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorResponseTest {

  @Test
  public void shouldPopulateAllFields() {
    ErrorResponse response = new ErrorResponse(404, "Not Found", "Order not found", "/orders/1", "abc-123");
    assertEquals(404, response.getStatus());
    assertEquals("Not Found", response.getError());
    assertEquals("Order not found", response.getMessage());
    assertEquals("/orders/1", response.getPath());
    assertEquals("abc-123", response.getCorrelationId());
  }

  @Test
  public void shouldSetTimestampAutomatically() {
    ErrorResponse response = new ErrorResponse(500, "Internal Server Error", "Unexpected", "/api", null);
    assertNotNull(response.getTimestamp());
  }
}
