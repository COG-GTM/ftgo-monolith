package net.chrisrichardson.ftgo.orderservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class OrderServiceProxyConfiguration {

  @Value("${order.service.url:http://localhost:8082}")
  private String orderServiceUrl;

  @Bean
  public RestTemplate orderServiceRestTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(5000);

    RestTemplate restTemplate = new RestTemplate(factory);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    restTemplate.setMessageConverters(Collections.singletonList(converter));

    return restTemplate;
  }

  @Bean
  public OrderServiceProxy orderServiceProxy(RestTemplate orderServiceRestTemplate) {
    return new OrderServiceProxy(orderServiceRestTemplate, orderServiceUrl);
  }
}
