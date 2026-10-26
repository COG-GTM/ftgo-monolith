package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RestaurantClientConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
          .withUserConfiguration(RestaurantRepositoryConfiguration.class, RestaurantClientConfiguration.class);

  @Test
  public void shouldUseTheInProcessClientByDefault() {
    contextRunner.run(context ->
            assertTrue(context.getBean(RestaurantClient.class) instanceof InProcessRestaurantClient));
  }

  @Test
  public void shouldUseTheRemoteClientWhenConfigured() {
    contextRunner.withPropertyValues("ftgo.restaurant-service.mode=remote").run(context ->
            assertTrue(context.getBean(RestaurantClient.class) instanceof RemoteRestaurantClient));
  }

  @Configuration
  static class RestaurantRepositoryConfiguration {
    @Bean
    public RestaurantRepository restaurantRepository() {
      return mock(RestaurantRepository.class);
    }
  }
}
