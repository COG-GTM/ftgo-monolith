package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import(CommonConfiguration.class)
public class OrderServiceClientConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public OrderServiceClient orderServiceClient(RestTemplateBuilder builder,
                                                @Value("${order.service.url:http://localhost:8082}") String url) {
    RestTemplate restTemplate = builder.build();
    return new OrderServiceClient(restTemplate, url);
  }
}
