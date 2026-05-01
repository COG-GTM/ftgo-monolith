package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiRequestLogTest {

  @Test
  public void shouldCreateWithRequiredFields() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders/1",
            "consumerId=1", "127.0.0.1", "Mozilla/5.0");

    assertEquals("corr-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders/1", log.getRequestUri());
    assertEquals("consumerId=1", log.getQueryString());
    assertEquals("127.0.0.1", log.getRemoteAddr());
    assertEquals("Mozilla/5.0", log.getUserAgent());
    assertNotNull(log.getRequestTimestamp());
  }

  @Test
  public void shouldCompleteWithStatus() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders/1",
            null, "127.0.0.1", "Mozilla/5.0");

    log.complete(200, 42);

    assertEquals(Integer.valueOf(200), log.getResponseStatus());
    assertEquals(Long.valueOf(42), log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithError() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders/1",
            null, "127.0.0.1", "Mozilla/5.0");

    log.complete(500, 100, "Internal Server Error");

    assertEquals(Integer.valueOf(500), log.getResponseStatus());
    assertEquals(Long.valueOf(100), log.getDurationMs());
    assertEquals("Internal Server Error", log.getErrorMessage());
  }
}
