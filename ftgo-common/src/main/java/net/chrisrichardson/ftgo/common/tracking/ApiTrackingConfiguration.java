package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
@Import(ApiTrackingSecurityConfiguration.class)
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
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

  @Bean
  public ApiRequestLogRetentionJob apiRequestLogRetentionJob(
          @Value("${ftgo.api-tracking.retention-days:7}") int retentionDays) {
    if (retentionDays < 1) {
      throw new IllegalArgumentException("ftgo.api-tracking.retention-days must be >= 1, was " + retentionDays);
    }
    return new ApiRequestLogRetentionJob(apiRequestLogRepository, retentionDays);
  }
}
