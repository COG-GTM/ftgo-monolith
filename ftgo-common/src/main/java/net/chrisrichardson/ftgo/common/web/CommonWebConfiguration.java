package net.chrisrichardson.ftgo.common.web;

import net.chrisrichardson.ftgo.common.MoneyModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Shared web plumbing for any FTGO service: correlation-id MDC filter, common error mapping and Money JSON support.
 */
@Configuration
@ComponentScan
public class CommonWebConfiguration {

  @Bean
  public MoneyModule moneyModule() {
    return new MoneyModule();
  }
}
