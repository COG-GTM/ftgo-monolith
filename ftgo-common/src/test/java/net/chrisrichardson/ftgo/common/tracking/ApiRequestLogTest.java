package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiRequestLogTest {

  @Test
  public void shouldCreateWithRequiredFields() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders/1", "status=active", "127.0.0.1", "TestAgent/1.0");

    assertEquals("corr-123", log.getCorrelationId());
    assertEquals("GET", log.getHttpMethod());
    assertEquals("/orders/1", log.getRequestUri());
    assertEquals("status=active", log.getQueryString());
    assertEquals("127.0.0.1", log.getRemoteAddr());
    assertEquals("TestAgent/1.0", log.getUserAgent());
    assertNotNull(log.getRequestTimestamp());
    assertNull(log.getResponseStatus());
    assertNull(log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithStatus() {
    ApiRequestLog log = new ApiRequestLog("corr-456", "POST", "/orders", null, "10.0.0.1", "Mozilla/5.0");

    log.complete(201, 150L);

    assertEquals(Integer.valueOf(201), log.getResponseStatus());
    assertEquals(Long.valueOf(150L), log.getDurationMs());
    assertNull(log.getErrorMessage());
  }

  @Test
  public void shouldCompleteWithError() {
    ApiRequestLog log = new ApiRequestLog("corr-789", "DELETE", "/orders/99", null, "10.0.0.1", "curl/7.0");

    log.complete(500, 200L, "Internal server error");

    assertEquals(Integer.valueOf(500), log.getResponseStatus());
    assertEquals(Long.valueOf(200L), log.getDurationMs());
    assertEquals("Internal server error", log.getErrorMessage());
  }
}
