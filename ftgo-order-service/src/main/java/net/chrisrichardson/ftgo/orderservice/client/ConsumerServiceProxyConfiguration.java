package net.chrisrichardson.ftgo.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConsumerServiceProxyConfiguration {

  @Bean
  public ConsumerServiceProxy consumerServiceProxy(
          @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(5000);
    requestFactory.setReadTimeout(5000);
    return new ConsumerServiceProxy(new RestTemplate(requestFactory), consumerServiceUrl);
  }
}
