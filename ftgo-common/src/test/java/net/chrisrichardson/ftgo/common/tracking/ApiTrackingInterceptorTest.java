package net.chrisrichardson.ftgo.common.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTrackingInterceptorTest {

  @Mock
  private ApiRequestLogRepository apiRequestLogRepository;

  private ApiTrackingInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new ApiTrackingInterceptor(apiRequestLogRepository);
  }

  @Test
  void preHandleShouldGenerateCorrelationIdWhenMissing() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(response.getHeader("X-Correlation-ID")).isNotNull().isNotEmpty();
  }

  @Test
  void preHandleShouldUseProvidedCorrelationId() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/1");
    request.addHeader("X-Correlation-ID", "my-corr-id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.preHandle(request, response, new Object());

    assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("my-corr-id");
  }

  @Test
  void afterCompletionShouldSaveLogEntry() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(200);
    when(apiRequestLogRepository.save(any(ApiRequestLog.class))).thenAnswer(inv -> inv.getArgument(0));

    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);

    ArgumentCaptor<ApiRequestLog> captor = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(apiRequestLogRepository).save(captor.capture());

    ApiRequestLog saved = captor.getValue();
    assertThat(saved.getHttpMethod()).isEqualTo("POST");
    assertThat(saved.getRequestUri()).isEqualTo("/orders");
    assertThat(saved.getResponseStatus()).isEqualTo(200);
    assertThat(saved.getDurationMs()).isNotNull();
  }

  @Test
  void afterCompletionShouldRecordErrorMessage() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/1");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(500);
    when(apiRequestLogRepository.save(any(ApiRequestLog.class))).thenAnswer(inv -> inv.getArgument(0));

    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), new RuntimeException("Server error"));

    ArgumentCaptor<ApiRequestLog> captor = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(apiRequestLogRepository).save(captor.capture());

    assertThat(captor.getValue().getErrorMessage()).isEqualTo("Server error");
  }

  @Test
  void afterCompletionShouldHandleSaveException() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(apiRequestLogRepository.save(any())).thenThrow(new RuntimeException("DB error"));

    interceptor.preHandle(request, response, new Object());
    interceptor.afterCompletion(request, response, new Object(), null);

    verify(apiRequestLogRepository).save(any());
  }

  @Test
  void afterCompletionShouldHandleMissingAttributes() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.afterCompletion(request, response, new Object(), null);

    verify(apiRequestLogRepository, never()).save(any());
  }

  @Test
  void postHandleShouldDoNothing() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.postHandle(request, response, new Object(), null);

    verifyNoInteractions(apiRequestLogRepository);
  }

  @Test
  void preHandleShouldGenerateCorrelationIdWhenEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    request.addHeader("X-Correlation-ID", "");
    MockHttpServletResponse response = new MockHttpServletResponse();

    interceptor.preHandle(request, response, new Object());

    assertThat(response.getHeader("X-Correlation-ID")).isNotEmpty();
  }
}
