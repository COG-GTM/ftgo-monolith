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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ApiTrackingInterceptorTest {

  private ApiRequestLogRepository repository;
  private ApiTrackingInterceptor interceptor;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    interceptor = new ApiTrackingInterceptor(repository);
  }

  @After
  public void clearMdc() {
    MDC.clear();
  }

  @Test
  public void preHandlePropagatesCorrelationIdAndCapturesRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    request.setQueryString("a=1");
    request.setRemoteAddr("192.0.2.4");
    request.addHeader("X-Correlation-ID", "abc-123");
    request.addHeader("User-Agent", "JUnit");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(request, response, new Object()));

    assertEquals("abc-123", response.getHeader("X-Correlation-ID"));
    assertEquals("abc-123", MDC.get("correlationId"));
    ApiRequestLog entry = (ApiRequestLog) request.getAttribute("apiTracking.logEntry");
    assertNotNull(entry);
    assertEquals("abc-123", entry.getCorrelationId());
    assertEquals("GET", entry.getHttpMethod());
    assertEquals("/orders", entry.getRequestUri());
    assertEquals("a=1", entry.getQueryString());
    assertEquals("192.0.2.4", entry.getRemoteAddr());
    assertEquals("JUnit", entry.getUserAgent());
    assertTrue(request.getAttribute("apiTracking.startTime") instanceof Long);
    verifyZeroInteractions(repository);
  }

  @Test
  public void missingAndEmptyCorrelationIdsAreReplacedWithUuids() {
    assertGeneratedCorrelationId(null);
    assertGeneratedCorrelationId("");
  }

  @Test
  public void whitespaceCorrelationIdCurrentlyAccepted() {
    // Suspected bug: blank/unvalidated client X-Correlation-ID is trusted verbatim (only null/empty replaced).
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    request.addHeader("X-Correlation-ID", "   ");
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.preHandle(request, response, new Object());

    assertEquals("   ", response.getHeader("X-Correlation-ID"));
    assertEquals("   ", ((ApiRequestLog) request.getAttribute("apiTracking.logEntry")).getCorrelationId());
  }

  @Test
  public void afterCompletionPersistsResponseAndClearsMdc() throws Exception {
    MockHttpServletRequest request = requestWithCorrelationId();
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, new Object());
    response.setStatus(201);

    interceptor.afterCompletion(request, response, new Object(), null);

    ArgumentCaptor<ApiRequestLog> saved = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(saved.capture());
    assertEquals(Integer.valueOf(201), saved.getValue().getResponseStatus());
    assertTrue(saved.getValue().getDurationMs() >= 0);
    assertEquals("abc-123", saved.getValue().getCorrelationId());
    assertEquals(null, saved.getValue().getErrorMessage());
    assertEquals(null, MDC.get("correlationId"));
  }

  @Test
  public void afterCompletionUsesElapsedTimeFromRequestAttribute() throws Exception {
    MockHttpServletRequest request = requestWithCorrelationId();
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, new Object());
    request.setAttribute("apiTracking.startTime", System.currentTimeMillis() - 500);

    interceptor.afterCompletion(request, response, new Object(), null);

    ArgumentCaptor<ApiRequestLog> saved = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(saved.capture());
    assertTrue(saved.getValue().getDurationMs() >= 500);
  }

  @Test
  public void afterCompletionWithExceptionCurrentlyRecordsDefaultStatus200() throws Exception {
    // Suspected bug: when a handler throws, afterCompletion records response.getStatus() (still 200) so failed requests are logged as successes.
    MockHttpServletRequest request = requestWithCorrelationId();
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, new Object());

    interceptor.afterCompletion(request, response, new Object(), new IllegalStateException("kaboom"));

    ArgumentCaptor<ApiRequestLog> saved = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(saved.capture());
    assertEquals("kaboom", saved.getValue().getErrorMessage());
    assertEquals(Integer.valueOf(200), saved.getValue().getResponseStatus());
  }

  @Test
  public void afterCompletionWithoutPreHandleDoesNotPersistAndClearsMdc() throws Exception {
    MDC.put("correlationId", "leftover");

    interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(),
            new Object(), null);

    verifyZeroInteractions(repository);
    assertEquals(null, MDC.get("correlationId"));
  }

  @Test
  public void postHandleIsANoOp() throws Exception {
    MockHttpServletRequest request = requestWithCorrelationId();
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, new Object());
    ApiRequestLog entry = (ApiRequestLog) request.getAttribute("apiTracking.logEntry");

    interceptor.postHandle(request, response, new Object(), null);

    verifyZeroInteractions(repository);
    assertEquals("abc-123", MDC.get("correlationId"));
    assertEquals(null, entry.getResponseStatus());
    assertEquals(null, entry.getDurationMs());
  }

  @Test
  public void afterCompletionWithLogEntryButNoStartTimeDoesNotPersistAndClearsMdc() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    ApiRequestLog entry = new ApiRequestLog("abc-123", "GET", "/orders", null, null, null);
    request.setAttribute("apiTracking.logEntry", entry);
    MDC.put("correlationId", "abc-123");

    interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

    verifyZeroInteractions(repository);
    assertEquals(null, entry.getResponseStatus());
    assertEquals(null, MDC.get("correlationId"));
  }

  @Test
  public void repositoryFailureIsSwallowedAndMdcIsCleared() throws Exception {
    when(repository.save(any(ApiRequestLog.class))).thenThrow(new RuntimeException("database unavailable"));
    MockHttpServletRequest request = requestWithCorrelationId();
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, new Object());

    interceptor.afterCompletion(request, response, new Object(), null);

    verify(repository).save(any(ApiRequestLog.class));
    assertEquals(null, MDC.get("correlationId"));
  }

  private void assertGeneratedCorrelationId(String incomingId) {
    MDC.clear();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    if (incomingId != null) {
      request.addHeader("X-Correlation-ID", incomingId);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.preHandle(request, response, new Object());

    String generatedId = response.getHeader("X-Correlation-ID");
    UUID.fromString(generatedId);
    assertEquals(generatedId, ((ApiRequestLog) request.getAttribute("apiTracking.logEntry")).getCorrelationId());
    assertEquals(generatedId, MDC.get("correlationId"));
  }

  private MockHttpServletRequest requestWithCorrelationId() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    request.addHeader("X-Correlation-ID", "abc-123");
    return request;
  }
}
