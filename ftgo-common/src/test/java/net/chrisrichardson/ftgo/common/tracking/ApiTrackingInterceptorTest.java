package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
  }

  @Test
  public void shouldGenerateCorrelationIdWhenMissing() {
    request.setMethod("GET");
    request.setRequestURI("/orders/1");

    interceptor.preHandle(request, response, new Object());

    String correlationId = response.getHeader("X-Correlation-ID");
    assertNotNull(correlationId);
    assertFalse(correlationId.isEmpty());
  }

  @Test
  public void shouldPropagateExistingCorrelationId() {
    request.setMethod("GET");
    request.setRequestURI("/orders/1");
    request.addHeader("X-Correlation-ID", "existing-id-123");

    interceptor.preHandle(request, response, new Object());

    assertEquals("existing-id-123", response.getHeader("X-Correlation-ID"));
  }

  @Test
  public void shouldSetCorrelationIdInResponseHeader() {
    request.setMethod("POST");
    request.setRequestURI("/orders");

    interceptor.preHandle(request, response, new Object());

    assertNotNull(response.getHeader("X-Correlation-ID"));
  }

  @Test
  public void shouldPersistLogAfterCompletion() throws Exception {
    request.setMethod("GET");
    request.setRequestURI("/orders/1");
    interceptor.preHandle(request, response, new Object());

    response.setStatus(200);
    interceptor.afterCompletion(request, response, new Object(), null);

    verify(repository).save(any(ApiRequestLog.class));
  }

  @Test
  public void shouldRecordErrorMessageOnException() throws Exception {
    request.setMethod("GET");
    request.setRequestURI("/orders/1");
    interceptor.preHandle(request, response, new Object());

    response.setStatus(500);
    Exception ex = new RuntimeException("Something broke");
    interceptor.afterCompletion(request, response, new Object(), ex);

    verify(repository).save(argThat(log ->
            "Something broke".equals(log.getErrorMessage())
    ));
  }

  @Test
  public void shouldHandleSaveFailureGracefully() throws Exception {
    doThrow(new RuntimeException("DB error")).when(repository).save(any(ApiRequestLog.class));

    request.setMethod("GET");
    request.setRequestURI("/orders/1");
    interceptor.preHandle(request, response, new Object());

    response.setStatus(200);
    interceptor.afterCompletion(request, response, new Object(), null);

    verify(repository).save(any(ApiRequestLog.class));
  }
}
