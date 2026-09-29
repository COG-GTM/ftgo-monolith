package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ApiRequestLogTest {

  private ApiRequestLog newLog() {
    return new ApiRequestLog("corr-123", "GET", "/orders/1", "page=2", "10.0.0.1", "curl/7.64");
  }

  @Test
  public void shouldCreateWithRequiredFields() {
    LocalDateTime before = LocalDateTime.now();
    ApiRequestLog log = newLog();
    LocalDateTime after = LocalDateTime.now();

    assertEquals("corr-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders/1", log.getRequestUri());
    assertEquals("page=2", log.getQueryString());
    assertEquals("10.0.0.1", log.getRemoteAddr());
    assertEquals("curl/7.64", log.getUserAgent());

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

  @Test
  public void shouldAllowNullOptionalFields() {
    ApiRequestLog log = new ApiRequestLog("corr-1", "POST", "/orders", null, null, null);

    assertNull(log.getQueryString());
    assertNull(log.getRemoteAddr());
    assertNull(log.getUserAgent());
    assertNotNull(log.getRequestTimestamp());
  }
}
