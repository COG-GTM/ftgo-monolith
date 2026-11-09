package net.chrisrichardson.ftgo.restaurantservice.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestaurantServiceClientConfiguration {

  @Bean
  @ConditionalOnProperty(name = RestaurantServiceClient.MODE_PROPERTY, havingValue = RestaurantServiceClient.MODE_REMOTE)
  public RestaurantServiceClient httpRestaurantServiceClient(@Value("${" + RestaurantServiceClient.URL_PROPERTY + "}") String baseUrl,
                                                             @Value("${ftgo.restaurant-service.timeout-ms:2000}") int timeoutMs) {
    return new HttpRestaurantServiceClient(restaurantServiceRestTemplate(timeoutMs), baseUrl);
  }

  public static RestTemplate restaurantServiceRestTemplate(int timeoutMs) {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new MoneyModule())
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(timeoutMs);
    requestFactory.setReadTimeout(timeoutMs);
    RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    return restTemplate;
  }
}
