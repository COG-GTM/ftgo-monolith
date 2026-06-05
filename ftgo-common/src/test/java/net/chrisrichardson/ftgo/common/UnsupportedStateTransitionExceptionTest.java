package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnsupportedStateTransitionExceptionTest {

  private enum TestState { ACTIVE, INACTIVE }

  @Test
  public void shouldContainCurrentStateInMessage() {
    UnsupportedStateTransitionException ex = new UnsupportedStateTransitionException(TestState.ACTIVE);
    assertThat(ex.getMessage()).contains("ACTIVE");
  }

  @Test
  public void shouldBeRuntimeException() {
    UnsupportedStateTransitionException ex = new UnsupportedStateTransitionException(TestState.INACTIVE);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void shouldContainCurrentStatePrefix() {
    UnsupportedStateTransitionException ex = new UnsupportedStateTransitionException(TestState.ACTIVE);
    assertThat(ex.getMessage()).startsWith("current state:");
  }
}
