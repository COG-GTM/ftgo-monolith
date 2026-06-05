package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ApiTrackingInterceptorTest {

  private ApiRequestLogRepository logRepository;
  private ApiTrackingInterceptor interceptor;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Before
  public void setUp() {
    logRepository = mock(ApiRequestLogRepository.class);
    interceptor = new ApiTrackingInterceptor(logRepository);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
  }

  @Test
  public void preHandleShouldGenerateCorrelationIdWhenMissing() {
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/orders");
    when(request.getQueryString()).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("User-Agent")).thenReturn("test");

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    verify(response).setHeader(eq("X-Correlation-ID"), anyString());
    verify(request).setAttribute(eq("apiTracking.startTime"), anyLong());
    verify(request).setAttribute(eq("apiTracking.logEntry"), any(ApiRequestLog.class));
  }

  @Test
  public void preHandleShouldUseExistingCorrelationId() {
    when(request.getHeader("X-Correlation-ID")).thenReturn("existing-id");
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/orders");
    when(request.getQueryString()).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("10.0.0.1");
    when(request.getHeader("User-Agent")).thenReturn("curl");

    interceptor.preHandle(request, response, new Object());

    verify(response).setHeader("X-Correlation-ID", "existing-id");
  }

  @Test
  public void afterCompletionShouldPersistLog() {
    long startTime = System.currentTimeMillis() - 100;
    ApiRequestLog logEntry = new ApiRequestLog("corr-1", "GET", "/test", null, "127.0.0.1", "test");

    when(request.getAttribute("apiTracking.startTime")).thenReturn(startTime);
    when(request.getAttribute("apiTracking.logEntry")).thenReturn(logEntry);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/test");
    when(response.getStatus()).thenReturn(200);

    interceptor.afterCompletion(request, response, new Object(), null);

    verify(logRepository).save(logEntry);
    assertThat(logEntry.getResponseStatus()).isEqualTo(200);
    assertThat(logEntry.getDurationMs()).isGreaterThanOrEqualTo(0);
  }

  @Test
  public void afterCompletionShouldHandleException() {
    long startTime = System.currentTimeMillis() - 50;
    ApiRequestLog logEntry = new ApiRequestLog("corr-2", "POST", "/orders", null, "127.0.0.1", "test");

    when(request.getAttribute("apiTracking.startTime")).thenReturn(startTime);
    when(request.getAttribute("apiTracking.logEntry")).thenReturn(logEntry);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/orders");
    when(response.getStatus()).thenReturn(500);

    Exception ex = new RuntimeException("something broke");
    interceptor.afterCompletion(request, response, new Object(), ex);

    verify(logRepository).save(logEntry);
    assertThat(logEntry.getErrorMessage()).isEqualTo("something broke");
  }

  @Test
  public void afterCompletionShouldHandleMissingAttributes() {
    when(request.getAttribute("apiTracking.startTime")).thenReturn(null);
    when(request.getAttribute("apiTracking.logEntry")).thenReturn(null);

    interceptor.afterCompletion(request, response, new Object(), null);

    verify(logRepository, never()).save(any());
  }

  @Test
  public void afterCompletionShouldHandleSaveFailure() {
    long startTime = System.currentTimeMillis() - 10;
    ApiRequestLog logEntry = new ApiRequestLog("corr-3", "GET", "/test", null, "127.0.0.1", "test");

    when(request.getAttribute("apiTracking.startTime")).thenReturn(startTime);
    when(request.getAttribute("apiTracking.logEntry")).thenReturn(logEntry);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/test");
    when(response.getStatus()).thenReturn(200);
    doThrow(new RuntimeException("DB error")).when(logRepository).save(any());

    interceptor.afterCompletion(request, response, new Object(), null);
    // Should not throw
  }

  @Test
  public void postHandleShouldDoNothing() {
    interceptor.postHandle(request, response, new Object(), null);
    // Just verifying no exceptions
  }
}
