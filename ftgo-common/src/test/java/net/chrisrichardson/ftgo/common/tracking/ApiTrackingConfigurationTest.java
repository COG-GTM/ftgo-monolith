package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ApiTrackingConfigurationTest {

  @Test
  public void shouldCreateInterceptorBean() {
    ApiRequestLogRepository repo = mock(ApiRequestLogRepository.class);
    ApiTrackingConfiguration config = new ApiTrackingConfiguration(repo);

    ApiTrackingInterceptor interceptor = config.apiTrackingInterceptor();
    assertThat(interceptor).isNotNull();
  }

  @Test
  public void shouldAddInterceptorToRegistry() {
    ApiRequestLogRepository repo = mock(ApiRequestLogRepository.class);
    ApiTrackingConfiguration config = new ApiTrackingConfiguration(repo);

    InterceptorRegistry registry = new InterceptorRegistry();
    config.addInterceptors(registry);
    // Should not throw
  }
}
