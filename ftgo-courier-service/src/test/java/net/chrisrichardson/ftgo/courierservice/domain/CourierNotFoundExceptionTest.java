package net.chrisrichardson.ftgo.courierservice.domain;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CourierNotFoundExceptionTest {

  @Test
  public void messageContainsCourierIdAndExceptionIsRuntimeException() {
    CourierNotFoundException exception = new CourierNotFoundException(99L);

    assertTrue(exception instanceof RuntimeException);
    assertTrue(exception.getMessage().contains("99"));
  }
}
