package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMinimumNotMetExceptionTest {

  @Test
  void shouldBeRuntimeException() {
    OrderMinimumNotMetException ex = new OrderMinimumNotMetException();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
