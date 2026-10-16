package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExceptionsTest {

  private enum State {
    APPROVED
  }

  @Test
  public void unsupportedTransitionIncludesCurrentState() {
    UnsupportedStateTransitionException exception =
            new UnsupportedStateTransitionException(State.APPROVED);

    assertTrue(exception instanceof RuntimeException);
    assertEquals("current state: APPROVED", exception.getMessage());
  }

  @Test
  public void unsupportedTransitionHandlesNullState() {
    UnsupportedStateTransitionException exception =
            new UnsupportedStateTransitionException((Enum) null);

    assertTrue(exception instanceof RuntimeException);
    assertEquals("current state: null", exception.getMessage());
  }

  @Test
  public void notYetImplementedExceptionHasNullMessage() {
    NotYetImplementedException exception = new NotYetImplementedException();

    assertTrue(exception instanceof RuntimeException);
    assertNull(exception.getMessage());
  }
}
