package net.chrisrichardson.ftgo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import(InternationalizationConfiguration.class)
public class CommonConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getFactory().setCharacterEscapes(null);
    return mapper;
  }

  @Bean
  public CommonJsonMapperInitializer commonJsonMapperInitializer() {
    return new CommonJsonMapperInitializer();
  }
}
