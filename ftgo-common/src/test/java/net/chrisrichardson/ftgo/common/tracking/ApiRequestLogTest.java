package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ApiRequestLogTest {

  @Test
  public void constructorStoresRequestFieldsAndInitializesTimestamp() {
    LocalDateTime before = LocalDateTime.now();
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders", "a=1",
            "127.0.0.1", "JUnit");
    LocalDateTime after = LocalDateTime.now();

    assertEquals("abc-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders", log.getRequestUri());
    assertEquals("a=1", log.getQueryString());
    assertEquals("127.0.0.1", log.getRemoteAddr());
    assertEquals("JUnit", log.getUserAgent());
    assertTrue(!log.getRequestTimestamp().isBefore(before));
    assertTrue(!log.getRequestTimestamp().isAfter(after));
    assertNull(log.getId());
    assertNull(log.getResponseStatus());
    assertNull(log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void completeWithoutErrorStoresStatusAndDuration() {
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders", null, null, null);

    log.complete(200, 15);

    assertEquals(Integer.valueOf(200), log.getResponseStatus());
    assertEquals(Long.valueOf(15), log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void completeWithErrorStoresAllCompletionFields() {
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders", null, null, null);

    log.complete(500, 20, "boom");

    assertEquals(Integer.valueOf(500), log.getResponseStatus());
    assertEquals(Long.valueOf(20), log.getDurationMs());
    assertEquals("boom", log.getErrorMessage());
  }

  @Test
  public void completeWithoutErrorCurrentlyKeepsPreviousErrorMessage() {
    // Suspected bug: 2-arg complete() does not clear a previously recorded errorMessage.
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders", null, null, null);
    log.complete(500, 1, "boom");

    log.complete(200, 2);

    assertEquals(Integer.valueOf(200), log.getResponseStatus());
    assertEquals(Long.valueOf(2), log.getDurationMs());
    assertEquals("boom", log.getErrorMessage());
  }
}
