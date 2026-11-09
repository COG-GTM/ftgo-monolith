package net.chrisrichardson.ftgo.restaurantservice.api.client;

import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class RestaurantServiceClientConfigurationTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
          .withUserConfiguration(RestaurantServiceClientConfiguration.class);

  @Test
  public void shouldNotCreateRemoteClientByDefault() {
    runner.run(context -> assertThat(context).doesNotHaveBean(RestaurantServiceClient.class));
  }

  @Test
  public void shouldNotCreateRemoteClientInProcessMode() {
    runner.withPropertyValues("ftgo.restaurant-service.mode=in-process")
            .run(context -> assertThat(context).doesNotHaveBean(RestaurantServiceClient.class));
  }

  @Test
  public void shouldCreateHttpClientInRemoteMode() {
    runner.withPropertyValues("ftgo.restaurant-service.mode=remote", "ftgo.restaurant-service.url=http://localhost:8082")
            .run(context -> assertThat(context).getBean(RestaurantServiceClient.class)
                    .isInstanceOf(HttpRestaurantServiceClient.class));
  }
}
