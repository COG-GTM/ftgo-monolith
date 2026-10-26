package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineItemQuantityChangeTest {

  @Test
  public void shouldStoreAllValues() {
    Money current = new Money("50.00");
    Money newTotal = new Money("70.00");
    Money delta = new Money("20.00");

    LineItemQuantityChange change = new LineItemQuantityChange(current, newTotal, delta);

    assertThat(change.getCurrentOrderTotal()).isEqualTo(current);
    assertThat(change.getNewOrderTotal()).isEqualTo(newTotal);
    assertThat(change.getDelta()).isEqualTo(delta);
  }
}
