package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final String operatorToken;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository,
                                  @Value("${ftgo.tracking.operator-token:}") String operatorToken) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.operatorToken = operatorToken;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ApiTrackingAccessInterceptor(operatorToken))
            .addPathPatterns("/api/tracking", "/api/tracking/**");

    registry.addInterceptor(apiTrackingInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/api/tracking/**", "/actuator/**");
  }

  @Bean
  public ApiTrackingInterceptor apiTrackingInterceptor() {
    return new ApiTrackingInterceptor(apiRequestLogRepository);
  }
}
