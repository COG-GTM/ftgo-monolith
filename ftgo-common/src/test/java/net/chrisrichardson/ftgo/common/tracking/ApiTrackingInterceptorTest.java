package net.chrisrichardson.ftgo.common.tracking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ApiTrackingInterceptorTest {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String LOG_ENTRY_ATTR = "apiTracking.logEntry";

  private ApiRequestLogRepository repository;
  private ApiTrackingInterceptor interceptor;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final Object handler = new Object();

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    interceptor = new ApiTrackingInterceptor(repository);
    request = new MockHttpServletRequest("GET", "/orders/99");
    request.setQueryString("expand=lineItems");
    request.setRemoteAddr("192.168.1.10");
    request.addHeader("User-Agent", "JUnit");
    response = new MockHttpServletResponse();
  }

  @After
  public void clearMdc() {
    MDC.clear();
  }

  @Test
  public void shouldGenerateCorrelationIdWhenMissing() {
    assertTrue(interceptor.preHandle(request, response, handler));

    String correlationId = MDC.get("correlationId");
    assertNotNull(correlationId);
    UUID.fromString(correlationId);
    assertEquals(correlationId, logEntry().getCorrelationId());
  }

  @Test
  public void shouldGenerateCorrelationIdWhenHeaderIsEmpty() {
    request.addHeader(CORRELATION_ID_HEADER, "");

    interceptor.preHandle(request, response, handler);

    String correlationId = response.getHeader(CORRELATION_ID_HEADER);
    assertNotNull(correlationId);
    UUID.fromString(correlationId);
  }

  @Test
  public void shouldPropagateExistingCorrelationId() {
    request.addHeader(CORRELATION_ID_HEADER, "abc-123");

    interceptor.preHandle(request, response, handler);

    assertEquals("abc-123", MDC.get("correlationId"));
    assertEquals("abc-123", logEntry().getCorrelationId());
  }

  @Test
  public void shouldSetCorrelationIdInResponseHeader() {
    request.addHeader(CORRELATION_ID_HEADER, "abc-123");

    interceptor.preHandle(request, response, handler);

    assertEquals("abc-123", response.getHeader(CORRELATION_ID_HEADER));
  }

  @Test
  public void shouldCaptureRequestDetailsInLogEntry() {
    interceptor.preHandle(request, response, handler);

    ApiRequestLog entry = logEntry();
    assertEquals("GET", entry.getHttpMethod());
    assertEquals("/orders/99", entry.getRequestUri());
    assertEquals("expand=lineItems", entry.getQueryString());
    assertEquals("192.168.1.10", entry.getRemoteAddr());
    assertEquals("JUnit", entry.getUserAgent());
  }

  @Test
  public void shouldPersistLogAfterCompletion() {
    interceptor.preHandle(request, response, handler);
    ApiRequestLog entry = logEntry();
    response.setStatus(201);

    interceptor.afterCompletion(request, response, handler, null);

    ArgumentCaptor<ApiRequestLog> captor = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(captor.capture());
    ApiRequestLog saved = captor.getValue();
    assertSame(entry, saved);
    assertEquals(Integer.valueOf(201), saved.getResponseStatus());
    assertNotNull(saved.getDurationMs());
    assertTrue(saved.getDurationMs() >= 0);
    assertNull(saved.getErrorMessage());
    assertNull(MDC.get("correlationId"));
  }

  @Test
  public void shouldRecordErrorMessageOnException() {
    interceptor.preHandle(request, response, handler);
    response.setStatus(500);

    interceptor.afterCompletion(request, response, handler, new IllegalStateException("kaboom"));

    ArgumentCaptor<ApiRequestLog> captor = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(captor.capture());
    assertEquals(Integer.valueOf(500), captor.getValue().getResponseStatus());
    assertEquals("kaboom", captor.getValue().getErrorMessage());
  }

  @Test
  public void shouldHandleSaveFailureGracefully() {
    doThrow(new RuntimeException("db down")).when(repository).save(any(ApiRequestLog.class));
    interceptor.preHandle(request, response, handler);

    interceptor.afterCompletion(request, response, handler, null);

    verify(repository).save(any(ApiRequestLog.class));
    assertNull(MDC.get("correlationId"));
  }

  @Test
  public void shouldSkipPersistenceWhenPreHandleDidNotRun() {
    MDC.put("correlationId", "stale");

    interceptor.afterCompletion(request, response, handler, null);

    verify(repository, never()).save(any(ApiRequestLog.class));
    assertNull(MDC.get("correlationId"));
  }

  private ApiRequestLog logEntry() {
    return (ApiRequestLog) request.getAttribute(LOG_ENTRY_ATTR);
  }
}
