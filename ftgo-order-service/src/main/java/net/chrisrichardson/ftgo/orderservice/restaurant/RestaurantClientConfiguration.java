package net.chrisrichardson.ftgo.orderservice.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Selects how the order service reaches the restaurant bounded context.
 * <p>
 * {@code ftgo.restaurant-service.mode=in-process} (the default) keeps the monolith's existing
 * behavior; {@code remote} routes the same calls to the extracted restaurant service.
 */
@Configuration
public class RestaurantClientConfiguration {

  @Bean
  @ConditionalOnProperty(name = "ftgo.restaurant-service.mode", havingValue = "in-process", matchIfMissing = true)
  public RestaurantClient inProcessRestaurantClient(RestaurantRepository restaurantRepository) {
    return new InProcessRestaurantClient(restaurantRepository);
  }

  @Bean
  @ConditionalOnProperty(name = "ftgo.restaurant-service.mode", havingValue = "remote")
  public RestaurantClient remoteRestaurantClient(
          @Value("${ftgo.restaurant-service.base-url:http://localhost:8082}") String baseUrl,
          @Value("${ftgo.restaurant-service.connect-timeout-millis:5000}") int connectTimeout,
          @Value("${ftgo.restaurant-service.read-timeout-millis:5000}") int readTimeout) {
    return new RemoteRestaurantClient(makeRestTemplate(connectTimeout, readTimeout), baseUrl);
  }

  static RestTemplate makeRestTemplate(int connectTimeout, int readTimeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());

    RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    return restTemplate;
  }
}
