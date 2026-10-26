package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotYetImplementedExceptionTest {

  @Test
  void shouldBeARuntimeException() {
    NotYetImplementedException ex = new NotYetImplementedException();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
