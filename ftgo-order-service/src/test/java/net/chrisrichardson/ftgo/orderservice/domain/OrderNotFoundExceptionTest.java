package net.chrisrichardson.ftgo.orderservice.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderNotFoundExceptionTest {

  @Test
  public void shouldStoreOrderId() {
    OrderNotFoundException ex = new OrderNotFoundException(42L);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
