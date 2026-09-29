package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ErrorResponseTest {

  private static final int STATUS = 404;
  private static final String ERROR = "Not Found";
  private static final String MESSAGE = "Order not found: 99";
  private static final String PATH = "/orders/99";
  private static final String CORRELATION_ID = "abc-123";

  @Test
  public void shouldPopulateAllFields() {
    ErrorResponse response = new ErrorResponse(STATUS, ERROR, MESSAGE, PATH, CORRELATION_ID);

    assertEquals(STATUS, response.getStatus());
    assertEquals(ERROR, response.getError());
    assertEquals(MESSAGE, response.getMessage());
    assertEquals(PATH, response.getPath());
    assertEquals(CORRELATION_ID, response.getCorrelationId());
  }

  @Test
  public void shouldSetTimestampAutomatically() {
    LocalDateTime before = LocalDateTime.now();
    ErrorResponse response = new ErrorResponse(STATUS, ERROR, MESSAGE, PATH, CORRELATION_ID);
    LocalDateTime after = LocalDateTime.now();

    LocalDateTime timestamp = response.getTimestamp();
    assertNotNull(timestamp);
    assertFalse(timestamp.isBefore(before));
    assertFalse(timestamp.isAfter(after));
  }

  @Test
  public void shouldLeaveTimestampUnsetForDefaultConstructor() {
    assertNull(new ErrorResponse().getTimestamp());
  }
}
