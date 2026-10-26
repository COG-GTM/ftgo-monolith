package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonJsonMapperInitializerTest {

  @Test
  public void shouldRegisterModulesAndDisableTimestamps() {
    CommonJsonMapperInitializer initializer = new CommonJsonMapperInitializer();
    ObjectMapper mapper = new ObjectMapper();
    ReflectionTestUtils.setField(initializer, "objectMapper", mapper);

    initializer.initialize();

    assertThat(mapper.getRegisteredModuleIds()).isNotEmpty();
  }
}
