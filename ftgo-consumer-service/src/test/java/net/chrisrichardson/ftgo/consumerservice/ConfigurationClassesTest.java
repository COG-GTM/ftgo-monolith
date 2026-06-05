package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerConfiguration;
import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationClassesTest {

  @Test
  public void shouldInstantiateConsumerWebConfiguration() {
    ConsumerWebConfiguration config = new ConsumerWebConfiguration();
    assertThat(config).isNotNull();
  }

  @Test
  public void shouldInstantiateConsumerServiceConfiguration() {
    ConsumerServiceConfiguration config = new ConsumerServiceConfiguration();
    assertThat(config).isNotNull();
  }

  @Test
  public void shouldCreateConsumerServiceBean() {
    ConsumerConfiguration config = new ConsumerConfiguration();
    assertThat(config.consumerService()).isNotNull();
  }
}
