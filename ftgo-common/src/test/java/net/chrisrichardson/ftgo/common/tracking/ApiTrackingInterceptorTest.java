package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApiTrackingInterceptorTest {

  private ApiRequestLogRepository repository;
  private ApiTrackingInterceptor interceptor;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    interceptor = new ApiTrackingInterceptor(repository);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setMethod("GET");
    request.setRequestURI("/orders/1");
    request.setRemoteAddr("127.0.0.1");
  }

  @Test
  public void shouldGenerateCorrelationIdWhenMissing() {
    interceptor.preHandle(request, response, new Object());
    String correlationId = response.getHeader("X-Correlation-ID");
    assertNotNull(correlationId);
    assertFalse(correlationId.isEmpty());
  }

  @Test
  public void shouldPropagateExistingCorrelationId() {
    request.addHeader("X-Correlation-ID", "existing-id-123");
    interceptor.preHandle(request, response, new Object());
    assertEquals("existing-id-123", response.getHeader("X-Correlation-ID"));
  }

  @Test
  public void shouldSetCorrelationIdInResponseHeader() {
    interceptor.preHandle(request, response, new Object());
    assertNotNull(response.getHeader("X-Correlation-ID"));
  }

  @Test
  public void shouldPersistLogAfterCompletion() {
    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);
    verify(repository).save(any(ApiRequestLog.class));
  }

  @Test
  public void shouldRecordErrorMessageOnException() {
    interceptor.preHandle(request, response, new Object());
    Exception ex = new RuntimeException("Something went wrong");
    interceptor.afterCompletion(request, response, new Object(), ex);
    verify(repository).save(argThat(log -> "Something went wrong".equals(log.getErrorMessage())));
  }

  @Test
  public void shouldHandleSaveFailureGracefully() {
    doThrow(new RuntimeException("DB down")).when(repository).save(any(ApiRequestLog.class));
    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);
  }
}
