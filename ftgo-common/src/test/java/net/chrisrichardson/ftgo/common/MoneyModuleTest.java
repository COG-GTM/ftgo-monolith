package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyModuleTest {

  @Test
  void shouldReturnModuleName() {
    MoneyModule module = new MoneyModule();
    assertThat(module.getModuleName()).isEqualTo("FtgoCommonMOdule");
  }
}
