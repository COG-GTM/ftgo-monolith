package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import(CourierServiceConfiguration.class)
@ComponentScan
public class CourierWebConfiguration implements WebMvcConfigurer {

  private final CourierService courierService;

  public CourierWebConfiguration(CourierService courierService) {
    this.courierService = courierService;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(courierAuthenticationInterceptor())
            .addPathPatterns(CourierAuthenticationInterceptor.PATH_PATTERNS);
  }

  @Bean
  public CourierAuthenticationInterceptor courierAuthenticationInterceptor() {
    return new CourierAuthenticationInterceptor(courierService);
  }
}
