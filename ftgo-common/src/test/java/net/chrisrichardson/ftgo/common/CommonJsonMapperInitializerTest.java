package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CommonJsonMapperInitializerTest {

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private CommonJsonMapperInitializer initializer;

  @Test
  void shouldRegisterModules() {
    initializer.initialize();
    assertThat(objectMapper.getRegisteredModuleIds()).isNotEmpty();
  }
}
