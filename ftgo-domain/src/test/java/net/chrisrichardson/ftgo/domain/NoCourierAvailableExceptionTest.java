package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoCourierAvailableExceptionTest {

  @Test
  void shouldHaveMessage() {
    NoCourierAvailableException ex = new NoCourierAvailableException();
    assertThat(ex.getMessage()).contains("No courier available");
  }
}
