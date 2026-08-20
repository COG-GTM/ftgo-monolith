package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final String adminToken;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository,
                                  @Value("${ftgo.api-tracking.admin-token:}") String adminToken) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.adminToken = adminToken;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiTrackingInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/api/tracking/**", "/actuator/**");

    registry.addInterceptor(apiTrackingAuthInterceptor())
            .addPathPatterns("/api/tracking/**");
  }

  @Bean
  public ApiTrackingInterceptor apiTrackingInterceptor() {
    return new ApiTrackingInterceptor(apiRequestLogRepository);
  }

  @Bean
  public ApiTrackingAuthInterceptor apiTrackingAuthInterceptor() {
    return new ApiTrackingAuthInterceptor(adminToken);
  }
}
