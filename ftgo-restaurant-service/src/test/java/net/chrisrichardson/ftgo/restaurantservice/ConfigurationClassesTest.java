package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantServiceDomainConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationClassesTest {

  @Test
  public void shouldInstantiateRestaurantServiceDomainConfiguration() {
    RestaurantServiceDomainConfiguration config = new RestaurantServiceDomainConfiguration();
    assertThat(config).isNotNull();
  }

  @Test
  public void shouldCreateRestaurantServiceBean() {
    RestaurantServiceDomainConfiguration config = new RestaurantServiceDomainConfiguration();
    assertThat(config.restaurantService()).isNotNull();
  }

  @Test
  public void shouldInstantiateRestaurantServiceConfiguration() {
    RestaurantServiceConfiguration config = new RestaurantServiceConfiguration();
    assertThat(config).isNotNull();
  }
}
