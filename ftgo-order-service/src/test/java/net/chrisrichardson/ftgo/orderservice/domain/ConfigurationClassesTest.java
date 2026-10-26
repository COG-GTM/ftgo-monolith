package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.orderservice.web.OrderWebConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationClassesTest {

  @Test
  public void shouldInstantiateOptimisticOfflineLockException() {
    OptimisticOfflineLockException ex = new OptimisticOfflineLockException();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void shouldInstantiateOrderServiceWithRepositoriesConfiguration() {
    OrderServiceWithRepositoriesConfiguration config = new OrderServiceWithRepositoriesConfiguration();
    assertThat(config).isNotNull();
  }

  @Test
  public void shouldInstantiateOrderWebConfiguration() {
    OrderWebConfiguration config = new OrderWebConfiguration();
    assertThat(config).isNotNull();
  }
}
