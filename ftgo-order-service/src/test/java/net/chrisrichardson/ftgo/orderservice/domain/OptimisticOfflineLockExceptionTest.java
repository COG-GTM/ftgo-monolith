package net.chrisrichardson.ftgo.orderservice.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptimisticOfflineLockExceptionTest {

  @Test
  void shouldBeRuntimeException() {
    OptimisticOfflineLockException ex = new OptimisticOfflineLockException();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
