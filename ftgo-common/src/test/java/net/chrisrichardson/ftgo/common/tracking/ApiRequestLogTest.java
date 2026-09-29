package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ApiRequestLogTest {

  private ApiRequestLog newLog() {
    return new ApiRequestLog("corr-123", "GET", "/orders/1", "consumerId=5", "127.0.0.1", "JUnit");
  }

  @Test
  public void shouldCreateWithRequiredFields() {
    LocalDateTime before = LocalDateTime.now();
    ApiRequestLog log = newLog();
    LocalDateTime after = LocalDateTime.now();

    assertEquals("corr-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders/1", log.getRequestUri());
    assertEquals("consumerId=5", log.getQueryString());
    assertEquals("127.0.0.1", log.getRemoteAddr());
    assertEquals("JUnit", log.getUserAgent());

    assertNotNull(log.getRequestTimestamp());
    assertFalse(log.getRequestTimestamp().isBefore(before));
    assertFalse(log.getRequestTimestamp().isAfter(after));

    assertNull(log.getId());
    assertNull(log.getResponseStatus());
    assertNull(log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithStatus() {
    ApiRequestLog log = newLog();

    log.complete(200, 42L);

    assertEquals(Integer.valueOf(200), log.getResponseStatus());
    assertEquals(Long.valueOf(42L), log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithError() {
    ApiRequestLog log = newLog();

    log.complete(500, 17L, "boom");

    assertEquals(Integer.valueOf(500), log.getResponseStatus());
    assertEquals(Long.valueOf(17L), log.getDurationMs());
    assertEquals("boom", log.getErrorMessage());
  }
}
