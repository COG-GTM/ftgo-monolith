package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonConfigurationTest {

  @Test
  public void shouldCreateObjectMapperBean() {
    CommonConfiguration config = new CommonConfiguration();
    ObjectMapper mapper = config.objectMapper();
    assertThat(mapper).isNotNull();
  }

  @Test
  public void shouldCreateCommonJsonMapperInitializerBean() {
    CommonConfiguration config = new CommonConfiguration();
    CommonJsonMapperInitializer initializer = config.commonJsonMapperInitializer();
    assertThat(initializer).isNotNull();
  }
}
