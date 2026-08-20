package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class ApiTrackingConfiguration implements WebMvcConfigurer {

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final int retentionDays;

  public ApiTrackingConfiguration(ApiRequestLogRepository apiRequestLogRepository,
                                  @Value("${ftgo.api-tracking.retention-days:30}") int retentionDays) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.retentionDays = retentionDays;
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
  public ApiRequestLogRetentionJob apiRequestLogRetentionJob() {
    return new ApiRequestLogRetentionJob(apiRequestLogRepository, retentionDays);
  }
}
