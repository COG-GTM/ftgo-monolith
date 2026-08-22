package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;

  @Value("${ftgo.tracking.api.key:}")
  private String trackingApiKey;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiTrackingInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/api/tracking/**", "/actuator/**");
    registry.addInterceptor(new TrackingApiAuthInterceptor(trackingApiKey))
            .addPathPatterns("/api/tracking/**");
  }

  @Bean
  public ApiTrackingInterceptor apiTrackingInterceptor() {
    return new ApiTrackingInterceptor(apiRequestLogRepository);
  }
}
