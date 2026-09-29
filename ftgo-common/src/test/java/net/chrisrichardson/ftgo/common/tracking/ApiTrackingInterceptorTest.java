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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApiTrackingInterceptorTest {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private ApiRequestLogRepository repository;
  private ApiTrackingInterceptor interceptor;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    interceptor = new ApiTrackingInterceptor(repository);

    request = new MockHttpServletRequest("GET", "/orders/1");
    request.setQueryString("expand=lineItems");
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("User-Agent", "JUnit");
    response = new MockHttpServletResponse();
  }

  @After
  public void tearDown() {
    MDC.clear();
  }

  private ApiRequestLog captureSavedLog() {
    ArgumentCaptor<ApiRequestLog> captor = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(captor.capture());
    return captor.getValue();
  }

  @Test
  public void shouldGenerateCorrelationIdWhenMissing() {
    assertTrue(interceptor.preHandle(request, response, new Object()));

    String correlationId = MDC.get("correlationId");
    assertNotNull(correlationId);
    assertEquals(correlationId, UUID.fromString(correlationId).toString());
  }

  @Test
  public void shouldGenerateCorrelationIdWhenHeaderIsEmpty() {
    request.addHeader(CORRELATION_ID_HEADER, "");

    interceptor.preHandle(request, response, new Object());

    String correlationId = response.getHeader(CORRELATION_ID_HEADER);
    assertNotNull(correlationId);
    UUID.fromString(correlationId);
  }

  @Test
  public void shouldPropagateExistingCorrelationId() {
    request.addHeader(CORRELATION_ID_HEADER, "existing-id");

    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);

    assertEquals("existing-id", captureSavedLog().getCorrelationId());
  }

  @Test
  public void shouldSetCorrelationIdInResponseHeader() {
    request.addHeader(CORRELATION_ID_HEADER, "existing-id");

    interceptor.preHandle(request, response, new Object());

    assertEquals("existing-id", response.getHeader(CORRELATION_ID_HEADER));
    assertEquals("existing-id", MDC.get("correlationId"));
  }

  @Test
  public void shouldPersistLogAfterCompletion() {
    interceptor.preHandle(request, response, new Object());
    String correlationId = response.getHeader(CORRELATION_ID_HEADER);
    response.setStatus(201);

    interceptor.afterCompletion(request, response, new Object(), null);

    ApiRequestLog saved = captureSavedLog();
    assertEquals(correlationId, saved.getCorrelationId());
    assertEquals("GET", saved.getHttpMethod());
    assertEquals("/orders/1", saved.getRequestUri());
    assertEquals("expand=lineItems", saved.getQueryString());
    assertEquals("10.0.0.1", saved.getRemoteAddr());
    assertEquals("JUnit", saved.getUserAgent());
    assertEquals(Integer.valueOf(201), saved.getResponseStatus());
    assertNotNull(saved.getDurationMs());
    assertTrue(saved.getDurationMs() >= 0);
    assertNull(saved.getErrorMessage());
    assertNull(MDC.get("correlationId"));
  }

  @Test
  public void shouldRecordErrorMessageOnException() {
    interceptor.preHandle(request, response, new Object());
    response.setStatus(500);

    interceptor.afterCompletion(request, response, new Object(), new RuntimeException("boom"));

    ApiRequestLog saved = captureSavedLog();
    assertEquals(Integer.valueOf(500), saved.getResponseStatus());
    assertEquals("boom", saved.getErrorMessage());
  }

  @Test
  public void shouldHandleSaveFailureGracefully() {
    when(repository.save(any(ApiRequestLog.class))).thenThrow(new RuntimeException("db down"));

    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);

    verify(repository).save(any(ApiRequestLog.class));
    assertNull(MDC.get("correlationId"));
  }

  @Test
  public void shouldNotPersistWhenPreHandleDidNotRun() {
    MDC.put("correlationId", "stale");

    interceptor.afterCompletion(request, response, new Object(), null);

    verify(repository, never()).save(any(ApiRequestLog.class));
    assertNull(MDC.get("correlationId"));
  }
}
