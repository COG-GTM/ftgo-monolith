package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Bean
  public FilterRegistrationBean<ApiTrackingAuthenticationFilter> apiTrackingAuthenticationFilter(
          ApiTrackingSecurityProperties properties) {
    FilterRegistrationBean<ApiTrackingAuthenticationFilter> registration =
            new FilterRegistrationBean<>(new ApiTrackingAuthenticationFilter(properties));
    registration.addUrlPatterns("/api/tracking/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiTrackingInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/api/tracking/**", "/actuator/**");
  }

  @Bean
  public ApiTrackingInterceptor apiTrackingInterceptor() {
    return new ApiTrackingInterceptor(apiRequestLogRepository);
  }
}
