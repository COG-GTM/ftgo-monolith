package net.chrisrichardson.ftgo.orderservice.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Wires the {@link CourierServiceProxy} for the order service.
 *
 * <p>The courier service base URL is configurable via {@code courier.service.url} (default
 * {@code http://localhost:8084}). The {@link RestTemplate} uses 5s connect/read timeouts and a
 * Jackson mapper configured with field visibility so the shared {@code ftgo-domain.Courier} entity
 * (which exposes getters but no setters) can be reconstructed from the courier service's JSON.
 */
@Configuration
public class CourierServiceProxyConfiguration {

  @Bean
  public RestTemplate courierServiceRestTemplate(
          @Value("${courier.service.connect-timeout-ms:5000}") int connectTimeoutMs,
          @Value("${courier.service.read-timeout-ms:5000}") int readTimeoutMs) {

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeoutMs);
    requestFactory.setReadTimeout(readTimeoutMs);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    return restTemplate;
  }

  @Bean
  public CourierServiceProxy courierServiceProxy(
          RestTemplate courierServiceRestTemplate,
          @Value("${courier.service.url:http://localhost:8084}") String courierServiceUrl) {
    return new CourierServiceProxy(courierServiceRestTemplate, courierServiceUrl);
  }
}
