package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiRequestLogTest {

  @Test
  public void shouldCreateWithRequiredFields() {
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders/1", null, "127.0.0.1", "Mozilla/5.0");
    assertEquals("abc-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders/1", log.getRequestUri());
    assertNull(log.getQueryString());
    assertEquals("127.0.0.1", log.getRemoteAddr());
    assertEquals("Mozilla/5.0", log.getUserAgent());
    assertNotNull(log.getRequestTimestamp());
  }

  @Test
  public void shouldCompleteWithStatus() {
    ApiRequestLog log = new ApiRequestLog("abc-123", "POST", "/orders", null, "127.0.0.1", null);
    log.complete(201, 150L);
    assertEquals(Integer.valueOf(201), log.getResponseStatus());
    assertEquals(Long.valueOf(150), log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithError() {
    ApiRequestLog log = new ApiRequestLog("abc-123", "GET", "/orders/1", null, "127.0.0.1", null);
    log.complete(500, 200L, "Internal server error");
    assertEquals(Integer.valueOf(500), log.getResponseStatus());
    assertEquals(Long.valueOf(200), log.getDurationMs());
    assertEquals("Internal server error", log.getErrorMessage());
  }
}
