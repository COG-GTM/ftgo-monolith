package net.chrisrichardson.ftgo.common.tracking;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ApiTrackingInterceptorTest {

  private final ApiRequestLogRepository repository = mock(ApiRequestLogRepository.class);
  private final ApiTrackingInterceptor interceptor = new ApiTrackingInterceptor(repository);

  private String recordedUri(MockHttpServletRequest request) {
    MockHttpServletResponse response = new MockHttpServletResponse();
    interceptor.preHandle(request, response, null);
    interceptor.afterCompletion(request, response, null, null);
    ArgumentCaptor<ApiRequestLog> saved = ArgumentCaptor.forClass(ApiRequestLog.class);
    verify(repository).save(saved.capture());
    return saved.getValue().getRequestUri();
  }

  @Test
  public void recordsOriginalUriWhenRequestWasRewritten() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    request.setAttribute(OriginalRequestUri.ATTRIBUTE, "/orders/");
    assertThat(recordedUri(request)).isEqualTo("/orders/");
  }

  @Test
  public void recordsRequestUriOtherwise() {
    assertThat(recordedUri(new MockHttpServletRequest("GET", "/consumers/1"))).isEqualTo("/consumers/1");
  }
}
