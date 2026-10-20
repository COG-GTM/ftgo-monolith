package net.chrisrichardson.ftgo.orderservice.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OptimisticOfflineLockExceptionTest {

  @Test
  public void isAnUncheckedExceptionWithoutMessageOrCause() {
    OptimisticOfflineLockException exception = new OptimisticOfflineLockException();

    assertTrue(exception instanceof RuntimeException);
    assertNull(exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  public void canBeThrownAndCaughtAsRuntimeException() {
    assertThatThrownBy(() -> {
      throw new OptimisticOfflineLockException();
    }).isExactlyInstanceOf(OptimisticOfflineLockException.class);
  }
}
