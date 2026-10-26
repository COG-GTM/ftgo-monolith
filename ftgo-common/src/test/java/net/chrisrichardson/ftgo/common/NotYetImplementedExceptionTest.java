package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NotYetImplementedExceptionTest {

  @Test
  public void shouldBeRuntimeException() {
    NotYetImplementedException ex = new NotYetImplementedException();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void shouldBeThrowable() {
    assertThatThrownBy(() -> { throw new NotYetImplementedException(); })
            .isInstanceOf(NotYetImplementedException.class);
  }
}
