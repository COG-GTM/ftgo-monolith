package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnsupportedStateTransitionExceptionTest {

  private enum TestState { ACTIVE, INACTIVE }

  @Test
  void shouldContainCurrentStateInMessage() {
    UnsupportedStateTransitionException ex = new UnsupportedStateTransitionException(TestState.ACTIVE);

    assertThat(ex.getMessage()).contains("ACTIVE");
    assertThat(ex.getMessage()).contains("current state:");
  }
}
