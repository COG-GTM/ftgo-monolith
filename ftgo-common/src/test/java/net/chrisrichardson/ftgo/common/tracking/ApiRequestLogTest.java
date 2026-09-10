package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiRequestLogTest {

  private static String repeat(char c, int n) {
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      sb.append(c);
    }
    return sb.toString();
  }

  @Test
  public void shouldTruncateOversizedRequestFieldsToColumnLimits() {
    ApiRequestLog log = new ApiRequestLog(
            repeat('c', 300),
            "GET",
            repeat('u', 2000),
            repeat('q', 3000),
            repeat('a', 300),
            repeat('h', 2000));

    assertEquals(ApiRequestLog.CORRELATION_ID_MAX_LENGTH, log.getCorrelationId().length());
    assertEquals(ApiRequestLog.REQUEST_URI_MAX_LENGTH, log.getRequestUri().length());
    assertEquals(ApiRequestLog.QUERY_STRING_MAX_LENGTH, log.getQueryString().length());
    assertEquals(ApiRequestLog.REMOTE_ADDR_MAX_LENGTH, log.getRemoteAddr().length());
    assertEquals(ApiRequestLog.USER_AGENT_MAX_LENGTH, log.getUserAgent().length());
  }

  @Test
  public void shouldTruncateOversizedErrorMessage() {
    ApiRequestLog log = new ApiRequestLog("cid", "GET", "/orders", null, "127.0.0.1", "ua");
    log.complete(500, 10, repeat('e', 5000));
    assertEquals(ApiRequestLog.ERROR_MESSAGE_MAX_LENGTH, log.getErrorMessage().length());
  }

  @Test
  public void shouldLeaveInBoundsAndNullValuesUnchanged() {
    ApiRequestLog log = new ApiRequestLog("cid", "GET", "/orders", null, "127.0.0.1", null);
    assertEquals("cid", log.getCorrelationId());
    assertEquals("/orders", log.getRequestUri());
    assertNull(log.getQueryString());
    assertNull(log.getUserAgent());
  }

  @Test
  public void shouldRejectOversizedOrEmptyCorrelationIdHeader() {
    assertTrue(ApiTrackingInterceptor.isValidCorrelationId("abc-123"));
    assertFalse(ApiTrackingInterceptor.isValidCorrelationId(null));
    assertFalse(ApiTrackingInterceptor.isValidCorrelationId(""));
    assertFalse(ApiTrackingInterceptor.isValidCorrelationId(repeat('x', 129)));
  }
}
